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
 * Experimental finite state transducers with eager but allocation-free outputs ("holder" design).
 *
 * <p>This package is a copy of {@link org.apache.lucene.util.fst} with one change to the traversal
 * API: arc outputs are decoded into mutable, reusable {@link
 * org.apache.lucene.util.fst3.OutputHolder holders} owned by each {@link
 * org.apache.lucene.util.fst3.FST.Arc} ({@link org.apache.lucene.util.fst3.FST.Arc#output()},
 * {@link org.apache.lucene.util.fst3.FST.Arc#nextFinalOutput()}) instead of into freshly allocated
 * boxed values. {@link org.apache.lucene.util.fst3.Outputs} gained {@link
 * org.apache.lucene.util.fst3.Outputs#newHolder()}, {@link
 * org.apache.lucene.util.fst3.Outputs#readInto} and {@link
 * org.apache.lucene.util.fst3.Outputs#addInto}; each implementation defines its holder (for example
 * {@link org.apache.lucene.util.fst3.PositiveIntOutputs.LongHolder} with a public {@code long
 * value}). Traversal stays eager (every arc output is decoded when the arc is read) but allocates
 * nothing once the holders exist. The enumerators keep a cumulative stack of holders, {@link
 * org.apache.lucene.util.fst3.Util#get(FST, org.apache.lucene.util.BytesRef, OutputHolder)} fills a
 * caller-supplied holder, and {@link org.apache.lucene.util.fst3.Util.TopNSearcher} compares and
 * accumulates holders.
 *
 * <p>Construction ({@link org.apache.lucene.util.fst3.FSTCompiler}) is deliberately unchanged and
 * still works on boxed values; the serialized format is identical to that of {@link
 * org.apache.lucene.util.fst.FST}, so FSTs can be exchanged between the packages.
 *
 * <p>FST Construction example:
 *
 * <pre><code class="language-java">
 *     String inputValues[] = {"cat", "dog", "dogs"};
 *     long outputValues[] = {5, 7, 12};
 *
 *     PositiveIntOutputs outputs = PositiveIntOutputs.getSingleton();
 *     FSTCompiler&lt;Long&gt; fstCompiler = new FSTCompiler.Builder&lt;&gt;(INPUT_TYPE.BYTE1, outputs).build();
 *     BytesRefBuilder scratchBytes = new BytesRefBuilder();
 *     IntsRefBuilder scratchInts = new IntsRefBuilder();
 *     for (int i = 0; i &lt; inputValues.length; i++) {
 *       scratchBytes.copyChars(inputValues[i]);
 *       fstCompiler.add(Util.toIntsRef(scratchBytes.toBytesRef(), scratchInts), outputValues[i]);
 *     }
 *     FST&lt;Long&gt; fst = FST.fromFSTReader(fstCompiler.compile(), fstCompiler.getFSTReader());
 * </code></pre>
 *
 * Retrieval by key, boxed or into a reusable holder:
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
 *     BytesRefFSTEnum.InputOutput&lt;Long&gt; mapEntry;
 *     while ((mapEntry = iterator.next()) != null) {
 *       System.out.println(mapEntry.input.utf8ToString());
 *       System.out.println(((PositiveIntOutputs.LongHolder) mapEntry.output).value);
 *     }
 * </code></pre>
 *
 * N-shortest paths by weight:
 *
 * <pre><code class="language-java">
 *     Arc&lt;Long&gt; firstArc = fst.getFirstArc(new Arc&lt;&gt;());
 *     Util.TopResults&lt;Long&gt; paths =
 *         Util.shortestPaths(fst, firstArc, outputs.newHolder(), PositiveIntOutputs.HOLDER_COMPARATOR, 3, true);
 *     System.out.println(Util.toBytesRef(paths.topN.get(0).input(), scratchBytes).utf8ToString()); // cat
 *     System.out.println(paths.topN.get(0).output()); // 5
 * </code></pre>
 *
 * @lucene.experimental
 */
package org.apache.lucene.util.fst3;
