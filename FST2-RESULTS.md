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
10. `fst3: JMH impl=holder variant; test lint fix`, `fst3: results added to FST2-RESULTS.md`
11. `fst4: verbatim copy of fst2`
12. `fst4: hybrid design (lazy positions in Arc, holder accumulation via Outputs.readAddInto)`
13. `fst4: correctness tests ...`, `fst4: side-by-side showcase tests (old vs hybrid API)`
14. `fst4: JMH impl=hybrid variant`, `fst4: results added to FST2-RESULTS.md`

Sections 1 to 5 cover the lazy, position-based design (`fst2`). Section 6 covers the alternative
"holder" design (`fst3`), section 7 the hybrid of the two (`fst4`). **Section 8 at the end holds
the consolidated performance metrics for all four implementations**; the per-section tables in
sections 1 and 6 are the earlier two- and three-way snapshots of the same runs.

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

Superseded by the four-way tables in section 8 (same `old`/`new` runs, plus the `holder` run from
`fst3-*.json`). The notes below refer to those numbers.

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

## 7. The hybrid (`fst4`): lazy positions + holder accumulation

`org.apache.lucene.util.fst4` combines the two previous experiments: the lazy, position-only
`Arc` of `fst2` (nothing is decoded while traversing, membership and misses are free) with the
reusable holders of `fst3` for the moment a value *is* needed. The one new primitive is
`Outputs.readAddInto(in, acc)`: decode the next output and add it to the accumulator in place,
appending directly into its storage. There is no boxed `add` on the read side and no prefix copy.
Same 43 tests, same compat test (byte-identical files), same showcase pairs, same benchmarks with
`impl=hybrid`.

### 7.1 Reading the hybrid numbers (full tables in section 8)

- **Zero allocation on every reused-arc walk for every outputs type** (like the holder design),
  and **the lowest fixed cost of all through `Util.get`**: 120 B/op on heap for none, long, bytes
  *and* pair (old: 112 to 734 B; holder: 144 to 350 B), because a fresh `Arc` carries no holders
  and the caller supplies one accumulator.
- **Enumeration allocates 4 to 6 KB per million terms for every outputs type**, versus 48 to 473 MB
  for old/lazy and 5 to 11 KB for the holder design. It is also the fastest of the four on pairs
  (0.84 on heap, 0.78 off heap) and it does **not** have the holder design's byte-sequence
  slowdown (1.06 / 1.10 versus 1.39 / 1.35): depths whose arc has no output alias their parent's
  holder instead of copying it, and `readAddInto` appends in place.
- **Membership and misses decode nothing**, exactly as in the lazy design (the code is identical);
  the 0.82 to 1.15 spread between `contains` rows of the lazy and hybrid variants is a direct
  measurement of this machine's noise, since they run the same code.
- **Pair lookups are the fastest of the four**: `lookupHit/pair` 0.76 / 0.77, `lookupHitWalk/pair`
  0.68 / 0.78.
- **Top-N is the weak spot**: 59.5 KB per query and 1.06 to 1.16x slower than old. The generic
  holder-based `TopNSearcher` allocates a holder per queued path (plus the `IntsRefBuilder` every
  variant allocates) and decodes into a scratch holder for the comparator; the lazy design's
  specialized `LongTopNSearcher` (14 KB, 0.80 to 0.84) shows what a primitive path buys here. A
  `LongHolder`-specialized or pooled-path searcher would close the gap.
- Construction unchanged (0.97 to 1.02).

**Net**: the hybrid dominates both parents on lookups and enumeration for every outputs type
(allocation) and matches the better parent on time in every row except top-N. It needs two new
methods on `Outputs` (`newHolder`, `readAddInto`) and the non-generic, position-based `Arc`.

### 7.2 What the hybrid looks like

- `FST.Arc` is the lazy, non-generic arc of `fst2`: it carries `outputPos` / `nextFinalOutputPos`
  only, so traversal, membership and misses decode nothing.
- `OutputHolder<T>` is the holder of `fst3` (`newHolder`, `setNoOutput`, `isNoOutput`,
  `copyFrom`, `get`, `set`).
- `Outputs<T>` gains exactly two methods: `newHolder()` and `readAddInto(DataInput, acc)` (plus
  the `readFinalOutputAddInto` default). `readAddInto` decodes the next output and adds it to the
  accumulator *in place*: `value += readVLong()` for longs, an in-place append for byte/int/char
  sequences, component-wise for pairs. There is no `addInto(prefix, output, dest)` and no boxed
  `add` on the read side, so the prefix is never copied.
- `FST.readAddInto(pos, in, acc)` (position-aware: `NO_OUTPUT_POS` is a no-op, the flag bit
  selects the final-output decoder, the reader position is saved and restored),
  `FST.readOutputInto(pos, in, dest)` and `FST.foldInto(positions, n, in, dest)`. The boxed
  `readOutput` / `output(arc, in)` remain for `toDot` and the suffix cache. `LongOutputs` and the
  whole `*Long` API of `fst2` are gone: `PositiveIntOutputs.LongHolder.value` is the primitive.
- `FSTEnum` keeps the positions stack of `fst2` and a *lazily filled* cumulative holder stack:
  `currentOutput()` recomputes only the depths that changed since the last call, and a depth whose
  arc carries no output **aliases** its parent's holder instead of copying it. A full enumeration
  therefore decodes each arc output once and copies a prefix only at depths that actually add
  something.
- `Util.get(fst, input, holder)` accumulates with `readAddInto` as it walks (arcs without output
  are free; a miss stops early); `Util.contains` decodes nothing; `TopNSearcher` paths own holders
  and the zero-output test short-circuits on `arc.hasOutput()` before decoding into a scratch
  holder.
- Construction is unchanged; `TestFST4Compat.testIdenticalBytes` pins the format; the same 43
  tests pass with `-Ptests.multiplier=3`.

### 7.3 API decisions that felt awkward (hybrid)

- **Still a cast to the concrete holder** for the primitive (`(LongHolder) acc`), as in `fst3`.
- **Holders are live, mutable state** handed out by `currentOutput()` / `InputOutput.output()`;
  copy them if they must outlive the next move.
- **`readAddInto` must agree with `read` + `add`** for every `Outputs`; this is a second algebra
  (two methods instead of `fst3`'s three), still to be implemented by every external `Outputs`.
- **`EMPTY_OUTPUT_POS` boxes once**: the empty-string output lives in the metadata, so
  `readAddInto` falls back to `acc.set(outputs.add(acc.get(), emptyOutput))` on that path (only
  the virtual root arc; negligible but ugly).
- **`TopNSearcher` zero test**: `arc.hasOutput() == false || comparator.compare(NO_OUTPUT,
  decoded) == 0`. The short-circuit is what keeps the search allocation-light; the second half is
  needed for comparators that look at part of a pair.
- **`Util.get` still allocates an `Arc` and a reader per call** (pre-existing TODO), which is the
  entire per-lookup allocation of this design.

### 7.4 Not done (hybrid)

- `ListOfOutputs` (misc) not ported; `readFinalOutputAddInto` exercised only through its default.
- `Test2BFST*` not ported; only `size=1000000` run (`-p impl=hybrid`, merged with the earlier JSON
  files; same machine, JDK and data).

### 7.5 Hybrid-API showcase bodies (from `TestFST4Showcase`; the `_old` bodies are identical to section 3)

### Exact lookup of a long output (`testTransducerLookup`, hybrid variant)

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

### WFST-style top-N completion (`testSuggester`, hybrid variant)

```java
    var in = newWfst.getBytesReader();
    var scratch = new BytesRefBuilder();
    PositiveIntOutputs.LongHolder prefixOutput = PositiveIntOutputs.getSingleton().newHolder();
    for (String prefix : prefixes) {
      var arc = newWfst.getFirstArc(new FST.Arc());
      prefixOutput.setNoOutput();
      for (int i = 0; i < prefix.length(); i++) {
        arc = newWfst.findTargetArc(prefix.charAt(i), arc, arc, in);
        assertNotNull(arc);
        newWfst.readAddInto(arc.outputPos(), in, prefixOutput);
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

### Manual arc walk summing outputs (`testManualWalk`, hybrid variant)

```java
    var in = newLongFst.getBytesReader();
    PositiveIntOutputs.LongHolder acc = PositiveIntOutputs.getSingleton().newHolder();
    for (BytesRef key : hits) {
      var arc = newLongFst.getFirstArc(new FST.Arc());
      acc.setNoOutput();
      for (int i = 0; i < key.length; i++) {
        arc = newLongFst.findTargetArc(key.bytes[key.offset + i] & 0xFF, arc, arc, in);
        assertNotNull(arc);
        newLongFst.readAddInto(arc.outputPos(), in, acc);
      }
      assertTrue(arc.isFinal());
      newLongFst.readAddInto(arc.nextFinalOutputPos(), in, acc);
      assertEquals(reference.get(key).longValue(), acc.value);
    }
```

### Pair outputs: lookup and manual walk (`testPairOutputs`, hybrid variant)

```java
    var in = newPairFst.getBytesReader();
    PairOutputs.PairHolder<Long, BytesRef> result = newPairOutputs.newHolder();
    long[] positions = new long[64];
    for (BytesRef key : hits) {
      // Lookup through Util into a reusable holder; components are read without boxing.
      assertTrue(Util.get(newPairFst, key, result));
      assertEquals(
          reference.get(key).longValue(), ((PositiveIntOutputs.LongHolder) result.output1).value);
      assertEquals(
          bytesOutput(key), ((ByteSequenceOutputs.BytesHolder) result.output2).bytes.get());

      // Manual walk: only positions are collected; a single fold accumulates at the end.
      var arc = newPairFst.getFirstArc(new FST.Arc());
      int n = 0;
      for (int i = 0; i < key.length; i++) {
        arc = newPairFst.findTargetArc(key.bytes[key.offset + i] & 0xFF, arc, arc, in);
        positions[n++] = arc.outputPos();
      }
      assertTrue(arc.isFinal());
      positions[n++] = arc.nextFinalOutputPos();
      newPairFst.foldInto(positions, n, in, result);
      assertEquals(reference.get(key), result.get().output1);
      assertEquals(bytesOutput(key), result.get().output2);
    }
```

## 8. Performance metrics: all four implementations

Setup as in section 1 (JMH 1.37, OpenJDK 25.0.3, Threadripper 3970X, 1,000,000 terms, 1 fork,
5 x 1 s warmup, 5 x 2 s measurement, `-prof gc`). Each implementation column comes from its own
run (`-p impl=...`) on the same machine, JDK, data seed and jar; the four runs are about an hour
apart. "±" is the JMH 99.9% confidence half-width; on this box differences below about 15% are
noise. The tables are generated by a script from `fst2-*.json`, `fst3-*.json` and `fst4-*.json`
(untracked, next to this file). Implementations: **old** = `org.apache.lucene.util.fst` (eager,
boxed), **lazy** = `fst2` (positions, boxed fold / primitive long path), **holder** = `fst3`
(eager into reusable holders), **hybrid** = `fst4` (positions + holder accumulation).

### 8.1 Digest (allocation on heap; time ratios to old, lower is better)

| benchmark | outputs | alloc old → lazy / holder / hybrid (on heap) | time ratio to old, on heap (lazy / holder / hybrid) | time ratio to old, off heap (lazy / holder / hybrid) |
|---|---|---|---|---|
| lookupHitWalk | none | 0.0 B → 0.0 B / 0.0 B / 0.0 B | 0.95 / 0.91 / 0.96 | 0.81 / 1.01 / 0.78 |
| lookupHitWalk | long | 190.9 B → 0.0 B / 0.0 B / 0.0 B | 1.07 / 0.89 / 0.99 | 0.84 / 1.02 / 0.86 |
| lookupHitWalk | bytes | 52.1 B → 51.7 B / 0.0 B / 0.0 B | 1.11 / 1.24 / 1.07 | 0.95 / 1.06 / 0.99 |
| lookupHitWalk | pair | 622.4 B → 472.5 B / 0.0 B / 0.0 B | 0.89 / 0.78 / 0.68 | 1.12 / 0.87 / 0.78 |
| lookupHit | none | 112.0 B → 120.0 B / 144.0 B / 120.0 B | 0.92 / 1.02 / 1.00 | 0.86 / 1.04 / 0.95 |
| lookupHit | long | 302.9 B → 120.0 B / 160.0 B / 120.0 B | 1.04 / 0.98 / 1.08 | 0.77 / 0.97 / 1.02 |
| lookupHit | bytes | 164.1 B → 171.7 B / 254.4 B / 120.0 B | 1.07 / 1.13 / 0.96 | 0.87 / 1.05 / 0.91 |
| lookupHit | pair | 734.4 B → 592.5 B / 350.4 B / 120.0 B | 1.01 / 0.96 / 0.76 | 1.06 / 0.87 / 0.77 |
| lookupMiss | none | 112.0 B → 120.0 B / 144.0 B / 120.0 B | 0.98 / 1.05 / 1.00 | 1.01 / 0.98 / 0.96 |
| lookupMiss | long | 271.6 B → 120.0 B / 160.0 B / 120.0 B | 1.17 / 0.97 / 1.09 | 1.04 / 0.97 / 1.03 |
| lookupMiss | bytes | 128.3 B → 120.0 B / 233.6 B / 120.0 B | 0.91 / 1.17 / 1.01 | 0.95 / 1.05 / 0.99 |
| lookupMiss | pair | 475.9 B → 120.0 B / 329.6 B / 120.0 B | 0.74 / 1.02 / 1.01 | 0.80 / 0.98 / 0.95 |
| contains | none | 112.0 B → 120.0 B / 144.0 B / 120.0 B | 0.96 / 0.97 / 0.96 | 0.94 / 0.96 / 0.82 |
| contains | long | 302.8 B → 120.0 B / 160.0 B / 120.0 B | 0.95 / 0.96 / 0.95 | 1.04 / 1.23 / 1.12 |
| contains | bytes | 164.1 B → 120.0 B / 254.4 B / 120.0 B | 0.92 / 1.07 / 0.95 | 0.93 / 1.17 / 1.15 |
| contains | pair | 734.4 B → 120.0 B / 350.4 B / 120.0 B | 0.76 / 0.87 / 0.73 | 0.65 / 0.77 / 0.70 |
| enumerateAll | none | 3.6 KB → 3.6 KB / 4.4 KB / 4.1 KB | 0.84 / 0.96 / 0.83 | 0.87 / 0.90 / 0.86 |
| enumerateAll | long | 48.00 MB → 4.5 KB / 4.9 KB / 4.8 KB | 1.01 / 0.88 / 1.01 | 0.93 / 0.85 / 0.95 |
| enumerateAll | bytes | 51.65 MB → 51.68 MB / 8.0 KB / 5.2 KB | 1.07 / 1.39 / 1.06 | 0.96 / 1.35 / 1.10 |
| enumerateAll | pair | 308.42 MB → 472.73 MB / 10.9 KB / 6.3 KB | 1.19 / 1.02 / 0.84 | 1.34 / 1.18 / 0.78 |
| topN | N/A | 62.9 KB → 14.0 KB / 68.2 KB / 59.5 KB | 0.84 / 0.98 / 1.16 | 0.80 / 0.92 / 1.06 |
| construct | none | 367.49 MB → 367.49 MB / 367.49 MB / 367.49 MB | 0.95 / 0.96 / 0.99 | 1.02 / 1.02 / 1.02 |
| construct | long | 654.16 MB → 637.38 MB / 637.38 MB / 637.38 MB | 0.95 / 0.98 / 0.97 | 1.00 / 0.98 / 0.99 |
| construct | bytes | 508.03 MB → 491.75 MB / 480.70 MB / 491.75 MB | 0.98 / 0.98 / 0.97 | 0.99 / 0.99 / 0.98 |
| construct | pair | 1,811.04 MB → 1,784.31 MB / 1,784.30 MB / 1,784.64 MB | 1.00 / 1.00 / 0.98 | 0.99 / 1.02 / 0.99 |

How to read it: `lookupHitWalk` isolates output handling (reused `Arc` and reader); `lookupHit`,
`lookupMiss` and `contains` go through `Util` and include its per-call `Arc`/reader allocation
(112 to 120 B on heap, 440 to 448 B off heap); `enumerateAll` is a full pass over the FST;
`topN` is a 10-best completion of a two-letter prefix; `construct` should be flat.

### 8.2 Full tables

### store=onheap, size=1,000,000 terms: time per op (mean ± 99.9% CI)

| benchmark | outputs | old | lazy (fst2) | holder (fst3) | hybrid (fst4) | lazy/old | holder/old | hybrid/old |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| lookupHit | none | 427.5 ns ± 87.5 ns | 393.6 ns ± 28.2 ns | 436.4 ns ± 64.1 ns | 426.5 ns ± 77.7 ns | 0.92 | 1.02 | 1.00 |
| lookupHit | long | 571.3 ns ± 111.3 ns | 595.8 ns ± 59.1 ns | 559.1 ns ± 106.6 ns | 615.8 ns ± 61.0 ns | 1.04 | 0.98 | 1.08 |
| lookupHit | bytes | 577.3 ns ± 91.4 ns | 618.8 ns ± 76.7 ns | 653.0 ns ± 115.9 ns | 553.9 ns ± 99.7 ns | 1.07 | 1.13 | 0.96 |
| lookupHit | pair | 927.8 ns ± 126.3 ns | 936.6 ns ± 173.8 ns | 887.6 ns ± 114.3 ns | 701.0 ns ± 256.6 ns | 1.01 | 0.96 | 0.76 |
| lookupMiss | none | 222.2 ns ± 33.2 ns | 217.2 ns ± 10.6 ns | 234.4 ns ± 25.8 ns | 222.3 ns ± 38.1 ns | 0.98 | 1.05 | 1.00 |
| lookupMiss | long | 285.7 ns ± 53.6 ns | 334.7 ns ± 50.5 ns | 277.6 ns ± 16.6 ns | 310.1 ns ± 40.8 ns | 1.17 | 0.97 | 1.09 |
| lookupMiss | bytes | 275.7 ns ± 51.1 ns | 250.3 ns ± 51.5 ns | 322.9 ns ± 61.1 ns | 278.3 ns ± 46.9 ns | 0.91 | 1.17 | 1.01 |
| lookupMiss | pair | 425.1 ns ± 100.6 ns | 315.3 ns ± 71.1 ns | 435.5 ns ± 65.0 ns | 427.6 ns ± 59.9 ns | 0.74 | 1.02 | 1.01 |
| lookupHitWalk | none | 433.9 ns ± 124.9 ns | 411.9 ns ± 74.2 ns | 393.7 ns ± 106.6 ns | 418.0 ns ± 80.1 ns | 0.95 | 0.91 | 0.96 |
| lookupHitWalk | long | 607.0 ns ± 122.6 ns | 651.5 ns ± 68.3 ns | 538.4 ns ± 76.7 ns | 603.3 ns ± 88.2 ns | 1.07 | 0.89 | 0.99 |
| lookupHitWalk | bytes | 529.9 ns ± 104.3 ns | 586.0 ns ± 117.3 ns | 659.2 ns ± 107.1 ns | 568.8 ns ± 145.1 ns | 1.11 | 1.24 | 1.07 |
| lookupHitWalk | pair | 1.02 µs ± 201.8 ns | 913.4 ns ± 141.8 ns | 797.2 ns ± 300.3 ns | 693.3 ns ± 119.2 ns | 0.89 | 0.78 | 0.68 |
| contains | none | 422.1 ns ± 107.7 ns | 406.6 ns ± 55.3 ns | 409.2 ns ± 81.5 ns | 403.8 ns ± 66.2 ns | 0.96 | 0.97 | 0.96 |
| contains | long | 586.0 ns ± 114.9 ns | 558.7 ns ± 126.5 ns | 565.0 ns ± 168.7 ns | 557.6 ns ± 71.7 ns | 0.95 | 0.96 | 0.95 |
| contains | bytes | 556.4 ns ± 130.5 ns | 513.6 ns ± 117.4 ns | 594.5 ns ± 173.6 ns | 527.2 ns ± 135.6 ns | 0.92 | 1.07 | 0.95 |
| contains | pair | 912.8 ns ± 94.3 ns | 690.8 ns ± 137.5 ns | 794.0 ns ± 124.5 ns | 670.4 ns ± 159.2 ns | 0.76 | 0.87 | 0.73 |
| enumerateAll | none | 233.35 ms ± 7.93 ms | 195.46 ms ± 8.31 ms | 224.99 ms ± 19.67 ms | 193.87 ms ± 25.67 ms | 0.84 | 0.96 | 0.83 |
| enumerateAll | long | 262.70 ms ± 42.28 ms | 264.93 ms ± 33.23 ms | 230.91 ms ± 30.13 ms | 266.31 ms ± 29.54 ms | 1.01 | 0.88 | 1.01 |
| enumerateAll | bytes | 270.74 ms ± 45.52 ms | 289.50 ms ± 52.21 ms | 377.00 ms ± 29.56 ms | 286.97 ms ± 21.41 ms | 1.07 | 1.39 | 1.06 |
| enumerateAll | pair | 422.42 ms ± 49.76 ms | 501.75 ms ± 13.79 ms | 428.76 ms ± 30.22 ms | 354.06 ms ± 37.44 ms | 1.19 | 1.02 | 0.84 |
| topN | N/A | 30.10 µs ± 3.06 µs | 25.23 µs ± 3.27 µs | 29.37 µs ± 2.61 µs | 34.83 µs ± 3.71 µs | 0.84 | 0.98 | 1.16 |
| construct | none | 1,812.63 ms ± 290.98 ms | 1,727.40 ms ± 177.11 ms | 1,747.27 ms ± 90.50 ms | 1,801.69 ms ± 192.86 ms | 0.95 | 0.96 | 0.99 |
| construct | long | 2,150.43 ms ± 201.06 ms | 2,040.52 ms ± 94.22 ms | 2,113.18 ms ± 159.88 ms | 2,085.74 ms ± 121.19 ms | 0.95 | 0.98 | 0.97 |
| construct | bytes | 2,055.32 ms ± 213.03 ms | 2,014.20 ms ± 15.78 ms | 2,020.23 ms ± 123.42 ms | 2,002.59 ms ± 108.34 ms | 0.98 | 0.98 | 0.97 |
| construct | pair | 2,982.40 ms ± 190.24 ms | 2,981.92 ms ± 213.87 ms | 2,988.08 ms ± 92.08 ms | 2,916.01 ms ± 266.57 ms | 1.00 | 1.00 | 0.98 |

### store=onheap, size=1,000,000 terms: allocation per op (gc.alloc.rate.norm)

| benchmark | outputs | old B/op | lazy B/op | holder B/op | hybrid B/op |
|---|---|---:|---:|---:|---:|
| lookupHit | none | 112.0 B | 120.0 B | 144.0 B | 120.0 B |
| lookupHit | long | 302.9 B | 120.0 B | 160.0 B | 120.0 B |
| lookupHit | bytes | 164.1 B | 171.7 B | 254.4 B | 120.0 B |
| lookupHit | pair | 734.4 B | 592.5 B | 350.4 B | 120.0 B |
| lookupMiss | none | 112.0 B | 120.0 B | 144.0 B | 120.0 B |
| lookupMiss | long | 271.6 B | 120.0 B | 160.0 B | 120.0 B |
| lookupMiss | bytes | 128.3 B | 120.0 B | 233.6 B | 120.0 B |
| lookupMiss | pair | 475.9 B | 120.0 B | 329.6 B | 120.0 B |
| lookupHitWalk | none | 0.0 B | 0.0 B | 0.0 B | 0.0 B |
| lookupHitWalk | long | 190.9 B | 0.0 B | 0.0 B | 0.0 B |
| lookupHitWalk | bytes | 52.1 B | 51.7 B | 0.0 B | 0.0 B |
| lookupHitWalk | pair | 622.4 B | 472.5 B | 0.0 B | 0.0 B |
| contains | none | 112.0 B | 120.0 B | 144.0 B | 120.0 B |
| contains | long | 302.8 B | 120.0 B | 160.0 B | 120.0 B |
| contains | bytes | 164.1 B | 120.0 B | 254.4 B | 120.0 B |
| contains | pair | 734.4 B | 120.0 B | 350.4 B | 120.0 B |
| enumerateAll | none | 3.6 KB | 3.6 KB | 4.4 KB | 4.1 KB |
| enumerateAll | long | 48.00 MB | 4.5 KB | 4.9 KB | 4.8 KB |
| enumerateAll | bytes | 51.65 MB | 51.68 MB | 8.0 KB | 5.2 KB |
| enumerateAll | pair | 308.42 MB | 472.73 MB | 10.9 KB | 6.3 KB |
| topN | N/A | 62.9 KB | 14.0 KB | 68.2 KB | 59.5 KB |
| construct | none | 367.49 MB | 367.49 MB | 367.49 MB | 367.49 MB |
| construct | long | 654.16 MB | 637.38 MB | 637.38 MB | 637.38 MB |
| construct | bytes | 508.03 MB | 491.75 MB | 480.70 MB | 491.75 MB |
| construct | pair | 1,811.04 MB | 1,784.31 MB | 1,784.30 MB | 1,784.64 MB |

### store=offheap, size=1,000,000 terms: time per op (mean ± 99.9% CI)

| benchmark | outputs | old | lazy (fst2) | holder (fst3) | hybrid (fst4) | lazy/old | holder/old | hybrid/old |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| lookupHit | none | 466.3 ns ± 83.6 ns | 399.2 ns ± 58.7 ns | 482.6 ns ± 125.9 ns | 445.0 ns ± 86.1 ns | 0.86 | 1.04 | 0.95 |
| lookupHit | long | 699.7 ns ± 89.1 ns | 542.0 ns ± 94.4 ns | 678.7 ns ± 172.4 ns | 714.3 ns ± 123.1 ns | 0.77 | 0.97 | 1.02 |
| lookupHit | bytes | 696.4 ns ± 102.6 ns | 607.0 ns ± 84.6 ns | 729.8 ns ± 98.2 ns | 633.0 ns ± 134.3 ns | 0.87 | 1.05 | 0.91 |
| lookupHit | pair | 1.11 µs ± 155.2 ns | 1.17 µs ± 133.9 ns | 967.7 ns ± 85.2 ns | 856.6 ns ± 117.7 ns | 1.06 | 0.87 | 0.77 |
| lookupMiss | none | 313.2 ns ± 40.1 ns | 316.8 ns ± 48.5 ns | 307.4 ns ± 47.2 ns | 299.9 ns ± 44.8 ns | 1.01 | 0.98 | 0.96 |
| lookupMiss | long | 452.3 ns ± 38.9 ns | 469.2 ns ± 39.8 ns | 440.2 ns ± 56.7 ns | 466.3 ns ± 60.5 ns | 1.04 | 0.97 | 1.03 |
| lookupMiss | bytes | 355.4 ns ± 21.7 ns | 337.6 ns ± 68.8 ns | 374.6 ns ± 57.4 ns | 353.6 ns ± 56.9 ns | 0.95 | 1.05 | 0.99 |
| lookupMiss | pair | 597.7 ns ± 99.8 ns | 477.6 ns ± 44.7 ns | 588.3 ns ± 57.5 ns | 568.6 ns ± 68.4 ns | 0.80 | 0.98 | 0.95 |
| lookupHitWalk | none | 383.7 ns ± 93.4 ns | 310.2 ns ± 40.1 ns | 388.9 ns ± 50.3 ns | 297.7 ns ± 65.3 ns | 0.81 | 1.01 | 0.78 |
| lookupHitWalk | long | 624.6 ns ± 135.4 ns | 522.1 ns ± 153.8 ns | 638.6 ns ± 98.5 ns | 534.1 ns ± 97.0 ns | 0.84 | 1.02 | 0.86 |
| lookupHitWalk | bytes | 563.6 ns ± 113.3 ns | 538.1 ns ± 100.6 ns | 599.4 ns ± 188.2 ns | 557.9 ns ± 104.9 ns | 0.95 | 1.06 | 0.99 |
| lookupHitWalk | pair | 948.6 ns ± 111.6 ns | 1.06 µs ± 141.7 ns | 822.8 ns ± 141.0 ns | 735.8 ns ± 52.5 ns | 1.12 | 0.87 | 0.78 |
| contains | none | 475.1 ns ± 72.0 ns | 446.9 ns ± 90.2 ns | 457.2 ns ± 124.6 ns | 387.9 ns ± 95.5 ns | 0.94 | 0.96 | 0.82 |
| contains | long | 620.6 ns ± 86.8 ns | 644.1 ns ± 89.3 ns | 765.1 ns ± 153.4 ns | 693.6 ns ± 45.7 ns | 1.04 | 1.23 | 1.12 |
| contains | bytes | 585.4 ns ± 123.9 ns | 544.5 ns ± 112.0 ns | 684.6 ns ± 105.2 ns | 670.5 ns ± 132.3 ns | 0.93 | 1.17 | 1.15 |
| contains | pair | 1.05 µs ± 172.1 ns | 685.2 ns ± 94.5 ns | 810.0 ns ± 109.4 ns | 736.6 ns ± 144.0 ns | 0.65 | 0.77 | 0.70 |
| enumerateAll | none | 245.04 ms ± 34.29 ms | 212.10 ms ± 10.24 ms | 219.98 ms ± 19.12 ms | 210.56 ms ± 13.66 ms | 0.87 | 0.90 | 0.86 |
| enumerateAll | long | 306.67 ms ± 24.08 ms | 286.72 ms ± 29.31 ms | 261.45 ms ± 24.81 ms | 291.87 ms ± 40.65 ms | 0.93 | 0.85 | 0.95 |
| enumerateAll | bytes | 301.05 ms ± 34.35 ms | 288.67 ms ± 18.49 ms | 405.50 ms ± 55.46 ms | 329.99 ms ± 30.75 ms | 0.96 | 1.35 | 1.10 |
| enumerateAll | pair | 461.12 ms ± 61.10 ms | 618.59 ms ± 69.07 ms | 542.35 ms ± 65.44 ms | 361.09 ms ± 61.36 ms | 1.34 | 1.18 | 0.78 |
| topN | N/A | 36.51 µs ± 3.98 µs | 29.17 µs ± 3.58 µs | 33.58 µs ± 4.79 µs | 38.66 µs ± 5.86 µs | 0.80 | 0.92 | 1.06 |
| construct | none | 1,715.07 ms ± 108.48 ms | 1,754.58 ms ± 199.67 ms | 1,751.99 ms ± 79.63 ms | 1,742.55 ms ± 126.28 ms | 1.02 | 1.02 | 1.02 |
| construct | long | 2,100.10 ms ± 134.90 ms | 2,104.59 ms ± 166.03 ms | 2,054.68 ms ± 145.29 ms | 2,080.57 ms ± 199.18 ms | 1.00 | 0.98 | 0.99 |
| construct | bytes | 2,044.67 ms ± 117.42 ms | 2,017.47 ms ± 127.00 ms | 2,024.99 ms ± 103.64 ms | 1,998.22 ms ± 118.54 ms | 0.99 | 0.99 | 0.98 |
| construct | pair | 2,988.04 ms ± 48.13 ms | 2,952.09 ms ± 191.46 ms | 3,041.95 ms ± 321.92 ms | 2,952.25 ms ± 264.62 ms | 0.99 | 1.02 | 0.99 |

### store=offheap, size=1,000,000 terms: allocation per op (gc.alloc.rate.norm)

| benchmark | outputs | old B/op | lazy B/op | holder B/op | hybrid B/op |
|---|---|---:|---:|---:|---:|
| lookupHit | none | 440.0 B | 448.0 B | 472.0 B | 448.0 B |
| lookupHit | long | 630.9 B | 448.0 B | 488.0 B | 448.0 B |
| lookupHit | bytes | 492.1 B | 499.7 B | 582.5 B | 448.0 B |
| lookupHit | pair | 1.1 KB | 920.5 B | 678.5 B | 448.0 B |
| lookupMiss | none | 440.0 B | 448.0 B | 472.0 B | 448.0 B |
| lookupMiss | long | 599.7 B | 448.0 B | 488.0 B | 448.0 B |
| lookupMiss | bytes | 456.3 B | 448.0 B | 561.6 B | 448.0 B |
| lookupMiss | pair | 803.9 B | 448.0 B | 657.6 B | 448.0 B |
| lookupHitWalk | none | 0.0 B | 0.0 B | 0.0 B | 0.0 B |
| lookupHitWalk | long | 190.9 B | 0.0 B | 0.0 B | 0.0 B |
| lookupHitWalk | bytes | 52.1 B | 51.7 B | 0.0 B | 0.0 B |
| lookupHitWalk | pair | 622.4 B | 472.5 B | 0.0 B | 0.0 B |
| contains | none | 440.0 B | 448.0 B | 472.0 B | 448.0 B |
| contains | long | 630.9 B | 448.0 B | 488.0 B | 448.0 B |
| contains | bytes | 492.1 B | 448.0 B | 582.5 B | 448.0 B |
| contains | pair | 1.1 KB | 448.0 B | 678.5 B | 448.0 B |
| enumerateAll | none | 5.2 KB | 5.2 KB | 5.9 KB | 5.6 KB |
| enumerateAll | long | 48.01 MB | 6.3 KB | 6.7 KB | 6.8 KB |
| enumerateAll | bytes | 51.66 MB | 51.69 MB | 10.8 KB | 7.6 KB |
| enumerateAll | pair | 308.43 MB | 472.73 MB | 14.6 KB | 8.4 KB |
| topN | N/A | 63.5 KB | 14.6 KB | 68.9 KB | 60.1 KB |
| construct | none | 367.50 MB | 367.50 MB | 367.50 MB | 367.50 MB |
| construct | long | 654.17 MB | 637.38 MB | 637.38 MB | 637.38 MB |
| construct | bytes | 508.07 MB | 492.04 MB | 480.70 MB | 492.05 MB |
| construct | pair | 1,811.10 MB | 1,784.65 MB | 1,784.27 MB | 1,784.35 MB |

### 8.3 Summary

| criterion | old | lazy (fst2) | holder (fst3) | hybrid (fst4) |
|---|---|---|---|---|
| allocation, reused-arc walk (none/long/bytes/pair) | 0 / 191 B / 52 B / 622 B | 0 / 0 / 52 B / 473 B | 0 / 0 / 0 / 0 | 0 / 0 / 0 / 0 |
| allocation, full enumeration of 1M terms (long/bytes/pair) | 48 MB / 52 MB / 308 MB | 4.5 KB / 52 MB / 473 MB | 4.9 KB / 8 KB / 11 KB | 4.8 KB / 5.2 KB / 6.3 KB |
| membership and misses decode outputs? | yes | no | yes | no |
| time, pair enumeration vs old (on/off heap) | 1.00 | 1.19 / 1.34 | 1.02 / 1.18 | 0.84 / 0.78 |
| time, bytes enumeration vs old | 1.00 | 1.07 / 0.96 | 1.39 / 1.35 | 1.06 / 1.10 |
| top-N allocation / time vs old | 63 KB / 1.00 | 14 KB / 0.80-0.84 | 68 KB / 0.92-0.98 | 60 KB / 1.06-1.16 |
| new `Outputs` methods required | none | `skipOutput` overrides (+ optional `LongOutputs`) | `newHolder`, `readInto`, `addInto` | `newHolder`, `readAddInto` |
| `Arc` API | generic, boxed values | non-generic, positions | generic, holders | non-generic, positions |
| construction / file format | unchanged | unchanged, byte-identical | unchanged, byte-identical | unchanged, byte-identical |

The hybrid is the only variant that is allocation-free (modulo `Util`'s per-call `Arc` and reader)
for every outputs type on every traversal pattern measured, while keeping the lazy design's ability
to skip decoding entirely. Its remaining gap is the generic top-N searcher, which the lazy design's
primitive specialization beats; that is an implementation detail of `TopNSearcher`, not of the
output representation.
