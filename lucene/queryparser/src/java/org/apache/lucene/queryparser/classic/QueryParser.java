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
package org.apache.lucene.queryparser.classic;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.queryparser.classic.ClassicSyntaxAntlrParser.ClauseContext;
import org.apache.lucene.queryparser.classic.ClassicSyntaxAntlrParser.ConjClauseContext;
import org.apache.lucene.queryparser.classic.ClassicSyntaxAntlrParser.ModClauseContext;
import org.apache.lucene.queryparser.classic.ClassicSyntaxAntlrParser.QueryContext;
import org.apache.lucene.queryparser.classic.ClassicSyntaxAntlrParser.TermContext;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;

/**
 * Parser of the classic Lucene query syntax. The most important method is {@link #parse(String)}.
 *
 * <p>The syntax for query strings is as follows: A Query is a series of clauses. A clause may be
 * prefixed by:
 *
 * <ul>
 *   <li>a plus (<code>+</code>) or a minus (<code>-</code>) sign, indicating that the clause is
 *       required or prohibited respectively; or
 *   <li>a term followed by a colon, indicating the field to be searched. This enables one to
 *       construct queries which search multiple fields.
 * </ul>
 *
 * A clause may be either:
 *
 * <ul>
 *   <li>a term, indicating all the documents that contain this term; or
 *   <li>a nested query, enclosed in parentheses. Note that this may be used with a <code>+</code>/
 *       <code>-</code> prefix to require any of a set of terms.
 * </ul>
 *
 * Thus, in BNF, the query grammar is:
 *
 * <pre>
 *   Query  ::= ( Clause )*
 *   Clause ::= ["+", "-"] [&lt;TERM&gt; ":"] ( &lt;TERM&gt; | "(" Query ")" )
 * </pre>
 *
 * <p>Examples of appropriately formatted queries can be found in the <a
 * href="{@docRoot}/org/apache/lucene/queryparser/classic/package-summary.html#package.description">query
 * syntax documentation</a>.
 *
 * <p>In {@link TermRangeQuery}s, QueryParser tries to detect date values, e.g. <code>
 * date:[6/1/2005 TO 6/4/2005]</code> produces a range query that searches for "date" fields between
 * 2005-06-01 and 2005-06-04. Note that the format of the accepted input depends on {@link
 * #setLocale(Locale) the locale}. A {@link org.apache.lucene.document.DateTools.Resolution} has to
 * be set, if you want to use {@link DateTools} for date conversion.
 *
 * <p>The date resolution that shall be used for RangeQueries can be set using {@link
 * #setDateResolution(DateTools.Resolution)} or {@link #setDateResolution(String,
 * DateTools.Resolution)}. The former sets the default date resolution for all fields, whereas the
 * latter can be used to set field specific date resolutions. Field specific date resolutions take,
 * if set, precedence over the default date resolution.
 *
 * <p>If you don't use {@link DateTools} in your index, you can create your own query parser that
 * inherits QueryParser and overwrites {@link #getRangeQuery(String, String, String, boolean,
 * boolean)} to use a different method for date conversion.
 *
 * <p>Note that QueryParser is <em>not</em> thread-safe.
 *
 * <p><b>NOTE</b>: there is a new QueryParser in contrib, which matches the same syntax as this
 * class, but is more modular, enabling substantial customization to how a query is created.
 *
 * <p>The syntax is defined by the ANTLR grammars in this package ({@code
 * ClassicSyntaxAntlrLexer.g4} and {@code ClassicSyntaxAntlrParser.g4}); this class converts the
 * parse tree into a {@link Query}.
 */
public class QueryParser extends QueryParserBase {
  /**
   * The default operator for parsing queries. Use {@link QueryParserBase#setDefaultOperator} to
   * change it.
   */
  public static enum Operator {
    OR,
    AND
  }

  /** default split on whitespace behavior */
  public static final boolean DEFAULT_SPLIT_ON_WHITESPACE = false;

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

  private boolean splitOnWhitespace = DEFAULT_SPLIT_ON_WHITESPACE;

  /**
   * Create a query parser.
   *
   * @param f the default field for query terms.
   * @param a used to find terms in the query text.
   */
  public QueryParser(String f, Analyzer a) {
    init(f, a);
  }

  /**
   * Set to true if phrase queries will be automatically generated when the analyzer returns more
   * than one term from whitespace delimited text. NOTE: this behavior may not be suitable for all
   * languages.
   *
   * <p>Set to false if phrase queries should only be generated when surrounded by double quotes.
   *
   * <p>The combination splitOnWhitespace=false and autoGeneratePhraseQueries=true is disallowed.
   * See <a href="https://issues.apache.org/jira/browse/LUCENE-7533">LUCENE-7533</a>.
   */
  @Override
  public void setAutoGeneratePhraseQueries(boolean value) {
    if (splitOnWhitespace == false && value == true) {
      throw new IllegalArgumentException(
          "setAutoGeneratePhraseQueries(true) is disallowed when getSplitOnWhitespace() == false");
    }
    this.autoGeneratePhraseQueries = value;
  }

  /**
   * @see #setSplitOnWhitespace(boolean)
   */
  public boolean getSplitOnWhitespace() {
    return splitOnWhitespace;
  }

  /**
   * Whether query text should be split on whitespace prior to analysis. Default is <code>
   * {@value #DEFAULT_SPLIT_ON_WHITESPACE}</code>.
   *
   * <p>The combination splitOnWhitespace=false and autoGeneratePhraseQueries=true is disallowed.
   * See <a href="https://issues.apache.org/jira/browse/LUCENE-7533">LUCENE-7533</a>.
   */
  public void setSplitOnWhitespace(boolean splitOnWhitespace) {
    if (splitOnWhitespace == false && getAutoGeneratePhraseQueries() == true) {
      throw new IllegalArgumentException(
          "setSplitOnWhitespace(false) is disallowed when getAutoGeneratePhraseQueries() == true");
    }
    this.splitOnWhitespace = splitOnWhitespace;
  }

  @Override
  Query parseTopLevelQuery(String query, String field) throws ParseException {
    ClassicSyntaxAntlrLexer lexer = new ClassicSyntaxAntlrLexer(CharStreams.fromString(query));
    lexer.removeErrorListeners();
    lexer.addErrorListener(FAIL_ON_ERROR);

    ClassicSyntaxAntlrParser parser = new ClassicSyntaxAntlrParser(new CommonTokenStream(lexer));
    parser.removeErrorListeners();
    parser.addErrorListener(FAIL_ON_ERROR);

    try {
      return query(parser.topLevelQuery().query(), field);
    } catch (SyntaxError e) {
      throw new ParseException(e.getMessage());
    } catch (StackOverflowError _) {
      // Both the parser and the code building the query are recursive.
      throw new ParseException("The query is nested too deeply.");
    }
  }

  /**
   * Signals a grammar alternative this class does not know about: the grammar has changed but the
   * code building queries has not.
   */
  private static AssertionError unhandled(ParserRuleContext ctx) {
    return new AssertionError(
        "Unhandled alternative of grammar rule '"
            + ClassicSyntaxAntlrParser.ruleNames[ctx.getRuleIndex()]
            + "': "
            + ctx.getText());
  }

  /** Returns the text of an optional token or null if the token is absent. */
  private static String text(Token token) {
    return token == null ? null : token.getText();
  }

  private Query query(QueryContext ctx, String field) throws ParseException {
    // Top-level clauses: the first one has no conjunction.
    List<ModClauseContext> modClauses = new ArrayList<>();
    List<Token> conjunctions = new ArrayList<>();
    modClauses.add(ctx.modClause());
    conjunctions.add(null);
    for (ConjClauseContext conjClause : ctx.conjClause()) {
      modClauses.add(conjClause.modClause());
      conjunctions.add(conjClause.conjunction);
    }

    List<BooleanClause> clauses = new ArrayList<>();
    Query firstQuery = null;
    for (int i = 0; i < modClauses.size(); ) {
      int multiTermEnd = multiTermEnd(modClauses, conjunctions, i);
      if (multiTermEnd > i) {
        Query q = multiTerm(modClauses.subList(i, multiTermEnd), field, clauses);
        if (i == 0) {
          firstQuery = q;
        }
        i = multiTermEnd;
      } else {
        ModClauseContext modClause = modClauses.get(i);
        int conj = conjunction(modClause, conjunctions.get(i));
        int mods = modifiers(modClause);
        Query q = clause(modClause.clause(), field);
        addClause(clauses, conj, mods, q);
        if (i == 0 && mods == MOD_NONE) {
          firstQuery = q;
        }
        i++;
      }
    }

    if (clauses.size() == 1 && firstQuery != null) {
      return firstQuery;
    } else {
      return getBooleanQuery(clauses);
    }
  }

  /**
   * Returns the end (exclusive) of the multi-term starting at the given clause or {@code start} if
   * there is none.
   *
   * <p>A multi-term is two or more adjacent plain terms. A term followed by something that binds to
   * it (an explicit conjunction or a '*') is not part of a multi-term, unless it is the first one.
   */
  private static int multiTermEnd(
      List<ModClauseContext> modClauses, List<Token> conjunctions, int start) {
    if (!isPlainTerm(modClauses, conjunctions, start)
        || !isMultiTermPart(modClauses, conjunctions, start + 1)) {
      return start;
    }
    int end = start + 2;
    while (isMultiTermPart(modClauses, conjunctions, end)) {
      end++;
    }
    return end;
  }

  private static boolean isMultiTermPart(
      List<ModClauseContext> modClauses, List<Token> conjunctions, int i) {
    if (!isPlainTerm(modClauses, conjunctions, i)) {
      return false;
    }
    // The last clause is followed by the end of the (sub)query.
    return i + 1 == modClauses.size()
        || (conjunctions.get(i + 1) == null
            && modClauses.get(i + 1).getStart().getType() != ClassicSyntaxAntlrParser.STAR);
  }

  /** Returns true if the given clause is a term and nothing else: no field, no boost etc. */
  private static boolean isPlainTerm(
      List<ModClauseContext> modClauses, List<Token> conjunctions, int i) {
    if (i >= modClauses.size() || conjunctions.get(i) != null) {
      return false;
    }
    ModClauseContext modClause = modClauses.get(i);
    ClauseContext clause = modClause.clause();
    TermContext term = clause.term();
    return modClause.modifier == null
        && clause.field == null
        && term != null
        && term.bare != null
        && term.bare.getType() == ClassicSyntaxAntlrParser.TERM
        && term.boost == null
        && term.fuzzySlop == null;
  }

  private static int conjunction(ModClauseContext ctx, Token conjunction) {
    if (conjunction == null) {
      return CONJ_NONE;
    }
    return switch (conjunction.getType()) {
      case ClassicSyntaxAntlrParser.AND -> CONJ_AND;
      case ClassicSyntaxAntlrParser.OR -> CONJ_OR;
      default -> throw unhandled(ctx);
    };
  }

  private static int modifiers(ModClauseContext ctx) {
    if (ctx.modifier == null) {
      return MOD_NONE;
    }
    return switch (ctx.modifier.getType()) {
      case ClassicSyntaxAntlrParser.PLUS -> MOD_REQ;
      case ClassicSyntaxAntlrParser.MINUS, ClassicSyntaxAntlrParser.NOT -> MOD_NOT;
      default -> throw unhandled(ctx);
    };
  }

  private Query clause(ClauseContext ctx, String field) throws ParseException {
    if (ctx.field != null) {
      field =
          ctx.field.getType() == ClassicSyntaxAntlrParser.STAR
              ? "*"
              : discardEscapeChar(ctx.field.getText());
    }

    Query q;
    if (ctx.term() != null) {
      q = term(ctx.term(), field);
    } else if (ctx.query() != null) {
      q = query(ctx.query(), field);
    } else {
      throw unhandled(ctx);
    }
    return handleBoost(q, text(ctx.boost));
  }

  private Query term(TermContext ctx, String field) throws ParseException {
    Query q;
    if (ctx.bare != null) {
      String term = ctx.bare.getText();
      boolean prefix = false;
      boolean wildcard = false;
      boolean regexp = false;
      switch (ctx.bare.getType()) {
        case ClassicSyntaxAntlrParser.TERM, ClassicSyntaxAntlrParser.NUMBER -> {}
        case ClassicSyntaxAntlrParser.STAR, ClassicSyntaxAntlrParser.WILDTERM -> wildcard = true;
        case ClassicSyntaxAntlrParser.PREFIXTERM -> prefix = true;
        case ClassicSyntaxAntlrParser.REGEXPTERM -> regexp = true;
        // Drop the whitespace that follows the operator.
        case ClassicSyntaxAntlrParser.BAREOPER -> term = term.substring(0, 1);
        default -> throw unhandled(ctx);
      }
      q =
          handleBareTokenQuery(
              field, term, text(ctx.fuzzySlop), prefix, wildcard, ctx.fuzzySlop != null, regexp);
    } else if (ctx.open != null) {
      q =
          getRangeQuery(
              field,
              rangeBound(ctx.left),
              rangeBound(ctx.right),
              ctx.open.getType() == ClassicSyntaxAntlrParser.RANGEIN_START,
              ctx.close.getType() == ClassicSyntaxAntlrParser.RANGEIN_END);
    } else if (ctx.quoted != null) {
      q = handleQuotedTerm(field, text(ctx.quoted), text(ctx.fuzzySlop));
    } else {
      throw unhandled(ctx);
    }
    return handleBoost(q, text(ctx.boost));
  }

  /** Returns the lower or upper bound of a range or null if the range is open at that end. */
  private String rangeBound(Token token) throws ParseException {
    String text = token.getText();
    if (token.getType() == ClassicSyntaxAntlrParser.RANGE_QUOTED) {
      text = text.substring(1, text.length() - 1);
    } else if ("*".equals(text)) {
      return null;
    }
    return discardEscapeChar(text);
  }

  /** Returns the first query if splitOnWhitespace=true or otherwise the entire produced query */
  private Query multiTerm(List<ModClauseContext> terms, String field, List<BooleanClause> clauses)
      throws ParseException {
    if (splitOnWhitespace) {
      Query firstQuery = null;
      for (int i = 0; i < terms.size(); i++) {
        Query q = getFieldQuery(field, discardEscapeChar(terms.get(i).getText()), false);
        addClause(clauses, CONJ_NONE, MOD_NONE, q);
        if (i == 0) {
          firstQuery = q;
        }
      }
      return firstQuery;
    } else {
      // build up the text to send to analysis
      StringBuilder text = new StringBuilder();
      for (ModClauseContext term : terms) {
        if (!text.isEmpty()) {
          text.append(' ');
        }
        text.append(term.getText());
      }
      Query q = getFieldQuery(field, discardEscapeChar(text.toString()), false);
      addClause(clauses, CONJ_NONE, MOD_NONE, q);
      return q;
    }
  }
}
