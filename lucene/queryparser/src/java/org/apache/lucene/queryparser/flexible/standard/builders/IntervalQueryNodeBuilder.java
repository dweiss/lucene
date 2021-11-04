package org.apache.lucene.queryparser.flexible.standard.builders;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.IntervalQueryNode;
import org.apache.lucene.search.Query;

public class IntervalQueryNodeBuilder implements StandardQueryBuilder {
  private final Analyzer analyzer;

  public IntervalQueryNodeBuilder(Analyzer analyzer) {
    this.analyzer = analyzer;
  }

  @Override
  public Query build(QueryNode queryNode) throws QueryNodeException {
    return ((IntervalQueryNode) queryNode).getQuery(analyzer);
  }
}
