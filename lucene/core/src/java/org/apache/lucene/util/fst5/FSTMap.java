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
import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.RamUsageEstimator;

/**
 * A sorted map from byte sequences to byte sequences, layered over a long-valued {@link FST} and a
 * {@link BytesRefStore}. The FST maps each key to its ordinal (its index in sorted order); the
 * store maps the ordinal to the value.
 *
 * <p>Ordinal outputs are "structural": after output pushing, the output of an arc is the number of
 * keys accepted before it within its node's subtree, so identical suffix subtrees carry identical
 * outputs and suffix sharing in the FST is preserved independently of the values. (The single
 * exception: two nodes that differ only in finality are merged by an FSA, because finality is
 * stored on the incoming arc, but a final node's own key shifts its children's ordinals by one, so
 * such pairs stay distinct here. In practice this costs a fraction of a percent of the nodes.) The
 * values are compressed by the store instead of being pushed along arcs.
 *
 * @lucene.experimental
 */
public final class FSTMap implements Accountable {

  private static final long BASE_RAM_BYTES_USED =
      RamUsageEstimator.shallowSizeOfInstance(FSTMap.class);

  private final FST fst;
  private final BytesRefStore store;

  public FSTMap(FST fst, BytesRefStore store) {
    this.fst = fst;
    this.store = store;
  }

  /** The key automaton (outputs are ordinals). */
  public FST fst() {
    return fst;
  }

  /** The value store (indexed by ordinal). */
  public BytesRefStore store() {
    return store;
  }

  /** Number of entries. */
  public long size() {
    return store.size();
  }

  /** Ordinal of the key, or {@link Util#NOT_FOUND}. */
  public long ordinal(BytesRef key) throws IOException {
    return Util.get(fst, key);
  }

  /** Ordinal of the key, or {@link Util#NOT_FOUND}; reuses the given arc and reader. */
  public long ordinal(BytesRef key, FST.Arc arc, FST.BytesReader reader) throws IOException {
    return Util.get(fst, key, arc, reader);
  }

  /** True if the key is present (no value decoding takes place). */
  public boolean contains(BytesRef key) throws IOException {
    return Util.contains(fst, key);
  }

  /**
   * Copies the value for {@code key} into {@code value} and returns true, or returns false (leaving
   * {@code value} untouched) if the key is absent.
   */
  public boolean get(BytesRef key, BytesRefBuilder value) throws IOException {
    return get(key, value, new FST.Arc(), fst.getBytesReader());
  }

  /** Allocation-free variant of {@link #get(BytesRef, BytesRefBuilder)}. */
  public boolean get(BytesRef key, BytesRefBuilder value, FST.Arc arc, FST.BytesReader reader)
      throws IOException {
    long ord = Util.get(fst, key, arc, reader);
    if (ord == Util.NOT_FOUND) {
      return false;
    }
    store.get(ord, value);
    return true;
  }

  /** Value for an ordinal previously returned by {@link #ordinal}. */
  public void value(long ordinal, BytesRefBuilder value) throws IOException {
    store.get(ordinal, value);
  }

  /** Sorted cursor over all entries. */
  public Entries entries() {
    return new Entries(this);
  }

  /** A reusable cursor; the key is valid until the next call to {@link #next()}. */
  public static final class Entries {
    private final FSTMap map;
    private final BytesRefFSTEnum fstEnum;
    private BytesRefFSTEnum.InputOutput current;

    Entries(FSTMap map) {
      this.map = map;
      this.fstEnum = new BytesRefFSTEnum(map.fst);
    }

    /** Advances to the next entry; false at the end. */
    public boolean next() throws IOException {
      current = fstEnum.next();
      return current != null;
    }

    /** Seeks to the smallest key &gt;= target; false if there is none. */
    public boolean seekCeil(BytesRef target) throws IOException {
      current = fstEnum.seekCeil(target);
      return current != null;
    }

    public BytesRef key() {
      return current.input;
    }

    public long ordinal() {
      return current.output;
    }

    /** Copies the current value into {@code dest}. */
    public void value(BytesRefBuilder dest) throws IOException {
      map.store.get(current.output, dest);
    }
  }

  // ---- serialization

  /** Loads an on-heap map. */
  public static FSTMap load(DataInput metaIn, DataInput in) throws IOException {
    FST.FSTMetadata meta = FST.readMetadata(metaIn);
    FST fst = new FST(meta, in);
    BytesRefStore store = BytesRefStore.loadOnHeap(in);
    return new FSTMap(fst, store);
  }

  /** Loads a map whose FST bytes and store sections stay in the (possibly mmapped) input. */
  public static FSTMap loadOffHeap(DataInput metaIn, IndexInput in) throws IOException {
    FST.FSTMetadata meta = FST.readMetadata(metaIn);
    long start = in.getFilePointer();
    FST fst = FST.fromFSTReader(meta, new OffHeapFSTStore(in, start, meta));
    in.seek(start + meta.getNumBytes());
    BytesRefStore store = BytesRefStore.load(in);
    return new FSTMap(fst, store);
  }

  @Override
  public long ramBytesUsed() {
    return BASE_RAM_BYTES_USED + fst.ramBytesUsed() + store.ramBytesUsed();
  }

  @Override
  public String toString() {
    return "FSTMap(" + fst + ", " + store + ")";
  }

  // ---- building

  /** Builds an {@link FSTMap} from (key, value) pairs added in sorted key order. */
  public static final class Builder {
    private final FSTCompiler compiler;
    private final BytesRefStore.Builder values = new BytesRefStore.Builder();
    private final IntsRefBuilder scratchInts = new IntsRefBuilder();

    public Builder() {
      this(new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1).build());
    }

    public Builder(FSTCompiler compiler) {
      this.compiler = compiler;
    }

    /** Adds the next entry; keys must be unique and sorted. Returns the entry's ordinal. */
    public long add(BytesRef key, BytesRef value) throws IOException {
      long ord = values.size();
      compiler.add(Util.toIntsRef(key, scratchInts), ord);
      values.add(value);
      return ord;
    }

    /** Builds an on-heap map. */
    public FSTMap build() throws IOException {
      FST.FSTMetadata meta = compiler.compile();
      if (meta == null) {
        return null;
      }
      FST fst = FST.fromFSTReader(meta, compiler.getFSTReader());
      return new FSTMap(fst, values.build());
    }

    /**
     * Compiles and persists the map: FST metadata to {@code metaOut}; FST bytes followed by the
     * store to {@code out}. Returns null if nothing was added.
     */
    public FST.FSTMetadata save(DataOutput metaOut, DataOutput out) throws IOException {
      FST.FSTMetadata meta = compiler.compile();
      if (meta == null) {
        return null;
      }
      FST fst = FST.fromFSTReader(meta, compiler.getFSTReader());
      fst.save(metaOut, out);
      values.save(out);
      return meta;
    }
  }
}
