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
package org.apache.lucene.util.fst4;

/**
 * A mutable, reusable accumulator for one output value of an {@link Outputs} implementation. Arcs
 * only record output positions; values are decoded and accumulated into holders on demand ({@link
 * FST#readAddInto}, {@link FST#foldInto}, {@link BytesRefFSTEnum#currentOutput()}), so traversal
 * and accumulation do not allocate once the holders exist. The concrete holder type is defined by
 * the {@link Outputs} implementation (for example {@link PositiveIntOutputs.LongHolder} exposes a
 * public {@code long value}); callers that know the outputs type cast once and read the primitive.
 *
 * @lucene.experimental
 */
public abstract class OutputHolder<T> {

  /** Creates a new holder of the same concrete type, holding no output. */
  public abstract OutputHolder<T> newHolder();

  /** Resets this holder to {@link Outputs#getNoOutput()}. */
  public abstract void setNoOutput();

  /** True iff this holder holds {@link Outputs#getNoOutput()}. */
  public abstract boolean isNoOutput();

  /** Copies the value of {@code other} (same concrete type) into this holder. */
  public abstract void copyFrom(OutputHolder<T> other);

  /**
   * Returns the value as a boxed object. This may allocate; the result is the {@link
   * Outputs#getNoOutput()} singleton when {@link #isNoOutput()}.
   */
  public abstract T get();

  /** Sets this holder to the given boxed value (copying it, where applicable). */
  public abstract void set(T value);

  @Override
  public String toString() {
    return String.valueOf(get());
  }
}
