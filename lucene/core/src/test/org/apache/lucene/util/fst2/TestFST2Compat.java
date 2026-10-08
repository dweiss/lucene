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
package org.apache.lucene.util.fst2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import org.apache.lucene.store.ByteBuffersDataOutput;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.tests.util.LuceneTestCase;
import org.apache.lucene.tests.util.TestUtil;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRefBuilder;

/**
 * Pins the "no format change" claim of {@code fst2}: an FST built and saved by the old {@link
 * org.apache.lucene.util.fst.FSTCompiler} must be readable by the new {@link FST} (on heap and
 * through {@link OffHeapFSTStore}), and vice versa, with identical lookups. Additionally the bytes
 * written by both compilers for the same input must be identical.
 */
public class TestFST2Compat extends LuceneTestCase {

  /** A deterministic, sorted, de-duplicated set of terms. */
  private static List<BytesRef> randomTerms(Random random) {
    TreeSet<BytesRef> terms = new TreeSet<>();
    int count = atLeast(random, 200);
    while (terms.size() < count) {
      terms.add(newBytesRef(TestUtil.randomUnicodeString(random, 20)));
    }
    return new ArrayList<>(terms);
  }

  private static List<BytesRef> randomMisses(Random random, List<BytesRef> terms) {
    TreeSet<BytesRef> present = new TreeSet<>(terms);
    List<BytesRef> misses = new ArrayList<>();
    while (misses.size() < 100) {
      BytesRef t = newBytesRef(TestUtil.randomUnicodeString(random, 20));
      if (present.contains(t) == false) {
        misses.add(t);
      }
    }
    return misses;
  }

  /** The kinds of outputs under test; each knows how to produce the value for term number i. */
  private enum Kind {
    NONE,
    LONG,
    BYTES,
    PAIR;

    org.apache.lucene.util.fst.Outputs<Object> oldOutputs() {
      return switch (this) {
        case NONE -> cast(org.apache.lucene.util.fst.NoOutputs.getSingleton());
        case LONG -> cast(org.apache.lucene.util.fst.PositiveIntOutputs.getSingleton());
        case BYTES -> cast(org.apache.lucene.util.fst.ByteSequenceOutputs.getSingleton());
        case PAIR ->
            cast(
                new org.apache.lucene.util.fst.PairOutputs<>(
                    org.apache.lucene.util.fst.PositiveIntOutputs.getSingleton(),
                    org.apache.lucene.util.fst.ByteSequenceOutputs.getSingleton()));
      };
    }

    Outputs<Object> newOutputs() {
      return switch (this) {
        case NONE -> cast(NoOutputs.getSingleton());
        case LONG -> cast(PositiveIntOutputs.getSingleton());
        case BYTES -> cast(ByteSequenceOutputs.getSingleton());
        case PAIR ->
            cast(
                new PairOutputs<>(
                    PositiveIntOutputs.getSingleton(), ByteSequenceOutputs.getSingleton()));
      };
    }

    /** The value of term i, as an old-package object. */
    Object oldValue(Random random, org.apache.lucene.util.fst.Outputs<Object> outputs, int i) {
      return switch (this) {
        case NONE -> outputs.getNoOutput();
        case LONG -> randomLong(random, i);
        case BYTES -> randomBytes(random, i);
        case PAIR -> {
          @SuppressWarnings("unchecked")
          org.apache.lucene.util.fst.PairOutputs<Long, BytesRef> po =
              (org.apache.lucene.util.fst.PairOutputs<Long, BytesRef>) (Object) outputs;
          yield po.newPair(randomLong(random, i), randomBytes(random, i));
        }
      };
    }

    /** The value of term i, as a new-package object; must be the same value as oldValue. */
    Object newValue(Random random, Outputs<Object> outputs, int i) {
      return switch (this) {
        case NONE -> outputs.getNoOutput();
        case LONG -> randomLong(random, i);
        case BYTES -> randomBytes(random, i);
        case PAIR -> {
          @SuppressWarnings("unchecked")
          PairOutputs<Long, BytesRef> po = (PairOutputs<Long, BytesRef>) (Object) outputs;
          yield po.newPair(randomLong(random, i), randomBytes(random, i));
        }
      };
    }

    private static long randomLong(Random random, int i) {
      // Deterministic per term, mixing zero (NO_OUTPUT) and large values.
      Random r = new Random(random.nextLong() ^ i);
      return r.nextInt(10) == 0 ? 0 : TestUtil.nextLong(r, 1, 1L << 50);
    }

    private static BytesRef randomBytes(Random random, int i) {
      Random r = new Random(random.nextLong() ^ i);
      byte[] b = new byte[r.nextInt(8)];
      r.nextBytes(b);
      return new BytesRef(b);
    }

    @SuppressWarnings("unchecked")
    private static <X> X cast(Object o) {
      return (X) o;
    }
  }

  public void testOldToNew() throws IOException {
    for (Kind kind : Kind.values()) {
      doTestOldToNew(kind);
    }
  }

  public void testNewToOld() throws IOException {
    for (Kind kind : Kind.values()) {
      doTestNewToOld(kind);
    }
  }

  public void testIdenticalBytes() throws IOException {
    for (Kind kind : Kind.values()) {
      doTestIdenticalBytes(kind);
    }
  }

  private void doTestOldToNew(Kind kind) throws IOException {
    long seed = random().nextLong();
    List<BytesRef> terms = randomTerms(new Random(seed));
    List<BytesRef> misses = randomMisses(new Random(seed), terms);

    org.apache.lucene.util.fst.Outputs<Object> oldOutputs = kind.oldOutputs();
    Outputs<Object> newOutputs = kind.newOutputs();

    org.apache.lucene.util.fst.FST<Object> oldFst = buildOld(kind, oldOutputs, terms, seed);

    try (Directory dir = new MMapDirectory(createTempDir("fst2compat"))) {
      try (IndexOutput out = dir.createOutput("fst", IOContext.DEFAULT)) {
        oldFst.save(out, out);
      }
      try (IndexInput in = dir.openInput("fst", IOContext.DEFAULT)) {
        // On heap.
        FST.FSTMetadata<Object> meta = FST.readMetadata(in, newOutputs);
        FST<Object> onHeap = new FST<>(meta, in);
        assertSameContent(kind, oldFst, onHeap, terms, misses);
      }
      try (IndexInput in = dir.openInput("fst", IOContext.DEFAULT)) {
        // Off heap.
        FST.FSTMetadata<Object> meta = FST.readMetadata(in, newOutputs);
        FST<Object> offHeap =
            FST.fromFSTReader(meta, new OffHeapFSTStore(in, in.getFilePointer(), meta));
        assertSameContent(kind, oldFst, offHeap, terms, misses);
      }
    }
  }

  private void doTestNewToOld(Kind kind) throws IOException {
    long seed = random().nextLong();
    List<BytesRef> terms = randomTerms(new Random(seed));
    List<BytesRef> misses = randomMisses(new Random(seed), terms);

    org.apache.lucene.util.fst.Outputs<Object> oldOutputs = kind.oldOutputs();
    Outputs<Object> newOutputs = kind.newOutputs();

    FST<Object> newFst = buildNew(kind, newOutputs, terms, seed);

    try (Directory dir = new MMapDirectory(createTempDir("fst2compat"))) {
      try (IndexOutput out = dir.createOutput("fst", IOContext.DEFAULT)) {
        newFst.save(out, out);
      }
      try (IndexInput in = dir.openInput("fst", IOContext.DEFAULT)) {
        org.apache.lucene.util.fst.FST.FSTMetadata<Object> meta =
            org.apache.lucene.util.fst.FST.readMetadata(in, oldOutputs);
        org.apache.lucene.util.fst.FST<Object> onHeap =
            new org.apache.lucene.util.fst.FST<>(meta, in);
        assertSameContent(kind, onHeap, newFst, terms, misses);
      }
      try (IndexInput in = dir.openInput("fst", IOContext.DEFAULT)) {
        org.apache.lucene.util.fst.FST.FSTMetadata<Object> meta =
            org.apache.lucene.util.fst.FST.readMetadata(in, oldOutputs);
        org.apache.lucene.util.fst.FST<Object> offHeap =
            org.apache.lucene.util.fst.FST.fromFSTReader(
                meta,
                new org.apache.lucene.util.fst.OffHeapFSTStore(in, in.getFilePointer(), meta));
        assertSameContent(kind, offHeap, newFst, terms, misses);
      }
    }
  }

  private void doTestIdenticalBytes(Kind kind) throws IOException {
    long seed = random().nextLong();
    List<BytesRef> terms = randomTerms(new Random(seed));
    org.apache.lucene.util.fst.FST<Object> oldFst = buildOld(kind, kind.oldOutputs(), terms, seed);
    FST<Object> newFst = buildNew(kind, kind.newOutputs(), terms, seed);

    ByteBuffersDataOutput oldBytes = new ByteBuffersDataOutput();
    oldFst.save(oldBytes, oldBytes);
    ByteBuffersDataOutput newBytes = new ByteBuffersDataOutput();
    newFst.save(newBytes, newBytes);
    assertArrayEquals(kind.toString(), oldBytes.toArrayCopy(), newBytes.toArrayCopy());
  }

  private static org.apache.lucene.util.fst.FST<Object> buildOld(
      Kind kind,
      org.apache.lucene.util.fst.Outputs<Object> outputs,
      List<BytesRef> terms,
      long seed)
      throws IOException {
    org.apache.lucene.util.fst.FSTCompiler<Object> compiler =
        new org.apache.lucene.util.fst.FSTCompiler.Builder<>(
                org.apache.lucene.util.fst.FST.INPUT_TYPE.BYTE1, outputs)
            .build();
    IntsRefBuilder scratch = new IntsRefBuilder();
    Random random = new Random(seed);
    for (int i = 0; i < terms.size(); i++) {
      compiler.add(
          org.apache.lucene.util.fst.Util.toIntsRef(terms.get(i), scratch),
          kind.oldValue(random, outputs, i));
    }
    return org.apache.lucene.util.fst.FST.fromFSTReader(
        compiler.compile(), compiler.getFSTReader());
  }

  private static FST<Object> buildNew(
      Kind kind, Outputs<Object> outputs, List<BytesRef> terms, long seed) throws IOException {
    FSTCompiler<Object> compiler = new FSTCompiler.Builder<>(FST.INPUT_TYPE.BYTE1, outputs).build();
    IntsRefBuilder scratch = new IntsRefBuilder();
    Random random = new Random(seed);
    for (int i = 0; i < terms.size(); i++) {
      compiler.add(Util.toIntsRef(terms.get(i), scratch), kind.newValue(random, outputs, i));
    }
    return FST.fromFSTReader(compiler.compile(), compiler.getFSTReader());
  }

  /** Every lookup and a full enumeration must agree between the two implementations. */
  private static void assertSameContent(
      Kind kind,
      org.apache.lucene.util.fst.FST<Object> oldFst,
      FST<Object> newFst,
      List<BytesRef> terms,
      List<BytesRef> misses)
      throws IOException {
    assertEquals(oldFst.numBytes(), newFst.numBytes());

    for (BytesRef term : terms) {
      Object oldValue = org.apache.lucene.util.fst.Util.get(oldFst, term);
      Object newValue = Util.get(newFst, term);
      assertNotNull(oldValue);
      assertNotNull(newValue);
      assertSameValue(oldValue, newValue);
      assertTrue(Util.contains(newFst, term));
      if (kind == Kind.LONG) {
        assertEquals(((Long) oldValue).longValue(), Util.getLong(newFst, term, -1));
      }
    }
    for (BytesRef miss : misses) {
      assertNull(org.apache.lucene.util.fst.Util.get(oldFst, miss));
      assertNull(Util.get(newFst, miss));
      assertFalse(Util.contains(newFst, miss));
      if (kind == Kind.LONG) {
        assertEquals(-1, Util.getLong(newFst, miss, -1));
      }
    }

    org.apache.lucene.util.fst.BytesRefFSTEnum<Object> oldEnum =
        new org.apache.lucene.util.fst.BytesRefFSTEnum<>(oldFst);
    BytesRefFSTEnum<Object> newEnum = new BytesRefFSTEnum<>(newFst);
    int count = 0;
    while (true) {
      org.apache.lucene.util.fst.BytesRefFSTEnum.InputOutput<Object> o = oldEnum.next();
      BytesRefFSTEnum.InputOutput<Object> n = newEnum.next();
      if (o == null) {
        assertNull(n);
        break;
      }
      assertNotNull(n);
      assertEquals(o.input, n.input);
      assertEquals(terms.get(count), n.input);
      assertSameValue(o.output, n.output());
      if (kind == Kind.LONG) {
        assertEquals(((Long) o.output).longValue(), newEnum.currentOutputLong());
      }
      count++;
    }
    assertEquals(terms.size(), count);
  }

  /** Compares an old-package value with a new-package value (Pair types differ by package). */
  private static void assertSameValue(Object oldValue, Object newValue) {
    if (oldValue instanceof org.apache.lucene.util.fst.PairOutputs.Pair<?, ?> op) {
      PairOutputs.Pair<?, ?> np = (PairOutputs.Pair<?, ?>) newValue;
      assertSameValue(op.output1, np.output1);
      assertSameValue(op.output2, np.output2);
    } else if (oldValue == org.apache.lucene.util.fst.NoOutputs.getSingleton().getNoOutput()) {
      assertSame(NoOutputs.getSingleton().getNoOutput(), newValue);
    } else {
      assertEquals(oldValue, newValue);
    }
  }
}
