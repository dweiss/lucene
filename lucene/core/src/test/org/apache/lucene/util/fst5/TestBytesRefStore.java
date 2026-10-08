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
import org.apache.lucene.store.ByteBuffersDataOutput;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.tests.util.LuceneTestCase;
import org.apache.lucene.tests.util.TestUtil;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;

public class TestBytesRefStore extends LuceneTestCase {

  private static List<BytesRef> randomValues(int count, int poolSize, int maxLength) {
    List<BytesRef> pool = new ArrayList<>();
    for (int i = 0; i < poolSize; i++) {
      byte[] b = new byte[random().nextInt(maxLength + 1)];
      random().nextBytes(b);
      pool.add(new BytesRef(b));
    }
    List<BytesRef> values = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      values.add(pool.get(random().nextInt(pool.size())));
    }
    return values;
  }

  public void testRandom() throws IOException {
    int count = atLeast(500);
    // Sometimes many repeats (dedup pays off), sometimes almost none.
    int poolSize = random().nextBoolean() ? Math.max(1, count / 20) : count * 2;
    List<BytesRef> values = randomValues(count, poolSize, 100);
    checkAllWays(values);
  }

  public void testEmpty() throws IOException {
    checkAllWays(List.of());
  }

  public void testSingleEmptyValue() throws IOException {
    checkAllWays(List.of(new BytesRef()));
  }

  public void testLargeValue() throws IOException {
    byte[] big = new byte[30_000];
    random().nextBytes(big);
    checkAllWays(List.of(new BytesRef("a"), new BytesRef(big), new BytesRef(), new BytesRef(big)));
  }

  public void testDedupDecision() throws IOException {
    // Three distinct values repeated many times: must be deduplicated.
    List<BytesRef> few = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      few.add(new BytesRef("value-" + (i % 3)));
    }
    BytesRefStore store = build(few);
    assertTrue(store.isDeduplicated());
    assertEquals(3, store.numDistinctValues());
    assertEquals(1000, store.size());
    check(store, few);

    // All distinct: the indirection would only cost space.
    List<BytesRef> distinct = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      distinct.add(new BytesRef(TestUtil.randomUnicodeString(random(), 10) + "-" + i));
    }
    store = build(distinct);
    assertFalse(store.isDeduplicated());
    assertEquals(1000, store.numDistinctValues());
    check(store, distinct);
  }

  public void testBadOrdinal() throws IOException {
    BytesRefStore store = build(List.of(new BytesRef("a"), new BytesRef("b")));
    BytesRefBuilder scratch = new BytesRefBuilder();
    expectThrows(IndexOutOfBoundsException.class, () -> store.get(-1, scratch));
    expectThrows(IndexOutOfBoundsException.class, () -> store.get(2, scratch));
  }

  private static BytesRefStore build(List<BytesRef> values) throws IOException {
    BytesRefStore.Builder builder = new BytesRefStore.Builder();
    for (int i = 0; i < values.size(); i++) {
      assertEquals(i, builder.add(values.get(i)));
    }
    return builder.build();
  }

  private static void check(BytesRefStore store, List<BytesRef> values) throws IOException {
    assertEquals(values.size(), store.size());
    BytesRefBuilder scratch = new BytesRefBuilder();
    for (int i = 0; i < values.size(); i++) {
      store.get(i, scratch);
      assertEquals("ord=" + i, values.get(i), scratch.get());
    }
    // random access order too
    for (int i = 0; i < values.size(); i++) {
      int ord = random().nextInt(values.size());
      store.get(ord, scratch);
      assertEquals(values.get(ord), scratch.get());
    }
  }

  /** Builds, checks, then round-trips through the on-heap and the off-heap loader. */
  private void checkAllWays(List<BytesRef> values) throws IOException {
    BytesRefStore.Builder builder = new BytesRefStore.Builder();
    for (BytesRef v : values) {
      builder.add(v);
    }
    check(builder.build(), values);

    // on heap, from a DataInput
    ByteBuffersDataOutput out = new ByteBuffersDataOutput();
    builder.save(out);
    out.writeLong(0xCAFEBABEL); // trailing marker: loading must consume exactly the store
    var in = out.toDataInput();
    check(BytesRefStore.loadOnHeap(in), values);
    assertEquals(0xCAFEBABEL, in.readLong());

    // off heap, from an IndexInput
    try (Directory dir = new MMapDirectory(createTempDir("store"))) {
      try (IndexOutput o = dir.createOutput("store", IOContext.DEFAULT)) {
        o.writeVInt(42); // leading junk: the store need not start at offset 0
        builder.save(o);
        o.writeLong(0xCAFEBABEL);
      }
      try (IndexInput i = dir.openInput("store", IOContext.DEFAULT)) {
        assertEquals(42, i.readVInt());
        BytesRefStore store = BytesRefStore.load(i);
        assertEquals(0xCAFEBABEL, i.readLong());
        check(store, values);
      }
    }
  }
}
