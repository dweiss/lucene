<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

# fst2: lazy (position-based) FST outputs — results

Branch `fst2-lazy-outputs` (off `main` at ccd61ff0356). Commits, in order:

1. `fst2: verbatim copy of fst`
2. `fst2: lazy outputs in FST, enums, Util`
3. `fst2: correctness tests (ported FSTTester/TestFSTs, direct addressing, suffix cache, old/new compat)`
4. `fst2: side-by-side showcase tests (old vs new API)`
5. `fst2: JMH benchmarks comparing eager (fst) and lazy (fst2) outputs`
6. `fst2: results report (FST2-RESULTS.md)`, `fst2: benchmark: explicit enum cases for ecj lint`
7. `fst3: verbatim copy of fst`
8. `fst3: holder-based outputs (mutable reusable OutputHolder in Arc, Outputs.readInto/addInto)`
9. `fst3: correctness tests ...`, `fst3: side-by-side showcase tests (old vs holder API)`
10. `fst3: JMH impl=holder variant; test lint fix`

Sections 1 to 5 cover the lazy, position-based design (`fst2`). Section 6 covers the alternative
"holder" design (`fst3`) that was implemented afterwards, with a three-way comparison.

Everything below refers to `org.apache.lucene.util.fst` as **old** and `org.apache.lucene.util.fst2`
as **new**. The old package is untouched. All 43 `fst2` tests pass (also with
`-Ptests.multiplier=3` and `-Ptests.iters=3`); `TestFST2Compat` verifies that FSTs saved by
either compiler load in the other (on heap and through `OffHeapFSTStore`) with identical lookups and
enumerations, and that both compilers emit byte-identical files.

## 1. Benchmarks

`lucene/benchmark-jmh`: `FSTOutputsBenchmark` (lookups, enumeration, construction) and
`FSTOutputsTopNBenchmark` (shortest paths, long outputs only). Run exactly as:

```text
./gradlew :lucene:benchmark-jmh:assemble
java -jar lucene/benchmark-jmh/build/benchmarks/lucene-benchmark-jmh-11.0.0-SNAPSHOT.jar \
     'FSTOutputs.*' -prof gc -p size=1000000 -p store=onheap  -rf json -rff fst2-onheap.json
java -jar lucene/benchmark-jmh/build/benchmarks/lucene-benchmark-jmh-11.0.0-SNAPSHOT.jar \
     'FSTOutputs.*' -prof gc -p size=1000000 -p store=offheap -rf json -rff fst2-offheap.json
```

JMH 1.37, `openjdk version "25.0.3" 2026-04-21 LTS`, AMD Ryzen Threadripper 3970X 32-Core Processor, 64 cores, 1 fork, 5 x 1 s warmup, 5 x 2 s measurement
(`construct`: 5 x 5 s). 1,000,000 random lowercase terms of length 5 to 15; long outputs uniform
in [1, 2^40]; byte outputs 4 to 12 random bytes; pair = both. 16k hit keys and 16k miss keys cycled
per op. The tables are generated from `fst2-onheap.json` / `fst2-offheap.json` (left untracked
next to this file); "±" is the JMH 99.9% confidence half-width. Columns: mean time per op and
`gc.alloc.rate.norm` (bytes allocated per op). `enumerateAll` is per full pass over the FST, not
per term.

What each row does:

- `lookupHit` / `lookupMiss`: `Util.get` (old) versus `Util.contains` (none), `Util.getLong`
  (long) or `Util.get(fst, key, long[] scratch)` (bytes, pair) (new). Both allocate an `Arc` and a
  `BytesReader` per call (pre-existing TODO in `Util`), 112-120 B on heap and 440-448 B off heap
  (the `randomAccessSlice`). That fixed cost is the floor in those rows.
- `lookupHitWalk`: manual `findTargetArc` walk with a reused `Arc` and reader; old decodes and adds
  per arc, new adds primitives (long) or folds positions once at the end (bytes, pair). This is the
  row that isolates the output handling.
- `contains`: membership on hit keys for every outputs type; old must decode, new only skips.
- `enumerateAll`: `BytesRefFSTEnum` over the whole FST consuming `io.output` (old) versus
  `currentOutputLong()` / `currentOutput()` (new).
- `topN`: two-letter prefix walk, then `Util.shortestPaths(..., naturalOrder(), 10, true)` versus
  `Util.shortestPathsLong(..., 10, true)`.
- `construct`: build the FST from the term list; should be identical within noise.

### store=onheap, size=1,000,000 terms

| benchmark | outputs | old ns/op | new ns/op | new/old | old B/op | new B/op |
|---|---|---:|---:|---:|---:|---:|
| lookupHit | none | 427.5 ns ± 87.5 ns | 393.6 ns ± 28.2 ns | 0.92 | 112.0 B | 120.0 B |
| lookupHit | long | 571.3 ns ± 111.3 ns | 595.8 ns ± 59.1 ns | 1.04 | 302.9 B | 120.0 B |
| lookupHit | bytes | 577.3 ns ± 91.4 ns | 618.8 ns ± 76.7 ns | 1.07 | 164.1 B | 171.7 B |
| lookupHit | pair | 927.8 ns ± 126.3 ns | 936.6 ns ± 173.8 ns | 1.01 | 734.4 B | 592.5 B |
| lookupMiss | none | 222.2 ns ± 33.2 ns | 217.2 ns ± 10.6 ns | 0.98 | 112.0 B | 120.0 B |
| lookupMiss | long | 285.7 ns ± 53.6 ns | 334.7 ns ± 50.5 ns | 1.17 | 271.6 B | 120.0 B |
| lookupMiss | bytes | 275.7 ns ± 51.1 ns | 250.3 ns ± 51.5 ns | 0.91 | 128.3 B | 120.0 B |
| lookupMiss | pair | 425.1 ns ± 100.6 ns | 315.3 ns ± 71.1 ns | 0.74 | 475.9 B | 120.0 B |
| lookupHitWalk | none | 433.9 ns ± 124.9 ns | 411.9 ns ± 74.2 ns | 0.95 | 0.0 B | 0.0 B |
| lookupHitWalk | long | 607.0 ns ± 122.6 ns | 651.5 ns ± 68.3 ns | 1.07 | 190.9 B | 0.0 B |
| lookupHitWalk | bytes | 529.9 ns ± 104.3 ns | 586.0 ns ± 117.3 ns | 1.11 | 52.1 B | 51.7 B |
| lookupHitWalk | pair | 1.02 µs ± 201.8 ns | 913.4 ns ± 141.8 ns | 0.89 | 622.4 B | 472.5 B |
| contains | none | 422.1 ns ± 107.7 ns | 406.6 ns ± 55.3 ns | 0.96 | 112.0 B | 120.0 B |
| contains | long | 586.0 ns ± 114.9 ns | 558.7 ns ± 126.5 ns | 0.95 | 302.8 B | 120.0 B |
| contains | bytes | 556.4 ns ± 130.5 ns | 513.6 ns ± 117.4 ns | 0.92 | 164.1 B | 120.0 B |
| contains | pair | 912.8 ns ± 94.3 ns | 690.8 ns ± 137.5 ns | 0.76 | 734.4 B | 120.0 B |
| enumerateAll | none | 233.35 ms ± 7.93 ms | 195.46 ms ± 8.31 ms | 0.84 | 3.6 KB | 3.6 KB |
| enumerateAll | long | 262.70 ms ± 42.28 ms | 264.93 ms ± 33.23 ms | 1.01 | 48.00 MB | 4.5 KB |
| enumerateAll | bytes | 270.74 ms ± 45.52 ms | 289.50 ms ± 52.21 ms | 1.07 | 51.65 MB | 51.68 MB |
| enumerateAll | pair | 422.42 ms ± 49.76 ms | 501.75 ms ± 13.79 ms | 1.19 | 308.42 MB | 472.73 MB |
| topN | N/A | 30.10 µs ± 3.06 µs | 25.23 µs ± 3.27 µs | 0.84 | 62.9 KB | 14.0 KB |
| construct | none | 1,812.63 ms ± 290.98 ms | 1,727.40 ms ± 177.11 ms | 0.95 | 367.49 MB | 367.49 MB |
| construct | long | 2,150.43 ms ± 201.06 ms | 2,040.52 ms ± 94.22 ms | 0.95 | 654.16 MB | 637.38 MB |
| construct | bytes | 2,055.32 ms ± 213.03 ms | 2,014.20 ms ± 15.78 ms | 0.98 | 508.03 MB | 491.75 MB |
| construct | pair | 2,982.40 ms ± 190.24 ms | 2,981.92 ms ± 213.87 ms | 1.00 | 1,811.04 MB | 1,784.31 MB |

### store=offheap, size=1,000,000 terms

| benchmark | outputs | old ns/op | new ns/op | new/old | old B/op | new B/op |
|---|---|---:|---:|---:|---:|---:|
| lookupHit | none | 466.3 ns ± 83.6 ns | 399.2 ns ± 58.7 ns | 0.86 | 440.0 B | 448.0 B |
| lookupHit | long | 699.7 ns ± 89.1 ns | 542.0 ns ± 94.4 ns | 0.77 | 630.9 B | 448.0 B |
| lookupHit | bytes | 696.4 ns ± 102.6 ns | 607.0 ns ± 84.6 ns | 0.87 | 492.1 B | 499.7 B |
| lookupHit | pair | 1.11 µs ± 155.2 ns | 1.17 µs ± 133.9 ns | 1.06 | 1.1 KB | 920.5 B |
| lookupMiss | none | 313.2 ns ± 40.1 ns | 316.8 ns ± 48.5 ns | 1.01 | 440.0 B | 448.0 B |
| lookupMiss | long | 452.3 ns ± 38.9 ns | 469.2 ns ± 39.8 ns | 1.04 | 599.7 B | 448.0 B |
| lookupMiss | bytes | 355.4 ns ± 21.7 ns | 337.6 ns ± 68.8 ns | 0.95 | 456.3 B | 448.0 B |
| lookupMiss | pair | 597.7 ns ± 99.8 ns | 477.6 ns ± 44.7 ns | 0.80 | 803.9 B | 448.0 B |
| lookupHitWalk | none | 383.7 ns ± 93.4 ns | 310.2 ns ± 40.1 ns | 0.81 | 0.0 B | 0.0 B |
| lookupHitWalk | long | 624.6 ns ± 135.4 ns | 522.1 ns ± 153.8 ns | 0.84 | 190.9 B | 0.0 B |
| lookupHitWalk | bytes | 563.6 ns ± 113.3 ns | 538.1 ns ± 100.6 ns | 0.95 | 52.1 B | 51.7 B |
| lookupHitWalk | pair | 948.6 ns ± 111.6 ns | 1.06 µs ± 141.7 ns | 1.12 | 622.4 B | 472.5 B |
| contains | none | 475.1 ns ± 72.0 ns | 446.9 ns ± 90.2 ns | 0.94 | 440.0 B | 448.0 B |
| contains | long | 620.6 ns ± 86.8 ns | 644.1 ns ± 89.3 ns | 1.04 | 630.9 B | 448.0 B |
| contains | bytes | 585.4 ns ± 123.9 ns | 544.5 ns ± 112.0 ns | 0.93 | 492.1 B | 448.0 B |
| contains | pair | 1.05 µs ± 172.1 ns | 685.2 ns ± 94.5 ns | 0.65 | 1.1 KB | 448.0 B |
| enumerateAll | none | 245.04 ms ± 34.29 ms | 212.10 ms ± 10.24 ms | 0.87 | 5.2 KB | 5.2 KB |
| enumerateAll | long | 306.67 ms ± 24.08 ms | 286.72 ms ± 29.31 ms | 0.93 | 48.01 MB | 6.3 KB |
| enumerateAll | bytes | 301.05 ms ± 34.35 ms | 288.67 ms ± 18.49 ms | 0.96 | 51.66 MB | 51.69 MB |
| enumerateAll | pair | 461.12 ms ± 61.10 ms | 618.59 ms ± 69.07 ms | 1.34 | 308.43 MB | 472.73 MB |
| topN | N/A | 36.51 µs ± 3.98 µs | 29.17 µs ± 3.58 µs | 0.80 | 63.5 KB | 14.6 KB |
| construct | none | 1,715.07 ms ± 108.48 ms | 1,754.58 ms ± 199.67 ms | 1.02 | 367.50 MB | 367.50 MB |
| construct | long | 2,100.10 ms ± 134.90 ms | 2,104.59 ms ± 166.03 ms | 1.00 | 654.17 MB | 637.38 MB |
| construct | bytes | 2,044.67 ms ± 117.42 ms | 2,017.47 ms ± 127.00 ms | 0.99 | 508.07 MB | 492.04 MB |
| construct | pair | 2,988.04 ms ± 48.13 ms | 2,952.09 ms ± 191.46 ms | 0.99 | 1,811.10 MB | 1,784.65 MB |

### Reading the numbers

- **Allocation is the signal; timings are mostly within noise on this machine** (±10-25%).
  Where the time difference exceeds the error, it favors the new code: `contains`/`lookupMiss` on
  pair outputs (0.65-0.80), `topN` (0.80-0.84), and most off-heap lookups (0.77-0.87).
- **Primitive long path.** `lookupHitWalk/long` goes from 191 B/op to 0 B/op; `enumerateAll/long`
  from 48 MB to 4.5 KB per million terms (the enum's arrays); `topN` from 63 KB to 14 KB per query.
  `lookupHit/long` and `contains/long` drop to the 120 B / 448 B floor set by `Util`'s per-call
  `Arc` and reader allocation.
- **Membership and misses no longer decode anything**: `contains` and `lookupMiss` sit at the floor
  for every outputs type, including `pair`, where old allocated 0.5-1.1 KB per lookup.
- **Boxed byte outputs are a wash**: `ByteSequenceOutputs.add(NO_OUTPUT, x)` returns `x` without
  copying and byte outputs usually sit on a single arc, so folding at the end allocates the same
  51.7 MB per enumeration and ~52 B per lookup as the old cumulative add.
- **Boxed pair outputs regress on enumeration** (308 MB -> 473 MB, 1.19-1.34x slower). The old enum
  keeps a cumulative boxed stack and calls `add` once per *pushed arc*; the new `currentOutput()`
  re-folds the whole path once per *term*, and `PairOutputs.add`/`read` allocate a `Pair` (plus a
  `BytesRef`) for every arc with an output. For deep paths with outputs spread over several arcs
  this is strictly more work. The lookup rows (`lookupHit*/pair`) still improve because `Util.get`
  decodes each arc once either way and the fold skips `NO_OUTPUT_POS` entries. See the API notes
  below for the fix (cache the fold per depth, i.e. a lazily filled cumulative stack).
- **Construction is unchanged**: all `construct` ratios are 0.95-1.02 with overlapping error bars,
  and the byte-identical output is asserted by `TestFST2Compat.testIdenticalBytes`.
- **No deoptimization storm from the `impl` branch**: with `-XX:+PrintCompilation` on
  `lookupHit impl=new,outputs=long`, the only fst2 "made not entrant" events inside the measured
  iterations are normal C1->C2 tier replacements; all uncommon traps occur at the next trial's
  setup (a fresh `FSTCompiler` run exercises `readArc` with the block-pool reader).

## 2. Construction and the suffix cache

Construction is unchanged by design: `FSTCompiler`, its boxed `FSTCompiler.Arc<T>` /
`UnCompiledNode<T>` and `Outputs.common/subtract/add/write` are byte-for-byte the same code as in
`fst`. `TestFST2Compat.testIdenticalBytes` pins this: for the same input the two compilers produce
identical serialized bytes for `NoOutputs`, `PositiveIntOutputs`, `ByteSequenceOutputs` and
`PairOutputs<Long, BytesRef>`. The `construct` rows in the tables above should therefore be read as
a noise estimate, not as a result.

`FSTSuffixNodeCache` reads frozen nodes back through the new `FST.readFirstRealTargetArc` /
`readNextRealArc`, so it now receives positions instead of values. It decodes them on the spot with
`fst.output(scratchArc, in)` / `fst.nextFinalOutput(scratchArc, in)` for hashing and for the
equality check against the unfrozen node. That is exactly the boxing the old code did, just moved
one call deeper; the reader position is saved and restored by `readOutput`, so the arc iteration is
undisturbed. A Design 2 implementation would hash and compare the encoded bytes directly and skip
the decode entirely (TODO left in the class).

One deviation from the plan: `PairOutputs.skipFinalOutput` was *not* changed to call the
components' `skipFinalOutput`. `PairOutputs` inherits `writeFinalOutput -> write` and
`readFinalOutput -> read`, so the final output of a pair is encoded with the components' plain
`write`; the inherited `skipFinalOutput -> skipOutput` is the correct mirror, and the proposed
override would have been wrong for a component that distinguishes final outputs (e.g. the misc
module's `ListOfOutputs`). A comment in `PairOutputs` records this.

## 3. Side-by-side API diff (from `TestFST2Showcase`)

### Exact lookup of a long output (`testTransducerLookup`)

Old (`org.apache.lucene.util.fst`):

```java
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
```

New (`org.apache.lucene.util.fst2`):

```java
    List<Long> sample = new ArrayList<>();
    for (BytesRef key : hits) {
      long v = Util.getLong(newLongFst, key, -1);
      assertTrue(v != -1);
      assertEquals(reference.get(key).longValue(), v);
      sample.add(v);
    }
    for (BytesRef key : misses) {
      long v = Util.getLong(newLongFst, key, -1);
      assertEquals(-1, v);
    }
    printSample("lookup(new)", sample);
```

### WFST-style top-N completion (`testSuggester`)

Old (`org.apache.lucene.util.fst`):

```java
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
```

New (`org.apache.lucene.util.fst2`):

```java
    var in = newWfst.getBytesReader();
    var scratch = new BytesRefBuilder();
    for (String prefix : prefixes) {
      var arc = newWfst.getFirstArc(new FST.Arc());
      long prefixOutput = 0;
      for (int i = 0; i < prefix.length(); i++) {
        arc = newWfst.findTargetArc(prefix.charAt(i), arc, arc, in);
        assertNotNull(arc);
        prefixOutput += newWfst.outputLong(arc, in);
      }
      var results = Util.shortestPathsLong(newWfst, arc, prefixOutput, TOP_N, true);
      List<String> completions = new ArrayList<>();
      for (var result : results) {
        completions.add(prefix + Util.toBytesRef(result.input(), scratch).utf8ToString());
        long weight = Long.MAX_VALUE - result.output();
        assertEquals(reference.get(new BytesRef(completions.getLast())).longValue(), weight);
      }
      assertEquals(prefix, expectedCompletions(prefix), completions);
      printSample("suggest(new) " + prefix, completions);
    }
```

### Manual arc walk summing outputs (`testManualWalk`)

Old (`org.apache.lucene.util.fst`):

```java
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
```

New (`org.apache.lucene.util.fst2`):

```java
    var in = newLongFst.getBytesReader();
    for (BytesRef key : hits) {
      var arc = newLongFst.getFirstArc(new FST.Arc());
      long acc = 0;
      for (int i = 0; i < key.length; i++) {
        arc = newLongFst.findTargetArc(key.bytes[key.offset + i] & 0xFF, arc, arc, in);
        assertNotNull(arc);
        acc += newLongFst.outputLong(arc, in);
      }
      assertTrue(arc.isFinal());
      acc += newLongFst.nextFinalOutputLong(arc, in);
      assertEquals(reference.get(key).longValue(), acc);
    }
```

## 4. API decisions that felt awkward

- **`InputOutput` needs a back-reference to the enum.** `BytesRefFSTEnum.InputOutput<T>` used to
  be a dumb pair; now `io.output()` must delegate to `enum.currentOutput()` because the value is
  only computable from the enum's position stack and reader. It also now throws `IOException`.
  Alternative: drop `InputOutput` and expose `current()` as the `BytesRef` plus `currentOutput()` /
  `currentOutputLong()` on the enum, which is what the showcase tests end up using anyway.
- **`FINAL_ENCODED_FLAG` tagging.** A position alone does not say whether it was written with
  `write` or `writeFinalOutput`, so bit 62 of `nextFinalOutputPos` carries that, and it must travel
  into `outputPos` when the virtual `END_LABEL` arc is synthesized. No in-tree `Outputs` in core
  distinguishes the two (only misc's `ListOfOutputs` does), so the flag is pure insurance.
  Alternative: give `Outputs` a single `read(in, boolean isFinal)` and drop the distinction from
  positions; or store two positions and let `Arc` know which decoder to use via a boolean field.
- **`long[] scratch` in `Util.get`.** The boxed `Util.get` has to collect positions before it can
  fold, so it either allocates a `long[input.length + 1]` per call or takes a caller-supplied
  scratch array (both overloads exist). The primitive `Util.getLong` does not need it because it
  accumulates with `addLong` on the fly. Alternative: an accumulator object (`Outputs.newAccumulator()`
  with `add(pos, in)` / `result()`) would make the boxed path streaming as well and remove the array;
  this is essentially the "accumulator design" the plan mentions.
- **`EMPTY_OUTPUT_POS`.** The empty-string output lives in the metadata, not in the byte array, so
  a second sentinel is needed and every decode path has a three-way branch (`NO_OUTPUT_POS`,
  `EMPTY_OUTPUT_POS`, real position). Alternative: the compiler could append the encoded empty
  output to the byte array (the `TODO` in `FSTMetadata.save` already suggests encoding it as an arc
  into the root), giving it a real position and removing the sentinel; that is a format change.
- **`readOutput` saves and restores the reader position.** Needed because the enum and the suffix
  cache decode mid-iteration on the same reader; it costs two field writes per decode. Alternative:
  a second, dedicated `BytesReader` per enum / cache for decoding only.
- **`Util.get` still allocates.** Independently of outputs, `Util.get` / `getLong` / `contains`
  allocate a fresh `Arc` and `fst.getBytesReader()` per call (the latter is a `randomAccessSlice`
  for the off-heap store). This is the pre-existing `TODO` in `Util` and it dominates the
  per-lookup allocation of both implementations, which is why the plan's "0 B/op" expectation only
  materializes in `lookupHitWalk` (reused `Arc` and reader). Alternative: a reusable
  `Util.Lookup` object, or caller-supplied `Arc` and `BytesReader` overloads.
- **`currentOutput()` re-folds the whole path per term.** This is what makes boxed `PairOutputs`
  enumeration slower and more allocation-heavy than the old cumulative stack (see the benchmark
  notes). Alternative: keep a lazily filled cumulative boxed stack in `FSTEnum` (fold only the
  depths that changed since the last call, invalidating on pop), which restores the old amortized
  one-`add`-per-pushed-arc behavior while still decoding nothing until asked.
- **`LongTopNSearcher` is a copy, not a specialization.** It mirrors `TopNSearcher` with `long`
  fields and a fixed `Long.compare` ordering; no comparator can be plugged in. Alternative: keep one
  searcher parameterized by an accumulator abstraction (see the scratch item above).
- **Reader type coupling.** Positions are only meaningful with the same kind of `BytesReader` that
  produced them (reverse on-heap, reverse off-heap, or the compiler's block-pool reader with its
  `posDelta`). Today this is implicit; nothing stops a caller from decoding a position with a reader
  from a different store.

## 5. Not done, and why

- `ListOfOutputs` (misc module) was not ported or tested; the plan excluded it and it is the only
  in-tree `Outputs` that distinguishes final outputs, so `FINAL_ENCODED_FLAG` is exercised only by
  the generic code path, not by a differing decoder.
- `Test2BFST` / `Test2BFSTOffHeap` were not ported (plan: skip).
- Only the `size=1000000` row of the parameter matrix was run (plan's run matrix); `size=100000`
  is available in the benchmark but was not executed for the tables above.
- Showcase `testPairOutputs` interprets "only decodes the final arc" as collecting positions along
  the walk and folding once at the end, plus a partial decode of just the last arc's final output;
  a pair output cannot be reconstructed from the last arc alone.

## 6. Alternative: the holder design (`fst3`)

`org.apache.lucene.util.fst3` is a second experimental copy of `fst`. It keeps traversal **eager**
(every arc output is decoded when the arc is read) but decodes into mutable, reusable
`OutputHolder`s owned by each `Arc`, so nothing is allocated once the holders exist. It is the
natural competitor to the lazy design: same goal (allocation-free traversal), opposite strategy
(reuse values instead of deferring them). Same tests (43, all green with `-Ptests.multiplier=3`),
same old/new compat test including byte-identical files, same showcase pairs, same benchmarks with
`impl=holder`.

### 6.1 Three-way benchmark tables

Same setup as section 1; the `holder` column was produced by a separate run (`-p impl=holder`) on
the same machine and merged with the `old`/`new` JSON files. Ratios are relative to `old`.
Generated from `fst2-*.json` and `fst3-*.json` (untracked, next to this file).

### store=onheap, size=1,000,000 terms

| benchmark | outputs | old ns/op | lazy (fst2) ns/op | holder (fst3) ns/op | lazy/old | holder/old | old B/op | lazy B/op | holder B/op |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|
| lookupHit | none | 427.5 ns ± 87.5 ns | 393.6 ns ± 28.2 ns | 436.4 ns ± 64.1 ns | 0.92 | 1.02 | 112.0 B | 120.0 B | 144.0 B |
| lookupHit | long | 571.3 ns ± 111.3 ns | 595.8 ns ± 59.1 ns | 559.1 ns ± 106.6 ns | 1.04 | 0.98 | 302.9 B | 120.0 B | 160.0 B |
| lookupHit | bytes | 577.3 ns ± 91.4 ns | 618.8 ns ± 76.7 ns | 653.0 ns ± 115.9 ns | 1.07 | 1.13 | 164.1 B | 171.7 B | 254.4 B |
| lookupHit | pair | 927.8 ns ± 126.3 ns | 936.6 ns ± 173.8 ns | 887.6 ns ± 114.3 ns | 1.01 | 0.96 | 734.4 B | 592.5 B | 350.4 B |
| lookupMiss | none | 222.2 ns ± 33.2 ns | 217.2 ns ± 10.6 ns | 234.4 ns ± 25.8 ns | 0.98 | 1.05 | 112.0 B | 120.0 B | 144.0 B |
| lookupMiss | long | 285.7 ns ± 53.6 ns | 334.7 ns ± 50.5 ns | 277.6 ns ± 16.6 ns | 1.17 | 0.97 | 271.6 B | 120.0 B | 160.0 B |
| lookupMiss | bytes | 275.7 ns ± 51.1 ns | 250.3 ns ± 51.5 ns | 322.9 ns ± 61.1 ns | 0.91 | 1.17 | 128.3 B | 120.0 B | 233.6 B |
| lookupMiss | pair | 425.1 ns ± 100.6 ns | 315.3 ns ± 71.1 ns | 435.5 ns ± 65.0 ns | 0.74 | 1.02 | 475.9 B | 120.0 B | 329.6 B |
| lookupHitWalk | none | 433.9 ns ± 124.9 ns | 411.9 ns ± 74.2 ns | 393.7 ns ± 106.6 ns | 0.95 | 0.91 | 0.0 B | 0.0 B | 0.0 B |
| lookupHitWalk | long | 607.0 ns ± 122.6 ns | 651.5 ns ± 68.3 ns | 538.4 ns ± 76.7 ns | 1.07 | 0.89 | 190.9 B | 0.0 B | 0.0 B |
| lookupHitWalk | bytes | 529.9 ns ± 104.3 ns | 586.0 ns ± 117.3 ns | 659.2 ns ± 107.1 ns | 1.11 | 1.24 | 52.1 B | 51.7 B | 0.0 B |
| lookupHitWalk | pair | 1.02 µs ± 201.8 ns | 913.4 ns ± 141.8 ns | 797.2 ns ± 300.3 ns | 0.89 | 0.78 | 622.4 B | 472.5 B | 0.0 B |
| contains | none | 422.1 ns ± 107.7 ns | 406.6 ns ± 55.3 ns | 409.2 ns ± 81.5 ns | 0.96 | 0.97 | 112.0 B | 120.0 B | 144.0 B |
| contains | long | 586.0 ns ± 114.9 ns | 558.7 ns ± 126.5 ns | 565.0 ns ± 168.7 ns | 0.95 | 0.96 | 302.8 B | 120.0 B | 160.0 B |
| contains | bytes | 556.4 ns ± 130.5 ns | 513.6 ns ± 117.4 ns | 594.5 ns ± 173.6 ns | 0.92 | 1.07 | 164.1 B | 120.0 B | 254.4 B |
| contains | pair | 912.8 ns ± 94.3 ns | 690.8 ns ± 137.5 ns | 794.0 ns ± 124.5 ns | 0.76 | 0.87 | 734.4 B | 120.0 B | 350.4 B |
| enumerateAll | none | 233.35 ms ± 7.93 ms | 195.46 ms ± 8.31 ms | 224.99 ms ± 19.67 ms | 0.84 | 0.96 | 3.6 KB | 3.6 KB | 4.4 KB |
| enumerateAll | long | 262.70 ms ± 42.28 ms | 264.93 ms ± 33.23 ms | 230.91 ms ± 30.13 ms | 1.01 | 0.88 | 48.00 MB | 4.5 KB | 4.9 KB |
| enumerateAll | bytes | 270.74 ms ± 45.52 ms | 289.50 ms ± 52.21 ms | 377.00 ms ± 29.56 ms | 1.07 | 1.39 | 51.65 MB | 51.68 MB | 8.0 KB |
| enumerateAll | pair | 422.42 ms ± 49.76 ms | 501.75 ms ± 13.79 ms | 428.76 ms ± 30.22 ms | 1.19 | 1.02 | 308.42 MB | 472.73 MB | 10.9 KB |
| topN | N/A | 30.10 µs ± 3.06 µs | 25.23 µs ± 3.27 µs | 29.37 µs ± 2.61 µs | 0.84 | 0.98 | 62.9 KB | 14.0 KB | 68.2 KB |
| construct | none | 1,812.63 ms ± 290.98 ms | 1,727.40 ms ± 177.11 ms | 1,747.27 ms ± 90.50 ms | 0.95 | 0.96 | 367.49 MB | 367.49 MB | 367.49 MB |
| construct | long | 2,150.43 ms ± 201.06 ms | 2,040.52 ms ± 94.22 ms | 2,113.18 ms ± 159.88 ms | 0.95 | 0.98 | 654.16 MB | 637.38 MB | 637.38 MB |
| construct | bytes | 2,055.32 ms ± 213.03 ms | 2,014.20 ms ± 15.78 ms | 2,020.23 ms ± 123.42 ms | 0.98 | 0.98 | 508.03 MB | 491.75 MB | 480.70 MB |
| construct | pair | 2,982.40 ms ± 190.24 ms | 2,981.92 ms ± 213.87 ms | 2,988.08 ms ± 92.08 ms | 1.00 | 1.00 | 1,811.04 MB | 1,784.31 MB | 1,784.30 MB |

### store=offheap, size=1,000,000 terms

| benchmark | outputs | old ns/op | lazy (fst2) ns/op | holder (fst3) ns/op | lazy/old | holder/old | old B/op | lazy B/op | holder B/op |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|
| lookupHit | none | 466.3 ns ± 83.6 ns | 399.2 ns ± 58.7 ns | 482.6 ns ± 125.9 ns | 0.86 | 1.04 | 440.0 B | 448.0 B | 472.0 B |
| lookupHit | long | 699.7 ns ± 89.1 ns | 542.0 ns ± 94.4 ns | 678.7 ns ± 172.4 ns | 0.77 | 0.97 | 630.9 B | 448.0 B | 488.0 B |
| lookupHit | bytes | 696.4 ns ± 102.6 ns | 607.0 ns ± 84.6 ns | 729.8 ns ± 98.2 ns | 0.87 | 1.05 | 492.1 B | 499.7 B | 582.5 B |
| lookupHit | pair | 1.11 µs ± 155.2 ns | 1.17 µs ± 133.9 ns | 967.7 ns ± 85.2 ns | 1.06 | 0.87 | 1.1 KB | 920.5 B | 678.5 B |
| lookupMiss | none | 313.2 ns ± 40.1 ns | 316.8 ns ± 48.5 ns | 307.4 ns ± 47.2 ns | 1.01 | 0.98 | 440.0 B | 448.0 B | 472.0 B |
| lookupMiss | long | 452.3 ns ± 38.9 ns | 469.2 ns ± 39.8 ns | 440.2 ns ± 56.7 ns | 1.04 | 0.97 | 599.7 B | 448.0 B | 488.0 B |
| lookupMiss | bytes | 355.4 ns ± 21.7 ns | 337.6 ns ± 68.8 ns | 374.6 ns ± 57.4 ns | 0.95 | 1.05 | 456.3 B | 448.0 B | 561.6 B |
| lookupMiss | pair | 597.7 ns ± 99.8 ns | 477.6 ns ± 44.7 ns | 588.3 ns ± 57.5 ns | 0.80 | 0.98 | 803.9 B | 448.0 B | 657.6 B |
| lookupHitWalk | none | 383.7 ns ± 93.4 ns | 310.2 ns ± 40.1 ns | 388.9 ns ± 50.3 ns | 0.81 | 1.01 | 0.0 B | 0.0 B | 0.0 B |
| lookupHitWalk | long | 624.6 ns ± 135.4 ns | 522.1 ns ± 153.8 ns | 638.6 ns ± 98.5 ns | 0.84 | 1.02 | 190.9 B | 0.0 B | 0.0 B |
| lookupHitWalk | bytes | 563.6 ns ± 113.3 ns | 538.1 ns ± 100.6 ns | 599.4 ns ± 188.2 ns | 0.95 | 1.06 | 52.1 B | 51.7 B | 0.0 B |
| lookupHitWalk | pair | 948.6 ns ± 111.6 ns | 1.06 µs ± 141.7 ns | 822.8 ns ± 141.0 ns | 1.12 | 0.87 | 622.4 B | 472.5 B | 0.0 B |
| contains | none | 475.1 ns ± 72.0 ns | 446.9 ns ± 90.2 ns | 457.2 ns ± 124.6 ns | 0.94 | 0.96 | 440.0 B | 448.0 B | 472.0 B |
| contains | long | 620.6 ns ± 86.8 ns | 644.1 ns ± 89.3 ns | 765.1 ns ± 153.4 ns | 1.04 | 1.23 | 630.9 B | 448.0 B | 488.0 B |
| contains | bytes | 585.4 ns ± 123.9 ns | 544.5 ns ± 112.0 ns | 684.6 ns ± 105.2 ns | 0.93 | 1.17 | 492.1 B | 448.0 B | 582.5 B |
| contains | pair | 1.05 µs ± 172.1 ns | 685.2 ns ± 94.5 ns | 810.0 ns ± 109.4 ns | 0.65 | 0.77 | 1.1 KB | 448.0 B | 678.5 B |
| enumerateAll | none | 245.04 ms ± 34.29 ms | 212.10 ms ± 10.24 ms | 219.98 ms ± 19.12 ms | 0.87 | 0.90 | 5.2 KB | 5.2 KB | 5.9 KB |
| enumerateAll | long | 306.67 ms ± 24.08 ms | 286.72 ms ± 29.31 ms | 261.45 ms ± 24.81 ms | 0.93 | 0.85 | 48.01 MB | 6.3 KB | 6.7 KB |
| enumerateAll | bytes | 301.05 ms ± 34.35 ms | 288.67 ms ± 18.49 ms | 405.50 ms ± 55.46 ms | 0.96 | 1.35 | 51.66 MB | 51.69 MB | 10.8 KB |
| enumerateAll | pair | 461.12 ms ± 61.10 ms | 618.59 ms ± 69.07 ms | 542.35 ms ± 65.44 ms | 1.34 | 1.18 | 308.43 MB | 472.73 MB | 14.6 KB |
| topN | N/A | 36.51 µs ± 3.98 µs | 29.17 µs ± 3.58 µs | 33.58 µs ± 4.79 µs | 0.80 | 0.92 | 63.5 KB | 14.6 KB | 68.9 KB |
| construct | none | 1,715.07 ms ± 108.48 ms | 1,754.58 ms ± 199.67 ms | 1,751.99 ms ± 79.63 ms | 1.02 | 1.02 | 367.50 MB | 367.50 MB | 367.50 MB |
| construct | long | 2,100.10 ms ± 134.90 ms | 2,104.59 ms ± 166.03 ms | 2,054.68 ms ± 145.29 ms | 1.00 | 0.98 | 654.17 MB | 637.38 MB | 637.38 MB |
| construct | bytes | 2,044.67 ms ± 117.42 ms | 2,017.47 ms ± 127.00 ms | 2,024.99 ms ± 103.64 ms | 0.99 | 0.99 | 508.07 MB | 492.04 MB | 480.70 MB |
| construct | pair | 2,988.04 ms ± 48.13 ms | 2,952.09 ms ± 191.46 ms | 3,041.95 ms ± 321.92 ms | 0.99 | 1.02 | 1,811.10 MB | 1,784.65 MB | 1,784.27 MB |

### 6.2 Reading the holder numbers

- **Zero allocation on every reused-arc walk, for every outputs type.** `lookupHitWalk` is 0 B/op
  for none, long, bytes *and* pair (the lazy design only reaches 0 B for long, because its boxed
  fold still allocates the result). `enumerateAll` drops to 4-15 KB per million terms for all
  outputs types, including pair (old: 308 MB, lazy: 473 MB) and bytes (old and lazy: 52 MB).
- **It fixes the lazy design's pair regression**: `enumerateAll/pair` is back at the old speed
  (1.02 on heap) and `lookupHitWalk/pair` is the fastest of the three (0.78-0.87).
- **But byte-sequence outputs get slower**: `enumerateAll/bytes` 1.35-1.39x, `lookupHitWalk/bytes`
  1.06-1.24x. The old `add(NO_OUTPUT, x)` returns `x` itself, so the cumulative stack shares
  references and copies nothing; the holder stack must *copy* the prefix bytes into the next
  depth's holder on every push (`addInto` has value semantics). Zero allocation is paid for with
  O(depth x length) copying. A copy-on-write or "same as parent" flag in the holder stack would
  remove most of it, at the cost of more state.
- **Lookups through `Util.get` allocate slightly more than the lazy design** (144-160 B on heap
  versus 120 B): a fresh `Arc` now also creates its two holders on first use, and `BytesHolder`
  carries a `BytesRefBuilder`. The per-call `Arc` + reader allocation in `Util` (pre-existing TODO)
  dominates both.
- **Top-N allocates the most of the three** (68 KB per query versus 63 KB old and 14 KB lazy):
  every queued path gets its own holder plus an `Arc` copy that creates two more holders. The lazy
  `LongTopNSearcher` carries a primitive per path and wins clearly here.
- **Membership and misses cannot skip decoding** (eager by construction): `contains` and
  `lookupMiss` are at best on par with old, and off heap 1.1-1.2x slower for long/bytes (within
  noise, but consistently on the wrong side), whereas the lazy design skips every output.
- **Timings otherwise within noise**, with the same caveat as before (±10-25%). Long paths:
  `enumerateAll/long` 0.85-0.88 (faster than both), `lookupHitWalk/long` 0.89 on heap / 1.02 off
  heap.
- Construction is unchanged (0.96-1.02, byte-identical files asserted).

**Net**: the holder design is the better fit when every traversal needs the values (enumeration,
WFST walks with byte or pair outputs); the lazy design is the better fit when many traversals do
not (membership, misses, top-N over longs) or when decoding is expensive relative to copying. A
hybrid (lazy positions in `Arc`, holder-based `fold`/accumulate instead of boxed `add`) would get
the lazy design's skipping with the holder design's allocation-free boxed path; that is the
"accumulator over positions" variant and was not implemented.

### 6.3 What the holder design looks like

- `OutputHolder<T>` (new abstract class): `newHolder()`, `setNoOutput()`, `isNoOutput()`,
  `copyFrom(OutputHolder<T>)`, `T get()` (boxed snapshot, may allocate), `set(T)`.
- `Outputs<T>` gains `newHolder()`, `readInto(DataInput, OutputHolder<T>)`,
  `readFinalOutputInto(...)` and `addInto(prefix, output, dest)` (`dest` may alias `prefix`, never
  `output`). The boxed algebra (`common`, `subtract`, `add`, `read`, `write`) is untouched and still
  drives construction.
- Each `Outputs` defines its holder: `PositiveIntOutputs.LongHolder` (public `long value`),
  `ByteSequenceOutputs.BytesHolder` (a `BytesRefBuilder`), `IntSequenceOutputs.IntsHolder`,
  `CharSequenceOutputs.CharsHolder`, `PairOutputs.PairHolder` (one holder per component),
  `NoOutputs.NoHolder` (stateless).
- `FST.Arc<T>` keeps two holders, created on first use (`ensureHolders`) and reused; `readArc`
  decodes with `readInto` / `readFinalOutputInto` or calls `setNoOutput()`. `Arc.copyFrom` copies
  holder contents. `arc.output()` / `arc.nextFinalOutput()` return the holders; callers that know
  the outputs type cast once (`(LongHolder) arc.output()`) and read `.value` after each read.
- `FSTEnum` keeps a cumulative stack of holders, one per depth (`addInto(output[upto-1],
  arc.output(), output[upto])`); `InputOutput.output` is the depth's holder (valid until the next
  move).
- `Util.get(fst, input, OutputHolder<T> result)` returns a boolean and fills the holder; the boxed
  `Util.get` is built on it. `Util.contains` exists for symmetry but still decodes into the arc's
  holder on every arc (the design is eager).
- `TopNSearcher` paths own a holder; the comparator is a `Comparator<OutputHolder<T>>`
  (`PositiveIntOutputs.HOLDER_COMPARATOR` for longs); the "zero output" test compares against a
  no-output holder; results are boxed once at the end (`Result<T>` unchanged).
- Construction is unchanged (`FSTCompiler`, `FSTSuffixNodeCache` box through `holder.get()` exactly
  where `fst2` boxes through `readOutput`). `TestFST3Compat.testIdenticalBytes` pins the format.

### 6.4 API decisions that felt awkward (holder design)

- **Casting to the concrete holder.** `arc.output()` is an `OutputHolder<T>`; the primitive is
  only reachable through a cast to `PositiveIntOutputs.LongHolder`. The cast is cheap (one type
  check) and stable (the arc keeps the same holder object), but it is unchecked at the API level
  and ugly in user code. Alternatives: a second type parameter (`FST<T, H extends
  OutputHolder<T>>`, `Arc<H>`) so `arc.output()` is already a `LongHolder`, at the cost of generic
  noise everywhere; or primitive accessors on `OutputHolder` (`long asLong()`) that throw for other
  outputs.
- **Holders are shared mutable state.** `InputOutput.output`, `arc.output()` and
  `TopNSearcher` paths all hand out holders that the next operation overwrites. The old API had
  the same hazard for `BytesRef` outputs (see `testIllegallyModifyRootArc`), but for `Long` it did
  not; this design makes every output type "live". Defensive copies (`newHolder()` + `copyFrom`)
  are the escape hatch.
- **`Outputs` doubles in size.** Every implementation now carries two parallel algebras (boxed
  for construction, holder-based for traversal) that must agree byte for byte; `ListOfOutputs`
  (misc) and any external `Outputs` would have to implement both. The lazy design needed only
  `skipOutput` overrides (plus the optional `LongOutputs`).
- **`addInto` aliasing rules.** `dest` may be `prefix` but not `output`; this is asserted for the
  sequence outputs but is a convention, not a type.
- **`newHolder()` lives on both `Outputs` and `OutputHolder`.** `Arc.copyFrom` has no `Outputs`
  reference and needs a template to create holders, hence the second factory.
- **Eager by construction.** Membership tests and misses still decode every output on the path
  (`contains` is only a `get` that ignores the result), and `enumerateAll/none` still runs the
  holder machinery. Only the lazy design can skip decoding entirely.
- **`PairHolder.get()` allocates two levels** (`Pair` + component boxes/copies), so the boxed
  fallback for pairs is as expensive as before; this matters only where `get()` is called.

### 6.5 Not done (holder design)

- `ListOfOutputs` (misc) was not ported; `readFinalOutputInto` is therefore exercised only through
  its default.
- `Test2BFST*` not ported (plan: skip).
- Only `size=1000000` was run; the holder runs used `-p impl=holder` and were merged with the
  earlier `old`/`new` JSON files (same machine, same JDK, same data seed, about an hour apart).

### 6.6 Holder-API showcase bodies (from `TestFST3Showcase`; the `_old` bodies are identical to section 3)

### Exact lookup of a long output (`testTransducerLookup`, holder variant)

```java
    List<Long> sample = new ArrayList<>();
    PositiveIntOutputs.LongHolder result = PositiveIntOutputs.getSingleton().newHolder();
    for (BytesRef key : hits) {
      assertTrue(Util.get(newLongFst, key, result));
      assertEquals(reference.get(key).longValue(), result.value);
      sample.add(result.value);
    }
    for (BytesRef key : misses) {
      assertFalse(Util.get(newLongFst, key, result));
    }
    printSample("lookup(new)", sample);
```

### WFST-style top-N completion (`testSuggester`, holder variant)

```java
    var in = newWfst.getBytesReader();
    var scratch = new BytesRefBuilder();
    PositiveIntOutputs.LongHolder prefixOutput = PositiveIntOutputs.getSingleton().newHolder();
    for (String prefix : prefixes) {
      FST.Arc<Long> arc = newWfst.getFirstArc(new FST.Arc<>());
      prefixOutput.value = 0;
      for (int i = 0; i < prefix.length(); i++) {
        arc = newWfst.findTargetArc(prefix.charAt(i), arc, arc, in);
        assertNotNull(arc);
        prefixOutput.value += ((PositiveIntOutputs.LongHolder) arc.output()).value;
      }
      var results =
          Util.shortestPaths(
              newWfst, arc, prefixOutput, PositiveIntOutputs.HOLDER_COMPARATOR, TOP_N, true);
      List<String> completions = new ArrayList<>();
      for (var result : results) {
        completions.add(prefix + Util.toBytesRef(result.input(), scratch).utf8ToString());
        long weight = Long.MAX_VALUE - result.output();
        assertEquals(reference.get(new BytesRef(completions.getLast())).longValue(), weight);
      }
      assertEquals(prefix, expectedCompletions(prefix), completions);
      printSample("suggest(new) " + prefix, completions);
    }
```

### Manual arc walk summing outputs (`testManualWalk`, holder variant)

```java
    var in = newLongFst.getBytesReader();
    FST.Arc<Long> arc = new FST.Arc<>();
    for (BytesRef key : hits) {
      newLongFst.getFirstArc(arc);
      long acc = 0;
      for (int i = 0; i < key.length; i++) {
        arc = newLongFst.findTargetArc(key.bytes[key.offset + i] & 0xFF, arc, arc, in);
        assertNotNull(arc);
        acc += ((PositiveIntOutputs.LongHolder) arc.output()).value;
      }
      assertTrue(arc.isFinal());
      acc += ((PositiveIntOutputs.LongHolder) arc.nextFinalOutput()).value;
      assertEquals(reference.get(key).longValue(), acc);
    }
```

### Pair outputs: lookup and manual walk (`testPairOutputs`, holder variant)

```java
    var in = newPairFst.getBytesReader();
    PairOutputs.PairHolder<Long, BytesRef> result = newPairOutputs.newHolder();
    for (BytesRef key : hits) {
      // Lookup through Util into a reusable holder; components are read without boxing.
      assertTrue(Util.get(newPairFst, key, result));
      assertEquals(
          reference.get(key).longValue(), ((PositiveIntOutputs.LongHolder) result.output1).value);
      assertEquals(
          bytesOutput(key), ((ByteSequenceOutputs.BytesHolder) result.output2).bytes.get());

      // Manual walk: every arc's output is decoded into the arc's holder and accumulated in place.
      FST.Arc<PairOutputs.Pair<Long, BytesRef>> arc = newPairFst.getFirstArc(new FST.Arc<>());
      result.setNoOutput();
      for (int i = 0; i < key.length; i++) {
        arc = newPairFst.findTargetArc(key.bytes[key.offset + i] & 0xFF, arc, arc, in);
        newPairFst.outputs.addInto(result, arc.output(), result);
      }
      assertTrue(arc.isFinal());
      newPairFst.outputs.addInto(result, arc.nextFinalOutput(), result);
      assertEquals(reference.get(key), result.get().output1);
      assertEquals(bytesOutput(key), result.get().output2);
    }
```
