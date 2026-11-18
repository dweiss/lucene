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
package org.apache.lucene.queryparser.flexible.standard.parser;

import static org.apache.lucene.queryparser.flexible.standard.parser.EscapeQuerySyntaxImpl.discardEscapeChar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.apache.lucene.queryparser.flexible.core.QueryNodeParseException;
import org.apache.lucene.queryparser.flexible.core.messages.QueryParserMessages;
import org.apache.lucene.queryparser.flexible.core.nodes.AndQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.BoostQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.GroupQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.OrQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QuotedFieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.SlopQueryNode;
import org.apache.lucene.queryparser.flexible.core.parser.SyntaxParser;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.standard.nodes.IntervalQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.MinShouldMatchNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.RegexpQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.TermRangeQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.After;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.AnalyzedText;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.AtLeast;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.Before;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.ContainedBy;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.Containing;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.Extend;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.FuzzyTerm;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.IntervalFunction;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.MaxGaps;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.MaxWidth;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.NonOverlapping;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.NotContainedBy;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.NotContaining;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.NotWithin;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.Or;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.Ordered;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.Overlapping;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.Phrase;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.Unordered;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.UnorderedNoOverlaps;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.Wildcard;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.Within;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.BoostContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.ClauseContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.ConjQueryContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.DisjQueryContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.FieldNameContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.FieldRangeExprContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.FuzzyOpContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.GroupingExprContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalAfterContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalAtLeastContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalBeforeContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalContainedByContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalContainingContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalExtendContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalFunContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalFunctionContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalFuzzyTermContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalMaxGapsContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalMaxWidthContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalNonOverlappingContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalNotContainedByContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalNotContainingContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalNotWithinContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalOrContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalOrderedContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalOverlappingContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalPhraseContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalTextContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalUnorderedContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalUnorderedNoOverlapsContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalWildcardContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.IntervalWithinContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.ModClauseContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.QueryContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.QuotedTermContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.TermContext;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxAntlrParser.TermRangeExprContext;
import org.apache.lucene.search.FuzzyQuery;

/**
 * Parser for the standard Lucene syntax.
 *
 * <p>The syntax is defined by the ANTLR grammars in this package ({@code
 * StandardSyntaxAntlrLexer.g4} and {@code StandardSyntaxAntlrParser.g4}); this class converts the
 * parse tree into a {@link QueryNode} tree. Instances are stateless and thread-safe.
 */
public class StandardSyntaxParser implements SyntaxParser {
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
              msg,
              line,
              charPositionInLine,
              offendingSymbol instanceof Token token ? token.getText() : "");
        }
      };

  /** Carries a syntax error out of ANTLR's lexer and parser; never leaves this class. */
  private static final class SyntaxError extends RuntimeException {
    final int line;
    final int column;
    final String errorToken;

    SyntaxError(String message, int line, int column, String errorToken) {
      super(message, null, false, false);
      this.line = line;
      this.column = column;
      this.errorToken = errorToken;
    }
  }

  /** Creates a new parser. */
  public StandardSyntaxParser() {}

  /**
   * Parses a query string, returning a {@link
   * org.apache.lucene.queryparser.flexible.core.nodes.QueryNode}.
   *
   * @param query the query string to be parsed.
   * @param field the default field, may be null.
   * @throws QueryNodeParseException if the parsing fails
   */
  @Override
  public QueryNode parse(CharSequence query, CharSequence field) throws QueryNodeParseException {
    String input = query.toString();

    StandardSyntaxAntlrLexer lexer = new StandardSyntaxAntlrLexer(CharStreams.fromString(input));
    lexer.removeErrorListeners();
    lexer.addErrorListener(FAIL_ON_ERROR);

    StandardSyntaxAntlrParser parser = new StandardSyntaxAntlrParser(new CommonTokenStream(lexer));
    parser.removeErrorListeners();
    parser.addErrorListener(FAIL_ON_ERROR);

    try {
      return new NodeBuilder(input).query(parser.topLevelQuery().query(), field);
    } catch (ParseException e) {
      e.setQuery(query);
      throw e;
    } catch (SyntaxError e) {
      throw new ParseException(query, e.getMessage(), e.line, e.column, e.errorToken);
    } catch (StackOverflowError _) {
      // Both the parser and the node builder are recursive.
      throw new ParseException(query, "the query is nested too deeply.", -1, -1, "");
    }
  }

  /** Builds the query node tree from the parse tree. */
  private static final class NodeBuilder {
    private final String input;

    /**
     * ANTLR's token offsets are in code points, query nodes use char offsets: these only differ if
     * the input has surrogate pairs.
     */
    private final boolean hasSurrogatePairs;

    NodeBuilder(String input) {
      this.input = input;
      this.hasSurrogatePairs = input.codePointCount(0, input.length()) != input.length();
    }

    /** Returns the char offset of the token's first character. */
    private int begin(Token token) {
      return charOffset(token.getStartIndex());
    }

    /** Returns the char offset following the token's last character. */
    private int end(Token token) {
      return charOffset(token.getStopIndex() + 1);
    }

    private int charOffset(int codePointOffset) {
      return hasSurrogatePairs ? input.offsetByCodePoints(0, codePointOffset) : codePointOffset;
    }

    /**
     * Signals a grammar alternative this class does not know about: the grammar has changed but the
     * code building query nodes has not.
     */
    private static AssertionError unhandled(ParserRuleContext ctx) {
      return new AssertionError(
          "Unhandled alternative of grammar rule '"
              + StandardSyntaxAntlrParser.ruleNames[ctx.getRuleIndex()]
              + "': "
              + ctx.getText());
    }

    private static int parseInt(Token token) {
      return Integer.parseInt(token.getText());
    }

    private static float parseFloat(Token token) {
      return Float.parseFloat(token.getText());
    }

    /** Returns the token's text without the first and the last character (quotes, typically). */
    private static String unquote(Token token) {
      String text = token.getText();
      return text.substring(1, text.length() - 1);
    }

    QueryNode query(QueryContext ctx, CharSequence field) throws ParseException {
      ArrayList<QueryNode> clauses = new ArrayList<>();
      for (DisjQueryContext disjQuery : ctx.disjQuery()) {
        clauses.add(disjQuery(disjQuery, field));
      }

      // Handle the case of a "pure" negation query which
      // needs to be wrapped as a boolean query, otherwise
      // the returned result drops the negation.
      if (clauses.size() == 1) {
        QueryNode first = clauses.get(0);
        if (first instanceof ModifierQueryNode modifierNode
            && modifierNode.getModifier() == ModifierQueryNode.Modifier.MOD_NOT) {
          clauses.set(0, new BooleanQueryNode(Collections.singletonList(first)));
        }
      }

      return clauses.size() == 1 ? clauses.get(0) : new BooleanQueryNode(clauses);
    }

    private QueryNode disjQuery(DisjQueryContext ctx, CharSequence field) throws ParseException {
      ArrayList<QueryNode> clauses = new ArrayList<>();
      for (ConjQueryContext conjQuery : ctx.conjQuery()) {
        clauses.add(conjQuery(conjQuery, field));
      }
      return clauses.size() == 1 ? clauses.get(0) : new OrQueryNode(clauses);
    }

    private QueryNode conjQuery(ConjQueryContext ctx, CharSequence field) throws ParseException {
      ArrayList<QueryNode> clauses = new ArrayList<>();
      for (ModClauseContext modClause : ctx.modClause()) {
        clauses.add(modClause(modClause, field));
      }
      return clauses.size() == 1 ? clauses.get(0) : new AndQueryNode(clauses);
    }

    private QueryNode modClause(ModClauseContext ctx, CharSequence field) throws ParseException {
      QueryNode q = clause(ctx.clause(), field);
      if (ctx.modifier != null) {
        ModifierQueryNode.Modifier modifier =
            switch (ctx.modifier.getType()) {
              case StandardSyntaxAntlrParser.PLUS -> ModifierQueryNode.Modifier.MOD_REQ;
              case StandardSyntaxAntlrParser.MINUS, StandardSyntaxAntlrParser.NOT ->
                  ModifierQueryNode.Modifier.MOD_NOT;
              default -> throw unhandled(ctx);
            };
        q = new ModifierQueryNode(q, modifier);
      }
      return q;
    }

    private QueryNode clause(ClauseContext ctx, CharSequence field) throws ParseException {
      if (ctx.fieldRangeExpr() != null) {
        return fieldRangeExpr(ctx.fieldRangeExpr());
      }

      if (ctx.fieldName() != null) {
        field = fieldName(ctx.fieldName());
      }

      if (ctx.term() != null) {
        return term(ctx.term(), field);
      } else if (ctx.groupingExpr() != null) {
        return groupingExpr(ctx.groupingExpr(), field);
      } else if (ctx.intervalFunction() != null) {
        return new IntervalQueryNode(
            field == null ? null : field.toString(), intervalFunction(ctx.intervalFunction()));
      } else {
        throw unhandled(ctx);
      }
    }

    /** A field name, with escape characters stripped. */
    private static CharSequence fieldName(FieldNameContext ctx) throws ParseException {
      return discardEscapeChar(ctx.TERM().getText());
    }

    private QueryNode groupingExpr(GroupingExprContext ctx, CharSequence field)
        throws ParseException {
      QueryNode q = query(ctx.query(), field);
      if (ctx.boost() != null) {
        q = boost(ctx.boost(), q);
      }

      if (ctx.minShouldMatch != null) {
        return new MinShouldMatchNode(parseInt(ctx.minShouldMatch), new GroupQueryNode(q));
      } else {
        return new GroupQueryNode(q);
      }
    }

    private static QueryNode boost(BoostContext ctx, QueryNode node) {
      return new BoostQueryNode(node, parseFloat(ctx.NUMBER().getSymbol()));
    }

    private QueryNode fuzzyOp(FuzzyOpContext ctx, CharSequence field, Token term)
        throws ParseException {
      float fms = FuzzyQuery.defaultMaxEdits;
      if (ctx.similarity != null) {
        fms = parseFloat(ctx.similarity);
        if (fms < 0.0f) {
          throw new ParseException(
              new MessageImpl(QueryParserMessages.INVALID_SYNTAX_FUZZY_LIMITS));
        } else if (fms >= 1.0f && fms != (int) fms) {
          throw new ParseException(new MessageImpl(QueryParserMessages.INVALID_SYNTAX_FUZZY_EDITS));
        }
      }
      return new FuzzyQueryNode(
          field, discardEscapeChar(term.getText()), fms, begin(term), end(term));
    }

    private TermRangeQueryNode fieldRangeExpr(FieldRangeExprContext ctx) throws ParseException {
      CharSequence field = fieldName(ctx.fieldName());

      Token value = ctx.value;
      CharSequence text =
          discardEscapeChar(
              value.getType() == StandardSyntaxAntlrParser.QUOTED
                  ? unquote(value)
                  : value.getText());
      FieldQueryNode bound = new FieldQueryNode(field, text, begin(value), end(value));
      FieldQueryNode open = new FieldQueryNode(field, "*", begin(value), end(value));

      return switch (ctx.operator.getType()) {
        case StandardSyntaxAntlrParser.OP_LESSTHAN ->
            new TermRangeQueryNode(open, bound, true, false);
        case StandardSyntaxAntlrParser.OP_LESSTHANEQ ->
            new TermRangeQueryNode(open, bound, true, true);
        case StandardSyntaxAntlrParser.OP_MORETHAN ->
            new TermRangeQueryNode(bound, open, false, true);
        case StandardSyntaxAntlrParser.OP_MORETHANEQ ->
            new TermRangeQueryNode(bound, open, true, true);
        default -> throw unhandled(ctx);
      };
    }

    private QueryNode term(TermContext ctx, CharSequence field) throws ParseException {
      QueryNode q;
      if (ctx.REGEXPTERM() != null) {
        String v = unquote(ctx.REGEXPTERM().getSymbol());
        q = new RegexpQueryNode(field, v, 0, v.length());
      } else if (ctx.termRangeExpr() != null) {
        q = termRangeExpr(ctx.termRangeExpr(), field);
      } else if (ctx.quotedTerm() != null) {
        q = quotedTerm(ctx.quotedTerm(), field);
      } else if (ctx.TERM() != null || ctx.NUMBER() != null) {
        Token term = (ctx.TERM() != null ? ctx.TERM() : ctx.NUMBER()).getSymbol();
        if (ctx.fuzzyOp() != null) {
          q = fuzzyOp(ctx.fuzzyOp(), field, term);
        } else {
          q = new FieldQueryNode(field, discardEscapeChar(term.getText()), begin(term), end(term));
        }
      } else {
        throw unhandled(ctx);
      }

      if (ctx.boost() != null) {
        q = boost(ctx.boost(), q);
      }
      return q;
    }

    private QueryNode quotedTerm(QuotedTermContext ctx, CharSequence field) throws ParseException {
      Token term = ctx.QUOTED().getSymbol();
      QueryNode q =
          new QuotedFieldQueryNode(
              field, discardEscapeChar(unquote(term)), begin(term) + 1, end(term) - 1);
      if (ctx.slop != null) {
        q = new SlopQueryNode(q, parseInt(ctx.slop));
      }
      return q;
    }

    private TermRangeQueryNode termRangeExpr(TermRangeExprContext ctx, CharSequence field)
        throws ParseException {
      return new TermRangeQueryNode(
          rangeBound(ctx.left, field),
          rangeBound(ctx.right, field),
          ctx.open.getType() == StandardSyntaxAntlrParser.RANGEIN_START,
          ctx.close.getType() == StandardSyntaxAntlrParser.RANGEIN_END);
    }

    private FieldQueryNode rangeBound(Token token, CharSequence field) throws ParseException {
      String text =
          token.getType() == StandardSyntaxAntlrParser.RANGE_QUOTED
              ? unquote(token)
              : token.getText();
      return new FieldQueryNode(field, discardEscapeChar(text), begin(token), end(token));
    }

    private IntervalFunction intervalFun(IntervalFunContext ctx) {
      if (ctx.intervalFunction() != null) {
        return intervalFunction(ctx.intervalFunction());
      } else if (ctx.intervalText() != null) {
        return intervalText(ctx.intervalText());
      } else {
        throw unhandled(ctx);
      }
    }

    private List<IntervalFunction> intervalFuns(List<IntervalFunContext> ctxs) {
      ArrayList<IntervalFunction> sources = new ArrayList<>();
      for (IntervalFunContext ctx : ctxs) {
        sources.add(intervalFun(ctx));
      }
      return sources;
    }

    private IntervalFunction intervalFunction(IntervalFunctionContext ctx) {
      return switch (ctx.getChild(ParserRuleContext.class, 0)) {
        case IntervalAtLeastContext fn ->
            new AtLeast(parseInt(fn.minShouldMatch), intervalFuns(fn.sources));
        case IntervalMaxWidthContext fn ->
            new MaxWidth(parseInt(fn.maxWidth), intervalFun(fn.source));
        case IntervalMaxGapsContext fn -> new MaxGaps(parseInt(fn.maxGaps), intervalFun(fn.source));
        case IntervalOrderedContext fn -> new Ordered(intervalFuns(fn.sources));
        case IntervalUnorderedContext fn -> new Unordered(intervalFuns(fn.sources));
        case IntervalUnorderedNoOverlapsContext fn ->
            new UnorderedNoOverlaps(intervalFun(fn.a), intervalFun(fn.b));
        case IntervalOrContext fn -> new Or(intervalFuns(fn.sources));
        case IntervalWildcardContext fn ->
            new Wildcard(
                intervalTerm(fn.wildcard),
                fn.maxExpansions == null ? 0 : parseInt(fn.maxExpansions));
        case IntervalAfterContext fn ->
            new After(intervalFun(fn.source), intervalFun(fn.reference));
        case IntervalBeforeContext fn ->
            new Before(intervalFun(fn.source), intervalFun(fn.reference));
        case IntervalPhraseContext fn -> new Phrase(intervalFuns(fn.sources));
        case IntervalContainingContext fn ->
            new Containing(intervalFun(fn.big), intervalFun(fn.small));
        case IntervalNotContainingContext fn ->
            new NotContaining(intervalFun(fn.minuend), intervalFun(fn.subtrahend));
        case IntervalContainedByContext fn ->
            new ContainedBy(intervalFun(fn.small), intervalFun(fn.big));
        case IntervalNotContainedByContext fn ->
            new NotContainedBy(intervalFun(fn.small), intervalFun(fn.big));
        case IntervalWithinContext fn ->
            new Within(intervalFun(fn.source), parseInt(fn.positions), intervalFun(fn.reference));
        case IntervalNotWithinContext fn ->
            new NotWithin(
                intervalFun(fn.minuend), parseInt(fn.positions), intervalFun(fn.subtrahend));
        case IntervalOverlappingContext fn ->
            new Overlapping(intervalFun(fn.source), intervalFun(fn.reference));
        case IntervalNonOverlappingContext fn ->
            new NonOverlapping(intervalFun(fn.minuend), intervalFun(fn.subtrahend));
        case IntervalExtendContext fn ->
            new Extend(intervalFun(fn.source), parseInt(fn.before), parseInt(fn.after));
        case IntervalFuzzyTermContext fn ->
            new FuzzyTerm(
                intervalTerm(fn.fuzzyTerm),
                fn.maxEdits == null ? null : parseInt(fn.maxEdits),
                fn.maxExpansions == null ? null : parseInt(fn.maxExpansions));
        default -> throw unhandled(ctx);
      };
    }

    private static IntervalFunction intervalText(IntervalTextContext ctx) {
      return new AnalyzedText(intervalTerm(ctx.getStart()));
    }

    /** The text of a term inside an interval function, with any quotes removed. */
    private static String intervalTerm(Token token) {
      return token.getType() == StandardSyntaxAntlrParser.QUOTED ? unquote(token) : token.getText();
    }
  }
}
