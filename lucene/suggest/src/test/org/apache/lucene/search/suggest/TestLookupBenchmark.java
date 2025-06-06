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
package org.apache.lucene.search.suggest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Callable;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.search.suggest.analyzing.BlendedInfixSuggester;
import org.apache.lucene.search.suggest.analyzing.FreeTextSuggester;
import org.apache.lucene.search.suggest.analyzing.FuzzySuggester;
import org.apache.lucene.search.suggest.fst.FSTCompletionLookup;
import org.apache.lucene.search.suggest.fst.WFSTCompletionLookup;
import org.apache.lucene.search.suggest.tst.TSTLookup;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.tests.analysis.MockAnalyzer;
import org.apache.lucene.tests.analysis.MockTokenizer;
import org.apache.lucene.tests.util.LuceneTestCase;
import org.apache.lucene.util.IOUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;

/** Benchmarks tests for implementations of {@link Lookup} interface. */
@Ignore("COMMENT ME TO RUN BENCHMARKS!")
public class TestLookupBenchmark extends LuceneTestCase {
  private final List<Class<? extends Lookup>> benchmarkClasses =
      Arrays.asList(
          FuzzySuggester.class,
          AnalyzingSuggester.class,
          AnalyzingInfixSuggester.class,
          TSTLookup.class,
          FSTCompletionLookup.class,
          WFSTCompletionLookup.class,
          BlendedInfixSuggester.class,
          FreeTextSuggester.class);

  private static final int rounds = 15;
  private static final int warmup = 5;

  private final int num = 7;
  private final boolean onlyMorePopular = false;

  private static final Random random = new Random(0xdeadbeef);

  /** Input term/weight pairs. */
  private static Input[] dictionaryInput;

  /** Benchmark term/weight pairs (randomized order). */
  private static List<Input> benchmarkInput;

  /** Loads terms and frequencies from Wikipedia (cached). */
  @BeforeClass
  public static void setup() throws Exception {
    assert false : "disable assertions before running benchmarks!";
    List<Input> input = readTop50KWiki();
    Collections.shuffle(input, random);
    TestLookupBenchmark.dictionaryInput = input.toArray(new Input[0]);
    Collections.shuffle(input, random);
    TestLookupBenchmark.benchmarkInput = input;
  }

  static final Charset UTF_8 = StandardCharsets.UTF_8;

  /** Collect the multilingual input for benchmarks/ tests. */
  public static List<Input> readTop50KWiki() throws Exception {
    List<Input> input = new ArrayList<>();
    var name = "Top50KWiki.utf8";
    URL resource =
        IOUtils.requireResourceNonNull(TestLookupBenchmark.class.getResource(name), name);

    String line;
    BufferedReader br = new BufferedReader(new InputStreamReader(resource.openStream(), UTF_8));
    while ((line = br.readLine()) != null) {
      int tab = line.indexOf('|');
      assertTrue("No | separator?: " + line, tab >= 0);
      int weight = Integer.parseInt(line.substring(tab + 1));
      String key = line.substring(0, tab);
      input.add(new Input(key, weight));
    }
    br.close();
    return input;
  }

  /** Test construction time. */
  public void testConstructionTime() throws Exception {
    System.err.println("-- construction time");
    for (final Class<? extends Lookup> cls : benchmarkClasses) {
      BenchmarkResult result =
          measure(
              () -> {
                final Lookup lookup = buildLookup(cls, dictionaryInput);
                return lookup.hashCode();
              });

      System.err.printf(
          Locale.ROOT,
          "%-15s input: %d, time[ms]: %s%n",
          cls.getSimpleName(),
          dictionaryInput.length,
          result.average);
    }
  }

  /** Test memory required for the storage. */
  public void testStorageNeeds() throws Exception {
    System.err.println("-- RAM consumption");
    for (Class<? extends Lookup> cls : benchmarkClasses) {
      Lookup lookup = buildLookup(cls, dictionaryInput);
      long sizeInBytes = lookup.ramBytesUsed();
      System.err.printf(
          Locale.ROOT, "%-15s size[B]:%,13d%n", lookup.getClass().getSimpleName(), sizeInBytes);
    }
  }

  /** Create {@link Lookup} instance and populate it. */
  private Lookup buildLookup(Class<? extends Lookup> cls, Input[] input) throws Exception {
    Lookup lookup;
    if (cls == TSTLookup.class
        || cls == FSTCompletionLookup.class
        || cls == WFSTCompletionLookup.class) {
      Constructor<? extends Lookup> ctor = cls.getConstructor(Directory.class, String.class);
      lookup = ctor.newInstance(FSDirectory.open(createTempDir("LookupBenchmarkTest")), "test");
    } else {
      try {
        lookup = cls.getConstructor().newInstance();
      } catch (@SuppressWarnings("unused") InstantiationException | NoSuchMethodException e) {
        Analyzer a = new MockAnalyzer(random, MockTokenizer.KEYWORD, false);
        if (cls == AnalyzingInfixSuggester.class || cls == BlendedInfixSuggester.class) {
          Constructor<? extends Lookup> ctor = cls.getConstructor(Directory.class, Analyzer.class);
          lookup = ctor.newInstance(FSDirectory.open(createTempDir("LookupBenchmarkTest")), a);
        } else if (cls == AnalyzingSuggester.class) {
          lookup =
              new AnalyzingSuggester(
                  FSDirectory.open(createTempDir("LookupBenchmarkTest")), "test", a);
        } else if (cls == FuzzySuggester.class) {
          lookup =
              new FuzzySuggester(FSDirectory.open(createTempDir("LookupBenchmarkTest")), "test", a);
        } else {
          Constructor<? extends Lookup> ctor = cls.getConstructor(Analyzer.class);
          lookup = ctor.newInstance(a);
        }
      }
    }
    lookup.build(new InputArrayIterator(input));
    return lookup;
  }

  /** Test performance of lookup on full hits. */
  public void testPerformanceOnFullHits() throws Exception {
    final int minPrefixLen = 100;
    final int maxPrefixLen = 200;
    runPerformanceTest(minPrefixLen, maxPrefixLen, num, onlyMorePopular);
  }

  /** Test performance of lookup on longer term prefixes (6-9 letters or shorter). */
  public void testPerformanceOnPrefixes6_9() throws Exception {
    final int minPrefixLen = 6;
    final int maxPrefixLen = 9;
    runPerformanceTest(minPrefixLen, maxPrefixLen, num, onlyMorePopular);
  }

  /** Test performance of lookup on short term prefixes (2-4 letters or shorter). */
  public void testPerformanceOnPrefixes2_4() throws Exception {
    final int minPrefixLen = 2;
    final int maxPrefixLen = 4;
    runPerformanceTest(minPrefixLen, maxPrefixLen, num, onlyMorePopular);
  }

  /** Run the actual benchmark. */
  public void runPerformanceTest(
      final int minPrefixLen, final int maxPrefixLen, final int num, final boolean onlyMorePopular)
      throws Exception {
    System.err.printf(
        Locale.ROOT,
        "-- prefixes: %d-%d, num: %d, onlyMorePopular: %s%n",
        minPrefixLen,
        maxPrefixLen,
        num,
        onlyMorePopular);

    for (Class<? extends Lookup> cls : benchmarkClasses) {
      final Lookup lookup = buildLookup(cls, dictionaryInput);

      final List<String> input = new ArrayList<>(benchmarkInput.size());
      for (Input tf : benchmarkInput) {
        String s = tf.term.utf8ToString();
        String sub =
            s.substring(
                0,
                Math.min(
                    s.length(), minPrefixLen + random.nextInt(maxPrefixLen - minPrefixLen + 1)));
        input.add(sub);
      }

      BenchmarkResult result =
          measure(
              () -> {
                int v = 0;
                for (String term : input) {
                  v += lookup.lookup(term, onlyMorePopular, num).size();
                }
                return v;
              });

      System.err.printf(
          Locale.ROOT,
          "%-15s queries: %d, time[ms]: %s, ~kQPS: %.0f%n",
          lookup.getClass().getSimpleName(),
          input.size(),
          result.average,
          input.size() / result.average.avg());
    }
  }

  /** Do the measurements. */
  private BenchmarkResult measure(Callable<Integer> callable) {
    final double NANOS_PER_MS = 1000000;

    try {
      List<Double> times = new ArrayList<>();
      for (int i = 0; i < warmup + rounds; i++) {
        final long start = System.nanoTime();
        guard = callable.call();
        times.add((System.nanoTime() - start) / NANOS_PER_MS);
      }
      return new BenchmarkResult(times, warmup, rounds);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /** Guard against opts. */
  @SuppressWarnings("unused")
  private volatile int guard;

  private static class BenchmarkResult {
    /** Average time per round (ms). */
    public final Average average;

    public BenchmarkResult(List<Double> times, int warmup, int rounds) {
      this.average = Average.from(times.subList(warmup, times.size()));
    }
  }
}
