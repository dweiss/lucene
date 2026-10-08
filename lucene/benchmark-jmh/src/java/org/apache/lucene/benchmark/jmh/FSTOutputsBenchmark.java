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
package org.apache.lucene.benchmark.jmh;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.IntsRefBuilder;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Compares FST traversal with eager, boxed outputs ({@code org.apache.lucene.util.fst}, {@code
 * impl=old}) against lazy, position-based outputs ({@code org.apache.lucene.util.fst2}, {@code
 * impl=new}). Run with {@code -prof gc}: the decisive numbers are {@code gc.alloc.rate.norm} and
 * {@code ns/op}.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 2)
@Fork(1)
public class FSTOutputsBenchmark {

  @Param({"old", "new"})
  public String impl;

  @Param({"none", "long", "bytes", "pair"})
  public String outputs;

  @Param({"onheap", "offheap"})
  public String store;

  @Param({"100000", "1000000"})
  public int size;

  enum Kind {
    NONE,
    LONG,
    BYTES,
    PAIR
  }

  FSTBenchmarkData data;
  boolean isOld;
  Kind kind;

  org.apache.lucene.util.fst.FST<Object> oldFst;
  org.apache.lucene.util.fst2.FST<Object> newFst;

  private Path tempDir;
  private Directory dir;
  private IndexInput oldIn;
  private IndexInput newIn;

  private int cursor;
  private final long[] scratch = new long[64];

  // Reused by lookupHitWalk so that only the output handling differs between the two impls.
  private org.apache.lucene.util.fst.FST.BytesReader oldReader;
  private final org.apache.lucene.util.fst.FST.Arc<Object> oldArc =
      new org.apache.lucene.util.fst.FST.Arc<>();
  private org.apache.lucene.util.fst2.FST.BytesReader newReader;
  private final org.apache.lucene.util.fst2.FST.Arc newArc =
      new org.apache.lucene.util.fst2.FST.Arc();

  @Setup(Level.Trial)
  public void setup() throws IOException {
    data = FSTBenchmarkData.generate(size, 0xF57L);
    isOld = impl.equals("old");
    kind = Kind.valueOf(outputs.toUpperCase(java.util.Locale.ROOT));

    if (store.equals("offheap")) {
      tempDir = Files.createTempDirectory("fst2bench");
      dir = new MMapDirectory(tempDir);
    }

    if (isOld) {
      org.apache.lucene.util.fst.FST<Object> fst = buildOld(kind, data);
      if (dir != null) {
        try (IndexOutput out = dir.createOutput("old", IOContext.DEFAULT)) {
          fst.save(out, out);
        }
        oldIn = dir.openInput("old", IOContext.DEFAULT);
        org.apache.lucene.util.fst.FST.FSTMetadata<Object> meta =
            org.apache.lucene.util.fst.FST.readMetadata(oldIn, oldOutputs(kind));
        fst =
            org.apache.lucene.util.fst.FST.fromFSTReader(
                meta,
                new org.apache.lucene.util.fst.OffHeapFSTStore(
                    oldIn, oldIn.getFilePointer(), meta));
      }
      oldFst = fst;
      oldReader = fst.getBytesReader();
    } else {
      org.apache.lucene.util.fst2.FST<Object> fst = buildNew(kind, data);
      if (dir != null) {
        try (IndexOutput out = dir.createOutput("new", IOContext.DEFAULT)) {
          fst.save(out, out);
        }
        newIn = dir.openInput("new", IOContext.DEFAULT);
        org.apache.lucene.util.fst2.FST.FSTMetadata<Object> meta =
            org.apache.lucene.util.fst2.FST.readMetadata(newIn, newOutputs(kind));
        fst =
            org.apache.lucene.util.fst2.FST.fromFSTReader(
                meta,
                new org.apache.lucene.util.fst2.OffHeapFSTStore(
                    newIn, newIn.getFilePointer(), meta));
      }
      newFst = fst;
      newReader = fst.getBytesReader();
    }
  }

  @TearDown(Level.Trial)
  public void tearDown() throws IOException {
    IOUtils.close(oldIn, newIn, dir);
    if (tempDir != null) {
      IOUtils.rm(tempDir);
    }
  }

  private int next() {
    return cursor = (cursor + 1) & FSTBenchmarkData.KEYS_MASK;
  }

  // ---------------------------------------------------------------------------------------------
  // Outputs factories (both packages) and FST construction.

  @SuppressWarnings("unchecked")
  static <X> X cast(Object o) {
    return (X) o;
  }

  static org.apache.lucene.util.fst.Outputs<Object> oldOutputs(Kind kind) {
    return switch (kind) {
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

  static org.apache.lucene.util.fst2.Outputs<Object> newOutputs(Kind kind) {
    return switch (kind) {
      case NONE -> cast(org.apache.lucene.util.fst2.NoOutputs.getSingleton());
      case LONG -> cast(org.apache.lucene.util.fst2.PositiveIntOutputs.getSingleton());
      case BYTES -> cast(org.apache.lucene.util.fst2.ByteSequenceOutputs.getSingleton());
      case PAIR ->
          cast(
              new org.apache.lucene.util.fst2.PairOutputs<>(
                  org.apache.lucene.util.fst2.PositiveIntOutputs.getSingleton(),
                  org.apache.lucene.util.fst2.ByteSequenceOutputs.getSingleton()));
    };
  }

  static org.apache.lucene.util.fst.FST<Object> buildOld(Kind kind, FSTBenchmarkData data)
      throws IOException {
    org.apache.lucene.util.fst.Outputs<Object> outputs = oldOutputs(kind);
    org.apache.lucene.util.fst.FSTCompiler<Object> compiler =
        new org.apache.lucene.util.fst.FSTCompiler.Builder<>(
                org.apache.lucene.util.fst.FST.INPUT_TYPE.BYTE1, outputs)
            .build();
    IntsRefBuilder scratch = new IntsRefBuilder();
    for (int i = 0; i < data.terms.length; i++) {
      Object value =
          switch (kind) {
            case NONE -> outputs.getNoOutput();
            case LONG -> data.longOutputs[i];
            case BYTES -> data.bytesOutputs[i];
            case PAIR -> {
              org.apache.lucene.util.fst.PairOutputs<Long, BytesRef> po = cast(outputs);
              yield po.newPair(data.longOutputs[i], data.bytesOutputs[i]);
            }
          };
      compiler.add(org.apache.lucene.util.fst.Util.toIntsRef(data.terms[i], scratch), value);
    }
    return org.apache.lucene.util.fst.FST.fromFSTReader(
        compiler.compile(), compiler.getFSTReader());
  }

  static org.apache.lucene.util.fst2.FST<Object> buildNew(Kind kind, FSTBenchmarkData data)
      throws IOException {
    org.apache.lucene.util.fst2.Outputs<Object> outputs = newOutputs(kind);
    org.apache.lucene.util.fst2.FSTCompiler<Object> compiler =
        new org.apache.lucene.util.fst2.FSTCompiler.Builder<>(
                org.apache.lucene.util.fst2.FST.INPUT_TYPE.BYTE1, outputs)
            .build();
    IntsRefBuilder scratch = new IntsRefBuilder();
    for (int i = 0; i < data.terms.length; i++) {
      Object value =
          switch (kind) {
            case NONE -> outputs.getNoOutput();
            case LONG -> data.longOutputs[i];
            case BYTES -> data.bytesOutputs[i];
            case PAIR -> {
              org.apache.lucene.util.fst2.PairOutputs<Long, BytesRef> po = cast(outputs);
              yield po.newPair(data.longOutputs[i], data.bytesOutputs[i]);
            }
          };
      compiler.add(org.apache.lucene.util.fst2.Util.toIntsRef(data.terms[i], scratch), value);
    }
    return org.apache.lucene.util.fst2.FST.fromFSTReader(
        compiler.compile(), compiler.getFSTReader());
  }

  // ---------------------------------------------------------------------------------------------
  // Benchmarks.

  private void lookup(BytesRef key, Blackhole bh) throws IOException {
    if (isOld) {
      Object v = org.apache.lucene.util.fst.Util.get(oldFst, key);
      if (kind == Kind.NONE) {
        bh.consume(v != null);
      } else {
        bh.consume(v);
      }
    } else {
      switch (kind) {
        case NONE -> bh.consume(org.apache.lucene.util.fst2.Util.contains(newFst, key));
        case LONG -> bh.consume(org.apache.lucene.util.fst2.Util.getLong(newFst, key, -1));
        case BYTES, PAIR -> bh.consume(org.apache.lucene.util.fst2.Util.get(newFst, key, scratch));
      }
    }
  }

  /** Exact lookup of a present key, decoding its output. */
  @Benchmark
  public void lookupHit(Blackhole bh) throws IOException {
    lookup(data.hits[next()], bh);
  }

  /** Exact lookup of an absent key. */
  @Benchmark
  public void lookupMiss(Blackhole bh) throws IOException {
    lookup(data.misses[next()], bh);
  }

  /**
   * Exact lookup of a present key with a manual arc walk that reuses the Arc and the BytesReader
   * (unlike {@code Util.get}, which allocates both per call). This isolates the cost of handling
   * outputs: eager decode + boxed add (old) versus position bookkeeping + primitive add or a single
   * fold at the end (new).
   */
  @Benchmark
  public void lookupHitWalk(Blackhole bh) throws IOException {
    BytesRef key = data.hits[next()];
    if (isOld) {
      org.apache.lucene.util.fst.FST<Object> fst = oldFst;
      org.apache.lucene.util.fst.FST.BytesReader in = oldReader;
      org.apache.lucene.util.fst.FST.Arc<Object> arc = fst.getFirstArc(oldArc);
      Object acc = fst.outputs.getNoOutput();
      for (int i = 0; i < key.length; i++) {
        if (fst.findTargetArc(key.bytes[key.offset + i] & 0xFF, arc, arc, in) == null) {
          bh.consume(false);
          return;
        }
        acc = fst.outputs.add(acc, arc.output());
      }
      if (arc.isFinal()) {
        acc = fst.outputs.add(acc, arc.nextFinalOutput());
      }
      bh.consume(acc);
    } else {
      org.apache.lucene.util.fst2.FST<Object> fst = newFst;
      org.apache.lucene.util.fst2.FST.BytesReader in = newReader;
      org.apache.lucene.util.fst2.FST.Arc arc = fst.getFirstArc(newArc);
      switch (kind) {
        case NONE -> {
          for (int i = 0; i < key.length; i++) {
            if (fst.findTargetArc(key.bytes[key.offset + i] & 0xFF, arc, arc, in) == null) {
              bh.consume(false);
              return;
            }
          }
          bh.consume(arc.isFinal());
        }
        case LONG -> {
          long acc = 0;
          for (int i = 0; i < key.length; i++) {
            if (fst.findTargetArc(key.bytes[key.offset + i] & 0xFF, arc, arc, in) == null) {
              bh.consume(false);
              return;
            }
            acc += fst.outputLong(arc, in);
          }
          if (arc.isFinal()) {
            acc += fst.nextFinalOutputLong(arc, in);
          }
          bh.consume(acc);
        }
        case BYTES, PAIR -> {
          int n = 0;
          for (int i = 0; i < key.length; i++) {
            if (fst.findTargetArc(key.bytes[key.offset + i] & 0xFF, arc, arc, in) == null) {
              bh.consume(false);
              return;
            }
            scratch[n++] = arc.outputPos();
          }
          if (arc.isFinal()) {
            scratch[n++] = arc.nextFinalOutputPos();
          }
          bh.consume(fst.fold(scratch, n, in));
        }
      }
    }
  }

  /** Membership only, for every outputs type: isolates skipping versus decoding. */
  @Benchmark
  public boolean contains() throws IOException {
    BytesRef key = data.hits[next()];
    if (isOld) {
      return org.apache.lucene.util.fst.Util.get(oldFst, key) != null;
    } else {
      return org.apache.lucene.util.fst2.Util.contains(newFst, key);
    }
  }

  /** Full enumeration of the FST, consuming every output. Reported per FST, not per term. */
  @Benchmark
  public void enumerateAll(Blackhole bh) throws IOException {
    if (isOld) {
      org.apache.lucene.util.fst.BytesRefFSTEnum<Object> e =
          new org.apache.lucene.util.fst.BytesRefFSTEnum<>(oldFst);
      org.apache.lucene.util.fst.BytesRefFSTEnum.InputOutput<Object> io;
      while ((io = e.next()) != null) {
        if (kind != Kind.NONE) {
          bh.consume(io.output);
        }
      }
    } else {
      org.apache.lucene.util.fst2.BytesRefFSTEnum<Object> e =
          new org.apache.lucene.util.fst2.BytesRefFSTEnum<>(newFst);
      while (e.next() != null) {
        switch (kind) {
          case NONE -> {}
          case LONG -> bh.consume(e.currentOutputLong());
          case BYTES, PAIR -> bh.consume(e.currentOutput());
        }
      }
    }
  }

  /** Builds the FST from the term list; construction is unchanged by design. */
  @Benchmark
  @Measurement(iterations = 5, time = 5)
  public long construct() throws IOException {
    if (isOld) {
      return buildOld(kind, data).numBytes();
    } else {
      return buildNew(kind, data).numBytes();
    }
  }
}
