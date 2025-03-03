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
package org.apache.lucene.tests.codecs.asserting;

import java.io.IOException;
import java.util.Collection;
import org.apache.lucene.codecs.StoredFieldsFormat;
import org.apache.lucene.codecs.StoredFieldsReader;
import org.apache.lucene.codecs.StoredFieldsWriter;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.index.StoredFieldDataInput;
import org.apache.lucene.index.StoredFieldVisitor;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.tests.util.TestUtil;
import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.BytesRef;

/** Just like the default stored fields format but with additional asserts. */
public class AssertingStoredFieldsFormat extends StoredFieldsFormat {
  private final StoredFieldsFormat in = TestUtil.getDefaultCodec().storedFieldsFormat();

  @Override
  public StoredFieldsReader fieldsReader(
      Directory directory, SegmentInfo si, FieldInfos fn, IOContext context) throws IOException {
    return new AssertingStoredFieldsReader(
        in.fieldsReader(directory, si, fn, context), si.maxDoc(), false);
  }

  @Override
  public StoredFieldsWriter fieldsWriter(Directory directory, SegmentInfo si, IOContext context)
      throws IOException {
    return new AssertingStoredFieldsWriter(in.fieldsWriter(directory, si, context));
  }

  static class AssertingStoredFieldsReader extends StoredFieldsReader {
    private final StoredFieldsReader in;
    private final int maxDoc;
    private final boolean merging;
    private final Thread creationThread;

    AssertingStoredFieldsReader(StoredFieldsReader in, int maxDoc, boolean merging) {
      this.in = in;
      this.maxDoc = maxDoc;
      this.merging = merging;
      this.creationThread = Thread.currentThread();
      // do a few simple checks on init
      assert toString() != null;
    }

    @Override
    public void close() throws IOException {
      in.close();
      in.close(); // close again
    }

    @Override
    public void document(int n, StoredFieldVisitor visitor) throws IOException {
      AssertingCodec.assertThread("StoredFieldsReader", creationThread);
      assert n >= 0 && n < maxDoc;
      in.document(n, visitor);
    }

    @Override
    public StoredFieldsReader clone() {
      assert merging == false : "Merge instances do not support cloning";
      return new AssertingStoredFieldsReader(in.clone(), maxDoc, false);
    }

    @Override
    public void checkIntegrity() throws IOException {
      in.checkIntegrity();
    }

    @Override
    public StoredFieldsReader getMergeInstance() {
      return new AssertingStoredFieldsReader(in.getMergeInstance(), maxDoc, true);
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "(" + in.toString() + ")";
    }
  }

  enum Status {
    UNDEFINED,
    STARTED,
    FINISHED;
  }

  static class AssertingStoredFieldsWriter extends StoredFieldsWriter {
    private final StoredFieldsWriter in;
    private int numWritten;
    private Status docStatus;

    AssertingStoredFieldsWriter(StoredFieldsWriter in) {
      this.in = in;
      this.docStatus = Status.UNDEFINED;
    }

    @Override
    public void startDocument() throws IOException {
      assert docStatus != Status.STARTED;
      in.startDocument();
      numWritten++;
      docStatus = Status.STARTED;
    }

    @Override
    public void finishDocument() throws IOException {
      assert docStatus == Status.STARTED;
      in.finishDocument();
      docStatus = Status.FINISHED;
    }

    @Override
    public void writeField(FieldInfo info, int value) throws IOException {
      assert docStatus == Status.STARTED;
      in.writeField(info, value);
    }

    @Override
    public void writeField(FieldInfo info, long value) throws IOException {
      assert docStatus == Status.STARTED;
      in.writeField(info, value);
    }

    @Override
    public void writeField(FieldInfo info, float value) throws IOException {
      assert docStatus == Status.STARTED;
      in.writeField(info, value);
    }

    @Override
    public void writeField(FieldInfo info, double value) throws IOException {
      assert docStatus == Status.STARTED;
      in.writeField(info, value);
    }

    @Override
    public void writeField(FieldInfo info, BytesRef value) throws IOException {
      assert docStatus == Status.STARTED;
      in.writeField(info, value);
    }

    @Override
    public void writeField(FieldInfo info, StoredFieldDataInput value) throws IOException {
      assert docStatus == Status.STARTED;
      in.writeField(info, value);
    }

    @Override
    public void writeField(FieldInfo info, String value) throws IOException {
      assert docStatus == Status.STARTED;
      in.writeField(info, value);
    }

    @Override
    public void finish(int numDocs) throws IOException {
      assert docStatus == (numDocs > 0 ? Status.FINISHED : Status.UNDEFINED);
      in.finish(numDocs);
      assert numDocs == numWritten;
    }

    @Override
    public void close() throws IOException {
      in.close();
      in.close(); // close again
    }

    @Override
    public long ramBytesUsed() {
      return in.ramBytesUsed();
    }

    @Override
    public Collection<Accountable> getChildResources() {
      return in.getChildResources();
    }
  }
}
