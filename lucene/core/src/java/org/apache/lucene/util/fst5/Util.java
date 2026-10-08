/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.util.fst5;

import static org.apache.lucene.util.fst5.FST.Arc.BitTable;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import org.apache.lucene.internal.hppc.IntArrayList;
import org.apache.lucene.internal.hppc.IntCursor;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst5.FST.Arc;
import org.apache.lucene.util.fst5.FST.BytesReader;

/**
 * Static helper methods.
 *
 * @lucene.experimental
 */
public final class Util {
  private Util() {}

  /**
   * Returned by {@link #get} when the input is not accepted. Outputs are non-negative, so this
   * value can never be a real output.
   */
  public static final long NOT_FOUND = -1;

  /** Looks up the output for this input, or {@link #NOT_FOUND} if the input is not accepted. */
  public static long get(FST fst, IntsRef input) throws IOException {
    return get(fst, input, new FST.Arc(), fst.getBytesReader());
  }

  /**
   * Looks up the output for this input, or {@link #NOT_FOUND} if the input is not accepted, reusing
   * the given arc and reader (allocation-free).
   */
  public static long get(FST fst, IntsRef input, FST.Arc arc, BytesReader fstReader)
      throws IOException {
    fst.getFirstArc(arc);

    // Accumulate output as we go
    long output = FST.NO_OUTPUT;
    for (int i = 0; i < input.length; i++) {
      if (fst.findTargetArc(input.ints[input.offset + i], arc, arc, fstReader) == null) {
        return NOT_FOUND;
      }
      output += arc.output();
    }

    if (arc.isFinal()) {
      return output + arc.nextFinalOutput();
    } else {
      return NOT_FOUND;
    }
  }

  // TODO: maybe a CharsRef version for BYTE2

  /** Looks up the output for this input, or {@link #NOT_FOUND} if the input is not accepted. */
  public static long get(FST fst, BytesRef input) throws IOException {
    return get(fst, input, new FST.Arc(), fst.getBytesReader());
  }

  /**
   * Looks up the output for this input, or {@link #NOT_FOUND} if the input is not accepted, reusing
   * the given arc and reader (allocation-free).
   */
  public static long get(FST fst, BytesRef input, FST.Arc arc, BytesReader fstReader)
      throws IOException {
    assert fst.metadata.inputType == FST.INPUT_TYPE.BYTE1;
    fst.getFirstArc(arc);

    // Accumulate output as we go
    long output = FST.NO_OUTPUT;
    for (int i = 0; i < input.length; i++) {
      if (fst.findTargetArc(input.bytes[i + input.offset] & 0xFF, arc, arc, fstReader) == null) {
        return NOT_FOUND;
      }
      output += arc.output();
    }

    if (arc.isFinal()) {
      return output + arc.nextFinalOutput();
    } else {
      return NOT_FOUND;
    }
  }

  /** Returns true if the input is accepted by the FST. */
  public static boolean contains(FST fst, BytesRef input) throws IOException {
    return contains(fst, input, new FST.Arc(), fst.getBytesReader());
  }

  /**
   * Returns true if the input is accepted by the FST, reusing the given arc and reader
   * (allocation-free).
   */
  public static boolean contains(FST fst, BytesRef input, FST.Arc arc, BytesReader fstReader)
      throws IOException {
    assert fst.metadata.inputType == FST.INPUT_TYPE.BYTE1;
    fst.getFirstArc(arc);
    for (int i = 0; i < input.length; i++) {
      if (fst.findTargetArc(input.bytes[i + input.offset] & 0xFF, arc, arc, fstReader) == null) {
        return false;
      }
    }
    return arc.isFinal();
  }

  /**
   * Represents a path in TopNSearcher.
   *
   * @lucene.experimental
   */
  public static class FSTPath {
    /** Holds the last arc appended to this path */
    public FST.Arc arc;

    /** Holds cost plus any usage-specific output: */
    public long output;

    public final IntsRefBuilder input;
    public final float boost;
    public final CharSequence context;

    // Custom int payload for consumers; the NRT suggester uses this to record if this path has
    // already enumerated a surface form
    public int payload;

    FSTPath(
        long output,
        FST.Arc arc,
        IntsRefBuilder input,
        float boost,
        CharSequence context,
        int payload) {
      this.arc = new FST.Arc().copyFrom(arc);
      this.output = output;
      this.input = input;
      this.boost = boost;
      this.context = context;
      this.payload = payload;
    }

    FSTPath newPath(long output, IntsRefBuilder input) {
      return new FSTPath(output, this.arc, input, this.boost, this.context, this.payload);
    }

    @Override
    public String toString() {
      return "input="
          + input.get()
          + " output="
          + output
          + " context="
          + context
          + " boost="
          + boost
          + " payload="
          + payload;
    }
  }

  /** Compares first by output (smaller is better), and then tie breaks by path.input. */
  private static final class TieBreakByInputComparator implements Comparator<FSTPath> {

    @Override
    public int compare(FSTPath a, FSTPath b) {
      int cmp = Long.compare(a.output, b.output);
      if (cmp == 0) {
        return a.input.get().compareTo(b.input.get());
      } else {
        return cmp;
      }
    }
  }

  /** Utility class to find top N shortest paths from start point(s). */
  public static class TopNSearcher {

    private final FST fst;
    private final BytesReader bytesReader;
    private final int topN;
    private final int maxQueueDepth;

    private final FST.Arc scratchArc = new FST.Arc();

    private final Comparator<FSTPath> pathComparator;

    TreeSet<FSTPath> queue;

    /**
     * Creates an unbounded TopNSearcher
     *
     * @param fst the {@link org.apache.lucene.util.fst5.FST} to search on
     * @param topN the number of top scoring entries to retrieve
     * @param maxQueueDepth the maximum size of the queue of possible top entries
     */
    public TopNSearcher(FST fst, int topN, int maxQueueDepth) {
      this(fst, topN, maxQueueDepth, new TieBreakByInputComparator());
    }

    public TopNSearcher(FST fst, int topN, int maxQueueDepth, Comparator<FSTPath> pathComparator) {
      this.fst = fst;
      this.bytesReader = fst.getBytesReader();
      this.topN = topN;
      this.maxQueueDepth = maxQueueDepth;
      this.pathComparator = pathComparator;
      queue = new TreeSet<>(pathComparator);
    }

    // If back plus this arc is competitive then add to queue:
    protected void addIfCompetitive(FSTPath path) {

      assert queue != null;

      long output = path.output + path.arc.output();

      if (queue.size() == maxQueueDepth) {
        FSTPath bottom = queue.last();
        int comp = pathComparator.compare(path, bottom);
        if (comp > 0) {
          // Doesn't compete
          return;
        } else if (comp == 0) {
          // Tie break by alpha sort on the input:
          path.input.append(path.arc.label());
          final int cmp = bottom.input.get().compareTo(path.input.get());
          path.input.setLength(path.input.length() - 1);

          // We should never see dups:
          assert cmp != 0;

          if (cmp < 0) {
            // Doesn't compete
            return;
          }
        }
        // Competes
      }
      // else ... Queue isn't full yet, so any path we hit competes:

      // copy over the current input to the new input
      // and add the arc.label to the end
      IntsRefBuilder newInput = new IntsRefBuilder();
      newInput.copyInts(path.input.get());
      newInput.append(path.arc.label());

      FSTPath newPath = path.newPath(output, newInput);
      if (acceptPartialPath(newPath)) {
        queue.add(newPath);
        if (queue.size() == maxQueueDepth + 1) {
          queue.pollLast();
        }
      }
    }

    public void addStartPaths(
        FST.Arc node, long startOutput, boolean allowEmptyString, IntsRefBuilder input)
        throws IOException {
      addStartPaths(node, startOutput, allowEmptyString, input, 0, null, -1);
    }

    /**
     * Adds all leaving arcs, including 'finished' arc, if the node is final, from this node into
     * the queue.
     */
    public void addStartPaths(
        FST.Arc node,
        long startOutput,
        boolean allowEmptyString,
        IntsRefBuilder input,
        float boost,
        CharSequence context,
        int payload)
        throws IOException {

      FSTPath path = new FSTPath(startOutput, node, input, boost, context, payload);
      fst.readFirstTargetArc(node, path.arc, bytesReader);

      // Bootstrap: find the min starting arc
      while (true) {
        if (allowEmptyString || path.arc.label() != FST.END_LABEL) {
          addIfCompetitive(path);
        }
        if (path.arc.isLast()) {
          break;
        }
        fst.readNextArc(path.arc, bytesReader);
      }
    }

    public TopResults search() throws IOException {

      final List<Result> results = new ArrayList<>();

      final BytesReader fstReader = fst.getBytesReader();

      // TODO: we could enable FST to sorting arcs by weight
      // as it freezes... can easily do this on first pass
      // (w/o requiring rewrite)

      // TODO: maybe we should make an FST.INPUT_TYPE.BYTE0.5!?
      // (nibbles)
      int rejectCount = 0;

      // For each top N path:
      while (results.size() < topN) {

        FSTPath path;

        if (queue == null) {
          // Ran out of paths
          break;
        }

        // Remove top path since we are now going to
        // pursue it:
        path = queue.pollFirst();

        if (path == null) {
          // There were less than topN paths available:
          break;
        }
        // System.out.println("pop path=" + path + " arc=" + path.arc.output);

        if (acceptPartialPath(path) == false) {
          continue;
        }

        if (path.arc.label() == FST.END_LABEL) {
          // Empty string!
          path.input.setLength(path.input.length() - 1);
          results.add(new Result(path.input.get(), path.output));
          continue;
        }

        if (results.size() == topN - 1 && maxQueueDepth == topN) {
          // Last path -- don't bother w/ queue anymore:
          queue = null;
        }

        // We take path and find its "0 output completion",
        // ie, just keep traversing the first arc with
        // NO_OUTPUT that we can find, since this must lead
        // to the minimum path that completes from
        // path.arc.

        // For each input letter:
        while (true) {

          fst.readFirstTargetArc(path.arc, path.arc, fstReader);

          // For each arc leaving this node:
          boolean foundZero = false;
          boolean arcCopyIsPending = false;
          while (true) {
            if (path.arc.output() == FST.NO_OUTPUT) {
              if (queue == null) {
                foundZero = true;
                break;
              } else if (!foundZero) {
                arcCopyIsPending = true;
                foundZero = true;
              } else {
                addIfCompetitive(path);
              }
            } else if (queue != null) {
              addIfCompetitive(path);
            }
            if (path.arc.isLast()) {
              break;
            }
            if (arcCopyIsPending) {
              scratchArc.copyFrom(path.arc);
              arcCopyIsPending = false;
            }
            fst.readNextArc(path.arc, fstReader);
          }

          assert foundZero;

          if (queue != null && !arcCopyIsPending) {
            path.arc.copyFrom(scratchArc);
          }

          if (path.arc.label() == FST.END_LABEL) {
            // Add final output:
            path.output += path.arc.output();
            if (acceptResult(path)) {
              results.add(new Result(path.input.get(), path.output));
            } else {
              rejectCount++;
            }
            break;
          } else {
            path.input.append(path.arc.label());
            path.output += path.arc.output();
            if (acceptPartialPath(path) == false) {
              break;
            }
          }
        }
      }
      return new TopResults(rejectCount + topN <= maxQueueDepth, results);
    }

    protected boolean acceptResult(FSTPath path) {
      return acceptResult(path.input.get(), path.output);
    }

    /** Override this to prevent considering a path before it's complete */
    protected boolean acceptPartialPath(FSTPath path) {
      return true;
    }

    protected boolean acceptResult(IntsRef input, long output) {
      return true;
    }
  }

  /**
   * Holds a single input (IntsRef) + output, returned by {@link #shortestPaths shortestPaths()}.
   */
  public record Result(IntsRef input, long output) {}

  /** Holds the results for a top N search using {@link TopNSearcher} */
  public static final class TopResults implements Iterable<Result> {

    /**
     * <code>true</code> iff this is a complete result ie. if the specified queue size was large
     * enough to find the complete list of results. This might be <code>false</code> if the {@link
     * TopNSearcher} rejected too many results.
     */
    public final boolean isComplete;

    /** The top results */
    public final List<Result> topN;

    TopResults(boolean isComplete, List<Result> topN) {
      this.topN = topN;
      this.isComplete = isComplete;
    }

    @Override
    public Iterator<Result> iterator() {
      return topN.iterator();
    }
  }

  /** Starting from node, find the top N min cost completions to a final node. */
  public static TopResults shortestPaths(
      FST fst, FST.Arc fromNode, long startOutput, int topN, boolean allowEmptyString)
      throws IOException {

    // All paths are kept, so we can pass topN for
    // maxQueueDepth and the pruning is admissible:
    TopNSearcher searcher = new TopNSearcher(fst, topN, topN);

    // since this search is initialized with a single start node
    // it is okay to start with an empty input path here
    searcher.addStartPaths(fromNode, startOutput, allowEmptyString, new IntsRefBuilder());
    return searcher.search();
  }

  /**
   * Dumps an {@link FST} to a GraphViz's <code>dot</code> language description for visualization.
   * Example of use:
   *
   * <pre><code class="language-java">
   * PrintWriter pw = new PrintWriter(&quot;out.dot&quot;);
   * Util.toDot(fst, pw, true, true);
   * pw.close();
   * </code></pre>
   *
   * and then, from command line:
   *
   * <pre>
   * dot -Tpng -o out.png out.dot
   * </pre>
   *
   * <p>Note: larger FSTs (a few thousand nodes) won't even render, don't bother.
   *
   * @param sameRank If <code>true</code>, the resulting <code>dot</code> file will try to order
   *     states in layers of breadth-first traversal. This may mess up arcs, but makes the output
   *     FST's structure a bit clearer.
   * @param labelStates If <code>true</code> states will have labels equal to their offsets in their
   *     binary format. Expands the graph considerably.
   * @see <a href="http://www.graphviz.org/">graphviz project</a>
   */
  public static void toDot(FST fst, Writer out, boolean sameRank, boolean labelStates)
      throws IOException {
    final String expandedNodeColor = "blue";

    // This is the start arc in the automaton (from the epsilon state to the first state
    // with outgoing transitions.
    final FST.Arc startArc = fst.getFirstArc(new FST.Arc());

    // A queue of transitions to consider for the next level.
    final List<FST.Arc> thisLevelQueue = new ArrayList<>();

    // A queue of transitions to consider when processing the next level.
    final List<FST.Arc> nextLevelQueue = new ArrayList<>();
    nextLevelQueue.add(startArc);
    // System.out.println("toDot: startArc: " + startArc);

    // A list of states on the same level (for ranking).
    final IntArrayList sameLevelStates = new IntArrayList();

    // A bitset of already seen states (target offset).
    final BitSet seen = new BitSet();
    seen.set((int) startArc.target());

    // Shape for states.
    final String stateShape = "circle";
    final String finalStateShape = "doublecircle";

    // Emit DOT prologue.
    out.write("digraph FST {\n");
    out.write("  rankdir = LR; splines=true; concentrate=true; ordering=out; ranksep=2.5; \n");

    if (!labelStates) {
      out.write("  node [shape=circle, width=.2, height=.2, style=filled]\n");
    }

    emitDotState(out, "initial", "point", "white", "");

    final long NO_OUTPUT = FST.NO_OUTPUT;
    final BytesReader r = fst.getBytesReader();

    // final FST.Arc scratchArc = new FST.Arc();

    {
      final String stateColor;
      if (fst.isExpandedTarget(startArc, r)) {
        stateColor = expandedNodeColor;
      } else {
        stateColor = null;
      }

      final boolean isFinal;
      final String finalOutput;
      if (startArc.isFinal()) {
        isFinal = true;
        finalOutput =
            startArc.nextFinalOutput() == NO_OUTPUT
                ? ""
                : Long.toString(startArc.nextFinalOutput());
      } else {
        isFinal = false;
        finalOutput = "";
      }

      emitDotState(
          out,
          Long.toString(startArc.target()),
          isFinal ? finalStateShape : stateShape,
          stateColor,
          finalOutput);
    }

    out.write("  initial -> " + startArc.target() + "\n");

    int level = 0;

    while (!nextLevelQueue.isEmpty()) {
      // we could double buffer here, but it doesn't matter probably.
      // System.out.println("next level=" + level);
      thisLevelQueue.addAll(nextLevelQueue);
      nextLevelQueue.clear();

      level++;
      out.write("\n  // Transitions and states at level: " + level + "\n");
      while (!thisLevelQueue.isEmpty()) {
        final FST.Arc arc = thisLevelQueue.remove(thisLevelQueue.size() - 1);
        // System.out.println("  pop: " + arc);
        if (FST.targetHasArcs(arc)) {
          // scan all target arcs
          // System.out.println("  readFirstTarget...");

          final long node = arc.target();

          fst.readFirstRealTargetArc(arc.target(), arc, r);

          // System.out.println("    firstTarget: " + arc);

          while (true) {

            // System.out.println("  cycle arc=" + arc);
            // Emit the unseen state and add it to the queue for the next level.
            if (arc.target() >= 0 && !seen.get((int) arc.target())) {

              /*
              boolean isFinal = false;
              long finalOutput = NO_OUTPUT;
              fst.readFirstTargetArc(arc, scratchArc);
              if (scratchArc.isFinal() && fst.targetHasArcs(scratchArc)) {
                // target is final
                isFinal = true;
                finalOutput = scratchArc.output == NO_OUTPUT ? null : scratchArc.output;
                System.out.println("dot hit final label=" + (char) scratchArc.label);
              }
              */
              final String stateColor;
              if (fst.isExpandedTarget(arc, r)) {
                stateColor = expandedNodeColor;
              } else {
                stateColor = null;
              }

              final String finalOutput;
              if (arc.nextFinalOutput() != NO_OUTPUT) {
                finalOutput = Long.toString(arc.nextFinalOutput());
              } else {
                finalOutput = "";
              }

              emitDotState(out, Long.toString(arc.target()), stateShape, stateColor, finalOutput);
              // To see the node address, use this instead:
              // emitDotState(out, Integer.toString(arc.target), stateShape, stateColor,
              // String.valueOf(arc.target));
              seen.set((int) arc.target());
              nextLevelQueue.add(new FST.Arc().copyFrom(arc));
              sameLevelStates.add((int) arc.target());
            }

            String outs;
            if (arc.output() != NO_OUTPUT) {
              outs = "/" + arc.output();
            } else {
              outs = "";
            }

            if (!FST.targetHasArcs(arc) && arc.isFinal() && arc.nextFinalOutput() != NO_OUTPUT) {
              // Tricky special case: sometimes, due to
              // pruning, the builder can [sillily] produce
              // an FST with an arc into the final end state
              // (-1) but also with a next final output; in
              // this case we pull that output up onto this
              // arc
              outs = outs + "/[" + arc.nextFinalOutput() + "]";
            }

            final String arcColor;
            if (arc.flag(FST.BIT_TARGET_NEXT)) {
              arcColor = "red";
            } else {
              arcColor = "black";
            }

            assert arc.label() != FST.END_LABEL;
            out.write(
                "  "
                    + node
                    + " -> "
                    + arc.target()
                    + " [label=\""
                    + printableLabel(arc.label())
                    + outs
                    + "\""
                    + (arc.isFinal() ? " style=\"bold\"" : "")
                    + " color=\""
                    + arcColor
                    + "\"]\n");

            // Break the loop if we're on the last arc of this state.
            if (arc.isLast()) {
              // System.out.println("    break");
              break;
            }
            fst.readNextRealArc(arc, r);
          }
        }
      }

      // Emit state ranking information.
      if (sameRank && sameLevelStates.size() > 1) {
        out.write("  {rank=same; ");
        for (IntCursor state : sameLevelStates) {
          out.write(state.value + "; ");
        }
        out.write(" }\n");
      }
      sameLevelStates.clear();
    }

    // Emit terminating state (always there anyway).
    out.write("  -1 [style=filled, color=black, shape=doublecircle, label=\"\"]\n\n");
    out.write("  {rank=sink; -1 }\n");

    out.write("}\n");
    out.flush();
  }

  /** Emit a single state in the <code>dot</code> language. */
  private static void emitDotState(
      Writer out, String name, String shape, String color, String label) throws IOException {
    out.write(
        "  "
            + name
            + " ["
            + (shape != null ? "shape=" + shape : "")
            + " "
            + (color != null ? "color=" + color : "")
            + " "
            + (label != null ? "label=\"" + label + "\"" : "label=\"\"")
            + " "
            + "]\n");
  }

  /** Ensures an arc's label is indeed printable (dot uses US-ASCII). */
  private static String printableLabel(int label) {
    // Any ordinary ascii character, except for " or \, are
    // printed as the character; else, as a hex string:
    if (label >= 0x20 && label <= 0x7d && label != 0x22 && label != 0x5c) { // " OR \
      return Character.toString((char) label);
    }
    return "0x" + Integer.toHexString(label);
  }

  /** Just maps each UTF16 unit (char) to the ints in an IntsRef. */
  public static IntsRef toUTF16(CharSequence s, IntsRefBuilder scratch) {
    final int charLimit = s.length();
    scratch.setLength(charLimit);
    scratch.growNoCopy(charLimit);
    for (int idx = 0; idx < charLimit; idx++) {
      scratch.setIntAt(idx, s.charAt(idx));
    }
    return scratch.get();
  }

  /**
   * Decodes the Unicode codepoints from the provided CharSequence and places them in the provided
   * scratch IntsRef, which must not be null, returning it.
   */
  public static IntsRef toUTF32(CharSequence s, IntsRefBuilder scratch) {
    int charIdx = 0;
    int intIdx = 0;
    final int charLimit = s.length();
    while (charIdx < charLimit) {
      scratch.grow(intIdx + 1);
      final int utf32 = Character.codePointAt(s, charIdx);
      scratch.setIntAt(intIdx, utf32);
      charIdx += Character.charCount(utf32);
      intIdx++;
    }
    scratch.setLength(intIdx);
    return scratch.get();
  }

  /**
   * Decodes the Unicode codepoints from the provided char[] and places them in the provided scratch
   * IntsRef, which must not be null, returning it.
   */
  public static IntsRef toUTF32(char[] s, int offset, int length, IntsRefBuilder scratch) {
    int charIdx = offset;
    int intIdx = 0;
    final int charLimit = offset + length;
    while (charIdx < charLimit) {
      scratch.grow(intIdx + 1);
      final int utf32 = Character.codePointAt(s, charIdx, charLimit);
      scratch.setIntAt(intIdx, utf32);
      charIdx += Character.charCount(utf32);
      intIdx++;
    }
    scratch.setLength(intIdx);
    return scratch.get();
  }

  /** Just takes unsigned byte values from the BytesRef and converts into an IntsRef. */
  public static IntsRef toIntsRef(BytesRef input, IntsRefBuilder scratch) {
    scratch.growNoCopy(input.length);
    for (int i = 0; i < input.length; i++) {
      scratch.setIntAt(i, input.bytes[i + input.offset] & 0xFF);
    }
    scratch.setLength(input.length);
    return scratch.get();
  }

  /** Just converts IntsRef to BytesRef; you must ensure the int values fit into a byte. */
  public static BytesRef toBytesRef(IntsRef input, BytesRefBuilder scratch) {
    scratch.growNoCopy(input.length);
    for (int i = 0; i < input.length; i++) {
      int value = input.ints[i + input.offset];
      // NOTE: we allow -128 to 255
      assert value >= Byte.MIN_VALUE && value <= 255 : "value " + value + " doesn't fit into byte";
      scratch.setByteAt(i, (byte) value);
    }
    scratch.setLength(input.length);
    return scratch.get();
  }

  // Uncomment for debugging:

  /*
  public static void dotToFile(FST fst, String filePath) throws IOException {
    Writer w = new OutputStreamWriter(new FileOutputStream(filePath));
    toDot(fst, w, true, true);
    w.close();
  }
  */

  /**
   * Reads the first arc greater or equal than the given label into the provided arc in place and
   * returns it iff found, otherwise return <code>null</code>.
   *
   * @param label the label to ceil on
   * @param fst the fst to operate on
   * @param follow the arc to follow reading the label from
   * @param arc the arc to read into in place
   * @param in the fst's {@link BytesReader}
   */
  public static Arc readCeilArc(int label, FST fst, Arc follow, Arc arc, BytesReader in)
      throws IOException {
    if (label == FST.END_LABEL) {
      return FST.readEndArc(follow, arc);
    }
    if (!FST.targetHasArcs(follow)) {
      return null;
    }
    fst.readFirstTargetArc(follow, arc, in);
    if (arc.bytesPerArc() != 0 && arc.label() != FST.END_LABEL) {
      if (arc.nodeFlags() == FST.ARCS_FOR_DIRECT_ADDRESSING) {
        // Fixed length arcs in a direct addressing node.
        int targetIndex = label - arc.label();
        if (targetIndex >= arc.numArcs()) {
          return null;
        } else if (targetIndex < 0) {
          return arc;
        } else {
          if (BitTable.isBitSet(targetIndex, arc, in)) {
            fst.readArcByDirectAddressing(arc, in, targetIndex);
            assert arc.label() == label;
          } else {
            int ceilIndex = BitTable.nextBitSet(targetIndex, arc, in);
            assert ceilIndex != -1;
            fst.readArcByDirectAddressing(arc, in, ceilIndex);
            assert arc.label() > label;
          }
          return arc;
        }
      } else if (arc.nodeFlags() == FST.ARCS_FOR_CONTINUOUS) {
        int targetIndex = label - arc.label();
        if (targetIndex >= arc.numArcs()) {
          return null;
        } else if (targetIndex < 0) {
          return arc;
        } else {
          fst.readArcByContinuous(arc, in, targetIndex);
          assert arc.label() == label;
          return arc;
        }
      }
      // Fixed length arcs in a binary search node.
      int idx = binarySearch(fst, arc, label);
      if (idx >= 0) {
        return fst.readArcByIndex(arc, in, idx);
      }
      idx = -1 - idx;
      if (idx == arc.numArcs()) {
        // DEAD END!
        return null;
      }
      return fst.readArcByIndex(arc, in, idx);
    }

    // Variable length arcs in a linear scan list,
    // or special arc with label == FST.END_LABEL.
    fst.readFirstRealTargetArc(follow.target(), arc, in);

    while (true) {
      // System.out.println("  non-bs cycle");
      if (arc.label() >= label) {
        // System.out.println("    found!");
        return arc;
      } else if (arc.isLast()) {
        return null;
      } else {
        fst.readNextRealArc(arc, in);
      }
    }
  }

  /**
   * Perform a binary search of Arcs encoded as a packed array
   *
   * @param fst the FST from which to read
   * @param arc the starting arc; sibling arcs greater than this will be searched. Usually the first
   *     arc in the array.
   * @param targetLabel the label to search for
   * @return the index of the Arc having the target label, or if no Arc has the matching label,
   *     {@code -1 - idx)}, where {@code idx} is the index of the Arc with the next highest label,
   *     or the total number of arcs if the target label exceeds the maximum.
   * @throws IOException when the FST reader does
   */
  static int binarySearch(FST fst, FST.Arc arc, int targetLabel) throws IOException {
    assert arc.nodeFlags() == FST.ARCS_FOR_BINARY_SEARCH
        : "Arc is not encoded as packed array for binary search (nodeFlags="
            + arc.nodeFlags()
            + ")";
    BytesReader in = fst.getBytesReader();
    int low = arc.arcIdx();
    int mid;
    int high = arc.numArcs() - 1;
    while (low <= high) {
      mid = (low + high) >>> 1;
      in.setPosition(arc.posArcsStart());
      in.skipBytes((long) arc.bytesPerArc() * mid + 1);
      final int midLabel = fst.readLabel(in);
      final int cmp = midLabel - targetLabel;
      if (cmp < 0) {
        low = mid + 1;
      } else if (cmp > 0) {
        high = mid - 1;
      } else {
        return mid;
      }
    }
    return -1 - low;
  }
}
