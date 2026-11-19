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
package org.apache.lucene.queryparser.surround.parser;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.apache.lucene.queryparser.surround.parser.SurroundSyntaxAntlrParser.AndQueryContext;
import org.apache.lucene.queryparser.surround.parser.SurroundSyntaxAntlrParser.FieldsQueryContext;
import org.apache.lucene.queryparser.surround.parser.SurroundSyntaxAntlrParser.FieldsQueryListContext;
import org.apache.lucene.queryparser.surround.parser.SurroundSyntaxAntlrParser.NQueryContext;
import org.apache.lucene.queryparser.surround.parser.SurroundSyntaxAntlrParser.NotQueryContext;
import org.apache.lucene.queryparser.surround.parser.SurroundSyntaxAntlrParser.OrQueryContext;
import org.apache.lucene.queryparser.surround.parser.SurroundSyntaxAntlrParser.PrefixOperatorQueryContext;
import org.apache.lucene.queryparser.surround.parser.SurroundSyntaxAntlrParser.PrimaryQueryContext;
import org.apache.lucene.queryparser.surround.parser.SurroundSyntaxAntlrParser.SimpleTermContext;
import org.apache.lucene.queryparser.surround.parser.SurroundSyntaxAntlrParser.WQueryContext;
import org.apache.lucene.queryparser.surround.query.AndQuery;
import org.apache.lucene.queryparser.surround.query.DistanceQuery;
import org.apache.lucene.queryparser.surround.query.FieldsQuery;
import org.apache.lucene.queryparser.surround.query.NotQuery;
import org.apache.lucene.queryparser.surround.query.OrQuery;
import org.apache.lucene.queryparser.surround.query.SrndPrefixQuery;
import org.apache.lucene.queryparser.surround.query.SrndQuery;
import org.apache.lucene.queryparser.surround.query.SrndTermQuery;
import org.apache.lucene.queryparser.surround.query.SrndTruncQuery;

/**
 * Parser of the surround query language. The only method that clients should need to call is {@link
 * #parse parse()}.
 *
 * <p>This parser generates queries that make use of position information (Span queries). It
 * provides positional operators (<code>w</code> and <code>n</code>) that accept a numeric distance,
 * as well as boolean operators (<code>and</code>, <code>or</code>, and <code>not</code>, wildcards
 * (<code>*</code> and <code>?</code>), quoting (with <code>"</code>), and boosting (via <code>^
 * </code>).
 *
 * <p>The operators (W, N, AND, OR, NOT) can be expressed lower-cased or upper-cased, and the
 * non-unary operators (everything but NOT) support both infix <code>(a AND b AND c)</code> and
 * prefix <code>AND(a, b,
 *   c)</code> notation.
 *
 * <p>The W and N operators express a positional relationship among their operands. W is ordered,
 * and N is unordered. The distance is 1 by default, meaning the operands are adjacent, or may be
 * provided as a prefix from 2-99. So, for example, 3W(a, b) means that terms a and b must appear
 * within three positions of each other, or in other words, up to two terms may appear between a and
 * b.
 *
 * <p>The syntax is defined by the ANTLR grammars in this package ({@code
 * SurroundSyntaxAntlrLexer.g4} and {@code SurroundSyntaxAntlrParser.g4}); this class converts the
 * parse tree into a {@link SrndQuery}.
 */
public class QueryParser {
  static final int MINIMUM_PREFIX_LENGTH = 3;
  static final int MINIMUM_CHARS_IN_TRUNC = 3;
  static final String TRUNCATION_ERROR_MESSAGE = "Too unrestrictive truncation: ";
  static final String BOOST_ERROR_MESSAGE = "Cannot handle boost value: ";

  /* CHECKME: These should be the same as for the tokenizer. How? */
  static final char TRUNCATOR = '*';
  static final char ANY_CHAR = '?';
  static final char FIELD_OPERATOR = ':';

  /** Aborts parsing on the first lexer or parser error. */
  private static final BaseErrorListener FAIL_ON_ERROR =
      new BaseErrorListener() {
        @Override
        public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e) {
          throw new SyntaxError(
              "Syntax error at line " + line + ", column " + (charPositionInLine + 1) + ": " + msg);
        }
      };

  /** Carries a syntax error out of ANTLR's lexer and parser; never leaves this class. */
  private static final class SyntaxError extends RuntimeException {
    SyntaxError(String message) {
      super(message, null, false, false);
    }
  }

  public static SrndQuery parse(String query) throws ParseException {
    QueryParser parser = new QueryParser();
    return parser.parse2(query);
  }

  public QueryParser() {}

  public SrndQuery parse2(String query) throws ParseException {
    SurroundSyntaxAntlrLexer lexer = new SurroundSyntaxAntlrLexer(CharStreams.fromString(query));
    lexer.removeErrorListeners();
    lexer.addErrorListener(FAIL_ON_ERROR);

    SurroundSyntaxAntlrParser parser = new SurroundSyntaxAntlrParser(new CommonTokenStream(lexer));
    parser.removeErrorListeners();
    parser.addErrorListener(FAIL_ON_ERROR);

    try {
      return fieldsQuery(parser.topSrndQuery().fieldsQuery());
    } catch (SyntaxError e) {
      throw new ParseException(e.getMessage());
    } catch (StackOverflowError _) {
      // Both the parser and the code building the query are recursive.
      throw new ParseException("The query is nested too deeply.");
    }
  }

  protected SrndQuery getFieldsQuery(SrndQuery q, ArrayList<String> fieldNames) {
    /* FIXME: check acceptable subquery: at least one subquery should not be
     * a fields query.
     */
    return new FieldsQuery(q, fieldNames, FIELD_OPERATOR);
  }

  protected SrndQuery getOrQuery(List<SrndQuery> queries, boolean infix, String operator) {
    return new OrQuery(queries, infix, operator);
  }

  protected SrndQuery getAndQuery(List<SrndQuery> queries, boolean infix, String operator) {
    return new AndQuery(queries, infix, operator);
  }

  protected SrndQuery getNotQuery(List<SrndQuery> queries, String operator) {
    return new NotQuery(queries, operator);
  }

  protected static int getOpDistance(String distanceOp) {
    /* W, 2W, 3W etc -> 1, 2 3, etc. Same for N, 2N ... */
    return distanceOp.length() == 1
        ? 1
        : Integer.parseInt(distanceOp.substring(0, distanceOp.length() - 1));
  }

  protected static void checkDistanceSubQueries(DistanceQuery distq, String opName)
      throws ParseException {
    String m = distq.distanceSubQueryNotAllowed();
    if (m != null) {
      throw new ParseException("Operator " + opName + ": " + m);
    }
  }

  protected SrndQuery getDistanceQuery(
      List<SrndQuery> queries, boolean infix, String operator, boolean ordered)
      throws ParseException {
    DistanceQuery dq =
        new DistanceQuery(queries, infix, getOpDistance(operator), operator, ordered);
    checkDistanceSubQueries(dq, operator);
    return dq;
  }

  protected SrndQuery getTermQuery(String term, boolean quoted) {
    return new SrndTermQuery(term, quoted);
  }

  protected boolean allowedSuffix(String suffixed) {
    return (suffixed.length() - 1) >= MINIMUM_PREFIX_LENGTH;
  }

  protected SrndQuery getPrefixQuery(String prefix, boolean quoted) {
    return new SrndPrefixQuery(prefix, quoted, TRUNCATOR);
  }

  protected boolean allowedTruncation(String truncated) {
    /* At least 3 normal characters needed. */
    int nrNormalChars = 0;
    for (int i = 0; i < truncated.length(); i++) {
      char c = truncated.charAt(i);
      if ((c != TRUNCATOR) && (c != ANY_CHAR)) {
        nrNormalChars++;
      }
    }
    return nrNormalChars >= MINIMUM_CHARS_IN_TRUNC;
  }

  protected SrndQuery getTruncQuery(String truncated) {
    return new SrndTruncQuery(truncated);
  }

  /**
   * Signals a grammar alternative this class does not know about: the grammar has changed but the
   * code building queries has not.
   */
  private static AssertionError unhandled(ParserRuleContext ctx) {
    return new AssertionError(
        "Unhandled alternative of grammar rule '"
            + SurroundSyntaxAntlrParser.ruleNames[ctx.getRuleIndex()]
            + "': "
            + ctx.getText());
  }

  private SrndQuery fieldsQuery(FieldsQueryContext ctx) throws ParseException {
    ArrayList<String> fieldNames = null;
    if (!ctx.fieldNames.isEmpty()) {
      fieldNames = new ArrayList<>();
      for (Token fieldName : ctx.fieldNames) {
        fieldNames.add(fieldName.getText());
      }
    }
    SrndQuery q = orQuery(ctx.orQuery());
    return (fieldNames == null) ? q : getFieldsQuery(q, fieldNames);
  }

  private SrndQuery orQuery(OrQueryContext ctx) throws ParseException {
    if (ctx.operators.isEmpty()) {
      return andQuery(ctx.andQuery(0));
    }
    ArrayList<SrndQuery> queries = new ArrayList<>();
    for (AndQueryContext andQuery : ctx.andQuery()) {
      queries.add(andQuery(andQuery));
    }
    /* keep only last used operator */
    return getOrQuery(queries, true /* infix */, ctx.operators.getLast().getText());
  }

  private SrndQuery andQuery(AndQueryContext ctx) throws ParseException {
    if (ctx.operators.isEmpty()) {
      return notQuery(ctx.notQuery(0));
    }
    ArrayList<SrndQuery> queries = new ArrayList<>();
    for (NotQueryContext notQuery : ctx.notQuery()) {
      queries.add(notQuery(notQuery));
    }
    /* keep only last used operator */
    return getAndQuery(queries, true /* infix */, ctx.operators.getLast().getText());
  }

  private SrndQuery notQuery(NotQueryContext ctx) throws ParseException {
    if (ctx.operators.isEmpty()) {
      return nQuery(ctx.nQuery(0));
    }
    ArrayList<SrndQuery> queries = new ArrayList<>();
    for (NQueryContext nQuery : ctx.nQuery()) {
      queries.add(nQuery(nQuery));
    }
    /* keep only last used operator */
    return getNotQuery(queries, ctx.operators.getLast().getText());
  }

  private SrndQuery nQuery(NQueryContext ctx) throws ParseException {
    SrndQuery q = wQuery(ctx.wQuery(0));
    for (int i = 0; i < ctx.operators.size(); i++) {
      ArrayList<SrndQuery> queries = new ArrayList<>();
      queries.add(q); /* left associative */
      queries.add(wQuery(ctx.wQuery(i + 1)));
      q =
          getDistanceQuery(
              queries, true /* infix */, ctx.operators.get(i).getText(), false /* not ordered */);
    }
    return q;
  }

  private SrndQuery wQuery(WQueryContext ctx) throws ParseException {
    SrndQuery q = primaryQuery(ctx.primaryQuery(0));
    for (int i = 0; i < ctx.operators.size(); i++) {
      ArrayList<SrndQuery> queries = new ArrayList<>();
      queries.add(q); /* left associative */
      queries.add(primaryQuery(ctx.primaryQuery(i + 1)));
      q =
          getDistanceQuery(
              queries, true /* infix */, ctx.operators.get(i).getText(), true /* ordered */);
    }
    return q;
  }

  /** A bracketed weighted query or a weighted term. */
  private SrndQuery primaryQuery(PrimaryQueryContext ctx) throws ParseException {
    SrndQuery q;
    if (ctx.fieldsQuery() != null) {
      q = fieldsQuery(ctx.fieldsQuery());
    } else if (ctx.prefixOperatorQuery() != null) {
      q = prefixOperatorQuery(ctx.prefixOperatorQuery());
    } else if (ctx.simpleTerm() != null) {
      q = simpleTerm(ctx.simpleTerm());
    } else {
      throw unhandled(ctx);
    }

    for (Token weight : ctx.weights) {
      float f;
      try {
        f = Float.parseFloat(weight.getText());
      } catch (Exception floatExc) {
        throw new ParseException(BOOST_ERROR_MESSAGE + weight.getText() + " (" + floatExc + ")");
      }
      if (f <= 0.0) {
        throw new ParseException(BOOST_ERROR_MESSAGE + weight.getText());
      }
      q.setWeight(f * q.getWeight()); /* left associative, fwiw */
    }
    return q;
  }

  private SrndQuery prefixOperatorQuery(PrefixOperatorQueryContext ctx) throws ParseException {
    List<SrndQuery> queries = fieldsQueryList(ctx.fieldsQueryList());
    String operator = ctx.operator.getText();
    return switch (ctx.operator.getType()) {
      case SurroundSyntaxAntlrParser.OR -> getOrQuery(queries, false /* not infix */, operator);
      case SurroundSyntaxAntlrParser.AND -> getAndQuery(queries, false /* not infix */, operator);
      case SurroundSyntaxAntlrParser.N ->
          getDistanceQuery(queries, false /* not infix */, operator, false /* not ordered */);
      case SurroundSyntaxAntlrParser.W ->
          getDistanceQuery(queries, false /* not infix */, operator, true /* ordered */);
      default -> throw unhandled(ctx);
    };
  }

  private List<SrndQuery> fieldsQueryList(FieldsQueryListContext ctx) throws ParseException {
    ArrayList<SrndQuery> queries = new ArrayList<>();
    for (FieldsQueryContext fieldsQuery : ctx.fieldsQuery()) {
      queries.add(fieldsQuery(fieldsQuery));
    }
    return queries;
  }

  private SrndQuery simpleTerm(SimpleTermContext ctx) throws ParseException {
    String term = ctx.term.getText();
    switch (ctx.term.getType()) {
      case SurroundSyntaxAntlrParser.TERM -> {
        return getTermQuery(term, false /* not quoted */);
      }
      case SurroundSyntaxAntlrParser.QUOTED -> {
        return getTermQuery(term.substring(1, term.length() - 1), true /* quoted */);
      }
      /* ending in * */
      case SurroundSyntaxAntlrParser.SUFFIXTERM -> {
        if (!allowedSuffix(term)) {
          throw new ParseException(TRUNCATION_ERROR_MESSAGE + term);
        }
        return getPrefixQuery(term.substring(0, term.length() - 1), false /* not quoted */);
      }
      /* with at least one * or ? */
      case SurroundSyntaxAntlrParser.TRUNCTERM -> {
        if (!allowedTruncation(term)) {
          throw new ParseException(TRUNCATION_ERROR_MESSAGE + term);
        }
        return getTruncQuery(term);
      }
      /* eg. "9b-b,m"* */
      case SurroundSyntaxAntlrParser.TRUNCQUOTED -> {
        if ((term.length() - 3) < MINIMUM_PREFIX_LENGTH) {
          throw new ParseException(TRUNCATION_ERROR_MESSAGE + term);
        }
        return getPrefixQuery(term.substring(1, term.length() - 2), true /* quoted */);
      }
      default -> throw unhandled(ctx);
    }
  }
}
