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

import static org.apache.lucene.util.fst5.FST5Tester.getRandomString;
import static org.apache.lucene.util.fst5.FST5Tester.simpleRandomString;
import static org.apache.lucene.util.fst5.FST5Tester.toIntsRef;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiTerms;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.ByteArrayDataInput;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.OutputStreamDataOutput;
import org.apache.lucene.tests.analysis.MockAnalyzer;
import org.apache.lucene.tests.index.RandomIndexWriter;
import org.apache.lucene.tests.store.MockDirectoryWrapper;
import org.apache.lucene.tests.util.LineFileDocs;
import org.apache.lucene.tests.util.LuceneTestCase;
import org.apache.lucene.tests.util.LuceneTestCase.SuppressCodecs;
import org.apache.lucene.tests.util.TestUtil;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.automaton.Automata;
import org.apache.lucene.util.automaton.Automaton;
import org.apache.lucene.util.automaton.CompiledAutomaton;
import org.apache.lucene.util.fst5.BytesRefFSTEnum.InputOutput;
import org.apache.lucene.util.fst5.FST.Arc;
import org.apache.lucene.util.fst5.FST.BytesReader;
import org.apache.lucene.util.fst5.Util.Result;
import org.junit.Ignore;

@SuppressCodecs({"SimpleText", "Direct"})
public class TestFST5s extends LuceneTestCase {

  private MockDirectoryWrapper dir;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    dir = newMockDirectory();
  }

  @Override
  public void tearDown() throws Exception {
    // can be null if we force simpletext (funky, some kind of bug in test runner maybe)
    if (dir != null) {
      dir.close();
    }
    super.tearDown();
  }

  public void testBasicFSA() throws IOException {
    String[] strings =
        new String[] {
          "station", "commotion", "elation", "elastic", "plastic", "stop", "ftop", "ftation", "stat"
        };
    String[] strings2 =
        new String[] {
          "station", "commotion", "elation", "elastic", "plastic", "stop", "ftop", "ftation"
        };
    IntsRef[] terms = new IntsRef[strings.length];
    IntsRef[] terms2 = new IntsRef[strings2.length];
    for (int inputMode = 0; inputMode < 2; inputMode++) {
      if (VERBOSE) {
        System.out.println("TEST: inputMode=" + inputModeToString(inputMode));
      }

      for (int idx = 0; idx < strings.length; idx++) {
        terms[idx] = toIntsRef(strings[idx], inputMode);
      }
      for (int idx = 0; idx < strings2.length; idx++) {
        terms2[idx] = toIntsRef(strings2[idx], inputMode);
      }
      Arrays.sort(terms2);

      doTest(inputMode, terms);

      // Test pre-determined FST sizes to make sure we haven't lost minimality (at least on this
      // trivial set of terms):

      // FSA (all outputs zero)
      {
        final List<FST5Tester.InputOutput> pairs = new ArrayList<>(terms2.length);
        for (IntsRef term : terms2) {
          pairs.add(new FST5Tester.InputOutput(term, FST.NO_OUTPUT));
        }
        FST5Tester tester = new FST5Tester(random(), dir, inputMode, pairs);
        FST fst = tester.doTest();
        assertNotNull(fst);
        assertEquals(22, tester.nodeCount);
        assertEquals(27, tester.arcCount);
      }

      // FST ord pos int
      {
        final List<FST5Tester.InputOutput> pairs = new ArrayList<>(terms2.length);
        for (int idx = 0; idx < terms2.length; idx++) {
          pairs.add(new FST5Tester.InputOutput(terms2[idx], idx));
        }
        FST5Tester tester = new FST5Tester(random(), dir, inputMode, pairs);
        final FST fst = tester.doTest();
        assertNotNull(fst);
        assertEquals(22, tester.nodeCount);
        assertEquals(27, tester.arcCount);
      }
    }
  }

  // given set of terms, test the different outputs for them
  private void doTest(int inputMode, IntsRef[] terms) throws IOException {
    Arrays.sort(terms);

    // All zero outputs (simple FSA)
    {
      final List<FST5Tester.InputOutput> pairs = new ArrayList<>(terms.length);
      for (IntsRef term : terms) {
        pairs.add(new FST5Tester.InputOutput(term, FST.NO_OUTPUT));
      }
      new FST5Tester(random(), dir, inputMode, pairs).doTest();
    }

    // ord
    {
      final List<FST5Tester.InputOutput> pairs = new ArrayList<>(terms.length);
      for (int idx = 0; idx < terms.length; idx++) {
        pairs.add(new FST5Tester.InputOutput(terms[idx], idx));
      }
      new FST5Tester(random(), dir, inputMode, pairs).doTest();
    }

    // random monotonically increasing positive number
    {
      final List<FST5Tester.InputOutput> pairs = new ArrayList<>(terms.length);
      long lastOutput = 0;
      for (IntsRef term : terms) {
        final long value = lastOutput + TestUtil.nextInt(random(), 1, 1000);
        lastOutput = value;
        pairs.add(new FST5Tester.InputOutput(term, value));
      }
      new FST5Tester(random(), dir, inputMode, pairs).doTest();
    }

    // random positive number
    {
      final List<FST5Tester.InputOutput> pairs = new ArrayList<>(terms.length);
      for (IntsRef term : terms) {
        pairs.add(new FST5Tester.InputOutput(term, TestUtil.nextLong(random(), 0, Long.MAX_VALUE)));
      }
      new FST5Tester(random(), dir, inputMode, pairs).doTest();
    }

    // mostly zero, sometimes a value (sparse outputs)
    {
      final List<FST5Tester.InputOutput> pairs = new ArrayList<>(terms.length);
      for (IntsRef term : terms) {
        pairs.add(
            new FST5Tester.InputOutput(
                term, random().nextInt(5) == 0 ? TestUtil.nextInt(random(), 1, 100) : 0));
      }
      new FST5Tester(random(), dir, inputMode, pairs).doTest();
    }
  }

  public void testRandomWords() throws IOException {
    if (TEST_NIGHTLY) {
      testRandomWords(1000, atLeast(2));
    } else {
      testRandomWords(100, 1);
    }
  }

  String inputModeToString(int mode) {
    if (mode == 0) {
      return "utf8";
    } else {
      return "utf32";
    }
  }

  private void testRandomWords(int maxNumWords, int numIter) throws IOException {
    Random random = new Random(random().nextLong());
    for (int iter = 0; iter < numIter; iter++) {
      if (VERBOSE) {
        System.out.println("\nTEST: iter " + iter);
      }
      for (int inputMode = 0; inputMode < 2; inputMode++) {
        final int numWords = random.nextInt(maxNumWords + 1);
        Set<IntsRef> termsSet = new HashSet<>();
        while (termsSet.size() < numWords) {
          final String term = getRandomString(random);
          termsSet.add(toIntsRef(term, inputMode));
        }
        doTest(inputMode, termsSet.toArray(IntsRef[]::new));
      }
    }
  }

  @Nightly
  public void testBigSet() throws IOException {
    testRandomWords(TestUtil.nextInt(random(), 50000, 60000), 1);
  }

  // Build FST for all unique terms in the test line docs
  // file, up until a doc limit
  public void testRealTerms() throws Exception {

    final LineFileDocs docs = new LineFileDocs(random());
    final int numDocs = TEST_NIGHTLY ? atLeast(1000) : atLeast(50);
    MockAnalyzer analyzer = new MockAnalyzer(random());
    analyzer.setMaxTokenLength(TestUtil.nextInt(random(), 1, IndexWriter.MAX_TERM_LENGTH));

    final IndexWriterConfig conf =
        newIndexWriterConfig(analyzer).setMaxBufferedDocs(-1).setRAMBufferSizeMB(64);
    final Path tempDir = createTempDir("fstlines");
    final Directory dir = newFSDirectory(tempDir);
    final IndexWriter writer = new IndexWriter(dir, conf);
    Document doc;
    int docCount = 0;
    while ((doc = docs.nextDoc()) != null && docCount < numDocs) {
      writer.addDocument(doc);
      docCount++;
    }
    IndexReader r = DirectoryReader.open(writer);
    writer.close();

    FSTCompiler.Builder builder = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1);

    double suffixRAMLimitMB;

    if (random().nextInt(10) == 4) {
      // no suffix sharing
      suffixRAMLimitMB = 0;
    } else if (random().nextInt(10) == 7) {
      // share all suffixes (minimal FST)
      suffixRAMLimitMB = Double.POSITIVE_INFINITY;
    } else {
      suffixRAMLimitMB = (random().nextDouble() + 0.01) * 10.0;
    }
    builder.suffixRAMLimitMB(suffixRAMLimitMB);

    FSTCompiler fstCompiler = builder.build();

    boolean storeOrd = random().nextBoolean();
    if (VERBOSE) {
      if (storeOrd) {
        System.out.println("FST stores ord");
      } else {
        System.out.println("FST stores docFreq");
      }
    }
    Terms terms = MultiTerms.getTerms(r, "body");
    if (terms != null) {
      final IntsRefBuilder scratchIntsRef = new IntsRefBuilder();
      final TermsEnum termsEnum = terms.iterator();
      if (VERBOSE) {
        System.out.println("TEST: got termsEnum=" + termsEnum);
      }
      BytesRef term;
      int ord = 0;

      Automaton automaton = Automata.makeAnyString();
      final TermsEnum termsEnum2 =
          terms.intersect(new CompiledAutomaton(automaton, false, false), null);

      while ((term = termsEnum.next()) != null) {
        BytesRef term2 = termsEnum2.next();
        assertNotNull(term2);
        assertEquals(term, term2);
        assertEquals(termsEnum.docFreq(), termsEnum2.docFreq());
        assertEquals(termsEnum.totalTermFreq(), termsEnum2.totalTermFreq());

        if (ord == 0) {
          try {
            termsEnum.ord();
          } catch (UnsupportedOperationException _) {
            if (VERBOSE) {
              System.out.println("TEST: codec doesn't support ord; FST stores docFreq");
            }
            storeOrd = false;
          }
        }
        final int output;
        if (storeOrd) {
          output = ord;
        } else {
          output = termsEnum.docFreq();
        }
        fstCompiler.add(Util.toIntsRef(term, scratchIntsRef), output);
        ord++;
        if (VERBOSE && ord % 100000 == 0 && LuceneTestCase.TEST_NIGHTLY) {
          System.out.println(ord + " terms...");
        }
      }
      FST fst = FST.fromFSTReader(fstCompiler.compile(), fstCompiler.getFSTReader());
      if (VERBOSE) {
        System.out.println(
            "FST: "
                + docCount
                + " docs; "
                + ord
                + " terms; "
                + fstCompiler.getNodeCount()
                + " nodes; "
                + fstCompiler.getArcCount()
                + " arcs;"
                + " "
                + fst.ramBytesUsed()
                + " bytes");
      }

      if (ord > 0) {
        final Random random = new Random(random().nextLong());
        // Now confirm BytesRefFSTEnum and TermsEnum act the
        // same:
        final BytesRefFSTEnum fstEnum = new BytesRefFSTEnum(fst);
        int num = atLeast(1000);
        for (int iter = 0; iter < num; iter++) {
          final BytesRef randomTerm = newBytesRef(getRandomString(random));

          if (VERBOSE) {
            System.out.println(
                "TEST: seek non-exist " + randomTerm.utf8ToString() + " " + randomTerm);
          }

          final TermsEnum.SeekStatus seekResult = termsEnum.seekCeil(randomTerm);
          final InputOutput fstSeekResult = fstEnum.seekCeil(randomTerm);

          if (seekResult == TermsEnum.SeekStatus.END) {
            assertNull(
                "got "
                    + (fstSeekResult == null ? "null" : fstSeekResult.input.utf8ToString())
                    + " but expected null",
                fstSeekResult);
          } else {
            assertSame(termsEnum, fstEnum, storeOrd);
            for (int nextIter = 0; nextIter < 10; nextIter++) {
              if (VERBOSE) {
                System.out.println("TEST: next");
                if (storeOrd) {
                  System.out.println("  ord=" + termsEnum.ord());
                }
              }
              if (termsEnum.next() != null) {
                if (VERBOSE) {
                  System.out.println("  term=" + termsEnum.term().utf8ToString());
                }
                assertNotNull(fstEnum.next());
                assertSame(termsEnum, fstEnum, storeOrd);
              } else {
                if (VERBOSE) {
                  System.out.println("  end!");
                }
                BytesRefFSTEnum.InputOutput nextResult = fstEnum.next();
                if (nextResult != null) {
                  System.out.println(
                      "expected null but got: input="
                          + nextResult.input.utf8ToString()
                          + " output="
                          + nextResult.output);
                  fail();
                }
                break;
              }
            }
          }
        }
      }
    }

    r.close();
    dir.close();
  }

  private void assertSame(TermsEnum termsEnum, BytesRefFSTEnum fstEnum, boolean storeOrd)
      throws Exception {
    if (termsEnum.term() == null) {
      assertNull(fstEnum.current());
    } else {
      assertNotNull(fstEnum.current());
      assertEquals(
          termsEnum.term().utf8ToString() + " != " + fstEnum.current().input.utf8ToString(),
          termsEnum.term(),
          fstEnum.current().input);
      if (storeOrd) {
        // fst stored the ord
        assertEquals(
            "term=" + termsEnum.term().utf8ToString() + " " + termsEnum.term(),
            termsEnum.ord(),
            fstEnum.current().output);
      } else {
        // fst stored the docFreq
        assertEquals(
            "term=" + termsEnum.term().utf8ToString() + " " + termsEnum.term(),
            termsEnum.docFreq(),
            (int) fstEnum.current().output);
      }
    }
  }

  private abstract static class VisitTerms {
    private final Path dirOut;
    private final Path wordsFileIn;
    private final int inputMode;
    private final FSTCompiler fstCompiler;

    public VisitTerms(Path dirOut, Path wordsFileIn, int inputMode, boolean noArcArrays)
        throws IOException {
      this.dirOut = dirOut;
      this.wordsFileIn = wordsFileIn;
      this.inputMode = inputMode;

      fstCompiler =
          new FSTCompiler.Builder(inputMode == 0 ? FST.INPUT_TYPE.BYTE1 : FST.INPUT_TYPE.BYTE4)
              .allowFixedLengthArcs(!noArcArrays)
              .build();
    }

    protected abstract long getOutput(IntsRef input, int ord) throws IOException;

    public void run(int limit, boolean verify) throws IOException {

      BufferedReader is = Files.newBufferedReader(wordsFileIn, StandardCharsets.UTF_8);
      try {
        final IntsRefBuilder intsRefBuilder = new IntsRefBuilder();
        long tStart = System.nanoTime();
        int ord = 0;
        while (true) {
          String w = is.readLine();
          if (w == null) {
            break;
          }
          toIntsRef(w, inputMode, intsRefBuilder);
          fstCompiler.add(intsRefBuilder.get(), getOutput(intsRefBuilder.get(), ord));

          ord++;
          if (ord % 500000 == 0) {
            System.out.printf(
                Locale.ROOT,
                "%6.2fs: %9d...",
                (System.nanoTime() - tStart) / (double) TimeUnit.SECONDS.toNanos(1),
                ord);
          }
          if (ord >= limit) {
            break;
          }
        }

        long tMid = System.nanoTime();
        System.out.println(
            ((tMid - tStart) / (double) TimeUnit.SECONDS.toNanos(1)) + " sec to add all terms");

        FST fst = FST.fromFSTReader(fstCompiler.compile(), fstCompiler.getFSTReader());
        long tEnd = System.nanoTime();
        System.out.println(
            ((tEnd - tMid) / (double) TimeUnit.SECONDS.toNanos(1)) + " sec to finish/pack");

        if (dirOut == null) {
          return;
        }

        System.out.println(
            ord
                + " terms; "
                + fstCompiler.getNodeCount()
                + " nodes; "
                + fstCompiler.getArcCount()
                + " arcs; tot size "
                + fst.ramBytesUsed());
        if (fstCompiler.getNodeCount() < 100) {
          Writer w = Files.newBufferedWriter(Paths.get("out.dot"), StandardCharsets.UTF_8);
          Util.toDot(fst, w, false, false);
          w.close();
          System.out.println("Wrote FST to out.dot");
        }

        Directory dir = FSDirectory.open(dirOut);
        IndexOutput out = dir.createOutput("fst.bin", IOContext.DEFAULT);
        fst.save(out, out);
        out.close();
        System.out.println("Saved FST to fst.bin.");

        if (!verify) {
          return;
        }

        System.out.println("\nNow verify...");

        is.close();
        is = Files.newBufferedReader(wordsFileIn, StandardCharsets.UTF_8);

        ord = 0;
        tStart = System.nanoTime();
        while (true) {
          String w = is.readLine();
          if (w == null) {
            break;
          }
          toIntsRef(w, inputMode, intsRefBuilder);
          long expected = getOutput(intsRefBuilder.get(), ord);
          long actual = Util.get(fst, intsRefBuilder.get());
          if (actual == Util.NOT_FOUND) {
            throw new RuntimeException("unexpected missing output on input=" + w);
          }
          if (actual != expected) {
            throw new RuntimeException(
                "wrong output (got " + actual + " but expected " + expected + ") on input=" + w);
          }
          ord++;
          if (ord % 500000 == 0) {
            System.out.println(
                (System.nanoTime() - tStart) / (double) TimeUnit.SECONDS.toNanos(1)
                    + "sec: "
                    + ord
                    + "...");
          }
          if (ord >= limit) {
            break;
          }
        }

        double totSec = (System.nanoTime() - tStart) / (double) TimeUnit.SECONDS.toNanos(1);
        System.out.println(
            "Verify took "
                + totSec
                + " sec + ("
                + (int) ((totSec * 1000000000 / ord))
                + " nsec per lookup)");

      } finally {
        is.close();
      }
    }
  }

  // java -cp ... org.apache.lucene.util.fst5.TestFST5s /xold/tmp/allTerms3.txt out
  public static void main(String[] args) throws IOException {
    int limit = Integer.MAX_VALUE;
    int inputMode = 0; // utf8
    boolean storeOrds = false;
    boolean storeDocFreqs = false;
    boolean verify = true;
    boolean noArcArrays = false;
    Path wordsFileIn = null;
    Path dirOut = null;

    int idx = 0;
    while (idx < args.length) {
      if (args[idx].equals("-limit")) {
        limit = Integer.parseInt(args[1 + idx]);
        idx++;
      } else if (args[idx].equals("-utf8")) {
        inputMode = 0;
      } else if (args[idx].equals("-utf32")) {
        inputMode = 1;
      } else if (args[idx].equals("-docFreq")) {
        storeDocFreqs = true;
      } else if (args[idx].equals("-noArcArrays")) {
        noArcArrays = true;
      } else if (args[idx].equals("-ords")) {
        storeOrds = true;
      } else if (args[idx].equals("-noverify")) {
        verify = false;
      } else if (args[idx].startsWith("-")) {
        System.err.println("Unrecognized option: " + args[idx]);
        System.exit(-1);
      } else {
        if (wordsFileIn == null) {
          wordsFileIn = Paths.get(args[idx]);
        } else if (dirOut == null) {
          dirOut = Paths.get(args[idx]);
        } else {
          System.err.println("Too many arguments, expected: input [output]");
          System.exit(-1);
        }
      }
      idx++;
    }

    if (wordsFileIn == null) {
      System.err.println("No input file.");
      System.exit(-1);
    }

    // ord benefits from share, docFreqs don't:

    if (storeOrds) {
      // Store only ords
      new VisitTerms(dirOut, wordsFileIn, inputMode, noArcArrays) {
        @Override
        public long getOutput(IntsRef input, int ord) {
          return ord;
        }
      }.run(limit, verify);
    } else if (storeDocFreqs) {
      // Store only docFreq
      new VisitTerms(dirOut, wordsFileIn, inputMode, noArcArrays) {
        Random rand;

        @Override
        public long getOutput(IntsRef input, int ord) {
          if (ord == 0) {
            rand = new Random(17);
          }
          return TestUtil.nextInt(rand, 1, 5000);
        }
      }.run(limit, verify);
    } else {
      // Store nothing
      new VisitTerms(dirOut, wordsFileIn, inputMode, noArcArrays) {
        @Override
        public long getOutput(IntsRef input, int ord) {
          return FST.NO_OUTPUT;
        }
      }.run(limit, verify);
    }
  }

  public void testSingleString() throws Exception {
    final FSTCompiler fstCompiler = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1).build();
    fstCompiler.add(Util.toIntsRef(newBytesRef("foobar"), new IntsRefBuilder()), FST.NO_OUTPUT);
    final BytesRefFSTEnum fstEnum =
        new BytesRefFSTEnum(FST.fromFSTReader(fstCompiler.compile(), fstCompiler.getFSTReader()));
    assertNull(fstEnum.seekFloor(newBytesRef("foo")));
    assertNull(fstEnum.seekCeil(newBytesRef("foobaz")));
  }

  public void testDuplicateFSAString() throws Exception {
    String str = "foobar";
    final FSTCompiler fstCompiler = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1).build();
    IntsRefBuilder ints = new IntsRefBuilder();
    for (int i = 0; i < 10; i++) {
      fstCompiler.add(Util.toIntsRef(newBytesRef(str), ints), FST.NO_OUTPUT);
    }
    FST fst = FST.fromFSTReader(fstCompiler.compile(), fstCompiler.getFSTReader());

    // count the input paths
    int count = 0;
    final BytesRefFSTEnum fstEnum = new BytesRefFSTEnum(fst);
    while (fstEnum.next() != null) {
      count++;
    }
    assertEquals(1, count);

    assertNotEquals(Util.NOT_FOUND, Util.get(fst, newBytesRef(str)));
    assertEquals(Util.NOT_FOUND, Util.get(fst, newBytesRef("foobaz")));
  }

  /** The same input twice with the same output is fine; with a different output it is not. */
  public void testDuplicateInputDifferentOutput() throws Exception {
    final FSTCompiler fstCompiler = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1).build();
    IntsRefBuilder ints = new IntsRefBuilder();
    fstCompiler.add(Util.toIntsRef(newBytesRef("foo"), ints), 5);
    fstCompiler.add(Util.toIntsRef(newBytesRef("foo"), ints), 5);
    expectThrows(
        UnsupportedOperationException.class,
        () -> fstCompiler.add(Util.toIntsRef(newBytesRef("foo"), ints), 6));

    final FSTCompiler emptyCompiler = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1).build();
    emptyCompiler.add(new IntsRef(), 5);
    emptyCompiler.add(new IntsRef(), 5);
    expectThrows(UnsupportedOperationException.class, () -> emptyCompiler.add(new IntsRef(), 6));
  }

  /** The empty input with a non-zero output must round trip through save/load. */
  public void testEmptyInputWithOutput() throws Exception {
    final FSTCompiler fstCompiler = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1).build();
    IntsRefBuilder ints = new IntsRefBuilder();
    fstCompiler.add(new IntsRef(), 5);
    fstCompiler.add(Util.toIntsRef(newBytesRef("a"), ints), 7);
    FST fst = FST.fromFSTReader(fstCompiler.compile(), fstCompiler.getFSTReader());
    assertTrue(fst.acceptsEmptyInput());
    assertEquals(5, fst.getEmptyOutput());
    assertEquals(5, Util.get(fst, new BytesRef()));
    assertEquals(7, Util.get(fst, newBytesRef("a")));
    assertTrue(Util.contains(fst, new BytesRef()));

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    OutputStreamDataOutput out = new OutputStreamDataOutput(os);
    fst.save(out, out);
    ByteArrayDataInput in = new ByteArrayDataInput(os.toByteArray());
    FST loaded = new FST(FST.readMetadata(in), in);
    assertTrue(loaded.acceptsEmptyInput());
    assertEquals(5, loaded.getEmptyOutput());
    assertEquals(5, Util.get(loaded, new BytesRef()));
    assertEquals(7, Util.get(loaded, newBytesRef("a")));

    BytesRefFSTEnum fstEnum = new BytesRefFSTEnum(loaded);
    InputOutput io = fstEnum.next();
    assertEquals(0, io.input.length);
    assertEquals(5, io.output);
    io = fstEnum.next();
    assertEquals(newBytesRef("a"), io.input);
    assertEquals(7, io.output);
    assertNull(fstEnum.next());
  }

  public void testSimple() throws Exception {

    // Build an FST mapping BytesRef -> Long
    final FSTCompiler fstCompiler = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1).build();

    final BytesRef a = newBytesRef("a");
    final BytesRef b = newBytesRef("b");
    final BytesRef c = newBytesRef("c");

    fstCompiler.add(Util.toIntsRef(a, new IntsRefBuilder()), 17L);
    fstCompiler.add(Util.toIntsRef(b, new IntsRefBuilder()), 42L);
    fstCompiler.add(Util.toIntsRef(c, new IntsRefBuilder()), 13824324872317238L);

    final FST fst = FST.fromFSTReader(fstCompiler.compile(), fstCompiler.getFSTReader());

    assertEquals(13824324872317238L, Util.get(fst, c));
    assertEquals(42, Util.get(fst, b));
    assertEquals(17, Util.get(fst, a));
    assertEquals(Util.NOT_FOUND, Util.get(fst, newBytesRef("d")));
    assertFalse(Util.contains(fst, newBytesRef("d")));

    BytesRefFSTEnum fstEnum = new BytesRefFSTEnum(fst);
    BytesRefFSTEnum.InputOutput seekResult;
    seekResult = fstEnum.seekFloor(a);
    assertNotNull(seekResult);
    assertEquals(17, seekResult.output);

    // goes to a
    seekResult = fstEnum.seekFloor(newBytesRef("aa"));
    assertNotNull(seekResult);
    assertEquals(17, seekResult.output);

    // goes to b
    seekResult = fstEnum.seekCeil(newBytesRef("aa"));
    assertNotNull(seekResult);
    assertEquals(b, seekResult.input);
    assertEquals(42, seekResult.output);
  }

  public void testPrimaryKeys() throws Exception {
    Directory dir = newDirectory();

    for (int cycle = 0; cycle < 2; cycle++) {
      if (VERBOSE) {
        System.out.println("TEST: cycle=" + cycle);
      }
      RandomIndexWriter w =
          new RandomIndexWriter(
              random(),
              dir,
              newIndexWriterConfig(new MockAnalyzer(random()))
                  .setOpenMode(IndexWriterConfig.OpenMode.CREATE));
      Document doc = new Document();
      Field idField = newStringField("id", "", Field.Store.NO);
      doc.add(idField);

      final int NUM_IDS = atLeast(200);
      if (VERBOSE) {
        System.out.println("TEST: NUM_IDS=" + NUM_IDS);
      }
      final Set<String> allIDs = new HashSet<>();
      for (int id = 0; id < NUM_IDS; id++) {
        String idString;
        if (cycle == 0) {
          // PKs are assigned sequentially
          idString = String.format(Locale.ROOT, "%07d", id);
        } else {
          while (true) {
            final String s = Long.toString(random().nextLong());
            if (!allIDs.contains(s)) {
              idString = s;
              break;
            }
          }
        }
        allIDs.add(idString);
        idField.setStringValue(idString);
        w.addDocument(doc);
      }

      // turn writer into reader:
      final IndexReader r = w.getReader();
      final IndexSearcher s = newSearcher(r);
      w.close();

      final List<String> allIDsList = new ArrayList<>(allIDs);
      final List<String> sortedAllIDsList = new ArrayList<>(allIDsList);
      Collections.sort(sortedAllIDsList);

      // Sprinkle in some non-existent PKs:
      Set<String> outOfBounds = new HashSet<>();
      for (int idx = 0; idx < NUM_IDS / 10; idx++) {
        String idString;
        if (cycle == 0) {
          idString = String.format(Locale.ROOT, "%07d", (NUM_IDS + idx));
        } else {
          do {
            idString = Long.toString(random().nextLong());
          } while (allIDs.contains(idString));
        }
        outOfBounds.add(idString);
        allIDsList.add(idString);
      }

      // Verify w/ TermQuery
      for (int iter = 0; iter < 2 * NUM_IDS; iter++) {
        final String id = allIDsList.get(random().nextInt(allIDsList.size()));
        final boolean exists = !outOfBounds.contains(id);
        if (VERBOSE) {
          System.out.println("TEST: TermQuery " + (exists ? "" : "non-exist ") + " id=" + id);
        }
        assertEquals(
            (exists ? "" : "non-exist ") + "id=" + id,
            exists ? 1 : 0,
            s.count(new TermQuery(new Term("id", id))));
      }

      // Verify w/ MultiTermsEnum
      final TermsEnum termsEnum = MultiTerms.getTerms(r, "id").iterator();
      for (int iter = 0; iter < 2 * NUM_IDS; iter++) {
        final String id;
        final String nextID;
        final boolean exists;

        if (random().nextBoolean()) {
          id = allIDsList.get(random().nextInt(allIDsList.size()));
          exists = !outOfBounds.contains(id);
          nextID = null;
          if (VERBOSE) {
            System.out.println("TEST: exactOnly " + (exists ? "" : "non-exist ") + "id=" + id);
          }
        } else {
          // Pick ID between two IDs:
          exists = false;
          final int idv = random().nextInt(NUM_IDS - 1);
          if (cycle == 0) {
            id = String.format(Locale.ROOT, "%07da", idv);
            nextID = String.format(Locale.ROOT, "%07d", idv + 1);
          } else {
            id = sortedAllIDsList.get(idv) + "a";
            nextID = sortedAllIDsList.get(idv + 1);
          }
          if (VERBOSE) {
            System.out.println("TEST: not exactOnly id=" + id + " nextID=" + nextID);
          }
        }

        final TermsEnum.SeekStatus status;
        if (nextID == null) {
          if (termsEnum.seekExact(newBytesRef(id))) {
            status = TermsEnum.SeekStatus.FOUND;
          } else {
            status = TermsEnum.SeekStatus.NOT_FOUND;
          }
        } else {
          status = termsEnum.seekCeil(newBytesRef(id));
        }

        if (nextID != null) {
          assertEquals(TermsEnum.SeekStatus.NOT_FOUND, status);
          assertEquals(
              "expected=" + nextID + " actual=" + termsEnum.term().utf8ToString(),
              newBytesRef(nextID),
              termsEnum.term());
        } else if (!exists) {
          assertEquals(TermsEnum.SeekStatus.NOT_FOUND, status);
        } else {
          assertEquals(TermsEnum.SeekStatus.FOUND, status);
        }
      }

      r.close();
    }
    dir.close();
  }

  public void testRandomTermLookup() throws Exception {
    Directory dir = newDirectory();

    RandomIndexWriter w =
        new RandomIndexWriter(
            random(),
            dir,
            newIndexWriterConfig(new MockAnalyzer(random()))
                .setOpenMode(IndexWriterConfig.OpenMode.CREATE));
    Document doc = new Document();
    Field f = newStringField("field", "", Field.Store.NO);
    doc.add(f);

    final int NUM_TERMS = (int) (1000 * RANDOM_MULTIPLIER * (1 + random().nextDouble()));
    if (VERBOSE) {
      System.out.println("TEST: NUM_TERMS=" + NUM_TERMS);
    }

    final Set<String> allTerms = new HashSet<>();
    while (allTerms.size() < NUM_TERMS) {
      allTerms.add(simpleRandomString(random()));
    }

    for (String term : allTerms) {
      f.setStringValue(term);
      w.addDocument(doc);
    }

    // turn writer into reader:
    if (VERBOSE) {
      System.out.println("TEST: get reader");
    }
    IndexReader r = w.getReader();
    if (VERBOSE) {
      System.out.println("TEST: got reader=" + r);
    }
    IndexSearcher s = newSearcher(r);
    w.close();

    final List<String> allTermsList = new ArrayList<>(allTerms);
    Collections.shuffle(allTermsList, random());

    // verify exact lookup
    for (String term : allTermsList) {
      if (VERBOSE) {
        System.out.println("TEST: term=" + term);
      }
      assertEquals("term=" + term, 1, s.count(new TermQuery(new Term("field", term))));
    }

    r.close();
    dir.close();
  }

  /**
   * Test state expansion (array format) on close-to-root states. Creates synthetic input that has
   * one expanded state on each level.
   *
   * @see <a href="https://issues.apache.org/jira/browse/LUCENE-2933">LUCENE-2933</a>
   */
  public void testExpandedCloseToRoot() throws Exception {
    class SyntheticData {
      FST compile(String[] lines) throws IOException {
        final FSTCompiler fstCompiler = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1).build();

        int line = 0;
        final BytesRefBuilder term = new BytesRefBuilder();
        final IntsRefBuilder scratchIntsRef = new IntsRefBuilder();
        while (line < lines.length) {
          String w = lines[line++];
          if (w == null) {
            break;
          }
          term.copyChars(w);
          fstCompiler.add(Util.toIntsRef(term.get(), scratchIntsRef), FST.NO_OUTPUT);
        }

        return FST.fromFSTReader(fstCompiler.compile(), fstCompiler.getFSTReader());
      }

      void generate(ArrayList<String> out, StringBuilder b, char from, char to, int depth) {
        if (depth == 0 || from == to) {
          String seq = b.toString() + "_" + out.size() + "_end";
          out.add(seq);
        } else {
          for (char c = from; c <= to; c++) {
            b.append(c);
            generate(out, b, from, c == to ? to : from, depth - 1);
            b.deleteCharAt(b.length() - 1);
          }
        }
      }

      public int verifyStateAndBelow(FST fst, Arc arc, int depth) throws IOException {
        if (FST.targetHasArcs(arc)) {
          int childCount = 0;
          BytesReader fstReader = fst.getBytesReader();
          for (arc = fst.readFirstTargetArc(arc, arc, fstReader);
              ;
              arc = fst.readNextArc(arc, fstReader), childCount++) {
            boolean expanded = fst.isExpandedTarget(arc, fstReader);
            int children = verifyStateAndBelow(fst, new FST.Arc().copyFrom(arc), depth + 1);

            assertEquals(
                (depth <= FSTCompiler.FIXED_LENGTH_ARC_SHALLOW_DEPTH
                        && children >= FSTCompiler.FIXED_LENGTH_ARC_SHALLOW_NUM_ARCS)
                    || children >= FSTCompiler.FIXED_LENGTH_ARC_DEEP_NUM_ARCS,
                expanded);
            if (arc.isLast()) break;
          }

          return childCount;
        }
        return 0;
      }
    }

    // Sanity check.
    assertTrue(
        FSTCompiler.FIXED_LENGTH_ARC_SHALLOW_NUM_ARCS < FSTCompiler.FIXED_LENGTH_ARC_DEEP_NUM_ARCS);
    assertTrue(FSTCompiler.FIXED_LENGTH_ARC_SHALLOW_DEPTH >= 0);

    SyntheticData s = new SyntheticData();

    ArrayList<String> out = new ArrayList<>();
    StringBuilder b = new StringBuilder();
    s.generate(out, b, 'a', 'i', 10);
    String[] input = out.toArray(String[]::new);
    Arrays.sort(input);
    FST fst = s.compile(input);
    FST.Arc arc = fst.getFirstArc(new FST.Arc());
    s.verifyStateAndBelow(fst, arc, 1);
  }

  @Ignore("not sure it's possible to get a final state output anymore w/o pruning?")
  public void testFinalOutputOnEndState() throws Exception {
    final FSTCompiler fstCompiler = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE4).build();
    fstCompiler.add(Util.toUTF32("slat", new IntsRefBuilder()), 10L);
    fstCompiler.add(Util.toUTF32("st", new IntsRefBuilder()), 17L);
    final FST fst = FST.fromFSTReader(fstCompiler.compile(), fstCompiler.getFSTReader());
    StringWriter w = new StringWriter();
    Util.toDot(fst, w, false, false);
    w.close();
    System.out.println(w.toString());
    assertTrue(w.toString().contains("label=\"t/[7]\""));
  }

  public void testInternalFinalState() throws Exception {
    final FSTCompiler fstCompiler = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1).build();
    fstCompiler.add(Util.toIntsRef(newBytesRef("stat"), new IntsRefBuilder()), FST.NO_OUTPUT);
    fstCompiler.add(Util.toIntsRef(newBytesRef("station"), new IntsRefBuilder()), FST.NO_OUTPUT);
    final FST fst = FST.fromFSTReader(fstCompiler.compile(), fstCompiler.getFSTReader());
    StringWriter w = new StringWriter();
    Util.toDot(fst, w, false, false);
    w.close();

    // check for accept state at label t
    assertTrue(w.toString().contains("[label=\"t\" style=\"bold\""));
    // check for accept state at label n
    assertTrue(w.toString().contains("[label=\"n\" style=\"bold\""));
  }

  // https://github.com/apache/lucene/issues/12697
  // Make sure the FST can be saved and loaded with different DataOutput for metadata
  public void testSaveDifferentMetaOut() throws Exception {
    FSTCompiler fstCompiler = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1).build();

    // first build the FST from scratch
    final IntsRefBuilder scratch = new IntsRefBuilder();
    fstCompiler.add(Util.toIntsRef(newBytesRef("aab"), scratch), 22L);
    fstCompiler.add(Util.toIntsRef(newBytesRef("aac"), scratch), 7L);
    fstCompiler.add(Util.toIntsRef(newBytesRef("ax"), scratch), 17L);

    FST fst = FST.fromFSTReader(fstCompiler.compile(), fstCompiler.getFSTReader());

    // save the FST to DataOutput, here it would not matter whether we are saving to different
    // DataOutput for meta or not
    ByteArrayOutputStream outOS = new ByteArrayOutputStream();
    OutputStreamDataOutput out = new OutputStreamDataOutput(outOS);
    fst.save(out, out);

    // load the FST, which will force it to use FSTStore instead of BytesStore
    ByteArrayDataInput in = new ByteArrayDataInput(outOS.toByteArray());
    FST loadedFST = new FST(FST.readMetadata(in), in);

    // now save the FST again, this time to different DataOutput for meta
    ByteArrayOutputStream metadataOS = new ByteArrayOutputStream();
    OutputStreamDataOutput metaOut = new OutputStreamDataOutput(metadataOS);
    ByteArrayOutputStream dataOS = new ByteArrayOutputStream();
    OutputStreamDataOutput dataOut = new OutputStreamDataOutput(dataOS);
    loadedFST.save(metaOut, dataOut);

    // finally load it again
    ByteArrayDataInput metaIn = new ByteArrayDataInput(metadataOS.toByteArray());
    ByteArrayDataInput dataIn = new ByteArrayDataInput(dataOS.toByteArray());
    loadedFST = new FST(FST.readMetadata(metaIn), dataIn);

    assertEquals(22L, Util.get(loadedFST, Util.toIntsRef(newBytesRef("aab"), scratch)));
    assertEquals(7L, Util.get(loadedFST, Util.toIntsRef(newBytesRef("aac"), scratch)));
    assertEquals(17L, Util.get(loadedFST, Util.toIntsRef(newBytesRef("ax"), scratch)));
  }

  // Make sure raw FST can differentiate between final vs
  // non-final end nodes
  public void testNonFinalStopNode() throws Exception {
    final FSTCompiler fstCompiler = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1).build();

    final FSTCompiler.UnCompiledNode rootNode = new FSTCompiler.UnCompiledNode(fstCompiler, 0);

    // Add final stop node
    {
      final FSTCompiler.UnCompiledNode node = new FSTCompiler.UnCompiledNode(fstCompiler, 0);
      node.isFinal = true;
      rootNode.addArc('a', node);
      final FSTCompiler.CompiledNode frozen = new FSTCompiler.CompiledNode();
      frozen.node = fstCompiler.addNode(node);
      rootNode.arcs[0].nextFinalOutput = 17L;
      rootNode.arcs[0].isFinal = true;
      rootNode.arcs[0].output = FST.NO_OUTPUT;
      rootNode.arcs[0].target = frozen;
    }

    // Add non-final stop node
    {
      final FSTCompiler.UnCompiledNode node = new FSTCompiler.UnCompiledNode(fstCompiler, 0);
      rootNode.addArc('b', node);
      final FSTCompiler.CompiledNode frozen = new FSTCompiler.CompiledNode();
      frozen.node = fstCompiler.addNode(node);
      rootNode.arcs[1].nextFinalOutput = FST.NO_OUTPUT;
      rootNode.arcs[1].output = 42L;
      rootNode.arcs[1].target = frozen;
    }

    fstCompiler.finish(fstCompiler.addNode(rootNode));

    final FST fst = new FST(fstCompiler.fst.metadata, fstCompiler.getFSTReader());

    StringWriter w = new StringWriter();
    Util.toDot(fst, w, false, false);
    w.close();

    checkStopNodes(fst);

    // Make sure it still works after save/load:
    Directory dir = newDirectory();
    IndexOutput out = dir.createOutput("fst", IOContext.DEFAULT);
    fst.save(out, out);
    out.close();

    IndexInput in = dir.openInput("fst", IOContext.DEFAULT);
    final FST fst2 = new FST(FST.readMetadata(in), in);
    checkStopNodes(fst2);
    in.close();
    dir.close();
  }

  private void checkStopNodes(FST fst) throws Exception {
    final FST.BytesReader reader = fst.getBytesReader();
    FST.Arc startArc = fst.getFirstArc(new FST.Arc());
    assertEquals(FST.NO_OUTPUT, startArc.output());
    assertEquals(FST.NO_OUTPUT, startArc.nextFinalOutput());

    FST.Arc arc = fst.readFirstTargetArc(startArc, new FST.Arc(), reader);
    assertEquals('a', arc.label());
    assertEquals(17, arc.nextFinalOutput());
    assertTrue(arc.isFinal());

    arc = fst.readNextArc(arc, reader);
    assertEquals('b', arc.label());
    assertFalse(arc.isFinal());
    assertEquals(42, arc.output());
  }

  public void testShortestPaths() throws Exception {
    final FSTCompiler fstCompiler = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1).build();

    final IntsRefBuilder scratch = new IntsRefBuilder();
    fstCompiler.add(Util.toIntsRef(newBytesRef("aab"), scratch), 22L);
    fstCompiler.add(Util.toIntsRef(newBytesRef("aac"), scratch), 7L);
    fstCompiler.add(Util.toIntsRef(newBytesRef("ax"), scratch), 17L);
    final FST fst = FST.fromFSTReader(fstCompiler.compile(), fstCompiler.getFSTReader());

    Util.TopResults res =
        Util.shortestPaths(fst, fst.getFirstArc(new FST.Arc()), FST.NO_OUTPUT, 3, true);
    assertTrue(res.isComplete);
    assertEquals(3, res.topN.size());
    assertEquals(Util.toIntsRef(newBytesRef("aac"), scratch), res.topN.get(0).input());
    assertEquals(7L, res.topN.get(0).output());

    assertEquals(Util.toIntsRef(newBytesRef("ax"), scratch), res.topN.get(1).input());
    assertEquals(17L, res.topN.get(1).output());

    assertEquals(Util.toIntsRef(newBytesRef("aab"), scratch), res.topN.get(2).input());
    assertEquals(22L, res.topN.get(2).output());
  }

  public void testRejectNoLimits() throws IOException {
    final FSTCompiler fstCompiler = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1).build();

    final IntsRefBuilder scratch = new IntsRefBuilder();
    fstCompiler.add(Util.toIntsRef(newBytesRef("aab"), scratch), 22L);
    fstCompiler.add(Util.toIntsRef(newBytesRef("aac"), scratch), 7L);
    fstCompiler.add(Util.toIntsRef(newBytesRef("adcd"), scratch), 17L);
    fstCompiler.add(Util.toIntsRef(newBytesRef("adcde"), scratch), 17L);

    fstCompiler.add(Util.toIntsRef(newBytesRef("ax"), scratch), 17L);
    final FST fst = FST.fromFSTReader(fstCompiler.compile(), fstCompiler.getFSTReader());
    final AtomicInteger rejectCount = new AtomicInteger();
    Util.TopNSearcher searcher =
        new Util.TopNSearcher(fst, 2, 6) {
          @Override
          protected boolean acceptResult(IntsRef input, long output) {
            boolean accept = output == 7;
            if (!accept) {
              rejectCount.incrementAndGet();
            }
            return accept;
          }
        };

    searcher.addStartPaths(
        fst.getFirstArc(new FST.Arc()), FST.NO_OUTPUT, true, new IntsRefBuilder());
    Util.TopResults res = searcher.search();
    assertEquals(rejectCount.get(), 4);
    assertTrue(res.isComplete); // rejected(4) + topN(2) <= maxQueueSize(6)

    assertEquals(1, res.topN.size());
    assertEquals(Util.toIntsRef(newBytesRef("aac"), scratch), res.topN.get(0).input());
    assertEquals(7L, res.topN.get(0).output());
    rejectCount.set(0);
    searcher =
        new Util.TopNSearcher(fst, 2, 5) {
          @Override
          protected boolean acceptResult(IntsRef input, long output) {
            boolean accept = output == 7;
            if (!accept) {
              rejectCount.incrementAndGet();
            }
            return accept;
          }
        };

    searcher.addStartPaths(
        fst.getFirstArc(new FST.Arc()), FST.NO_OUTPUT, true, new IntsRefBuilder());
    res = searcher.search();
    assertEquals(rejectCount.get(), 4);
    assertFalse(res.isComplete); // rejected(4) + topN(2) > maxQueueSize(5)
  }

  public void testShortestPathsRandom() throws Exception {
    final Random random = random();
    int numWords = atLeast(1000);

    final TreeMap<String, Long> slowCompleter = new TreeMap<>();
    final TreeSet<String> allPrefixes = new TreeSet<>();

    final FSTCompiler fstCompiler = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1).build();
    final IntsRefBuilder scratch = new IntsRefBuilder();

    for (int i = 0; i < numWords; i++) {
      String s;
      do {
        s = TestUtil.randomSimpleString(random);
      } while (slowCompleter.containsKey(s));

      for (int j = 1; j < s.length(); j++) {
        allPrefixes.add(s.substring(0, j));
      }
      int weight = TestUtil.nextInt(random, 1, 100); // weights 1..100
      slowCompleter.put(s, (long) weight);
    }

    for (Map.Entry<String, Long> e : slowCompleter.entrySet()) {
      fstCompiler.add(Util.toIntsRef(newBytesRef(e.getKey()), scratch), e.getValue());
    }

    final FST fst = FST.fromFSTReader(fstCompiler.compile(), fstCompiler.getFSTReader());

    BytesReader reader = fst.getBytesReader();

    for (String prefix : allPrefixes) {
      // 1. run prefix against fst, then complete by value

      long prefixOutput = 0;
      FST.Arc arc = fst.getFirstArc(new FST.Arc());
      for (int idx = 0; idx < prefix.length(); idx++) {
        if (fst.findTargetArc(prefix.charAt(idx), arc, arc, reader) == null) {
          fail();
        }
        prefixOutput += arc.output();
      }

      final int topN = TestUtil.nextInt(random, 1, 10);

      Util.TopResults r = Util.shortestPaths(fst, arc, FST.NO_OUTPUT, topN, true);
      assertTrue(r.isComplete);

      // 2. go thru whole treemap (slowCompleter) and check it's actually the best suggestion
      final List<Result> matches = new ArrayList<>();

      // TODO: could be faster... but it's slowCompleter for a reason
      for (Map.Entry<String, Long> e : slowCompleter.entrySet()) {
        if (e.getKey().startsWith(prefix)) {
          matches.add(
              new Result(
                  Util.toIntsRef(
                      newBytesRef(e.getKey().substring(prefix.length())), new IntsRefBuilder()),
                  e.getValue() - prefixOutput));
        }
      }

      assertTrue(matches.size() > 0);
      matches.sort(new TieBreakByInputComparator());
      if (matches.size() > topN) {
        matches.subList(topN, matches.size()).clear();
      }

      assertEquals(matches.size(), r.topN.size());

      for (int hit = 0; hit < r.topN.size(); hit++) {
        assertEquals(matches.get(hit).input(), r.topN.get(hit).input());
        assertEquals(matches.get(hit).output(), r.topN.get(hit).output());
      }
    }
  }

  private static final class TieBreakByInputComparator implements Comparator<Result> {
    @Override
    public int compare(Result a, Result b) {
      int cmp = Long.compare(a.output(), b.output());
      if (cmp == 0) {
        return a.input().compareTo(b.input());
      } else {
        return cmp;
      }
    }
  }

  /** Large (9-byte VLong) outputs on fixed-length (array) arcs. */
  public void testLargeOutputsOnArrayArcs() throws Exception {
    final FSTCompiler fstCompiler = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1).build();

    final IntsRefBuilder input = new IntsRefBuilder();
    input.append(0);
    final long base = 1L << 60;
    for (int arc = 0; arc < 6; arc++) {
      input.setIntAt(0, arc);
      fstCompiler.add(input.get(), base + arc);
    }

    final FST fst = FST.fromFSTReader(fstCompiler.compile(), fstCompiler.getFSTReader());
    for (int arc = 0; arc < 6; arc++) {
      input.setIntAt(0, arc);
      assertEquals(base + arc, Util.get(fst, input.get()));
    }
  }

  public void testSimpleDepth() throws Exception {
    FSTCompiler fstCompiler = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1).build();

    BytesRef ab = newBytesRef("ab");
    BytesRef ac = newBytesRef("ac");
    BytesRef bd = newBytesRef("bd");

    fstCompiler.add(Util.toIntsRef(ab, new IntsRefBuilder()), 3L);
    fstCompiler.add(Util.toIntsRef(ac, new IntsRefBuilder()), 5L);
    fstCompiler.add(Util.toIntsRef(bd, new IntsRefBuilder()), 7L);

    FST fst = FST.fromFSTReader(fstCompiler.compile(), fstCompiler.getFSTReader());

    assertEquals(3, Util.get(fst, ab));
    assertEquals(5, Util.get(fst, ac));
    assertEquals(7, Util.get(fst, bd));
  }
}
