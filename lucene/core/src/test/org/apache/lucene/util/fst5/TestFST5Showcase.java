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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.lucene.tests.util.LuceneTestCase;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.IntsRefBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Side-by-side showcase of the generic {@code org.apache.lucene.util.fst} API ({@code testXyz_old})
 * and the long-only {@code fst5} API ({@code testXyz}): primitive outputs in the automaton, byte
 * payloads through {@link FSTMap} and its ordinal-indexed {@link BytesRefStore}. Each pair computes
 * the same result from the same deterministic data and checks it against a plain Java reference.
 * The bodies are deliberately written out in full so a human can diff them.
 */
public class TestFST5Showcase extends LuceneTestCase {

  private static final int NUM_TERMS = 20_000;
  private static final int TOP_N = 5;

  // Reference data: sorted terms with weights.
  private static TreeMap<BytesRef, Long> reference;
  private static List<BytesRef> terms;
  private static List<BytesRef> hits;
  private static List<BytesRef> misses;
  private static List<String> prefixes;

  // Old (generic) FSTs.
  private static org.apache.lucene.util.fst.FST<Long> oldLongFst;
  private static org.apache.lucene.util.fst.FST<Object> oldFsa;
  private static org.apache.lucene.util.fst.FST<BytesRef> oldBytesFst;
  private static org.apache.lucene.util.fst.FST<Long> oldWfst;

  // New (long-only) FSTs and the map layer.
  private static FST newLongFst;
  private static FST newFsa;
  private static FSTMap newBytesMap;
  private static FST newWfst;

  @BeforeClass
  public static void buildData() throws IOException {
    Random random = new Random(42);
    TreeSet<BytesRef> set = new TreeSet<>();
    while (set.size() < NUM_TERMS) {
      set.add(randomTerm(random));
    }
    terms = new ArrayList<>(set);
    reference = new TreeMap<>();
    for (BytesRef term : terms) {
      // Outside the Long.valueOf cache on purpose.
      reference.put(term, 1 + (random.nextLong() >>> 24));
    }

    hits = new ArrayList<>();
    misses = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      hits.add(terms.get(random.nextInt(terms.size())));
      BytesRef miss;
      do {
        miss = randomTerm(random);
      } while (set.contains(miss));
      misses.add(miss);
    }

    prefixes = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      prefixes.add(terms.get(random.nextInt(terms.size())).utf8ToString().substring(0, 2));
    }

    buildOld();
    buildNew();
  }

  @AfterClass
  public static void releaseData() {
    reference = null;
    terms = hits = misses = null;
    prefixes = null;
    oldLongFst = oldWfst = null;
    oldFsa = null;
    oldBytesFst = null;
    newLongFst = newFsa = newWfst = null;
    newBytesMap = null;
  }

  private static BytesRef randomTerm(Random random) {
    int len = 3 + random.nextInt(10);
    char[] chars = new char[len];
    for (int i = 0; i < len; i++) {
      chars[i] = (char) ('a' + random.nextInt(26));
    }
    return new BytesRef(new String(chars));
  }

  /** Weight encoding used by WFSTCompletionLookup: smaller output is better. */
  private static long encodeWeight(long weight) {
    return Long.MAX_VALUE - weight;
  }

  private static BytesRef bytesOutput(BytesRef term) {
    // An arbitrary non-trivial byte sequence derived from the term: the term reversed.
    byte[] b = new byte[term.length];
    for (int i = 0; i < term.length; i++) {
      b[i] = term.bytes[term.offset + term.length - 1 - i];
    }
    return new BytesRef(b);
  }

  private static void buildOld() throws IOException {
    var longOutputs = org.apache.lucene.util.fst.PositiveIntOutputs.getSingleton();
    var bytesOutputs = org.apache.lucene.util.fst.ByteSequenceOutputs.getSingleton();
    var byte1 = org.apache.lucene.util.fst.FST.INPUT_TYPE.BYTE1;

    var longCompiler =
        new org.apache.lucene.util.fst.FSTCompiler.Builder<>(byte1, longOutputs).build();
    var fsaCompiler =
        new org.apache.lucene.util.fst.FSTCompiler.Builder<>(
                byte1, org.apache.lucene.util.fst.NoOutputs.getSingleton())
            .build();
    var bytesCompiler =
        new org.apache.lucene.util.fst.FSTCompiler.Builder<>(byte1, bytesOutputs).build();
    var wfstCompiler =
        new org.apache.lucene.util.fst.FSTCompiler.Builder<>(byte1, longOutputs).build();

    IntsRefBuilder scratch = new IntsRefBuilder();
    for (Map.Entry<BytesRef, Long> e : reference.entrySet()) {
      var input = org.apache.lucene.util.fst.Util.toIntsRef(e.getKey(), scratch);
      longCompiler.add(input, e.getValue());
      fsaCompiler.add(input, org.apache.lucene.util.fst.NoOutputs.getSingleton().getNoOutput());
      bytesCompiler.add(input, bytesOutput(e.getKey()));
      wfstCompiler.add(input, encodeWeight(e.getValue()));
    }
    oldLongFst =
        org.apache.lucene.util.fst.FST.fromFSTReader(
            longCompiler.compile(), longCompiler.getFSTReader());
    oldFsa =
        org.apache.lucene.util.fst.FST.fromFSTReader(
            fsaCompiler.compile(), fsaCompiler.getFSTReader());
    oldBytesFst =
        org.apache.lucene.util.fst.FST.fromFSTReader(
            bytesCompiler.compile(), bytesCompiler.getFSTReader());
    oldWfst =
        org.apache.lucene.util.fst.FST.fromFSTReader(
            wfstCompiler.compile(), wfstCompiler.getFSTReader());
  }

  private static void buildNew() throws IOException {
    var byte1 = FST.INPUT_TYPE.BYTE1;

    var longCompiler = new FSTCompiler.Builder(byte1).build();
    var fsaCompiler = new FSTCompiler.Builder(byte1).build();
    var bytesMapBuilder = new FSTMap.Builder();
    var wfstCompiler = new FSTCompiler.Builder(byte1).build();

    IntsRefBuilder scratch = new IntsRefBuilder();
    for (Map.Entry<BytesRef, Long> e : reference.entrySet()) {
      var input = Util.toIntsRef(e.getKey(), scratch);
      longCompiler.add(input, e.getValue());
      fsaCompiler.add(input, FST.NO_OUTPUT);
      bytesMapBuilder.add(e.getKey(), bytesOutput(e.getKey()));
      wfstCompiler.add(input, encodeWeight(e.getValue()));
    }
    newLongFst = FST.fromFSTReader(longCompiler.compile(), longCompiler.getFSTReader());
    newFsa = FST.fromFSTReader(fsaCompiler.compile(), fsaCompiler.getFSTReader());
    newBytesMap = bytesMapBuilder.build();
    newWfst = FST.fromFSTReader(wfstCompiler.compile(), wfstCompiler.getFSTReader());
  }

  private static void printSample(String what, List<?> values) {
    if (VERBOSE) {
      System.out.println(what + ": " + values.subList(0, Math.min(5, values.size())) + " ...");
    }
  }

  // ---------------------------------------------------------------------------------------------
  // 1. Full traversal with long outputs.

  public void testTraversal_old() throws IOException {
    org.apache.lucene.util.fst.BytesRefFSTEnum<Long> fstEnum =
        new org.apache.lucene.util.fst.BytesRefFSTEnum<>(oldLongFst);
    TreeMap<BytesRef, Long> collected = new TreeMap<>();
    org.apache.lucene.util.fst.BytesRefFSTEnum.InputOutput<Long> io;
    while ((io = fstEnum.next()) != null) {
      collected.put(BytesRef.deepCopyOf(io.input), io.output);
    }
    assertEquals(reference, collected);
    printSample("traversal(old)", new ArrayList<>(collected.entrySet()));
  }

  public void testTraversal() throws IOException {
    BytesRefFSTEnum fstEnum = new BytesRefFSTEnum(newLongFst);
    TreeMap<BytesRef, Long> collected = new TreeMap<>();
    BytesRefFSTEnum.InputOutput io;
    while ((io = fstEnum.next()) != null) {
      collected.put(BytesRef.deepCopyOf(io.input), io.output);
    }
    assertEquals(reference, collected);
    printSample("traversal(new)", new ArrayList<>(collected.entrySet()));
  }

  // ---------------------------------------------------------------------------------------------
  // 2. Full traversal with byte payloads: ByteSequenceOutputs vs. FSTMap over a BytesRefStore.

  public void testTraversalBytes_old() throws IOException {
    org.apache.lucene.util.fst.BytesRefFSTEnum<BytesRef> fstEnum =
        new org.apache.lucene.util.fst.BytesRefFSTEnum<>(oldBytesFst);
    org.apache.lucene.util.fst.BytesRefFSTEnum.InputOutput<BytesRef> io;
    int count = 0;
    while ((io = fstEnum.next()) != null) {
      assertEquals(bytesOutput(io.input), io.output);
      count++;
    }
    assertEquals(reference.size(), count);
  }

  public void testTraversalBytes() throws IOException {
    FSTMap.Entries entries = newBytesMap.entries();
    BytesRefBuilder value = new BytesRefBuilder();
    int count = 0;
    while (entries.next()) {
      entries.value(value);
      assertEquals(bytesOutput(entries.key()), value.get());
      assertEquals(count, entries.ordinal());
      count++;
    }
    assertEquals(reference.size(), count);
  }

  // ---------------------------------------------------------------------------------------------
  // 3. Membership: on an FSA, and on a transducer where the outputs are irrelevant.

  public void testHitMiss_old() throws IOException {
    for (BytesRef hit : hits) {
      assertTrue(org.apache.lucene.util.fst.Util.get(oldFsa, hit) != null);
      assertTrue(org.apache.lucene.util.fst.Util.get(oldLongFst, hit) != null);
    }
    for (BytesRef miss : misses) {
      assertFalse(org.apache.lucene.util.fst.Util.get(oldFsa, miss) != null);
      assertFalse(org.apache.lucene.util.fst.Util.get(oldLongFst, miss) != null);
    }
  }

  public void testHitMiss() throws IOException {
    for (BytesRef hit : hits) {
      assertTrue(Util.contains(newFsa, hit));
      assertTrue(Util.contains(newLongFst, hit));
    }
    for (BytesRef miss : misses) {
      assertFalse(Util.contains(newFsa, miss));
      assertFalse(Util.contains(newLongFst, miss));
    }
  }

  // ---------------------------------------------------------------------------------------------
  // 4. Exact lookup of a long output.

  public void testTransducerLookup_old() throws IOException {
    List<Long> sample = new ArrayList<>();
    for (BytesRef key : hits) {
      Long v = org.apache.lucene.util.fst.Util.get(oldLongFst, key);
      assertNotNull(v);
      assertEquals(reference.get(key), v);
      sample.add(v);
    }
    for (BytesRef key : misses) {
      Long v = org.apache.lucene.util.fst.Util.get(oldLongFst, key);
      assertNull(v);
    }
    printSample("lookup(old)", sample);
  }

  public void testTransducerLookup() throws IOException {
    List<Long> sample = new ArrayList<>();
    for (BytesRef key : hits) {
      long v = Util.get(newLongFst, key);
      assertNotEquals(Util.NOT_FOUND, v);
      assertEquals(reference.get(key).longValue(), v);
      sample.add(v);
    }
    for (BytesRef key : misses) {
      long v = Util.get(newLongFst, key);
      assertEquals(Util.NOT_FOUND, v);
    }
    printSample("lookup(new)", sample);
  }

  // ---------------------------------------------------------------------------------------------
  // 5. WFST-style suggester: top-N completions of a prefix by weight.

  /** Reference: the top N terms starting with prefix, by weight descending then term ascending. */
  private static List<String> expectedCompletions(String prefix) {
    BytesRef from = new BytesRef(prefix);
    BytesRef to = new BytesRef(prefix.substring(0, 1) + (char) (prefix.charAt(1) + 1));
    List<Map.Entry<BytesRef, Long>> candidates =
        new ArrayList<>(reference.subMap(from, true, to, false).entrySet());
    candidates.sort(
        Comparator.comparing((Map.Entry<BytesRef, Long> e) -> -e.getValue())
            .thenComparing(Map.Entry::getKey));
    List<String> expected = new ArrayList<>();
    for (var e : candidates.subList(0, Math.min(TOP_N, candidates.size()))) {
      expected.add(e.getKey().utf8ToString());
    }
    return expected;
  }

  public void testSuggester_old() throws IOException {
    var in = oldWfst.getBytesReader();
    var scratch = new BytesRefBuilder();
    for (String prefix : prefixes) {
      var arc = oldWfst.getFirstArc(new org.apache.lucene.util.fst.FST.Arc<>());
      Long prefixOutput = oldWfst.outputs.getNoOutput();
      for (int i = 0; i < prefix.length(); i++) {
        arc = oldWfst.findTargetArc(prefix.charAt(i), arc, arc, in);
        assertNotNull(arc);
        prefixOutput = oldWfst.outputs.add(prefixOutput, arc.output());
      }
      var results =
          org.apache.lucene.util.fst.Util.shortestPaths(
              oldWfst, arc, prefixOutput, Comparator.naturalOrder(), TOP_N, true);
      List<String> completions = new ArrayList<>();
      for (var result : results) {
        completions.add(
            prefix
                + org.apache.lucene.util.fst.Util.toBytesRef(result.input(), scratch)
                    .utf8ToString());
        long weight = Long.MAX_VALUE - result.output();
        assertEquals(reference.get(new BytesRef(completions.getLast())).longValue(), weight);
      }
      assertEquals(prefix, expectedCompletions(prefix), completions);
      printSample("suggest(old) " + prefix, completions);
    }
  }

  public void testSuggester() throws IOException {
    var in = newWfst.getBytesReader();
    var scratch = new BytesRefBuilder();
    for (String prefix : prefixes) {
      var arc = newWfst.getFirstArc(new FST.Arc());
      long prefixOutput = FST.NO_OUTPUT;
      for (int i = 0; i < prefix.length(); i++) {
        arc = newWfst.findTargetArc(prefix.charAt(i), arc, arc, in);
        assertNotNull(arc);
        prefixOutput += arc.output();
      }
      var results = Util.shortestPaths(newWfst, arc, prefixOutput, TOP_N, true);
      List<String> completions = new ArrayList<>();
      for (var result : results) {
        completions.add(prefix + Util.toBytesRef(result.input(), scratch).utf8ToString());
        long weight = Long.MAX_VALUE - result.output();
        assertEquals(reference.get(new BytesRef(completions.getLast())).longValue(), weight);
      }
      assertEquals(prefix, expectedCompletions(prefix), completions);
      printSample("suggest(new) " + prefix, completions);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // 6. Byte payload lookup: ByteSequenceOutputs vs. ordinal + BytesRefStore. Both through the
  //    convenience method and as a manual walk.

  public void testPayloadLookup_old() throws IOException {
    var in = oldBytesFst.getBytesReader();
    var outputs = oldBytesFst.outputs;
    for (BytesRef key : hits) {
      // Lookup through Util: concatenates the pushed byte fragments along the path.
      BytesRef value = org.apache.lucene.util.fst.Util.get(oldBytesFst, key);
      assertNotNull(value);
      assertEquals(bytesOutput(key), value);

      // Manual walk: every arc's fragment is decoded and appended eagerly.
      var arc = oldBytesFst.getFirstArc(new org.apache.lucene.util.fst.FST.Arc<>());
      BytesRef acc = outputs.getNoOutput();
      for (int i = 0; i < key.length; i++) {
        arc = oldBytesFst.findTargetArc(key.bytes[key.offset + i] & 0xFF, arc, arc, in);
        acc = outputs.add(acc, arc.output());
      }
      assertTrue(arc.isFinal());
      acc = outputs.add(acc, arc.nextFinalOutput());
      assertEquals(value, acc);
    }
    for (BytesRef key : misses) {
      assertNull(org.apache.lucene.util.fst.Util.get(oldBytesFst, key));
    }
  }

  public void testPayloadLookup() throws IOException {
    var fst = newBytesMap.fst();
    var store = newBytesMap.store();
    var in = fst.getBytesReader();
    BytesRefBuilder value = new BytesRefBuilder();
    BytesRefBuilder viaWalk = new BytesRefBuilder();
    for (BytesRef key : hits) {
      // Lookup through the map: the FST yields the ordinal, the store copies the value.
      assertTrue(newBytesMap.get(key, value));
      assertEquals(bytesOutput(key), value.get());

      // Manual walk: sum the ordinal deltas, then one random access into the store.
      var arc = fst.getFirstArc(new FST.Arc());
      long ord = FST.NO_OUTPUT;
      for (int i = 0; i < key.length; i++) {
        arc = fst.findTargetArc(key.bytes[key.offset + i] & 0xFF, arc, arc, in);
        ord += arc.output();
      }
      assertTrue(arc.isFinal());
      ord += arc.nextFinalOutput();
      store.get(ord, viaWalk);
      assertEquals(value.get(), viaWalk.get());
    }
    for (BytesRef key : misses) {
      assertFalse(newBytesMap.get(key, value));
    }
  }

  // ---------------------------------------------------------------------------------------------
  // 7. Manual walk summing long outputs by hand: the clearest diff of the Arc API.

  public void testManualWalk_old() throws IOException {
    var in = oldLongFst.getBytesReader();
    var outputs = oldLongFst.outputs;
    for (BytesRef key : hits) {
      var arc = oldLongFst.getFirstArc(new org.apache.lucene.util.fst.FST.Arc<>());
      Long acc = outputs.getNoOutput();
      for (int i = 0; i < key.length; i++) {
        arc = oldLongFst.findTargetArc(key.bytes[key.offset + i] & 0xFF, arc, arc, in);
        assertNotNull(arc);
        acc = outputs.add(acc, arc.output());
      }
      assertTrue(arc.isFinal());
      acc = outputs.add(acc, arc.nextFinalOutput());
      assertEquals(reference.get(key), acc);
    }
  }

  public void testManualWalk() throws IOException {
    var in = newLongFst.getBytesReader();
    for (BytesRef key : hits) {
      var arc = newLongFst.getFirstArc(new FST.Arc());
      long acc = FST.NO_OUTPUT;
      for (int i = 0; i < key.length; i++) {
        arc = newLongFst.findTargetArc(key.bytes[key.offset + i] & 0xFF, arc, arc, in);
        assertNotNull(arc);
        acc += arc.output();
      }
      assertTrue(arc.isFinal());
      acc += arc.nextFinalOutput();
      assertEquals(reference.get(key).longValue(), acc);
    }
  }
}
