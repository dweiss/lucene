package org.apache.lucene.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.lucene.analysis.TokenizerFactory;

public class ModuleCheckAnalysisSPI implements Supplier<Map<String, List<String>>> {
  @Override
  public Map<String, List<String>> get() {
    // TOOD: add other analysis components
    return Map.of("tokenizers", new ArrayList<>(TokenizerFactory.availableTokenizers()));
  }
}
