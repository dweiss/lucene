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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.tests.util.LuceneTestCase;
import org.apache.lucene.tests.util.TestUtil;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.UnicodeUtil;

/**
 * Helper class to test FSTs. A copy of {@code org.apache.lucene.tests.util.fst.FSTTester} ported to
 * the long-only API of {@code fst5}. Outputs are primitive longs everywhere; the FST is verified
 * both on heap and through {@link OffHeapFSTStore}.
 */
public class FST5Tester {
  public final Random random;
  public final List<InputOutput> pairs;
  public final int inputMode;
  public final Directory dir;
  public long nodeCount;
  public long arcCount;

  public FST5Tester(Random random, Directory dir, int inputMode, List<InputOutput> pairs) {
    this.random = random;
    this.dir = dir;
    this.inputMode = inputMode;
    this.pairs = pairs;
  }

  static String inputToString(int inputMode, IntsRef term) {
    return inputToString(inputMode, term, true);
  }

  static String inputToString(int inputMode, IntsRef term, boolean isValidUnicode) {
    if (!isValidUnicode) {
      return term.toString();
    } else if (inputMode == 0) {
      // utf8
      return toBytesRef(term).utf8ToString() + " " + term;
    } else {
      // utf32
      return UnicodeUtil.newString(term.ints, term.offset, term.length) + " " + term;
    }
  }

  private static BytesRef toBytesRef(IntsRef ir) {
    BytesRef br = new BytesRef(ir.length);
    for (int i = 0; i < ir.length; i++) {
      int x = ir.ints[ir.offset + i];
      assert x >= 0 && x <= 255;
      br.bytes[i] = (byte) x;
    }
    br.length = ir.length;
    return br;
  }

  public static String getRandomString(Random random) {
    final String term;
    if (random.nextBoolean()) {
      term = TestUtil.randomRealisticUnicodeString(random);
    } else {
      // we want to mix in limited-alphabet symbols so
      // we get more sharing of the nodes given how few
      // terms we are testing...
      term = simpleRandomString(random);
    }
    return term;
  }

  public static String simpleRandomString(Random r) {
    final int end = r.nextInt(10);
    if (end == 0) {
      // allow 0 length
      return "";
    }
    final char[] buffer = new char[end];
    for (int i = 0; i < end; i++) {
      buffer[i] = (char) TestUtil.nextInt(r, 97, 102);
    }
    return new String(buffer, 0, end);
  }

  public static IntsRef toIntsRef(String s, int inputMode) {
    return toIntsRef(s, inputMode, new IntsRefBuilder());
  }

  public static IntsRef toIntsRef(String s, int inputMode, IntsRefBuilder ir) {
    if (inputMode == 0) {
      // utf8
      return toIntsRef(new BytesRef(s), ir);
    } else {
      // utf32
      return toIntsRefUTF32(s, ir);
    }
  }

  static IntsRef toIntsRefUTF32(String s, IntsRefBuilder ir) {
    final int charLength = s.length();
    int charIdx = 0;
    int intIdx = 0;
    ir.clear();
    while (charIdx < charLength) {
      ir.grow(intIdx + 1);
      final int utf32 = s.codePointAt(charIdx);
      ir.append(utf32);
      charIdx += Character.charCount(utf32);
      intIdx++;
    }
    return ir.get();
  }

  static IntsRef toIntsRef(BytesRef br, IntsRefBuilder ir) {
    ir.growNoCopy(br.length);
    ir.clear();
    for (int i = 0; i < br.length; i++) {
      ir.append(br.bytes[br.offset + i] & 0xFF);
    }
    return ir.get();
  }

  /** Holds one input/output pair. */
  public record InputOutput(IntsRef input, long output) implements Comparable<InputOutput> {

    @Override
    public int compareTo(InputOutput other) {
      return input.compareTo(other.input);
    }
  }

  // runs the term, returning the output, or Util.NOT_FOUND if term
  // isn't accepted.  if prefixLength is non-null it must be
  // length 1 int array; prefixLength[0] is set to the length
  // of the term prefix that matches
  private long run(FST fst, IntsRef term, int[] prefixLength) throws IOException {
    assert prefixLength == null || prefixLength.length == 1;
    final FST.Arc arc = fst.getFirstArc(new FST.Arc());
    final FST.BytesReader fstReader = fst.getBytesReader();
    long output = FST.NO_OUTPUT;

    for (int i = 0; i <= term.length; i++) {
      final int label;
      if (i == term.length) {
        label = FST.END_LABEL;
      } else {
        label = term.ints[term.offset + i];
      }
      if (fst.findTargetArc(label, arc, arc, fstReader) == null) {
        if (prefixLength != null) {
          prefixLength[0] = i;
          return output;
        } else {
          return Util.NOT_FOUND;
        }
      }
      output += arc.output();
    }

    if (prefixLength != null) {
      prefixLength[0] = term.length;
    }

    return output;
  }

  private long randomAcceptedWord(FST fst, IntsRefBuilder in) throws IOException {
    FST.Arc arc = fst.getFirstArc(new FST.Arc());

    final List<FST.Arc> arcs = new ArrayList<>();
    in.clear();
    long output = FST.NO_OUTPUT;
    final FST.BytesReader fstReader = fst.getBytesReader();

    while (true) {
      // read all arcs:
      fst.readFirstTargetArc(arc, arc, fstReader);
      arcs.add(new FST.Arc().copyFrom(arc));
      while (!arc.isLast()) {
        fst.readNextArc(arc, fstReader);
        arcs.add(new FST.Arc().copyFrom(arc));
      }

      // pick one
      arc = arcs.get(random.nextInt(arcs.size()));
      arcs.clear();

      // accumulate output
      output += arc.output();

      // append label
      if (arc.label() == FST.END_LABEL) {
        break;
      }

      in.append(arc.label());
    }

    return output;
  }

  public FST doTest() throws IOException {

    final FSTCompiler.Builder fstCompilerBuilder =
        new FSTCompiler.Builder(inputMode == 0 ? FST.INPUT_TYPE.BYTE1 : FST.INPUT_TYPE.BYTE4);

    IndexOutput indexOutput = null;
    boolean useOffHeap = random.nextBoolean();

    if (useOffHeap) {
      indexOutput = dir.createOutput("fstOffHeap.bin", IOContext.DEFAULT);
      fstCompilerBuilder.dataOutput(indexOutput);
    }

    final FSTCompiler fstCompiler = fstCompilerBuilder.build();

    for (InputOutput pair : pairs) {
      fstCompiler.add(pair.input, pair.output);
    }

    FST fst = null;
    FST.FSTMetadata fstMetadata = fstCompiler.compile();

    // When the FST is verified through an OffHeapFSTStore, the backing input must stay open until
    // verification is done; it is then closed and the FST reloaded on heap before returning.
    IndexInput offHeapInput = null;
    String offHeapFile = null;
    boolean offHeapHasMetadata = false;

    if (useOffHeap) {
      indexOutput.close();
      if (fstMetadata == null) {
        dir.deleteFile("fstOffHeap.bin");
      } else if (random.nextBoolean()) {
        try (IndexInput in = dir.openInput("fstOffHeap.bin", IOContext.DEFAULT)) {
          fst = new FST(fstMetadata, in);
        } finally {
          dir.deleteFile("fstOffHeap.bin");
        }
      } else {
        offHeapFile = "fstOffHeap.bin";
        offHeapInput = dir.openInput(offHeapFile, IOContext.DEFAULT);
        fst = FST.fromFSTReader(fstMetadata, new OffHeapFSTStore(offHeapInput, 0, fstMetadata));
      }
    } else if (fstMetadata != null) {
      fst = FST.fromFSTReader(fstMetadata, fstCompiler.getFSTReader());
      if (random.nextBoolean()) {
        IOContext context = LuceneTestCase.newIOContext(random);
        try (IndexOutput out = dir.createOutput("fst.bin", context)) {
          fst.save(out, out);
        }
        if (random.nextBoolean()) {
          try (IndexInput in = dir.openInput("fst.bin", context)) {
            fst = new FST(FST.readMetadata(in), in);
          } finally {
            dir.deleteFile("fst.bin");
          }
        } else {
          offHeapFile = "fst.bin";
          offHeapHasMetadata = true;
          offHeapInput = dir.openInput(offHeapFile, context);
          FST.FSTMetadata metadata = FST.readMetadata(offHeapInput);
          fst =
              FST.fromFSTReader(
                  metadata,
                  new OffHeapFSTStore(offHeapInput, offHeapInput.getFilePointer(), metadata));
        }
      }
    }
    if (LuceneTestCase.VERBOSE && offHeapInput != null) {
      System.out.println("TEST: verifying through OffHeapFSTStore");
    }

    if (LuceneTestCase.VERBOSE && pairs.size() <= 20 && fst != null) {
      System.out.println("Printing FST as dot file to stdout:");
      final Writer w = new OutputStreamWriter(System.out, Charset.defaultCharset());
      Util.toDot(fst, w, false, false);
      w.flush();
      System.out.println("END dot file");
    }

    if (LuceneTestCase.VERBOSE) {
      if (fst == null) {
        System.out.println("  fst has 0 nodes (fully pruned)");
      } else {
        System.out.println(
            "  fst has "
                + fstCompiler.getNodeCount()
                + " nodes and "
                + fstCompiler.getArcCount()
                + " arcs");
      }
    }

    try {
      verifyUnPruned(inputMode, fst);
    } finally {
      if (offHeapInput != null) {
        offHeapInput.close();
      }
    }

    if (offHeapInput != null) {
      // Reload on heap so the returned FST does not depend on the (now closed) input.
      try (IndexInput in = dir.openInput(offHeapFile, IOContext.DEFAULT)) {
        fst = offHeapHasMetadata ? new FST(FST.readMetadata(in), in) : new FST(fstMetadata, in);
      } finally {
        dir.deleteFile(offHeapFile);
      }
    }

    nodeCount = fstCompiler.getNodeCount();
    arcCount = fstCompiler.getArcCount();

    return fst;
  }

  // FST is complete
  private void verifyUnPruned(int inputMode, FST fst) throws IOException {

    if (pairs.size() == 0) {
      assertNull(fst);
      return;
    }

    if (LuceneTestCase.VERBOSE) {
      System.out.println("TEST: now verify " + pairs.size() + " terms");
      for (InputOutput pair : pairs) {
        assertNotNull(pair);
        assertNotNull(pair.input);
        System.out.println("  " + inputToString(inputMode, pair.input) + ": " + pair.output);
      }
    }

    assertNotNull(fst);

    // visit valid pairs in order -- make sure all words
    // are accepted, and FSTEnum's next() steps through
    // them correctly
    if (LuceneTestCase.VERBOSE) {
      System.out.println("TEST: check valid terms/next()");
    }
    {
      IntsRefFSTEnum fstEnum = new IntsRefFSTEnum(fst);
      FST.Arc scratchArc = new FST.Arc();
      FST.BytesReader scratchReader = fst.getBytesReader();
      for (InputOutput pair : pairs) {
        IntsRef term = pair.input;
        if (LuceneTestCase.VERBOSE) {
          System.out.println(
              "TEST: check term=" + inputToString(inputMode, term) + " output=" + pair.output);
        }
        long output = run(fst, term, null);
        assertNotEquals(
            "term " + inputToString(inputMode, term) + " is not accepted", Util.NOT_FOUND, output);
        assertEquals(pair.output, output);

        // Util.get must agree, both allocating and reusing variants
        assertEquals(pair.output, Util.get(fst, term));
        assertEquals(pair.output, Util.get(fst, term, scratchArc, scratchReader));
        if (inputMode == 0) {
          BytesRef bytes = toBytesRef(term);
          assertEquals(pair.output, Util.get(fst, bytes));
          assertTrue(Util.contains(fst, bytes));
          assertTrue(Util.contains(fst, bytes, scratchArc, scratchReader));
        }

        // verify enum's next
        IntsRefFSTEnum.InputOutput t = fstEnum.next();
        assertNotNull(t);
        assertEquals(
            "expected input="
                + inputToString(inputMode, term)
                + " but fstEnum returned "
                + inputToString(inputMode, t.input),
            term,
            t.input);
        assertEquals(pair.output, t.output);
      }
      assertNull(fstEnum.next());
    }

    final Map<IntsRef, Long> termsMap = new HashMap<>();
    for (InputOutput pair : pairs) {
      termsMap.put(pair.input, pair.output);
    }

    // find random matching word and make sure it's valid
    if (LuceneTestCase.VERBOSE) {
      System.out.println("TEST: verify random accepted terms");
    }
    final IntsRefBuilder scratch = new IntsRefBuilder();
    int num = LuceneTestCase.atLeast(random, 500);
    for (int iter = 0; iter < num; iter++) {
      long output = randomAcceptedWord(fst, scratch);
      assertTrue(
          "accepted word " + inputToString(inputMode, scratch.get()) + " is not valid",
          termsMap.containsKey(scratch.get()));
      assertEquals(termsMap.get(scratch.get()).longValue(), output);
    }

    // test IntsRefFSTEnum.seek:
    if (LuceneTestCase.VERBOSE) {
      System.out.println("TEST: verify seek");
    }
    IntsRefFSTEnum fstEnum = new IntsRefFSTEnum(fst);
    num = LuceneTestCase.atLeast(random, 100);
    for (int iter = 0; iter < num; iter++) {
      if (LuceneTestCase.VERBOSE) {
        System.out.println("  iter=" + iter);
      }
      if (random.nextBoolean()) {
        // seek to term that doesn't exist:
        while (true) {
          final IntsRef term = toIntsRef(getRandomString(random), inputMode);
          int pos = Collections.binarySearch(pairs, new InputOutput(term, 0));
          if (pos < 0) {
            pos = -(pos + 1);
            // ok doesn't exist
            final IntsRefFSTEnum.InputOutput seekResult;
            if (random.nextInt(3) == 0) {
              if (LuceneTestCase.VERBOSE) {
                System.out.println(
                    "  do non-exist seekExact term=" + inputToString(inputMode, term));
              }
              seekResult = fstEnum.seekExact(term);
              pos = -1;
            } else if (random.nextBoolean()) {
              if (LuceneTestCase.VERBOSE) {
                System.out.println(
                    "  do non-exist seekFloor term=" + inputToString(inputMode, term));
              }
              seekResult = fstEnum.seekFloor(term);
              pos--;
            } else {
              if (LuceneTestCase.VERBOSE) {
                System.out.println(
                    "  do non-exist seekCeil term=" + inputToString(inputMode, term));
              }
              seekResult = fstEnum.seekCeil(term);
            }

            if (pos != -1 && pos < pairs.size()) {
              assertNotNull(
                  "got null but expected term=" + inputToString(inputMode, pairs.get(pos).input),
                  seekResult);
              if (LuceneTestCase.VERBOSE) {
                System.out.println("    got " + inputToString(inputMode, seekResult.input));
              }
              assertEquals(
                  "expected "
                      + inputToString(inputMode, pairs.get(pos).input)
                      + " but got "
                      + inputToString(inputMode, seekResult.input),
                  pairs.get(pos).input,
                  seekResult.input);
              assertEquals(pairs.get(pos).output, seekResult.output);
            } else {
              // seek()ed before start or beyond end
              assertNull(
                  "expected null but got "
                      + (seekResult == null ? "null" : inputToString(inputMode, seekResult.input)),
                  seekResult);
              if (LuceneTestCase.VERBOSE) {
                System.out.println("    got null");
              }
            }

            break;
          }
        }
      } else {
        // seek to term that does exist:
        InputOutput pair = pairs.get(random.nextInt(pairs.size()));
        final IntsRefFSTEnum.InputOutput seekResult;
        if (random.nextInt(3) == 2) {
          if (LuceneTestCase.VERBOSE) {
            System.out.println(
                "  do exists seekExact term=" + inputToString(inputMode, pair.input));
          }
          seekResult = fstEnum.seekExact(pair.input);
        } else if (random.nextBoolean()) {
          if (LuceneTestCase.VERBOSE) {
            System.out.println("  do exists seekFloor " + inputToString(inputMode, pair.input));
          }
          seekResult = fstEnum.seekFloor(pair.input);
        } else {
          if (LuceneTestCase.VERBOSE) {
            System.out.println("  do exists seekCeil " + inputToString(inputMode, pair.input));
          }
          seekResult = fstEnum.seekCeil(pair.input);
        }
        assertNotNull(seekResult);
        assertEquals(
            "got "
                + inputToString(inputMode, seekResult.input)
                + " but expected "
                + inputToString(inputMode, pair.input),
            pair.input,
            seekResult.input);
        assertEquals(pair.output, seekResult.output);
      }
    }

    if (LuceneTestCase.VERBOSE) {
      System.out.println("TEST: mixed next/seek");
    }

    // test mixed next/seek
    num = LuceneTestCase.atLeast(random, 100);
    for (int iter = 0; iter < num; iter++) {
      if (LuceneTestCase.VERBOSE) {
        System.out.println("TEST: iter " + iter);
      }
      // reset:
      fstEnum = new IntsRefFSTEnum(fst);
      int upto = -1;
      while (true) {
        boolean isDone = false;
        if (upto == pairs.size() - 1 || random.nextBoolean()) {
          // next
          upto++;
          if (LuceneTestCase.VERBOSE) {
            System.out.println("  do next");
          }
          isDone = fstEnum.next() == null;
        } else if (upto != -1 && upto < 0.75 * pairs.size() && random.nextBoolean()) {
          int attempt = 0;
          for (; attempt < 10; attempt++) {
            IntsRef term = toIntsRef(getRandomString(random), inputMode);
            if (!termsMap.containsKey(term) && term.compareTo(pairs.get(upto).input) > 0) {
              int pos = Collections.binarySearch(pairs, new InputOutput(term, 0));
              assert pos < 0;
              upto = -(pos + 1);

              if (random.nextBoolean()) {
                upto--;
                assertTrue(upto != -1);
                if (LuceneTestCase.VERBOSE) {
                  System.out.println(
                      "  do non-exist seekFloor(" + inputToString(inputMode, term) + ")");
                }
                isDone = fstEnum.seekFloor(term) == null;
              } else {
                if (LuceneTestCase.VERBOSE) {
                  System.out.println(
                      "  do non-exist seekCeil(" + inputToString(inputMode, term) + ")");
                }
                isDone = fstEnum.seekCeil(term) == null;
              }

              break;
            }
          }
          if (attempt == 10) {
            continue;
          }

        } else {
          final int inc = random.nextInt(pairs.size() - upto - 1);
          upto += inc;
          if (upto == -1) {
            upto = 0;
          }

          if (random.nextBoolean()) {
            if (LuceneTestCase.VERBOSE) {
              System.out.println(
                  "  do seekCeil(" + inputToString(inputMode, pairs.get(upto).input) + ")");
            }
            isDone = fstEnum.seekCeil(pairs.get(upto).input) == null;
          } else {
            if (LuceneTestCase.VERBOSE) {
              System.out.println(
                  "  do seekFloor(" + inputToString(inputMode, pairs.get(upto).input) + ")");
            }
            isDone = fstEnum.seekFloor(pairs.get(upto).input) == null;
          }
        }
        if (LuceneTestCase.VERBOSE) {
          if (!isDone) {
            System.out.println("    got " + inputToString(inputMode, fstEnum.current().input));
          } else {
            System.out.println("    got null");
          }
        }

        if (upto == pairs.size()) {
          assertTrue(isDone);
          break;
        } else {
          assertFalse(isDone);
          assertEquals(pairs.get(upto).input, fstEnum.current().input);
          assertEquals(pairs.get(upto).output, fstEnum.current().output);
        }
      }
    }
  }
}
