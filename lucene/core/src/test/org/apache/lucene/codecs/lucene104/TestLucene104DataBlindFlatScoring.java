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
package org.apache.lucene.codecs.lucene104;

import org.apache.lucene.codecs.hnsw.FlatVectorsScorer;
import org.apache.lucene.codecs.lucene104.Lucene104ScalarQuantizedVectorsFormat.Mode;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.KnnFloatVectorField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FloatVectorValues;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.VectorSimilarityFunction;
import org.apache.lucene.internal.vectorization.BaseVectorizationTestCase;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.tests.util.TestUtil;
import org.apache.lucene.util.VectorUtil;
import org.apache.lucene.util.hnsw.RandomVectorScorer;
import org.apache.lucene.util.quantization.QuantizedByteVectorValues.ScalarEncoding;

/**
 * A data-blind segment serves {@link FloatVectorValues} that dequantize stored bytes on read. The
 * flat scorer's off-heap fast path scores memory-mapped float data directly, so neither those
 * values nor the dequantizing view behind them (reached through {@code rescorer()}) may be mistaken
 * for full-precision vectors: per-ord and bulk scores have to match the dequantized vectors.
 */
public class TestLucene104DataBlindFlatScoring extends BaseVectorizationTestCase {

  public void testScoresMatchDequantizedVectors() throws Exception {
    FlatVectorsScorer flatScorer = PANAMA_OR_NATIVE_PROVIDER.getLucene99FlatVectorsScorer();
    int numVectors = atLeast(20);
    int dims = random().nextInt(4, 65);
    VectorSimilarityFunction similarityFunction =
        random().nextBoolean()
            ? VectorSimilarityFunction.DOT_PRODUCT
            : VectorSimilarityFunction.EUCLIDEAN;
    ScalarEncoding[] encodings = ScalarEncoding.values();
    ScalarEncoding encoding = encodings[random().nextInt(encodings.length)];
    try (Directory dir = new MMapDirectory(createTempDir())) {
      try (IndexWriter w =
          new IndexWriter(
              dir,
              new IndexWriterConfig()
                  .setUseCompoundFile(false)
                  .setCodec(
                      TestUtil.alwaysKnnVectorsFormat(
                          new Lucene104ScalarQuantizedVectorsFormat(
                              encoding, Mode.DATA_BLIND_WITHOUT_FLOATS))))) {
        for (int i = 0; i < numVectors; i++) {
          Document doc = new Document();
          doc.add(new KnnFloatVectorField("field", randomUnitVector(dims), similarityFunction));
          w.addDocument(doc);
        }
        w.forceMerge(1);
      }
      try (DirectoryReader reader = DirectoryReader.open(dir)) {
        LeafReader leaf = getOnlyLeafReader(reader);
        FloatVectorValues values = leaf.getFloatVectorValues("field");
        assertTrue(
            values instanceof Lucene104ScalarQuantizedVectorsReader.ScalarQuantizedVectorValues);
        FloatVectorValues view =
            ((Lucene104ScalarQuantizedVectorsReader.ScalarQuantizedVectorValues) values)
                .getRawVectorValues();
        assertTrue(view instanceof OffHeapScalarQuantizedFloatVectorValues);
        float[] query = randomUnitVector(dims);
        assertScoresMatch(flatScorer, similarityFunction, values, query);
        assertScoresMatch(flatScorer, similarityFunction, view, query);
      }
    }
  }

  private static void assertScoresMatch(
      FlatVectorsScorer flatScorer,
      VectorSimilarityFunction similarityFunction,
      FloatVectorValues values,
      float[] query)
      throws Exception {
    RandomVectorScorer scorer = flatScorer.getRandomVectorScorer(similarityFunction, values, query);
    int[] ords = new int[values.size()];
    float[] bulkScores = new float[values.size()];
    for (int ord = 0; ord < ords.length; ord++) {
      ords[ord] = ord;
    }
    scorer.bulkScore(ords, bulkScores, ords.length);
    for (int ord = 0; ord < ords.length; ord++) {
      float expected = similarityFunction.compare(query, values.vectorValue(ord));
      assertEquals("ord " + ord, expected, scorer.score(ord), 1e-4f);
      assertEquals("bulk ord " + ord, expected, bulkScores[ord], 1e-4f);
    }
  }

  private float[] randomUnitVector(int dims) {
    float[] vector = new float[dims];
    for (int i = 0; i < dims; i++) {
      vector[i] = random().nextFloat() * 2 - 1;
    }
    VectorUtil.l2normalize(vector);
    return vector;
  }
}
