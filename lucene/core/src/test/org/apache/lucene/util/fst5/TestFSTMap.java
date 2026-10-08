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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.tests.util.LuceneTestCase;
import org.apache.lucene.tests.util.TestUtil;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.IntsRefBuilder;

public class TestFSTMap extends LuceneTestCase {

  private static TreeMap<BytesRef, BytesRef> randomMap(int count) {
    TreeMap<BytesRef, BytesRef> map = new TreeMap<>();
    // A small value pool so that deduplication kicks in sometimes.
    List<BytesRef> pool = new ArrayList<>();
    int poolSize = random().nextBoolean() ? 5 : count;
    for (int i = 0; i < poolSize; i++) {
      byte[] b = new byte[random().nextInt(40)];
      random().nextBytes(b);
      pool.add(new BytesRef(b));
    }
    while (map.size() < count) {
      BytesRef key = newBytesRef(TestUtil.randomUnicodeString(random(), 12));
      map.put(key, pool.get(random().nextInt(pool.size())));
    }
    return map;
  }

  public void testRandom() throws IOException {
    TreeMap<BytesRef, BytesRef> reference = randomMap(atLeast(300));
    FSTMap.Builder builder = new FSTMap.Builder();
    long expectedOrd = 0;
    for (Map.Entry<BytesRef, BytesRef> e : reference.entrySet()) {
      assertEquals(expectedOrd++, builder.add(e.getKey(), e.getValue()));
    }
    check(builder.build(), reference);

    // Persist and reload, on heap and off heap.
    FSTMap.Builder again = new FSTMap.Builder();
    for (Map.Entry<BytesRef, BytesRef> e : reference.entrySet()) {
      again.add(e.getKey(), e.getValue());
    }
    try (Directory dir = new MMapDirectory(createTempDir("fstmap"))) {
      try (IndexOutput meta = dir.createOutput("meta", IOContext.DEFAULT);
          IndexOutput data = dir.createOutput("data", IOContext.DEFAULT)) {
        assertNotNull(again.save(meta, data));
        data.writeLong(0xCAFEBABEL);
      }
      try (IndexInput meta = dir.openInput("meta", IOContext.DEFAULT);
          IndexInput data = dir.openInput("data", IOContext.DEFAULT)) {
        FSTMap onHeap = FSTMap.load(meta, data);
        assertEquals(0xCAFEBABEL, data.readLong());
        check(onHeap, reference);
      }
      try (IndexInput meta = dir.openInput("meta", IOContext.DEFAULT);
          IndexInput data = dir.openInput("data", IOContext.DEFAULT)) {
        FSTMap offHeap = FSTMap.loadOffHeap(meta, data);
        assertEquals(0xCAFEBABEL, data.readLong());
        check(offHeap, reference);
      }
    }
  }

  public void testEmptyMap() throws IOException {
    assertNull(new FSTMap.Builder().build());
  }

  public void testEmptyKey() throws IOException {
    FSTMap.Builder builder = new FSTMap.Builder();
    builder.add(new BytesRef(), newBytesRef("empty"));
    builder.add(newBytesRef("a"), newBytesRef("letter a"));
    FSTMap map = builder.build();
    BytesRefBuilder value = new BytesRefBuilder();
    assertTrue(map.get(new BytesRef(), value));
    assertEquals(newBytesRef("empty"), value.get());
    assertTrue(map.get(newBytesRef("a"), value));
    assertEquals(newBytesRef("letter a"), value.get());
    assertEquals(0, map.ordinal(new BytesRef()));
    assertEquals(1, map.ordinal(newBytesRef("a")));
  }

  /**
   * Ordinal outputs are structural: an arc's pushed output is the number of keys accepted before it
   * within its node's subtree, so identical suffix subtrees carry identical outputs. The one
   * exception is finality: the FSA merges two nodes that differ only in being final (finality lives
   * on the incoming arc), while with ordinals a final node's own key shifts its children's outputs
   * by one. The ordinal FST is therefore at most slightly larger than the FSA, and much smaller
   * than the same keys with arbitrary outputs.
   */
  public void testOrdinalOutputsPreserveSuffixSharing() throws IOException {
    // Keys with lots of shared suffixes.
    TreeMap<BytesRef, BytesRef> keys = new TreeMap<>();
    String[] suffixes = {"ing", "ed", "s", "er", "ers", "ation", "ations", "", "ly", "ness"};
    for (int i = 0; i < 200; i++) {
      String stem = TestUtil.randomSimpleString(random(), 2, 8);
      for (String suffix : suffixes) {
        BytesRef key = newBytesRef(stem + suffix);
        keys.put(key, newBytesRef("v" + key.hashCode()));
      }
    }

    IntsRefBuilder scratch = new IntsRefBuilder();
    FSTCompiler fsa =
        new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1)
            .suffixRAMLimitMB(Double.POSITIVE_INFINITY)
            .build();
    FSTCompiler ordinals =
        new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1)
            .suffixRAMLimitMB(Double.POSITIVE_INFINITY)
            .build();
    FSTCompiler arbitrary =
        new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1)
            .suffixRAMLimitMB(Double.POSITIVE_INFINITY)
            .build();
    long ord = 0;
    for (BytesRef key : keys.keySet()) {
      fsa.add(Util.toIntsRef(key, scratch), FST.NO_OUTPUT);
      ordinals.add(Util.toIntsRef(key, scratch), ord++);
      arbitrary.add(Util.toIntsRef(key, scratch), 1 + (key.hashCode() & 0xFFFFFF));
    }
    fsa.compile();
    ordinals.compile();
    arbitrary.compile();

    assertTrue(
        "ordinals=" + ordinals.getNodeCount() + " fsa=" + fsa.getNodeCount(),
        ordinals.getNodeCount() >= fsa.getNodeCount()
            && ordinals.getNodeCount() <= fsa.getNodeCount() * 1.05);
    assertTrue(
        "arbitrary=" + arbitrary.getNodeCount() + " ordinals=" + ordinals.getNodeCount(),
        arbitrary.getNodeCount() > ordinals.getNodeCount() * 1.5);
    if (VERBOSE) {
      System.out.println(
          "nodes: fsa="
              + fsa.getNodeCount()
              + " ordinals="
              + ordinals.getNodeCount()
              + " arbitrary="
              + arbitrary.getNodeCount());
    }
  }

  private static void check(FSTMap map, TreeMap<BytesRef, BytesRef> reference) throws IOException {
    assertEquals(reference.size(), map.size());
    BytesRefBuilder value = new BytesRefBuilder();
    FST.Arc arc = new FST.Arc();
    FST.BytesReader reader = map.fst().getBytesReader();

    long ord = 0;
    for (Map.Entry<BytesRef, BytesRef> e : reference.entrySet()) {
      assertEquals(ord, map.ordinal(e.getKey()));
      assertEquals(ord, map.ordinal(e.getKey(), arc, reader));
      assertTrue(map.contains(e.getKey()));
      assertTrue(map.get(e.getKey(), value));
      assertEquals(e.getValue(), value.get());
      value.clear();
      assertTrue(map.get(e.getKey(), value, arc, reader));
      assertEquals(e.getValue(), value.get());
      map.value(ord, value);
      assertEquals(e.getValue(), value.get());
      ord++;
    }

    // misses
    for (int i = 0; i < 100; i++) {
      BytesRef miss = newBytesRef(TestUtil.randomUnicodeString(random(), 12));
      if (reference.containsKey(miss) == false) {
        assertEquals(Util.NOT_FOUND, map.ordinal(miss));
        assertFalse(map.contains(miss));
        value.clear();
        value.append(newBytesRef("untouched"));
        assertFalse(map.get(miss, value));
        assertEquals(newBytesRef("untouched"), value.get());
      }
    }

    // full enumeration
    FSTMap.Entries entries = map.entries();
    ord = 0;
    for (Map.Entry<BytesRef, BytesRef> e : reference.entrySet()) {
      assertTrue(entries.next());
      assertEquals(e.getKey(), entries.key());
      assertEquals(ord++, entries.ordinal());
      entries.value(value);
      assertEquals(e.getValue(), value.get());
    }
    assertFalse(entries.next());

    // seekCeil
    for (int i = 0; i < 50; i++) {
      BytesRef target = newBytesRef(TestUtil.randomUnicodeString(random(), 12));
      BytesRef expected = reference.ceilingKey(target);
      FSTMap.Entries cursor = map.entries();
      if (expected == null) {
        assertFalse(cursor.seekCeil(target));
      } else {
        assertTrue(cursor.seekCeil(target));
        assertEquals(expected, cursor.key());
        cursor.value(value);
        assertEquals(reference.get(expected), value.get());
      }
    }
  }
}
