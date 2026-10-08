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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import org.apache.lucene.util.BytesRef;

/**
 * Deterministic data shared by the FST outputs benchmarks: sorted unique terms, their long and byte
 * outputs, and fixed sets of hit and miss keys.
 */
final class FSTBenchmarkData {
  static final int KEYS = 1 << 14;
  static final int KEYS_MASK = KEYS - 1;

  final BytesRef[] terms;
  final long[] longOutputs;
  final BytesRef[] bytesOutputs;
  final BytesRef[] hits;
  final BytesRef[] misses;

  /** Two-letter prefixes taken from existing terms. */
  final BytesRef[] prefixes;

  private FSTBenchmarkData(
      BytesRef[] terms,
      long[] longOutputs,
      BytesRef[] bytesOutputs,
      BytesRef[] hits,
      BytesRef[] misses,
      BytesRef[] prefixes) {
    this.terms = terms;
    this.longOutputs = longOutputs;
    this.bytesOutputs = bytesOutputs;
    this.hits = hits;
    this.misses = misses;
    this.prefixes = prefixes;
  }

  static FSTBenchmarkData generate(int size, long seed) {
    Random random = new Random(seed);
    TreeSet<BytesRef> set = new TreeSet<>();
    while (set.size() < size) {
      set.add(randomTerm(random));
    }
    BytesRef[] terms = set.toArray(BytesRef[]::new);

    long[] longOutputs = new long[size];
    BytesRef[] bytesOutputs = new BytesRef[size];
    for (int i = 0; i < size; i++) {
      // Deliberately outside the Long.valueOf cache.
      longOutputs[i] = 1 + (random.nextLong() >>> 24);
      byte[] b = new byte[4 + random.nextInt(9)];
      random.nextBytes(b);
      bytesOutputs[i] = new BytesRef(b);
    }

    List<BytesRef> shuffled = new ArrayList<>(List.of(terms));
    Collections.shuffle(shuffled, random);
    BytesRef[] hits = new BytesRef[KEYS];
    for (int i = 0; i < KEYS; i++) {
      hits[i] = shuffled.get(i % shuffled.size());
    }
    BytesRef[] misses = new BytesRef[KEYS];
    for (int i = 0; i < KEYS; i++) {
      BytesRef miss;
      do {
        miss = randomTerm(random);
      } while (set.contains(miss));
      misses[i] = miss;
    }
    BytesRef[] prefixes = new BytesRef[1024];
    for (int i = 0; i < prefixes.length; i++) {
      BytesRef t = terms[random.nextInt(terms.length)];
      prefixes[i] = new BytesRef(t.utf8ToString().substring(0, 2));
    }
    return new FSTBenchmarkData(terms, longOutputs, bytesOutputs, hits, misses, prefixes);
  }

  private static BytesRef randomTerm(Random random) {
    int len = 5 + random.nextInt(11);
    char[] chars = new char[len];
    for (int i = 0; i < len; i++) {
      chars[i] = (char) ('a' + random.nextInt(26));
    }
    return new BytesRef(new String(chars));
  }
}
