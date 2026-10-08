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
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.lucene.codecs.CodecUtil;
import org.apache.lucene.internal.hppc.IntArrayList;
import org.apache.lucene.store.ByteBuffersDataInput;
import org.apache.lucene.store.ByteBuffersDataOutput;
import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.RandomAccessInput;
import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.BytesRefHash;
import org.apache.lucene.util.RamUsageEstimator;
import org.apache.lucene.util.packed.DirectReader;
import org.apache.lucene.util.packed.DirectWriter;

/**
 * A compact, random-access, read-only store of byte sequences addressed by a dense ordinal {@code
 * [0, size())}. This is the "side store" that complements a long-valued {@link FST} whose outputs
 * are ordinals: the FST maps a key to an ordinal, the store maps the ordinal to its payload.
 *
 * <p>Layout: a single concatenated blob of all (distinct) values, a fixed-width packed array of
 * {@code numValues + 1} offsets into that blob, and, only when it pays off, a fixed-width packed
 * array mapping each ordinal to its distinct value id (values that repeat across many ordinals are
 * then stored once). All three sections are addressed through {@link RandomAccessInput}, so the
 * same reader serves an on-heap copy and an off-heap (memory-mapped) {@link IndexInput}.
 *
 * <p>Lookups copy the value into a caller-supplied {@link BytesRefBuilder} and never allocate.
 *
 * @lucene.experimental
 */
public final class BytesRefStore implements Accountable {

  private static final String CODEC_NAME = "FST5BytesRefStore";
  private static final int VERSION_CURRENT = 0;

  private static final long BASE_RAM_BYTES_USED =
      RamUsageEstimator.shallowSizeOfInstance(BytesRefStore.class);

  private final long count;
  private final long numValues;
  private final RandomAccessInput offsets; // numValues + 1 packed offsets
  private final int offsetBits;
  private final RandomAccessInput ordToValue; // count packed value ids, or null
  private final int valueBits;
  private final RandomAccessInput blob;
  private final long blobLength;
  private final long ramBytesUsed;

  private BytesRefStore(
      long count,
      long numValues,
      RandomAccessInput offsets,
      int offsetBits,
      RandomAccessInput ordToValue,
      int valueBits,
      RandomAccessInput blob,
      long blobLength,
      long ramBytesUsed) {
    this.count = count;
    this.numValues = numValues;
    this.offsets = offsets;
    this.offsetBits = offsetBits;
    this.ordToValue = ordToValue;
    this.valueBits = valueBits;
    this.blob = blob;
    this.blobLength = blobLength;
    this.ramBytesUsed = ramBytesUsed;
  }

  /** Number of ordinals (values added to the builder). */
  public long size() {
    return count;
  }

  /** Number of distinct values actually stored. Equal to {@link #size()} unless deduplicated. */
  public long numDistinctValues() {
    return numValues;
  }

  /** Total length of the stored (distinct) values. */
  public long blobLength() {
    return blobLength;
  }

  /** True if ordinals are indirected through a distinct-value table. */
  public boolean isDeduplicated() {
    return ordToValue != null;
  }

  /**
   * Copies the value for the given ordinal into {@code dest}, replacing its content. Does not
   * allocate once {@code dest} has grown to the largest value length.
   */
  public void get(long ord, BytesRefBuilder dest) throws IOException {
    if (ord < 0 || ord >= count) {
      throw new IndexOutOfBoundsException("ord=" + ord + " size=" + count);
    }
    long valueId = ord;
    if (ordToValue != null) {
      valueId = DirectReader.getInstance(ordToValue, valueBits).get(ord);
    }
    // read two adjacent offsets
    final long start = readOffset(valueId);
    final long end = readOffset(valueId + 1);
    final int len = Math.toIntExact(end - start);
    dest.growNoCopy(len);
    blob.readBytes(start, dest.bytes(), 0, len);
    dest.setLength(len);
  }

  private long readOffset(long index) throws IOException {
    return DirectReader.getInstance(offsets, offsetBits).get(index);
  }

  @Override
  public long ramBytesUsed() {
    return ramBytesUsed;
  }

  @Override
  public String toString() {
    return "BytesRefStore(size="
        + count
        + ",distinct="
        + numValues
        + ",blob="
        + blobLength
        + ",offsetBits="
        + offsetBits
        + ",valueBits="
        + valueBits
        + ")";
  }

  // ---- serialization

  /**
   * Loads a store from an {@link IndexInput}, addressing its sections in place (off-heap when the
   * input is memory mapped). The input's position is advanced past the store.
   */
  public static BytesRefStore load(IndexInput in) throws IOException {
    Header h = Header.read(in);
    long pos = in.getFilePointer();
    RandomAccessInput offsets = in.randomAccessSlice(pos, h.offsetsBytes);
    pos += h.offsetsBytes;
    RandomAccessInput ordToValue =
        h.ordToValueBytes == 0 ? null : in.randomAccessSlice(pos, h.ordToValueBytes);
    pos += h.ordToValueBytes;
    RandomAccessInput blob = in.randomAccessSlice(pos, h.blobLength);
    pos += h.blobLength;
    in.seek(pos);
    return new BytesRefStore(
        h.count,
        h.numValues,
        offsets,
        h.offsetBits,
        ordToValue,
        h.valueBits,
        blob,
        h.blobLength,
        BASE_RAM_BYTES_USED);
  }

  /** Loads a store from a {@link DataInput}, copying its sections to the heap. */
  public static BytesRefStore loadOnHeap(DataInput in) throws IOException {
    Header h = Header.read(in);
    byte[] offsets = readArray(in, h.offsetsBytes);
    byte[] ordToValue = h.ordToValueBytes == 0 ? null : readArray(in, h.ordToValueBytes);
    byte[] blob = readArray(in, h.blobLength);
    long ram =
        BASE_RAM_BYTES_USED
            + RamUsageEstimator.sizeOf(offsets)
            + (ordToValue == null ? 0 : RamUsageEstimator.sizeOf(ordToValue))
            + RamUsageEstimator.sizeOf(blob);
    return new BytesRefStore(
        h.count,
        h.numValues,
        wrap(offsets),
        h.offsetBits,
        ordToValue == null ? null : wrap(ordToValue),
        h.valueBits,
        wrap(blob),
        h.blobLength,
        ram);
  }

  private static byte[] readArray(DataInput in, long length) throws IOException {
    byte[] bytes = new byte[Math.toIntExact(length)];
    in.readBytes(bytes, 0, bytes.length);
    return bytes;
  }

  private static RandomAccessInput wrap(byte[] bytes) {
    return new ByteBuffersDataInput(List.of(ByteBuffer.wrap(bytes)));
  }

  private record Header(
      long count,
      long numValues,
      int offsetBits,
      int valueBits,
      long offsetsBytes,
      long ordToValueBytes,
      long blobLength) {
    static Header read(DataInput in) throws IOException {
      CodecUtil.checkHeader(in, CODEC_NAME, VERSION_CURRENT, VERSION_CURRENT);
      long count = in.readVLong();
      long numValues = in.readVLong();
      int offsetBits = in.readByte();
      int valueBits = in.readByte();
      long offsetsBytes = in.readVLong();
      long ordToValueBytes = in.readVLong();
      long blobLength = in.readVLong();
      return new Header(
          count, numValues, offsetBits, valueBits, offsetsBytes, ordToValueBytes, blobLength);
    }

    void write(DataOutput out) throws IOException {
      CodecUtil.writeHeader(out, CODEC_NAME, VERSION_CURRENT);
      out.writeVLong(count);
      out.writeVLong(numValues);
      out.writeByte((byte) offsetBits);
      out.writeByte((byte) valueBits);
      out.writeVLong(offsetsBytes);
      out.writeVLong(ordToValueBytes);
      out.writeVLong(blobLength);
    }
  }

  // ---- building

  /**
   * Collects values in ordinal order. Values are hashed as they arrive so that repeated values can
   * be stored once; the decision to deduplicate is made at {@link #save} time by comparing the two
   * resulting sizes. Individual values are limited to {@link org.apache.lucene.util.ByteBlockPool}
   * block size (about 32 KB) by the underlying {@link BytesRefHash}.
   */
  public static final class Builder {
    private final BytesRefHash values = new BytesRefHash();
    private final IntArrayList ordToValueId = new IntArrayList();
    private long totalLength;
    private long distinctLength;

    /** Appends the next value; returns its ordinal. */
    public long add(BytesRef value) {
      int id = values.add(value);
      if (id < 0) {
        id = -id - 1;
      } else {
        distinctLength += value.length;
      }
      totalLength += value.length;
      ordToValueId.add(id);
      return ordToValueId.size() - 1;
    }

    public long size() {
      return ordToValueId.size();
    }

    /** Serializes the store to {@code out}. */
    public void save(DataOutput out) throws IOException {
      final long count = ordToValueId.size();
      final long numDistinct = values.size();
      final BytesRef scratch = new BytesRef();

      // Estimate both layouts and pick the smaller one.
      final int offsetBitsPlain = DirectWriter.unsignedBitsRequired(totalLength);
      final long plainBytes = totalLength + ((count + 1) * offsetBitsPlain + 7) / 8;
      final int offsetBitsDedup = DirectWriter.unsignedBitsRequired(distinctLength);
      final int valueBits = DirectWriter.unsignedBitsRequired(Math.max(0, numDistinct - 1));
      final long dedupBytes =
          distinctLength
              + ((numDistinct + 1) * offsetBitsDedup + 7) / 8
              + (count * valueBits + 7) / 8;
      final boolean dedup = numDistinct < count && dedupBytes < plainBytes;

      final long numValues = dedup ? numDistinct : count;
      final long blobLength = dedup ? distinctLength : totalLength;
      final int offsetBits = dedup ? offsetBitsDedup : offsetBitsPlain;

      // offsets (numValues + 1 entries) and the blob
      ByteBuffersDataOutput offsetsOut = new ByteBuffersDataOutput();
      ByteBuffersDataOutput blobOut = new ByteBuffersDataOutput();
      DirectWriter offsets = DirectWriter.getInstance(offsetsOut, numValues + 1, offsetBits);
      long pos = 0;
      for (long i = 0; i < numValues; i++) {
        int id = dedup ? (int) i : ordToValueId.get((int) i);
        values.get(id, scratch);
        offsets.add(pos);
        blobOut.writeBytes(scratch.bytes, scratch.offset, scratch.length);
        pos += scratch.length;
      }
      offsets.add(pos);
      offsets.finish();
      assert pos == blobLength;

      // ordinal -> distinct value id
      ByteBuffersDataOutput ordToValueOut = new ByteBuffersDataOutput();
      if (dedup) {
        DirectWriter w = DirectWriter.getInstance(ordToValueOut, count, valueBits);
        for (int i = 0; i < count; i++) {
          w.add(ordToValueId.get(i));
        }
        w.finish();
      }

      new Header(
              count,
              numValues,
              offsetBits,
              dedup ? valueBits : 0,
              offsetsOut.size(),
              ordToValueOut.size(),
              blobLength)
          .write(out);
      offsetsOut.copyTo(out);
      ordToValueOut.copyTo(out);
      blobOut.copyTo(out);
    }

    /** Builds an on-heap store. */
    public BytesRefStore build() throws IOException {
      ByteBuffersDataOutput out = new ByteBuffersDataOutput();
      save(out);
      return loadOnHeap(out.toDataInput());
    }
  }
}
