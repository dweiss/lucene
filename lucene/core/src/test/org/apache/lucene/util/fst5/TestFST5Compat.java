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
 * Pins the "no format change" claim of {@code fst5}: an FST built and saved by the old {@link
 * org.apache.lucene.util.fst.FSTCompiler} with {@code PositiveIntOutputs} (or {@code NoOutputs})
 * must be readable by the new {@link FST} (on heap and through {@link OffHeapFSTStore}), and vice
 * versa, with identical lookups. Additionally the bytes written by both compilers for the same
 * input must be identical.
 */
public class TestFST5Compat extends LuceneTestCase {

  /** A deterministic, sorted, de-duplicated set of non-empty terms. */
  private static List<BytesRef> randomTerms(Random random) {
    TreeSet<BytesRef> terms = new TreeSet<>();
    int count = atLeast(random, 200);
    while (terms.size() < count) {
      BytesRef t = newBytesRef(TestUtil.randomUnicodeString(random, 20));
      if (t.length > 0) {
        terms.add(t);
      }
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
    LONG;

    org.apache.lucene.util.fst.Outputs<Object> oldOutputs() {
      return switch (this) {
        case NONE -> cast(org.apache.lucene.util.fst.NoOutputs.getSingleton());
        case LONG -> cast(org.apache.lucene.util.fst.PositiveIntOutputs.getSingleton());
      };
    }

    /** The value of term i, as a long (zero for NONE). */
    long value(Random random, int i) {
      return switch (this) {
        case NONE -> 0;
        case LONG -> {
          // Deterministic per term, mixing zero (NO_OUTPUT) and large values.
          Random r = new Random(random.nextLong() ^ i);
          yield r.nextInt(10) == 0 ? 0 : TestUtil.nextLong(r, 1, 1L << 50);
        }
      };
    }

    /** The value of term i, as an old-package object. */
    Object oldValue(Random random, org.apache.lucene.util.fst.Outputs<Object> outputs, int i) {
      return switch (this) {
        case NONE -> outputs.getNoOutput();
        case LONG -> value(random, i);
      };
    }

    @SuppressWarnings("unchecked")
    static <X> X cast(Object o) {
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
    org.apache.lucene.util.fst.FST<Object> oldFst = buildOld(kind, oldOutputs, terms, seed);

    try (Directory dir = new MMapDirectory(createTempDir("fst5compat"))) {
      try (IndexOutput out = dir.createOutput("fst", IOContext.DEFAULT)) {
        oldFst.save(out, out);
      }
      try (IndexInput in = dir.openInput("fst", IOContext.DEFAULT)) {
        // On heap.
        FST.FSTMetadata meta = FST.readMetadata(in);
        FST onHeap = new FST(meta, in);
        assertSameContent(kind, oldFst, onHeap, terms, misses);
      }
      try (IndexInput in = dir.openInput("fst", IOContext.DEFAULT)) {
        // Off heap.
        FST.FSTMetadata meta = FST.readMetadata(in);
        FST offHeap = FST.fromFSTReader(meta, new OffHeapFSTStore(in, in.getFilePointer(), meta));
        assertSameContent(kind, oldFst, offHeap, terms, misses);
      }
    }
  }

  private void doTestNewToOld(Kind kind) throws IOException {
    long seed = random().nextLong();
    List<BytesRef> terms = randomTerms(new Random(seed));
    List<BytesRef> misses = randomMisses(new Random(seed), terms);

    org.apache.lucene.util.fst.Outputs<Object> oldOutputs = kind.oldOutputs();
    FST newFst = buildNew(kind, terms, seed);

    try (Directory dir = new MMapDirectory(createTempDir("fst5compat"))) {
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
    FST newFst = buildNew(kind, terms, seed);

    ByteBuffersDataOutput oldBytes = new ByteBuffersDataOutput();
    oldFst.save(oldBytes, oldBytes);
    ByteBuffersDataOutput newBytes = new ByteBuffersDataOutput();
    newFst.save(newBytes, newBytes);
    assertArrayEquals(kind.toString(), oldBytes.toArrayCopy(), newBytes.toArrayCopy());
  }

  /**
   * The only byte-level difference between the generic FST and this one: NoOutputs writes zero
   * bytes for the empty input's output, this writer always writes a VLong. Both directions must
   * still load.
   */
  public void testEmptyInputInterop() throws IOException {
    IntsRefBuilder scratch = new IntsRefBuilder();

    // old NoOutputs -> new
    {
      var outputs = org.apache.lucene.util.fst.NoOutputs.getSingleton();
      var compiler =
          new org.apache.lucene.util.fst.FSTCompiler.Builder<>(
                  org.apache.lucene.util.fst.FST.INPUT_TYPE.BYTE1, outputs)
              .build();
      compiler.add(new org.apache.lucene.util.IntsRef(), outputs.getNoOutput());
      compiler.add(
          org.apache.lucene.util.fst.Util.toIntsRef(newBytesRef("a"), scratch),
          outputs.getNoOutput());
      var oldFst =
          org.apache.lucene.util.fst.FST.fromFSTReader(compiler.compile(), compiler.getFSTReader());
      ByteBuffersDataOutput bytes = new ByteBuffersDataOutput();
      oldFst.save(bytes, bytes);
      var in = bytes.toDataInput();
      FST newFst = new FST(FST.readMetadata(in), in);
      assertTrue(newFst.acceptsEmptyInput());
      assertEquals(0, newFst.getEmptyOutput());
      assertEquals(0, Util.get(newFst, new BytesRef()));
      assertEquals(0, Util.get(newFst, newBytesRef("a")));
      assertEquals(Util.NOT_FOUND, Util.get(newFst, newBytesRef("b")));
    }

    // new (with a non-zero empty output) -> old PositiveIntOutputs
    {
      FSTCompiler compiler = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1).build();
      compiler.add(new org.apache.lucene.util.IntsRef(), 5);
      compiler.add(Util.toIntsRef(newBytesRef("a"), scratch), 7);
      FST newFst = FST.fromFSTReader(compiler.compile(), compiler.getFSTReader());
      ByteBuffersDataOutput bytes = new ByteBuffersDataOutput();
      newFst.save(bytes, bytes);
      var in = bytes.toDataInput();
      var outputs = org.apache.lucene.util.fst.PositiveIntOutputs.getSingleton();
      org.apache.lucene.util.fst.FST<Long> oldFst =
          new org.apache.lucene.util.fst.FST<>(
              org.apache.lucene.util.fst.FST.readMetadata(in, outputs), in);
      assertEquals(Long.valueOf(5), oldFst.getEmptyOutput());
      assertEquals(Long.valueOf(5), org.apache.lucene.util.fst.Util.get(oldFst, new BytesRef()));
      assertEquals(Long.valueOf(7), org.apache.lucene.util.fst.Util.get(oldFst, newBytesRef("a")));
    }

    // new (all zero) -> old NoOutputs
    {
      FSTCompiler compiler = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1).build();
      compiler.add(new org.apache.lucene.util.IntsRef(), 0);
      compiler.add(Util.toIntsRef(newBytesRef("a"), scratch), 0);
      FST newFst = FST.fromFSTReader(compiler.compile(), compiler.getFSTReader());
      ByteBuffersDataOutput bytes = new ByteBuffersDataOutput();
      newFst.save(bytes, bytes);
      var in = bytes.toDataInput();
      var outputs = org.apache.lucene.util.fst.NoOutputs.getSingleton();
      org.apache.lucene.util.fst.FST<Object> oldFst =
          new org.apache.lucene.util.fst.FST<>(
              org.apache.lucene.util.fst.FST.readMetadata(in, outputs), in);
      assertNotNull(org.apache.lucene.util.fst.Util.get(oldFst, new BytesRef()));
      assertNotNull(org.apache.lucene.util.fst.Util.get(oldFst, newBytesRef("a")));
      assertNull(org.apache.lucene.util.fst.Util.get(oldFst, newBytesRef("b")));
    }
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

  private static FST buildNew(Kind kind, List<BytesRef> terms, long seed) throws IOException {
    FSTCompiler compiler = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1).build();
    IntsRefBuilder scratch = new IntsRefBuilder();
    Random random = new Random(seed);
    for (int i = 0; i < terms.size(); i++) {
      compiler.add(Util.toIntsRef(terms.get(i), scratch), kind.value(random, i));
    }
    return FST.fromFSTReader(compiler.compile(), compiler.getFSTReader());
  }

  /** Every lookup and a full enumeration must agree between the two implementations. */
  private static void assertSameContent(
      Kind kind,
      org.apache.lucene.util.fst.FST<Object> oldFst,
      FST newFst,
      List<BytesRef> terms,
      List<BytesRef> misses)
      throws IOException {
    assertEquals(oldFst.numBytes(), newFst.numBytes());

    FST.Arc scratchArc = new FST.Arc();
    FST.BytesReader scratchReader = newFst.getBytesReader();
    for (BytesRef term : terms) {
      Object oldValue = org.apache.lucene.util.fst.Util.get(oldFst, term);
      long newValue = Util.get(newFst, term);
      assertNotNull(oldValue);
      assertNotEquals(Util.NOT_FOUND, newValue);
      assertSameValue(kind, oldValue, newValue);
      assertEquals(newValue, Util.get(newFst, term, scratchArc, scratchReader));
      assertTrue(Util.contains(newFst, term));
    }
    for (BytesRef miss : misses) {
      assertNull(org.apache.lucene.util.fst.Util.get(oldFst, miss));
      assertEquals(Util.NOT_FOUND, Util.get(newFst, miss));
      assertEquals(Util.NOT_FOUND, Util.get(newFst, miss, scratchArc, scratchReader));
      assertFalse(Util.contains(newFst, miss));
    }

    org.apache.lucene.util.fst.BytesRefFSTEnum<Object> oldEnum =
        new org.apache.lucene.util.fst.BytesRefFSTEnum<>(oldFst);
    BytesRefFSTEnum newEnum = new BytesRefFSTEnum(newFst);
    int count = 0;
    while (true) {
      org.apache.lucene.util.fst.BytesRefFSTEnum.InputOutput<Object> o = oldEnum.next();
      BytesRefFSTEnum.InputOutput n = newEnum.next();
      if (o == null) {
        assertNull(n);
        break;
      }
      assertNotNull(n);
      assertEquals(o.input, n.input);
      assertEquals(terms.get(count), n.input);
      assertSameValue(kind, o.output, n.output);
      count++;
    }
    assertEquals(terms.size(), count);
  }

  private static void assertSameValue(Kind kind, Object oldValue, long newValue) {
    switch (kind) {
      case NONE -> {
        assertSame(org.apache.lucene.util.fst.NoOutputs.getSingleton().getNoOutput(), oldValue);
        assertEquals(0, newValue);
      }
      case LONG -> assertEquals(((Long) oldValue).longValue(), newValue);
    }
  }
}
