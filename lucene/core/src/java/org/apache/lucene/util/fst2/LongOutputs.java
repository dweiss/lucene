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
package org.apache.lucene.util.fst2;

import java.io.IOException;
import org.apache.lucene.store.DataInput;

/**
 * Optional, primitive companion of an {@link Outputs} implementation whose values can be
 * represented as a {@code long}. An {@link Outputs} that implements this interface lets the {@link
 * FST} and the enumerators decode and accumulate outputs without boxing, see {@link
 * FST#readOutputLong}, {@link FST#foldLong} and {@link BytesRefFSTEnum#currentOutputLong}.
 *
 * <p>The primitive methods must agree with the boxed ones: {@code readLong(in)} must consume
 * exactly the bytes that {@link Outputs#read} consumes and produce the same value, and {@code
 * addLong} must mirror {@link Outputs#add}.
 *
 * @lucene.experimental
 */
public interface LongOutputs {

  /** The primitive equivalent of {@link Outputs#getNoOutput()}. */
  long noOutputLong();

  /** Decode an output previously written with {@link Outputs#write}. */
  long readLong(DataInput in) throws IOException;

  /**
   * Decode an output previously written with {@link Outputs#writeFinalOutput}. By default this just
   * calls {@link #readLong}.
   */
  default long readFinalOutputLong(DataInput in) throws IOException {
    return readLong(in);
  }

  /** The primitive equivalent of {@link Outputs#add}. */
  long addLong(long prefix, long output);

  /** Unbox a value produced by the boxed API. Used at the compiler boundary and in tests. */
  long toLong(Object boxed);
}
