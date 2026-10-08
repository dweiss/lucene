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
package org.apache.lucene.util.fst3;

import java.io.IOException;
import java.util.Arrays;
import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.CharsRefBuilder;
import org.apache.lucene.util.RamUsageEstimator;

/**
 * An FST {@link Outputs} implementation where each output is a sequence of characters.
 *
 * @lucene.experimental
 */
public final class CharSequenceOutputs extends Outputs<CharsRef> {

  private static final CharsRef NO_OUTPUT = new CharsRef();
  private static final CharSequenceOutputs singleton = new CharSequenceOutputs();

  private CharSequenceOutputs() {}

  public static CharSequenceOutputs getSingleton() {
    return singleton;
  }

  @Override
  public CharsRef common(CharsRef output1, CharsRef output2) {
    assert output1 != null;
    assert output2 != null;

    int mismatchPos =
        Arrays.mismatch(
            output1.chars,
            output1.offset,
            output1.offset + output1.length,
            output2.chars,
            output2.offset,
            output2.offset + output2.length);

    if (mismatchPos == 0) {
      // no common prefix
      return NO_OUTPUT;
    } else if (mismatchPos == -1) {
      // exactly equals
      return output1;
    } else if (mismatchPos == output1.length) {
      // output1 is a prefix of output2
      return output1;
    } else if (mismatchPos == output2.length) {
      // output2 is a prefix of output1
      return output2;
    } else {
      return new CharsRef(output1.chars, output1.offset, mismatchPos);
    }
  }

  @Override
  public CharsRef subtract(CharsRef output, CharsRef inc) {
    assert output != null;
    assert inc != null;
    if (inc == NO_OUTPUT) {
      // no prefix removed
      return output;
    } else if (inc.length == output.length) {
      // entire output removed
      return NO_OUTPUT;
    } else {
      assert inc.length < output.length
          : "inc.length=" + inc.length + " vs output.length=" + output.length;
      assert inc.length > 0;
      return new CharsRef(output.chars, output.offset + inc.length, output.length - inc.length);
    }
  }

  @Override
  public CharsRef add(CharsRef prefix, CharsRef output) {
    assert prefix != null;
    assert output != null;
    if (prefix == NO_OUTPUT) {
      return output;
    } else if (output == NO_OUTPUT) {
      return prefix;
    } else {
      assert prefix.length > 0;
      assert output.length > 0;
      CharsRef result = new CharsRef(prefix.length + output.length);
      System.arraycopy(prefix.chars, prefix.offset, result.chars, 0, prefix.length);
      System.arraycopy(output.chars, output.offset, result.chars, prefix.length, output.length);
      result.length = prefix.length + output.length;
      return result;
    }
  }

  @Override
  public void write(CharsRef prefix, DataOutput out) throws IOException {
    assert prefix != null;
    out.writeVInt(prefix.length);
    // TODO: maybe UTF8?
    for (int idx = 0; idx < prefix.length; idx++) {
      out.writeVInt(prefix.chars[prefix.offset + idx]);
    }
  }

  @Override
  public CharsRef read(DataInput in) throws IOException {
    final int len = in.readVInt();
    if (len == 0) {
      return NO_OUTPUT;
    } else {
      final CharsRef output = new CharsRef(len);
      for (int idx = 0; idx < len; idx++) {
        output.chars[idx] = (char) in.readVInt();
      }
      output.length = len;
      return output;
    }
  }

  /** Holder backed by a reusable {@link CharsRefBuilder}. */
  public static final class CharsHolder extends OutputHolder<CharsRef> {
    public final CharsRefBuilder chars = new CharsRefBuilder();

    @Override
    public CharsHolder newHolder() {
      return new CharsHolder();
    }

    @Override
    public void setNoOutput() {
      chars.clear();
    }

    @Override
    public boolean isNoOutput() {
      return chars.length() == 0;
    }

    @Override
    public void copyFrom(OutputHolder<CharsRef> other) {
      chars.copyChars(((CharsHolder) other).chars.get());
    }

    @Override
    public CharsRef get() {
      return chars.length() == 0 ? NO_OUTPUT : chars.toCharsRef();
    }

    @Override
    public void set(CharsRef value) {
      chars.copyChars(value);
    }
  }

  @Override
  public CharsHolder newHolder() {
    return new CharsHolder();
  }

  @Override
  public void readInto(DataInput in, OutputHolder<CharsRef> dest) throws IOException {
    final CharsRefBuilder b = ((CharsHolder) dest).chars;
    final int len = in.readVInt();
    b.grow(len);
    final char[] arr = b.chars();
    for (int idx = 0; idx < len; idx++) {
      arr[idx] = (char) in.readVInt();
    }
    b.setLength(len);
  }

  @Override
  public void addInto(
      OutputHolder<CharsRef> prefix, OutputHolder<CharsRef> output, OutputHolder<CharsRef> dest) {
    assert dest != output;
    final CharsRefBuilder d = ((CharsHolder) dest).chars;
    if (dest != prefix) {
      d.copyChars(((CharsHolder) prefix).chars.get());
    }
    final CharsRef o = ((CharsHolder) output).chars.get();
    d.append(o.chars, o.offset, o.length);
  }

  @Override
  public void skipOutput(DataInput in) throws IOException {
    final int len = in.readVInt();
    for (int idx = 0; idx < len; idx++) {
      in.readVInt();
    }
  }

  @Override
  public CharsRef getNoOutput() {
    return NO_OUTPUT;
  }

  @Override
  public String outputToString(CharsRef output) {
    return output.toString();
  }

  private static final long BASE_NUM_BYTES = RamUsageEstimator.shallowSizeOf(NO_OUTPUT);

  @Override
  public long ramBytesUsed(CharsRef output) {
    return BASE_NUM_BYTES + RamUsageEstimator.sizeOf(output.chars);
  }
}
