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
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IOUtils;
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

/**
 * Top-N (shortest paths) completion of a two-letter prefix on a {@code PositiveIntOutputs} FST:
 * eager {@code Util.shortestPaths} ({@code impl=old}) versus primitive {@code
 * Util.shortestPathsLong} ({@code impl=new}) versus holder-based {@code Util.shortestPaths} with
 * {@code PositiveIntOutputs.HOLDER_COMPARATOR} ({@code impl=holder}). Long outputs only, hence a
 * separate class from {@link FSTOutputsBenchmark}.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 2)
@Fork(1)
public class FSTOutputsTopNBenchmark {

  private static final int TOP_N = 10;

  @Param({"old", "new", "holder", "hybrid"})
  public String impl;

  @Param({"onheap", "offheap"})
  public String store;

  @Param({"100000", "1000000"})
  public int size;

  FSTBenchmarkData data;
  FSTOutputsBenchmark.Impl which;

  org.apache.lucene.util.fst.FST<Long> oldFst;
  org.apache.lucene.util.fst.FST.BytesReader oldReader;
  org.apache.lucene.util.fst.FST.Arc<Long> oldArc = new org.apache.lucene.util.fst.FST.Arc<>();

  org.apache.lucene.util.fst2.FST<Long> newFst;
  org.apache.lucene.util.fst2.FST.BytesReader newReader;
  org.apache.lucene.util.fst2.FST.Arc newArc = new org.apache.lucene.util.fst2.FST.Arc();

  org.apache.lucene.util.fst3.FST<Long> holderFst;
  org.apache.lucene.util.fst3.FST.BytesReader holderReader;
  org.apache.lucene.util.fst3.FST.Arc<Long> holderArc = new org.apache.lucene.util.fst3.FST.Arc<>();
  org.apache.lucene.util.fst3.PositiveIntOutputs.LongHolder holderPrefix =
      org.apache.lucene.util.fst3.PositiveIntOutputs.getSingleton().newHolder();

  org.apache.lucene.util.fst4.FST<Long> hybridFst;
  org.apache.lucene.util.fst4.FST.BytesReader hybridReader;
  org.apache.lucene.util.fst4.FST.Arc hybridArc = new org.apache.lucene.util.fst4.FST.Arc();
  org.apache.lucene.util.fst4.PositiveIntOutputs.LongHolder hybridPrefix =
      org.apache.lucene.util.fst4.PositiveIntOutputs.getSingleton().newHolder();

  private Path tempDir;
  private Directory dir;
  private IndexInput in;
  private int cursor;

  @Setup(Level.Trial)
  public void setup() throws IOException {
    data = FSTBenchmarkData.generate(size, 0xF57L);
    which = FSTOutputsBenchmark.Impl.valueOf(impl.toUpperCase(java.util.Locale.ROOT));
    if (store.equals("offheap")) {
      tempDir = Files.createTempDirectory("fst2bench");
      dir = new MMapDirectory(tempDir);
    }
    if (which == FSTOutputsBenchmark.Impl.OLD) {
      org.apache.lucene.util.fst.FST<Long> fst =
          FSTOutputsBenchmark.cast(
              FSTOutputsBenchmark.buildOld(FSTOutputsBenchmark.Kind.LONG, data));
      if (dir != null) {
        try (IndexOutput out = dir.createOutput("fst", IOContext.DEFAULT)) {
          fst.save(out, out);
        }
        in = dir.openInput("fst", IOContext.DEFAULT);
        org.apache.lucene.util.fst.FST.FSTMetadata<Long> meta =
            org.apache.lucene.util.fst.FST.readMetadata(
                in, org.apache.lucene.util.fst.PositiveIntOutputs.getSingleton());
        fst =
            org.apache.lucene.util.fst.FST.fromFSTReader(
                meta,
                new org.apache.lucene.util.fst.OffHeapFSTStore(in, in.getFilePointer(), meta));
      }
      oldFst = fst;
      oldReader = fst.getBytesReader();
    } else if (which == FSTOutputsBenchmark.Impl.NEW) {
      org.apache.lucene.util.fst2.FST<Long> fst =
          FSTOutputsBenchmark.cast(
              FSTOutputsBenchmark.buildNew(FSTOutputsBenchmark.Kind.LONG, data));
      if (dir != null) {
        try (IndexOutput out = dir.createOutput("fst", IOContext.DEFAULT)) {
          fst.save(out, out);
        }
        in = dir.openInput("fst", IOContext.DEFAULT);
        org.apache.lucene.util.fst2.FST.FSTMetadata<Long> meta =
            org.apache.lucene.util.fst2.FST.readMetadata(
                in, org.apache.lucene.util.fst2.PositiveIntOutputs.getSingleton());
        fst =
            org.apache.lucene.util.fst2.FST.fromFSTReader(
                meta,
                new org.apache.lucene.util.fst2.OffHeapFSTStore(in, in.getFilePointer(), meta));
      }
      newFst = fst;
      newReader = fst.getBytesReader();
    } else if (which == FSTOutputsBenchmark.Impl.HOLDER) {
      org.apache.lucene.util.fst3.FST<Long> fst =
          FSTOutputsBenchmark.cast(
              FSTOutputsBenchmark.buildHolder(FSTOutputsBenchmark.Kind.LONG, data));
      if (dir != null) {
        try (IndexOutput out = dir.createOutput("fst", IOContext.DEFAULT)) {
          fst.save(out, out);
        }
        in = dir.openInput("fst", IOContext.DEFAULT);
        org.apache.lucene.util.fst3.FST.FSTMetadata<Long> meta =
            org.apache.lucene.util.fst3.FST.readMetadata(
                in, org.apache.lucene.util.fst3.PositiveIntOutputs.getSingleton());
        fst =
            org.apache.lucene.util.fst3.FST.fromFSTReader(
                meta,
                new org.apache.lucene.util.fst3.OffHeapFSTStore(in, in.getFilePointer(), meta));
      }
      holderFst = fst;
      holderReader = fst.getBytesReader();
    } else {
      org.apache.lucene.util.fst4.FST<Long> fst =
          FSTOutputsBenchmark.cast(
              FSTOutputsBenchmark.buildHybrid(FSTOutputsBenchmark.Kind.LONG, data));
      if (dir != null) {
        try (IndexOutput out = dir.createOutput("fst", IOContext.DEFAULT)) {
          fst.save(out, out);
        }
        in = dir.openInput("fst", IOContext.DEFAULT);
        org.apache.lucene.util.fst4.FST.FSTMetadata<Long> meta =
            org.apache.lucene.util.fst4.FST.readMetadata(
                in, org.apache.lucene.util.fst4.PositiveIntOutputs.getSingleton());
        fst =
            org.apache.lucene.util.fst4.FST.fromFSTReader(
                meta,
                new org.apache.lucene.util.fst4.OffHeapFSTStore(in, in.getFilePointer(), meta));
      }
      hybridFst = fst;
      hybridReader = fst.getBytesReader();
    }
  }

  @TearDown(Level.Trial)
  public void tearDown() throws IOException {
    IOUtils.close(in, dir);
    if (tempDir != null) {
      IOUtils.rm(tempDir);
    }
  }

  /** Walks a two-letter prefix, then finds the TOP_N completions with the smallest output. */
  @Benchmark
  public Object topN() throws IOException {
    BytesRef prefix = data.prefixes[cursor = (cursor + 1) & (data.prefixes.length - 1)];
    if (which == FSTOutputsBenchmark.Impl.HYBRID) {
      org.apache.lucene.util.fst4.FST.Arc arc = hybridFst.getFirstArc(hybridArc);
      hybridPrefix.setNoOutput();
      for (int i = 0; i < prefix.length; i++) {
        arc =
            hybridFst.findTargetArc(prefix.bytes[prefix.offset + i] & 0xFF, arc, arc, hybridReader);
        if (arc == null) {
          return null;
        }
        hybridFst.readAddInto(arc.outputPos(), hybridReader, hybridPrefix);
      }
      return org.apache.lucene.util.fst4.Util.shortestPaths(
          hybridFst,
          arc,
          hybridPrefix,
          org.apache.lucene.util.fst4.PositiveIntOutputs.HOLDER_COMPARATOR,
          TOP_N,
          true);
    }
    if (which == FSTOutputsBenchmark.Impl.HOLDER) {
      org.apache.lucene.util.fst3.FST.Arc<Long> arc = holderFst.getFirstArc(holderArc);
      holderPrefix.value = 0;
      for (int i = 0; i < prefix.length; i++) {
        arc =
            holderFst.findTargetArc(prefix.bytes[prefix.offset + i] & 0xFF, arc, arc, holderReader);
        if (arc == null) {
          return null;
        }
        holderPrefix.value +=
            ((org.apache.lucene.util.fst3.PositiveIntOutputs.LongHolder) arc.output()).value;
      }
      return org.apache.lucene.util.fst3.Util.shortestPaths(
          holderFst,
          arc,
          holderPrefix,
          org.apache.lucene.util.fst3.PositiveIntOutputs.HOLDER_COMPARATOR,
          TOP_N,
          true);
    }
    if (which == FSTOutputsBenchmark.Impl.OLD) {
      org.apache.lucene.util.fst.FST.Arc<Long> arc = oldFst.getFirstArc(oldArc);
      Long prefixOutput = oldFst.outputs.getNoOutput();
      for (int i = 0; i < prefix.length; i++) {
        arc = oldFst.findTargetArc(prefix.bytes[prefix.offset + i] & 0xFF, arc, arc, oldReader);
        if (arc == null) {
          return null;
        }
        prefixOutput = oldFst.outputs.add(prefixOutput, arc.output());
      }
      return org.apache.lucene.util.fst.Util.shortestPaths(
          oldFst, arc, prefixOutput, Comparator.naturalOrder(), TOP_N, true);
    } else {
      org.apache.lucene.util.fst2.FST.Arc arc = newFst.getFirstArc(newArc);
      long prefixOutput = 0;
      for (int i = 0; i < prefix.length; i++) {
        arc = newFst.findTargetArc(prefix.bytes[prefix.offset + i] & 0xFF, arc, arc, newReader);
        if (arc == null) {
          return null;
        }
        prefixOutput += newFst.outputLong(arc, newReader);
      }
      return org.apache.lucene.util.fst2.Util.shortestPathsLong(
          newFst, arc, prefixOutput, TOP_N, true);
    }
  }
}
