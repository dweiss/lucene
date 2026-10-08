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
 * Experimental: finite state transducers with a single, primitive {@code long} output per arc, plus
 * an ordinal-indexed side store for byte-sequence payloads.
 *
 * <p>This package is the "Rust {@code fst} model" variant of {@link org.apache.lucene.util.fst}:
 * there is no pluggable {@code Outputs} abstraction. Every arc carries a non-negative {@code long}
 * (zero meaning "no output", written as no bytes at all), the output algebra is fixed to (min, +),
 * and nothing is ever boxed or allocated while reading. The byte format is identical to the generic
 * FST built with {@code PositiveIntOutputs}, so files are interchangeable in both directions.
 *
 * <p>Payloads that are not longs are not pushed along arcs. Instead, {@link
 * org.apache.lucene.util.fst5.FSTMap} assigns each key its ordinal as the FST output and keeps the
 * values in a {@link org.apache.lucene.util.fst5.BytesRefStore}, a compact random-access blob with
 * packed offsets (and optional deduplication of repeated values). Ordinal outputs are structural,
 * so they preserve suffix sharing in the automaton regardless of what the values look like (up to
 * nodes that differ only in finality, see {@link org.apache.lucene.util.fst5.FSTMap}).
 *
 * <p>Lookup, membership and enumeration:
 *
 * <pre><code class="language-java">
 *     FSTCompiler c = new FSTCompiler.Builder(FST.INPUT_TYPE.BYTE1).build();
 *     c.add(Util.toIntsRef(new BytesRef("cat"), scratch), 5);
 *     c.add(Util.toIntsRef(new BytesRef("dog"), scratch), 7);
 *     FST fst = FST.fromFSTReader(c.compile(), c.getFSTReader());
 *
 *     long v = Util.get(fst, new BytesRef("dog"));      // 7, or Util.NOT_FOUND
 *     boolean has = Util.contains(fst, new BytesRef("cow"));
 *
 *     BytesRefFSTEnum e = new BytesRefFSTEnum(fst);
 *     BytesRefFSTEnum.InputOutput io;
 *     while ((io = e.next()) != null) {
 *       // io.input is a BytesRef, io.output a long
 *     }
 *
 *     Util.TopResults top = Util.shortestPaths(fst, fst.getFirstArc(new FST.Arc()), 0, 3, true);
 * </code></pre>
 *
 * <p>Byte payloads through the map layer:
 *
 * <pre><code class="language-java">
 *     FSTMap.Builder b = new FSTMap.Builder();
 *     b.add(new BytesRef("cat"), new BytesRef("felis"));
 *     b.add(new BytesRef("dog"), new BytesRef("canis"));
 *     FSTMap map = b.build();
 *
 *     BytesRefBuilder value = new BytesRefBuilder();
 *     if (map.get(new BytesRef("dog"), value)) { ... }    // value = "canis", no allocation
 * </code></pre>
 *
 * <p>What is deliberately missing compared to the generic package: {@code Outputs}, byte / int /
 * char sequence outputs, pair outputs, and merging of duplicate inputs. A second long per arc (for
 * example a weight next to an ordinal) would be the natural extension and is not implemented here.
 */
package org.apache.lucene.util.fst5;
