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
 * Experimental finite state transducers: lazy, position-based outputs accumulated into reusable
 * holders (the "hybrid" of the {@code fst2} and {@code fst3} experiments).
 *
 * <p>This package is a copy of {@link org.apache.lucene.util.fst} with one change to the traversal
 * API. {@link org.apache.lucene.util.fst4.FST.Arc} is not generic and never holds decoded values:
 * it records the byte position of the arc's output ({@link
 * org.apache.lucene.util.fst4.FST.Arc#outputPos()}) and of the target node's final output ({@link
 * org.apache.lucene.util.fst4.FST.Arc#nextFinalOutputPos()}), so traversal is allocation free and
 * output-type agnostic, and membership tests or misses never decode anything. When a value is
 * needed it is accumulated into a mutable, reusable {@link
 * org.apache.lucene.util.fst4.OutputHolder} through {@link
 * org.apache.lucene.util.fst4.FST#readAddInto} (decode at a position and add in place) or {@link
 * org.apache.lucene.util.fst4.FST#foldInto}; no boxed {@code add} and no copy of the prefix is
 * involved, because {@link org.apache.lucene.util.fst4.Outputs#readAddInto} appends directly into
 * the accumulator. {@link org.apache.lucene.util.fst4.FST#readOutput} still returns boxed values
 * for convenience.
 *
 * <p>The enumerators keep positions per depth and a lazily filled cumulative stack of holders
 * ({@link org.apache.lucene.util.fst4.BytesRefFSTEnum#currentOutput()}): only the depths that
 * changed since the last call are recomputed, and depths whose arc carries no output alias their
 * parent's holder. {@link org.apache.lucene.util.fst4.Util#get(FST,
 * org.apache.lucene.util.BytesRef, OutputHolder)} fills a caller-supplied holder, {@link
 * org.apache.lucene.util.fst4.Util#contains} decodes nothing, and {@link
 * org.apache.lucene.util.fst4.Util.TopNSearcher} compares and accumulates holders.
 *
 * <p>Construction ({@link org.apache.lucene.util.fst4.FSTCompiler}) is deliberately unchanged and
 * still works on boxed values; the serialized format is identical to that of {@link
 * org.apache.lucene.util.fst.FST}.
 *
 * <p>Retrieval by key, boxed or into a reusable holder:
 *
 * <pre><code class="language-java">
 *     Long value = Util.get(fst, new BytesRef("dog"));           // 7, boxed
 *     PositiveIntOutputs.LongHolder holder = outputs.newHolder();
 *     if (Util.get(fst, new BytesRef("dog"), holder)) {
 *       System.out.println(holder.value);                          // 7, no allocation
 *     }
 * </code></pre>
 *
 * Iterate over key-value pairs in sorted order:
 *
 * <pre><code class="language-java">
 *     BytesRefFSTEnum&lt;Long&gt; iterator = new BytesRefFSTEnum&lt;&gt;(fst);
 *     while (iterator.next() != null) {
 *       System.out.println(iterator.current().input.utf8ToString());
 *       System.out.println(((PositiveIntOutputs.LongHolder) iterator.currentOutput()).value);
 *     }
 * </code></pre>
 *
 * N-shortest paths by weight:
 *
 * <pre><code class="language-java">
 *     Arc firstArc = fst.getFirstArc(new Arc());
 *     Util.TopResults&lt;Long&gt; paths =
 *         Util.shortestPaths(fst, firstArc, outputs.newHolder(), PositiveIntOutputs.HOLDER_COMPARATOR, 3, true);
 * </code></pre>
 *
 * @lucene.experimental
 */
package org.apache.lucene.util.fst4;
