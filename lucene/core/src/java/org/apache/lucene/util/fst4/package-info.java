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

/**
 * Experimental finite state transducers with lazy, position-based outputs.
 *
 * <p>This package is a copy of {@link org.apache.lucene.util.fst} with one change to the traversal
 * API: {@link org.apache.lucene.util.fst4.FST.Arc} is not generic and does not hold decoded output
 * values. Instead it records the byte position of the arc's output ({@link
 * org.apache.lucene.util.fst4.FST.Arc#outputPos()}) and of the target node's final output ({@link
 * org.apache.lucene.util.fst4.FST.Arc#nextFinalOutputPos()}) inside the FST bytes. Traversal is
 * therefore allocation free and output-type agnostic; a value is produced only when the caller asks
 * for it, either boxed ({@link org.apache.lucene.util.fst4.FST#readOutput}, {@link
 * org.apache.lucene.util.fst4.FST#fold}) or as a primitive {@code long} ({@link
 * org.apache.lucene.util.fst4.FST#readOutputLong}, {@link
 * org.apache.lucene.util.fst4.FST#foldLong}) when the {@link org.apache.lucene.util.fst4.Outputs}
 * implementation also implements {@link org.apache.lucene.util.fst4.LongOutputs}.
 *
 * <p>The enumerators expose the current output through {@link
 * org.apache.lucene.util.fst4.BytesRefFSTEnum#currentOutput()} and {@link
 * org.apache.lucene.util.fst4.BytesRefFSTEnum#currentOutputLong()}; {@link
 * org.apache.lucene.util.fst4.Util} gains {@link org.apache.lucene.util.fst4.Util#getLong}, {@link
 * org.apache.lucene.util.fst4.Util#contains} and {@link
 * org.apache.lucene.util.fst4.Util#shortestPathsLong}.
 *
 * <p>Construction ({@link org.apache.lucene.util.fst4.FSTCompiler}) is deliberately unchanged and
 * still works on boxed values; the serialized format is identical to that of {@link
 * org.apache.lucene.util.fst.FST}, so FSTs can be exchanged between the two packages.
 *
 * <p>TODO (Design 2): define the outputs algebra over encodings (byte slices) rather than decoded
 * values, so that construction and {@link org.apache.lucene.util.fst4.FSTSuffixNodeCache} stop
 * boxing as well.
 *
 * <p>FST Construction example:
 *
 * <pre><code class="language-java">
 *     // Input values (keys). These must be provided to Builder in Unicode code point (UTF8 or UTF32) sorted order.
 *     // Note that sorting by Java's String.compareTo, which is UTF16 sorted order, is not correct and can lead to
 *     // exceptions while building the FST:
 *     String inputValues[] = {"cat", "dog", "dogs"};
 *     long outputValues[] = {5, 7, 12};
 *
 *     PositiveIntOutputs outputs = PositiveIntOutputs.getSingleton();
 *     FSTCompiler&lt;Long&gt; fstCompiler = new FSTCompiler&lt;Long&gt;(INPUT_TYPE.BYTE1, outputs);
 *     BytesRefBuilder scratchBytes = new BytesRefBuilder();
 *     IntsRefBuilder scratchInts = new IntsRefBuilder();
 *     for (int i = 0; i &lt; inputValues.length; i++) {
 *       scratchBytes.copyChars(inputValues[i]);
 *       fstCompiler.add(Util.toIntsRef(scratchBytes.toBytesRef(), scratchInts), outputValues[i]);
 *     }
 *     FST&lt;Long&gt; fst = FST.fromFSTReader(fstCompiler.compile(), fstCompiler.getFSTReader());
 * </code></pre>
 *
 * Retrieval by key, boxed or primitive:
 *
 * <pre><code class="language-java">
 *     Long value = Util.get(fst, new BytesRef("dog"));
 *     System.out.println(value); // 7
 *     long v = Util.getLong(fst, new BytesRef("dog"), -1);
 *     System.out.println(v); // 7
 * </code></pre>
 *
 * Iterate over key-value pairs in sorted order:
 *
 * <pre><code class="language-java">
 *     BytesRefFSTEnum&lt;Long&gt; iterator = new BytesRefFSTEnum&lt;Long&gt;(fst);
 *     while (iterator.next() != null) {
 *       System.out.println(iterator.current().input.utf8ToString());
 *       System.out.println(iterator.currentOutputLong());
 *     }
 * </code></pre>
 *
 * N-shortest paths by weight:
 *
 * <pre><code class="language-java">
 *     Arc firstArc = fst.getFirstArc(new Arc());
 *     Util.LongTopResults paths = Util.shortestPathsLong(fst, firstArc, 0, 3, true);
 *     System.out.println(Util.toBytesRef(paths.topN().get(0).input(), scratchBytes).utf8ToString()); // cat
 *     System.out.println(paths.topN().get(0).output()); // 5
 * </code></pre>
 *
 * @lucene.experimental
 */
package org.apache.lucene.util.fst4;
