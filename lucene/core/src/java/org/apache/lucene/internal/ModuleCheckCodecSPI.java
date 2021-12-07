package org.apache.lucene.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.lucene.codecs.DocValuesFormat;

public class ModuleCheckCodecSPI implements Supplier<Map<String, List<String>>> {
  @Override
  public Map<String, List<String>> get() {
    return Map.of("docvaluesformat", new ArrayList<>(DocValuesFormat.availableDocValuesFormats()));
  }
}
