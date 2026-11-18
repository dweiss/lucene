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

// ANTLR GENERATED CODE: DO NOT EDIT.

package org.apache.lucene.queryparser.flexible.standard.parser;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;

@SuppressWarnings({
  "all",
  "warnings",
  "unchecked",
  "unused",
  "cast",
  "CheckReturnValue",
  "this-escape"
})
class StandardSyntaxAntlrParser extends Parser {
  static {
    RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION);
  }

  protected static final DFA[] _decisionToDFA;
  protected static final PredictionContextCache _sharedContextCache = new PredictionContextCache();
  public static final int WS = 1,
      AND = 2,
      OR = 3,
      NOT = 4,
      FN_PREFIX = 5,
      PLUS = 6,
      MINUS = 7,
      RPAREN = 8,
      OP_COLON = 9,
      OP_EQUAL = 10,
      OP_LESSTHAN = 11,
      OP_LESSTHANEQ = 12,
      OP_MORETHAN = 13,
      OP_MORETHANEQ = 14,
      CARAT = 15,
      TILDE = 16,
      AT = 17,
      QUOTED = 18,
      NUMBER = 19,
      TERM = 20,
      REGEXPTERM = 21,
      RANGEIN_START = 22,
      RANGEEX_START = 23,
      LPAREN = 24,
      FUNCTION_WS = 25,
      ATLEAST = 26,
      AFTER = 27,
      BEFORE = 28,
      CONTAINED_BY = 29,
      CONTAINING = 30,
      EXTEND = 31,
      FN_OR = 32,
      FUZZYTERM = 33,
      MAXGAPS = 34,
      MAXWIDTH = 35,
      NON_OVERLAPPING = 36,
      NOT_CONTAINED_BY = 37,
      NOT_CONTAINING = 38,
      NOT_WITHIN = 39,
      ORDERED = 40,
      OVERLAPPING = 41,
      PHRASE = 42,
      UNORDERED = 43,
      UNORDERED_NO_OVERLAPS = 44,
      WILDCARD = 45,
      WITHIN = 46,
      RANGE_WS = 47,
      RANGE_TO = 48,
      RANGEIN_END = 49,
      RANGEEX_END = 50,
      RANGE_QUOTED = 51,
      RANGE_GOOP = 52;
  public static final int RULE_topLevelQuery = 0,
      RULE_query = 1,
      RULE_disjQuery = 2,
      RULE_conjQuery = 3,
      RULE_modClause = 4,
      RULE_clause = 5,
      RULE_fieldName = 6,
      RULE_groupingExpr = 7,
      RULE_intervalFun = 8,
      RULE_intervalFunction = 9,
      RULE_intervalAtLeast = 10,
      RULE_intervalMaxWidth = 11,
      RULE_intervalMaxGaps = 12,
      RULE_intervalUnordered = 13,
      RULE_intervalUnorderedNoOverlaps = 14,
      RULE_intervalOrdered = 15,
      RULE_intervalOr = 16,
      RULE_intervalPhrase = 17,
      RULE_intervalBefore = 18,
      RULE_intervalAfter = 19,
      RULE_intervalContaining = 20,
      RULE_intervalNotContaining = 21,
      RULE_intervalContainedBy = 22,
      RULE_intervalNotContainedBy = 23,
      RULE_intervalWithin = 24,
      RULE_intervalExtend = 25,
      RULE_intervalNotWithin = 26,
      RULE_intervalOverlapping = 27,
      RULE_intervalNonOverlapping = 28,
      RULE_intervalWildcard = 29,
      RULE_intervalFuzzyTerm = 30,
      RULE_intervalText = 31,
      RULE_boost = 32,
      RULE_fuzzyOp = 33,
      RULE_fieldRangeExpr = 34,
      RULE_term = 35,
      RULE_quotedTerm = 36,
      RULE_termRangeExpr = 37;

  private static String[] makeRuleNames() {
    return new String[] {
      "topLevelQuery",
      "query",
      "disjQuery",
      "conjQuery",
      "modClause",
      "clause",
      "fieldName",
      "groupingExpr",
      "intervalFun",
      "intervalFunction",
      "intervalAtLeast",
      "intervalMaxWidth",
      "intervalMaxGaps",
      "intervalUnordered",
      "intervalUnorderedNoOverlaps",
      "intervalOrdered",
      "intervalOr",
      "intervalPhrase",
      "intervalBefore",
      "intervalAfter",
      "intervalContaining",
      "intervalNotContaining",
      "intervalContainedBy",
      "intervalNotContainedBy",
      "intervalWithin",
      "intervalExtend",
      "intervalNotWithin",
      "intervalOverlapping",
      "intervalNonOverlapping",
      "intervalWildcard",
      "intervalFuzzyTerm",
      "intervalText",
      "boost",
      "fuzzyOp",
      "fieldRangeExpr",
      "term",
      "quotedTerm",
      "termRangeExpr"
    };
  }

  public static final String[] ruleNames = makeRuleNames();

  private static String[] makeLiteralNames() {
    return new String[] {
      null,
      null,
      null,
      null,
      null,
      "'fn:'",
      "'+'",
      "'-'",
      "')'",
      "':'",
      "'='",
      "'<'",
      "'<='",
      "'>'",
      "'>='",
      "'^'",
      "'~'",
      "'@'",
      null,
      null,
      null,
      null,
      "'['",
      "'{'",
      "'('",
      null,
      null,
      "'after'",
      "'before'",
      null,
      "'containing'",
      "'extend'",
      "'or'",
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      "'ordered'",
      "'overlapping'",
      "'phrase'",
      "'unordered'",
      null,
      "'wildcard'",
      "'within'",
      null,
      "'TO'",
      "']'",
      "'}'"
    };
  }

  private static final String[] _LITERAL_NAMES = makeLiteralNames();

  private static String[] makeSymbolicNames() {
    return new String[] {
      null,
      "WS",
      "AND",
      "OR",
      "NOT",
      "FN_PREFIX",
      "PLUS",
      "MINUS",
      "RPAREN",
      "OP_COLON",
      "OP_EQUAL",
      "OP_LESSTHAN",
      "OP_LESSTHANEQ",
      "OP_MORETHAN",
      "OP_MORETHANEQ",
      "CARAT",
      "TILDE",
      "AT",
      "QUOTED",
      "NUMBER",
      "TERM",
      "REGEXPTERM",
      "RANGEIN_START",
      "RANGEEX_START",
      "LPAREN",
      "FUNCTION_WS",
      "ATLEAST",
      "AFTER",
      "BEFORE",
      "CONTAINED_BY",
      "CONTAINING",
      "EXTEND",
      "FN_OR",
      "FUZZYTERM",
      "MAXGAPS",
      "MAXWIDTH",
      "NON_OVERLAPPING",
      "NOT_CONTAINED_BY",
      "NOT_CONTAINING",
      "NOT_WITHIN",
      "ORDERED",
      "OVERLAPPING",
      "PHRASE",
      "UNORDERED",
      "UNORDERED_NO_OVERLAPS",
      "WILDCARD",
      "WITHIN",
      "RANGE_WS",
      "RANGE_TO",
      "RANGEIN_END",
      "RANGEEX_END",
      "RANGE_QUOTED",
      "RANGE_GOOP"
    };
  }

  private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
  public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

  /**
   * @deprecated Use {@link #VOCABULARY} instead.
   */
  @Deprecated public static final String[] tokenNames;

  static {
    tokenNames = new String[_SYMBOLIC_NAMES.length];
    for (int i = 0; i < tokenNames.length; i++) {
      tokenNames[i] = VOCABULARY.getLiteralName(i);
      if (tokenNames[i] == null) {
        tokenNames[i] = VOCABULARY.getSymbolicName(i);
      }

      if (tokenNames[i] == null) {
        tokenNames[i] = "<INVALID>";
      }
    }
  }

  @Override
  @Deprecated
  public String[] getTokenNames() {
    return tokenNames;
  }

  @Override
  public Vocabulary getVocabulary() {
    return VOCABULARY;
  }

  @Override
  public String getGrammarFileName() {
    return "StandardSyntaxAntlrParser.g4";
  }

  @Override
  public String[] getRuleNames() {
    return ruleNames;
  }

  @Override
  public String getSerializedATN() {
    return _serializedATN;
  }

  @Override
  public ATN getATN() {
    return _ATN;
  }

  public StandardSyntaxAntlrParser(TokenStream input) {
    super(input);
    _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
  }

  @SuppressWarnings("CheckReturnValue")
  public static class TopLevelQueryContext extends ParserRuleContext {
    public QueryContext query() {
      return getRuleContext(QueryContext.class, 0);
    }

    public TerminalNode EOF() {
      return getToken(StandardSyntaxAntlrParser.EOF, 0);
    }

    public TopLevelQueryContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_topLevelQuery;
    }
  }

  public final TopLevelQueryContext topLevelQuery() throws RecognitionException {
    TopLevelQueryContext _localctx = new TopLevelQueryContext(_ctx, getState());
    enterRule(_localctx, 0, RULE_topLevelQuery);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(76);
        query();
        setState(77);
        match(EOF);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class QueryContext extends ParserRuleContext {
    public List<DisjQueryContext> disjQuery() {
      return getRuleContexts(DisjQueryContext.class);
    }

    public DisjQueryContext disjQuery(int i) {
      return getRuleContext(DisjQueryContext.class, i);
    }

    public QueryContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_query;
    }
  }

  public final QueryContext query() throws RecognitionException {
    QueryContext _localctx = new QueryContext(_ctx, getState());
    enterRule(_localctx, 2, RULE_query);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(80);
        _errHandler.sync(this);
        _la = _input.LA(1);
        do {
          {
            {
              setState(79);
              disjQuery();
            }
          }
          setState(82);
          _errHandler.sync(this);
          _la = _input.LA(1);
        } while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 33292528L) != 0));
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class DisjQueryContext extends ParserRuleContext {
    public List<ConjQueryContext> conjQuery() {
      return getRuleContexts(ConjQueryContext.class);
    }

    public ConjQueryContext conjQuery(int i) {
      return getRuleContext(ConjQueryContext.class, i);
    }

    public List<TerminalNode> OR() {
      return getTokens(StandardSyntaxAntlrParser.OR);
    }

    public TerminalNode OR(int i) {
      return getToken(StandardSyntaxAntlrParser.OR, i);
    }

    public DisjQueryContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_disjQuery;
    }
  }

  public final DisjQueryContext disjQuery() throws RecognitionException {
    DisjQueryContext _localctx = new DisjQueryContext(_ctx, getState());
    enterRule(_localctx, 4, RULE_disjQuery);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(84);
        conjQuery();
        setState(89);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == OR) {
          {
            {
              setState(85);
              match(OR);
              setState(86);
              conjQuery();
            }
          }
          setState(91);
          _errHandler.sync(this);
          _la = _input.LA(1);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class ConjQueryContext extends ParserRuleContext {
    public List<ModClauseContext> modClause() {
      return getRuleContexts(ModClauseContext.class);
    }

    public ModClauseContext modClause(int i) {
      return getRuleContext(ModClauseContext.class, i);
    }

    public List<TerminalNode> AND() {
      return getTokens(StandardSyntaxAntlrParser.AND);
    }

    public TerminalNode AND(int i) {
      return getToken(StandardSyntaxAntlrParser.AND, i);
    }

    public ConjQueryContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_conjQuery;
    }
  }

  public final ConjQueryContext conjQuery() throws RecognitionException {
    ConjQueryContext _localctx = new ConjQueryContext(_ctx, getState());
    enterRule(_localctx, 6, RULE_conjQuery);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(92);
        modClause();
        setState(97);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == AND) {
          {
            {
              setState(93);
              match(AND);
              setState(94);
              modClause();
            }
          }
          setState(99);
          _errHandler.sync(this);
          _la = _input.LA(1);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class ModClauseContext extends ParserRuleContext {
    public Token modifier;

    public ClauseContext clause() {
      return getRuleContext(ClauseContext.class, 0);
    }

    public TerminalNode PLUS() {
      return getToken(StandardSyntaxAntlrParser.PLUS, 0);
    }

    public TerminalNode MINUS() {
      return getToken(StandardSyntaxAntlrParser.MINUS, 0);
    }

    public TerminalNode NOT() {
      return getToken(StandardSyntaxAntlrParser.NOT, 0);
    }

    public ModClauseContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_modClause;
    }
  }

  public final ModClauseContext modClause() throws RecognitionException {
    ModClauseContext _localctx = new ModClauseContext(_ctx, getState());
    enterRule(_localctx, 8, RULE_modClause);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(101);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 208L) != 0)) {
          {
            setState(100);
            ((ModClauseContext) _localctx).modifier = _input.LT(1);
            _la = _input.LA(1);
            if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 208L) != 0))) {
              ((ModClauseContext) _localctx).modifier = (Token) _errHandler.recoverInline(this);
            } else {
              if (_input.LA(1) == Token.EOF) matchedEOF = true;
              _errHandler.reportMatch(this);
              consume();
            }
          }
        }

        setState(103);
        clause();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class ClauseContext extends ParserRuleContext {
    public FieldRangeExprContext fieldRangeExpr() {
      return getRuleContext(FieldRangeExprContext.class, 0);
    }

    public TermContext term() {
      return getRuleContext(TermContext.class, 0);
    }

    public GroupingExprContext groupingExpr() {
      return getRuleContext(GroupingExprContext.class, 0);
    }

    public IntervalFunctionContext intervalFunction() {
      return getRuleContext(IntervalFunctionContext.class, 0);
    }

    public FieldNameContext fieldName() {
      return getRuleContext(FieldNameContext.class, 0);
    }

    public TerminalNode OP_COLON() {
      return getToken(StandardSyntaxAntlrParser.OP_COLON, 0);
    }

    public TerminalNode OP_EQUAL() {
      return getToken(StandardSyntaxAntlrParser.OP_EQUAL, 0);
    }

    public ClauseContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_clause;
    }
  }

  public final ClauseContext clause() throws RecognitionException {
    ClauseContext _localctx = new ClauseContext(_ctx, getState());
    enterRule(_localctx, 10, RULE_clause);
    int _la;
    try {
      setState(116);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 6, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1);
          {
            setState(105);
            fieldRangeExpr();
          }
          break;
        case 2:
          enterOuterAlt(_localctx, 2);
          {
            setState(109);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 4, _ctx)) {
              case 1:
                {
                  setState(106);
                  fieldName();
                  setState(107);
                  _la = _input.LA(1);
                  if (!(_la == OP_COLON || _la == OP_EQUAL)) {
                    _errHandler.recoverInline(this);
                  } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                  }
                }
                break;
            }
            setState(114);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
              case QUOTED:
              case NUMBER:
              case TERM:
              case REGEXPTERM:
              case RANGEIN_START:
              case RANGEEX_START:
                {
                  setState(111);
                  term();
                }
                break;
              case LPAREN:
                {
                  setState(112);
                  groupingExpr();
                }
                break;
              case FN_PREFIX:
                {
                  setState(113);
                  intervalFunction();
                }
                break;
              default:
                throw new NoViableAltException(this);
            }
          }
          break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class FieldNameContext extends ParserRuleContext {
    public TerminalNode TERM() {
      return getToken(StandardSyntaxAntlrParser.TERM, 0);
    }

    public FieldNameContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_fieldName;
    }
  }

  public final FieldNameContext fieldName() throws RecognitionException {
    FieldNameContext _localctx = new FieldNameContext(_ctx, getState());
    enterRule(_localctx, 12, RULE_fieldName);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(118);
        match(TERM);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class GroupingExprContext extends ParserRuleContext {
    public Token minShouldMatch;

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public QueryContext query() {
      return getRuleContext(QueryContext.class, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public BoostContext boost() {
      return getRuleContext(BoostContext.class, 0);
    }

    public TerminalNode AT() {
      return getToken(StandardSyntaxAntlrParser.AT, 0);
    }

    public TerminalNode NUMBER() {
      return getToken(StandardSyntaxAntlrParser.NUMBER, 0);
    }

    public GroupingExprContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_groupingExpr;
    }
  }

  public final GroupingExprContext groupingExpr() throws RecognitionException {
    GroupingExprContext _localctx = new GroupingExprContext(_ctx, getState());
    enterRule(_localctx, 14, RULE_groupingExpr);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(120);
        match(LPAREN);
        setState(121);
        query();
        setState(122);
        match(RPAREN);
        setState(124);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == CARAT) {
          {
            setState(123);
            boost();
          }
        }

        setState(128);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == AT) {
          {
            setState(126);
            match(AT);
            setState(127);
            ((GroupingExprContext) _localctx).minShouldMatch = match(NUMBER);
          }
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalFunContext extends ParserRuleContext {
    public IntervalFunctionContext intervalFunction() {
      return getRuleContext(IntervalFunctionContext.class, 0);
    }

    public IntervalTextContext intervalText() {
      return getRuleContext(IntervalTextContext.class, 0);
    }

    public IntervalFunContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalFun;
    }
  }

  public final IntervalFunContext intervalFun() throws RecognitionException {
    IntervalFunContext _localctx = new IntervalFunContext(_ctx, getState());
    enterRule(_localctx, 16, RULE_intervalFun);
    try {
      setState(132);
      _errHandler.sync(this);
      switch (_input.LA(1)) {
        case FN_PREFIX:
          enterOuterAlt(_localctx, 1);
          {
            setState(130);
            intervalFunction();
          }
          break;
        case QUOTED:
        case NUMBER:
        case TERM:
          enterOuterAlt(_localctx, 2);
          {
            setState(131);
            intervalText();
          }
          break;
        default:
          throw new NoViableAltException(this);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalFunctionContext extends ParserRuleContext {
    public IntervalAtLeastContext intervalAtLeast() {
      return getRuleContext(IntervalAtLeastContext.class, 0);
    }

    public IntervalMaxWidthContext intervalMaxWidth() {
      return getRuleContext(IntervalMaxWidthContext.class, 0);
    }

    public IntervalMaxGapsContext intervalMaxGaps() {
      return getRuleContext(IntervalMaxGapsContext.class, 0);
    }

    public IntervalOrderedContext intervalOrdered() {
      return getRuleContext(IntervalOrderedContext.class, 0);
    }

    public IntervalUnorderedContext intervalUnordered() {
      return getRuleContext(IntervalUnorderedContext.class, 0);
    }

    public IntervalUnorderedNoOverlapsContext intervalUnorderedNoOverlaps() {
      return getRuleContext(IntervalUnorderedNoOverlapsContext.class, 0);
    }

    public IntervalOrContext intervalOr() {
      return getRuleContext(IntervalOrContext.class, 0);
    }

    public IntervalWildcardContext intervalWildcard() {
      return getRuleContext(IntervalWildcardContext.class, 0);
    }

    public IntervalAfterContext intervalAfter() {
      return getRuleContext(IntervalAfterContext.class, 0);
    }

    public IntervalBeforeContext intervalBefore() {
      return getRuleContext(IntervalBeforeContext.class, 0);
    }

    public IntervalPhraseContext intervalPhrase() {
      return getRuleContext(IntervalPhraseContext.class, 0);
    }

    public IntervalContainingContext intervalContaining() {
      return getRuleContext(IntervalContainingContext.class, 0);
    }

    public IntervalNotContainingContext intervalNotContaining() {
      return getRuleContext(IntervalNotContainingContext.class, 0);
    }

    public IntervalContainedByContext intervalContainedBy() {
      return getRuleContext(IntervalContainedByContext.class, 0);
    }

    public IntervalNotContainedByContext intervalNotContainedBy() {
      return getRuleContext(IntervalNotContainedByContext.class, 0);
    }

    public IntervalWithinContext intervalWithin() {
      return getRuleContext(IntervalWithinContext.class, 0);
    }

    public IntervalNotWithinContext intervalNotWithin() {
      return getRuleContext(IntervalNotWithinContext.class, 0);
    }

    public IntervalOverlappingContext intervalOverlapping() {
      return getRuleContext(IntervalOverlappingContext.class, 0);
    }

    public IntervalNonOverlappingContext intervalNonOverlapping() {
      return getRuleContext(IntervalNonOverlappingContext.class, 0);
    }

    public IntervalExtendContext intervalExtend() {
      return getRuleContext(IntervalExtendContext.class, 0);
    }

    public IntervalFuzzyTermContext intervalFuzzyTerm() {
      return getRuleContext(IntervalFuzzyTermContext.class, 0);
    }

    public IntervalFunctionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalFunction;
    }
  }

  public final IntervalFunctionContext intervalFunction() throws RecognitionException {
    IntervalFunctionContext _localctx = new IntervalFunctionContext(_ctx, getState());
    enterRule(_localctx, 18, RULE_intervalFunction);
    try {
      setState(155);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 10, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1);
          {
            setState(134);
            intervalAtLeast();
          }
          break;
        case 2:
          enterOuterAlt(_localctx, 2);
          {
            setState(135);
            intervalMaxWidth();
          }
          break;
        case 3:
          enterOuterAlt(_localctx, 3);
          {
            setState(136);
            intervalMaxGaps();
          }
          break;
        case 4:
          enterOuterAlt(_localctx, 4);
          {
            setState(137);
            intervalOrdered();
          }
          break;
        case 5:
          enterOuterAlt(_localctx, 5);
          {
            setState(138);
            intervalUnordered();
          }
          break;
        case 6:
          enterOuterAlt(_localctx, 6);
          {
            setState(139);
            intervalUnorderedNoOverlaps();
          }
          break;
        case 7:
          enterOuterAlt(_localctx, 7);
          {
            setState(140);
            intervalOr();
          }
          break;
        case 8:
          enterOuterAlt(_localctx, 8);
          {
            setState(141);
            intervalWildcard();
          }
          break;
        case 9:
          enterOuterAlt(_localctx, 9);
          {
            setState(142);
            intervalAfter();
          }
          break;
        case 10:
          enterOuterAlt(_localctx, 10);
          {
            setState(143);
            intervalBefore();
          }
          break;
        case 11:
          enterOuterAlt(_localctx, 11);
          {
            setState(144);
            intervalPhrase();
          }
          break;
        case 12:
          enterOuterAlt(_localctx, 12);
          {
            setState(145);
            intervalContaining();
          }
          break;
        case 13:
          enterOuterAlt(_localctx, 13);
          {
            setState(146);
            intervalNotContaining();
          }
          break;
        case 14:
          enterOuterAlt(_localctx, 14);
          {
            setState(147);
            intervalContainedBy();
          }
          break;
        case 15:
          enterOuterAlt(_localctx, 15);
          {
            setState(148);
            intervalNotContainedBy();
          }
          break;
        case 16:
          enterOuterAlt(_localctx, 16);
          {
            setState(149);
            intervalWithin();
          }
          break;
        case 17:
          enterOuterAlt(_localctx, 17);
          {
            setState(150);
            intervalNotWithin();
          }
          break;
        case 18:
          enterOuterAlt(_localctx, 18);
          {
            setState(151);
            intervalOverlapping();
          }
          break;
        case 19:
          enterOuterAlt(_localctx, 19);
          {
            setState(152);
            intervalNonOverlapping();
          }
          break;
        case 20:
          enterOuterAlt(_localctx, 20);
          {
            setState(153);
            intervalExtend();
          }
          break;
        case 21:
          enterOuterAlt(_localctx, 21);
          {
            setState(154);
            intervalFuzzyTerm();
          }
          break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalAtLeastContext extends ParserRuleContext {
    public Token minShouldMatch;
    public IntervalFunContext intervalFun;
    public List<IntervalFunContext> sources = new ArrayList<IntervalFunContext>();

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode ATLEAST() {
      return getToken(StandardSyntaxAntlrParser.ATLEAST, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public TerminalNode NUMBER() {
      return getToken(StandardSyntaxAntlrParser.NUMBER, 0);
    }

    public List<IntervalFunContext> intervalFun() {
      return getRuleContexts(IntervalFunContext.class);
    }

    public IntervalFunContext intervalFun(int i) {
      return getRuleContext(IntervalFunContext.class, i);
    }

    public IntervalAtLeastContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalAtLeast;
    }
  }

  public final IntervalAtLeastContext intervalAtLeast() throws RecognitionException {
    IntervalAtLeastContext _localctx = new IntervalAtLeastContext(_ctx, getState());
    enterRule(_localctx, 20, RULE_intervalAtLeast);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(157);
        match(FN_PREFIX);
        setState(158);
        match(ATLEAST);
        setState(159);
        match(LPAREN);
        setState(160);
        ((IntervalAtLeastContext) _localctx).minShouldMatch = match(NUMBER);
        setState(162);
        _errHandler.sync(this);
        _la = _input.LA(1);
        do {
          {
            {
              setState(161);
              ((IntervalAtLeastContext) _localctx).intervalFun = intervalFun();
              ((IntervalAtLeastContext) _localctx)
                  .sources.add(((IntervalAtLeastContext) _localctx).intervalFun);
            }
          }
          setState(164);
          _errHandler.sync(this);
          _la = _input.LA(1);
        } while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1835040L) != 0));
        setState(166);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalMaxWidthContext extends ParserRuleContext {
    public Token maxWidth;
    public IntervalFunContext source;

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode MAXWIDTH() {
      return getToken(StandardSyntaxAntlrParser.MAXWIDTH, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public TerminalNode NUMBER() {
      return getToken(StandardSyntaxAntlrParser.NUMBER, 0);
    }

    public IntervalFunContext intervalFun() {
      return getRuleContext(IntervalFunContext.class, 0);
    }

    public IntervalMaxWidthContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalMaxWidth;
    }
  }

  public final IntervalMaxWidthContext intervalMaxWidth() throws RecognitionException {
    IntervalMaxWidthContext _localctx = new IntervalMaxWidthContext(_ctx, getState());
    enterRule(_localctx, 22, RULE_intervalMaxWidth);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(168);
        match(FN_PREFIX);
        setState(169);
        match(MAXWIDTH);
        setState(170);
        match(LPAREN);
        setState(171);
        ((IntervalMaxWidthContext) _localctx).maxWidth = match(NUMBER);
        setState(172);
        ((IntervalMaxWidthContext) _localctx).source = intervalFun();
        setState(173);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalMaxGapsContext extends ParserRuleContext {
    public Token maxGaps;
    public IntervalFunContext source;

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode MAXGAPS() {
      return getToken(StandardSyntaxAntlrParser.MAXGAPS, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public TerminalNode NUMBER() {
      return getToken(StandardSyntaxAntlrParser.NUMBER, 0);
    }

    public IntervalFunContext intervalFun() {
      return getRuleContext(IntervalFunContext.class, 0);
    }

    public IntervalMaxGapsContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalMaxGaps;
    }
  }

  public final IntervalMaxGapsContext intervalMaxGaps() throws RecognitionException {
    IntervalMaxGapsContext _localctx = new IntervalMaxGapsContext(_ctx, getState());
    enterRule(_localctx, 24, RULE_intervalMaxGaps);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(175);
        match(FN_PREFIX);
        setState(176);
        match(MAXGAPS);
        setState(177);
        match(LPAREN);
        setState(178);
        ((IntervalMaxGapsContext) _localctx).maxGaps = match(NUMBER);
        setState(179);
        ((IntervalMaxGapsContext) _localctx).source = intervalFun();
        setState(180);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalUnorderedContext extends ParserRuleContext {
    public IntervalFunContext intervalFun;
    public List<IntervalFunContext> sources = new ArrayList<IntervalFunContext>();

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode UNORDERED() {
      return getToken(StandardSyntaxAntlrParser.UNORDERED, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public List<IntervalFunContext> intervalFun() {
      return getRuleContexts(IntervalFunContext.class);
    }

    public IntervalFunContext intervalFun(int i) {
      return getRuleContext(IntervalFunContext.class, i);
    }

    public IntervalUnorderedContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalUnordered;
    }
  }

  public final IntervalUnorderedContext intervalUnordered() throws RecognitionException {
    IntervalUnorderedContext _localctx = new IntervalUnorderedContext(_ctx, getState());
    enterRule(_localctx, 26, RULE_intervalUnordered);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(182);
        match(FN_PREFIX);
        setState(183);
        match(UNORDERED);
        setState(184);
        match(LPAREN);
        setState(186);
        _errHandler.sync(this);
        _la = _input.LA(1);
        do {
          {
            {
              setState(185);
              ((IntervalUnorderedContext) _localctx).intervalFun = intervalFun();
              ((IntervalUnorderedContext) _localctx)
                  .sources.add(((IntervalUnorderedContext) _localctx).intervalFun);
            }
          }
          setState(188);
          _errHandler.sync(this);
          _la = _input.LA(1);
        } while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1835040L) != 0));
        setState(190);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalUnorderedNoOverlapsContext extends ParserRuleContext {
    public IntervalFunContext a;
    public IntervalFunContext b;

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode UNORDERED_NO_OVERLAPS() {
      return getToken(StandardSyntaxAntlrParser.UNORDERED_NO_OVERLAPS, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public List<IntervalFunContext> intervalFun() {
      return getRuleContexts(IntervalFunContext.class);
    }

    public IntervalFunContext intervalFun(int i) {
      return getRuleContext(IntervalFunContext.class, i);
    }

    public IntervalUnorderedNoOverlapsContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalUnorderedNoOverlaps;
    }
  }

  public final IntervalUnorderedNoOverlapsContext intervalUnorderedNoOverlaps()
      throws RecognitionException {
    IntervalUnorderedNoOverlapsContext _localctx =
        new IntervalUnorderedNoOverlapsContext(_ctx, getState());
    enterRule(_localctx, 28, RULE_intervalUnorderedNoOverlaps);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(192);
        match(FN_PREFIX);
        setState(193);
        match(UNORDERED_NO_OVERLAPS);
        setState(194);
        match(LPAREN);
        setState(195);
        ((IntervalUnorderedNoOverlapsContext) _localctx).a = intervalFun();
        setState(196);
        ((IntervalUnorderedNoOverlapsContext) _localctx).b = intervalFun();
        setState(197);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalOrderedContext extends ParserRuleContext {
    public IntervalFunContext intervalFun;
    public List<IntervalFunContext> sources = new ArrayList<IntervalFunContext>();

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode ORDERED() {
      return getToken(StandardSyntaxAntlrParser.ORDERED, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public List<IntervalFunContext> intervalFun() {
      return getRuleContexts(IntervalFunContext.class);
    }

    public IntervalFunContext intervalFun(int i) {
      return getRuleContext(IntervalFunContext.class, i);
    }

    public IntervalOrderedContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalOrdered;
    }
  }

  public final IntervalOrderedContext intervalOrdered() throws RecognitionException {
    IntervalOrderedContext _localctx = new IntervalOrderedContext(_ctx, getState());
    enterRule(_localctx, 30, RULE_intervalOrdered);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(199);
        match(FN_PREFIX);
        setState(200);
        match(ORDERED);
        setState(201);
        match(LPAREN);
        setState(203);
        _errHandler.sync(this);
        _la = _input.LA(1);
        do {
          {
            {
              setState(202);
              ((IntervalOrderedContext) _localctx).intervalFun = intervalFun();
              ((IntervalOrderedContext) _localctx)
                  .sources.add(((IntervalOrderedContext) _localctx).intervalFun);
            }
          }
          setState(205);
          _errHandler.sync(this);
          _la = _input.LA(1);
        } while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1835040L) != 0));
        setState(207);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalOrContext extends ParserRuleContext {
    public IntervalFunContext intervalFun;
    public List<IntervalFunContext> sources = new ArrayList<IntervalFunContext>();

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode FN_OR() {
      return getToken(StandardSyntaxAntlrParser.FN_OR, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public List<IntervalFunContext> intervalFun() {
      return getRuleContexts(IntervalFunContext.class);
    }

    public IntervalFunContext intervalFun(int i) {
      return getRuleContext(IntervalFunContext.class, i);
    }

    public IntervalOrContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalOr;
    }
  }

  public final IntervalOrContext intervalOr() throws RecognitionException {
    IntervalOrContext _localctx = new IntervalOrContext(_ctx, getState());
    enterRule(_localctx, 32, RULE_intervalOr);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(209);
        match(FN_PREFIX);
        setState(210);
        match(FN_OR);
        setState(211);
        match(LPAREN);
        setState(213);
        _errHandler.sync(this);
        _la = _input.LA(1);
        do {
          {
            {
              setState(212);
              ((IntervalOrContext) _localctx).intervalFun = intervalFun();
              ((IntervalOrContext) _localctx)
                  .sources.add(((IntervalOrContext) _localctx).intervalFun);
            }
          }
          setState(215);
          _errHandler.sync(this);
          _la = _input.LA(1);
        } while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1835040L) != 0));
        setState(217);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalPhraseContext extends ParserRuleContext {
    public IntervalFunContext intervalFun;
    public List<IntervalFunContext> sources = new ArrayList<IntervalFunContext>();

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode PHRASE() {
      return getToken(StandardSyntaxAntlrParser.PHRASE, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public List<IntervalFunContext> intervalFun() {
      return getRuleContexts(IntervalFunContext.class);
    }

    public IntervalFunContext intervalFun(int i) {
      return getRuleContext(IntervalFunContext.class, i);
    }

    public IntervalPhraseContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalPhrase;
    }
  }

  public final IntervalPhraseContext intervalPhrase() throws RecognitionException {
    IntervalPhraseContext _localctx = new IntervalPhraseContext(_ctx, getState());
    enterRule(_localctx, 34, RULE_intervalPhrase);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(219);
        match(FN_PREFIX);
        setState(220);
        match(PHRASE);
        setState(221);
        match(LPAREN);
        setState(223);
        _errHandler.sync(this);
        _la = _input.LA(1);
        do {
          {
            {
              setState(222);
              ((IntervalPhraseContext) _localctx).intervalFun = intervalFun();
              ((IntervalPhraseContext) _localctx)
                  .sources.add(((IntervalPhraseContext) _localctx).intervalFun);
            }
          }
          setState(225);
          _errHandler.sync(this);
          _la = _input.LA(1);
        } while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1835040L) != 0));
        setState(227);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalBeforeContext extends ParserRuleContext {
    public IntervalFunContext source;
    public IntervalFunContext reference;

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode BEFORE() {
      return getToken(StandardSyntaxAntlrParser.BEFORE, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public List<IntervalFunContext> intervalFun() {
      return getRuleContexts(IntervalFunContext.class);
    }

    public IntervalFunContext intervalFun(int i) {
      return getRuleContext(IntervalFunContext.class, i);
    }

    public IntervalBeforeContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalBefore;
    }
  }

  public final IntervalBeforeContext intervalBefore() throws RecognitionException {
    IntervalBeforeContext _localctx = new IntervalBeforeContext(_ctx, getState());
    enterRule(_localctx, 36, RULE_intervalBefore);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(229);
        match(FN_PREFIX);
        setState(230);
        match(BEFORE);
        setState(231);
        match(LPAREN);
        setState(232);
        ((IntervalBeforeContext) _localctx).source = intervalFun();
        setState(233);
        ((IntervalBeforeContext) _localctx).reference = intervalFun();
        setState(234);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalAfterContext extends ParserRuleContext {
    public IntervalFunContext source;
    public IntervalFunContext reference;

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode AFTER() {
      return getToken(StandardSyntaxAntlrParser.AFTER, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public List<IntervalFunContext> intervalFun() {
      return getRuleContexts(IntervalFunContext.class);
    }

    public IntervalFunContext intervalFun(int i) {
      return getRuleContext(IntervalFunContext.class, i);
    }

    public IntervalAfterContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalAfter;
    }
  }

  public final IntervalAfterContext intervalAfter() throws RecognitionException {
    IntervalAfterContext _localctx = new IntervalAfterContext(_ctx, getState());
    enterRule(_localctx, 38, RULE_intervalAfter);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(236);
        match(FN_PREFIX);
        setState(237);
        match(AFTER);
        setState(238);
        match(LPAREN);
        setState(239);
        ((IntervalAfterContext) _localctx).source = intervalFun();
        setState(240);
        ((IntervalAfterContext) _localctx).reference = intervalFun();
        setState(241);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalContainingContext extends ParserRuleContext {
    public IntervalFunContext big;
    public IntervalFunContext small;

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode CONTAINING() {
      return getToken(StandardSyntaxAntlrParser.CONTAINING, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public List<IntervalFunContext> intervalFun() {
      return getRuleContexts(IntervalFunContext.class);
    }

    public IntervalFunContext intervalFun(int i) {
      return getRuleContext(IntervalFunContext.class, i);
    }

    public IntervalContainingContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalContaining;
    }
  }

  public final IntervalContainingContext intervalContaining() throws RecognitionException {
    IntervalContainingContext _localctx = new IntervalContainingContext(_ctx, getState());
    enterRule(_localctx, 40, RULE_intervalContaining);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(243);
        match(FN_PREFIX);
        setState(244);
        match(CONTAINING);
        setState(245);
        match(LPAREN);
        setState(246);
        ((IntervalContainingContext) _localctx).big = intervalFun();
        setState(247);
        ((IntervalContainingContext) _localctx).small = intervalFun();
        setState(248);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalNotContainingContext extends ParserRuleContext {
    public IntervalFunContext minuend;
    public IntervalFunContext subtrahend;

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode NOT_CONTAINING() {
      return getToken(StandardSyntaxAntlrParser.NOT_CONTAINING, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public List<IntervalFunContext> intervalFun() {
      return getRuleContexts(IntervalFunContext.class);
    }

    public IntervalFunContext intervalFun(int i) {
      return getRuleContext(IntervalFunContext.class, i);
    }

    public IntervalNotContainingContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalNotContaining;
    }
  }

  public final IntervalNotContainingContext intervalNotContaining() throws RecognitionException {
    IntervalNotContainingContext _localctx = new IntervalNotContainingContext(_ctx, getState());
    enterRule(_localctx, 42, RULE_intervalNotContaining);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(250);
        match(FN_PREFIX);
        setState(251);
        match(NOT_CONTAINING);
        setState(252);
        match(LPAREN);
        setState(253);
        ((IntervalNotContainingContext) _localctx).minuend = intervalFun();
        setState(254);
        ((IntervalNotContainingContext) _localctx).subtrahend = intervalFun();
        setState(255);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalContainedByContext extends ParserRuleContext {
    public IntervalFunContext small;
    public IntervalFunContext big;

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode CONTAINED_BY() {
      return getToken(StandardSyntaxAntlrParser.CONTAINED_BY, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public List<IntervalFunContext> intervalFun() {
      return getRuleContexts(IntervalFunContext.class);
    }

    public IntervalFunContext intervalFun(int i) {
      return getRuleContext(IntervalFunContext.class, i);
    }

    public IntervalContainedByContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalContainedBy;
    }
  }

  public final IntervalContainedByContext intervalContainedBy() throws RecognitionException {
    IntervalContainedByContext _localctx = new IntervalContainedByContext(_ctx, getState());
    enterRule(_localctx, 44, RULE_intervalContainedBy);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(257);
        match(FN_PREFIX);
        setState(258);
        match(CONTAINED_BY);
        setState(259);
        match(LPAREN);
        setState(260);
        ((IntervalContainedByContext) _localctx).small = intervalFun();
        setState(261);
        ((IntervalContainedByContext) _localctx).big = intervalFun();
        setState(262);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalNotContainedByContext extends ParserRuleContext {
    public IntervalFunContext small;
    public IntervalFunContext big;

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode NOT_CONTAINED_BY() {
      return getToken(StandardSyntaxAntlrParser.NOT_CONTAINED_BY, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public List<IntervalFunContext> intervalFun() {
      return getRuleContexts(IntervalFunContext.class);
    }

    public IntervalFunContext intervalFun(int i) {
      return getRuleContext(IntervalFunContext.class, i);
    }

    public IntervalNotContainedByContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalNotContainedBy;
    }
  }

  public final IntervalNotContainedByContext intervalNotContainedBy() throws RecognitionException {
    IntervalNotContainedByContext _localctx = new IntervalNotContainedByContext(_ctx, getState());
    enterRule(_localctx, 46, RULE_intervalNotContainedBy);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(264);
        match(FN_PREFIX);
        setState(265);
        match(NOT_CONTAINED_BY);
        setState(266);
        match(LPAREN);
        setState(267);
        ((IntervalNotContainedByContext) _localctx).small = intervalFun();
        setState(268);
        ((IntervalNotContainedByContext) _localctx).big = intervalFun();
        setState(269);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalWithinContext extends ParserRuleContext {
    public IntervalFunContext source;
    public Token positions;
    public IntervalFunContext reference;

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode WITHIN() {
      return getToken(StandardSyntaxAntlrParser.WITHIN, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public List<IntervalFunContext> intervalFun() {
      return getRuleContexts(IntervalFunContext.class);
    }

    public IntervalFunContext intervalFun(int i) {
      return getRuleContext(IntervalFunContext.class, i);
    }

    public TerminalNode NUMBER() {
      return getToken(StandardSyntaxAntlrParser.NUMBER, 0);
    }

    public IntervalWithinContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalWithin;
    }
  }

  public final IntervalWithinContext intervalWithin() throws RecognitionException {
    IntervalWithinContext _localctx = new IntervalWithinContext(_ctx, getState());
    enterRule(_localctx, 48, RULE_intervalWithin);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(271);
        match(FN_PREFIX);
        setState(272);
        match(WITHIN);
        setState(273);
        match(LPAREN);
        setState(274);
        ((IntervalWithinContext) _localctx).source = intervalFun();
        setState(275);
        ((IntervalWithinContext) _localctx).positions = match(NUMBER);
        setState(276);
        ((IntervalWithinContext) _localctx).reference = intervalFun();
        setState(277);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalExtendContext extends ParserRuleContext {
    public IntervalFunContext source;
    public Token before;
    public Token after;

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode EXTEND() {
      return getToken(StandardSyntaxAntlrParser.EXTEND, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public IntervalFunContext intervalFun() {
      return getRuleContext(IntervalFunContext.class, 0);
    }

    public List<TerminalNode> NUMBER() {
      return getTokens(StandardSyntaxAntlrParser.NUMBER);
    }

    public TerminalNode NUMBER(int i) {
      return getToken(StandardSyntaxAntlrParser.NUMBER, i);
    }

    public IntervalExtendContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalExtend;
    }
  }

  public final IntervalExtendContext intervalExtend() throws RecognitionException {
    IntervalExtendContext _localctx = new IntervalExtendContext(_ctx, getState());
    enterRule(_localctx, 50, RULE_intervalExtend);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(279);
        match(FN_PREFIX);
        setState(280);
        match(EXTEND);
        setState(281);
        match(LPAREN);
        setState(282);
        ((IntervalExtendContext) _localctx).source = intervalFun();
        setState(283);
        ((IntervalExtendContext) _localctx).before = match(NUMBER);
        setState(284);
        ((IntervalExtendContext) _localctx).after = match(NUMBER);
        setState(285);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalNotWithinContext extends ParserRuleContext {
    public IntervalFunContext minuend;
    public Token positions;
    public IntervalFunContext subtrahend;

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode NOT_WITHIN() {
      return getToken(StandardSyntaxAntlrParser.NOT_WITHIN, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public List<IntervalFunContext> intervalFun() {
      return getRuleContexts(IntervalFunContext.class);
    }

    public IntervalFunContext intervalFun(int i) {
      return getRuleContext(IntervalFunContext.class, i);
    }

    public TerminalNode NUMBER() {
      return getToken(StandardSyntaxAntlrParser.NUMBER, 0);
    }

    public IntervalNotWithinContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalNotWithin;
    }
  }

  public final IntervalNotWithinContext intervalNotWithin() throws RecognitionException {
    IntervalNotWithinContext _localctx = new IntervalNotWithinContext(_ctx, getState());
    enterRule(_localctx, 52, RULE_intervalNotWithin);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(287);
        match(FN_PREFIX);
        setState(288);
        match(NOT_WITHIN);
        setState(289);
        match(LPAREN);
        setState(290);
        ((IntervalNotWithinContext) _localctx).minuend = intervalFun();
        setState(291);
        ((IntervalNotWithinContext) _localctx).positions = match(NUMBER);
        setState(292);
        ((IntervalNotWithinContext) _localctx).subtrahend = intervalFun();
        setState(293);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalOverlappingContext extends ParserRuleContext {
    public IntervalFunContext source;
    public IntervalFunContext reference;

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode OVERLAPPING() {
      return getToken(StandardSyntaxAntlrParser.OVERLAPPING, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public List<IntervalFunContext> intervalFun() {
      return getRuleContexts(IntervalFunContext.class);
    }

    public IntervalFunContext intervalFun(int i) {
      return getRuleContext(IntervalFunContext.class, i);
    }

    public IntervalOverlappingContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalOverlapping;
    }
  }

  public final IntervalOverlappingContext intervalOverlapping() throws RecognitionException {
    IntervalOverlappingContext _localctx = new IntervalOverlappingContext(_ctx, getState());
    enterRule(_localctx, 54, RULE_intervalOverlapping);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(295);
        match(FN_PREFIX);
        setState(296);
        match(OVERLAPPING);
        setState(297);
        match(LPAREN);
        setState(298);
        ((IntervalOverlappingContext) _localctx).source = intervalFun();
        setState(299);
        ((IntervalOverlappingContext) _localctx).reference = intervalFun();
        setState(300);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalNonOverlappingContext extends ParserRuleContext {
    public IntervalFunContext minuend;
    public IntervalFunContext subtrahend;

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode NON_OVERLAPPING() {
      return getToken(StandardSyntaxAntlrParser.NON_OVERLAPPING, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public List<IntervalFunContext> intervalFun() {
      return getRuleContexts(IntervalFunContext.class);
    }

    public IntervalFunContext intervalFun(int i) {
      return getRuleContext(IntervalFunContext.class, i);
    }

    public IntervalNonOverlappingContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalNonOverlapping;
    }
  }

  public final IntervalNonOverlappingContext intervalNonOverlapping() throws RecognitionException {
    IntervalNonOverlappingContext _localctx = new IntervalNonOverlappingContext(_ctx, getState());
    enterRule(_localctx, 56, RULE_intervalNonOverlapping);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(302);
        match(FN_PREFIX);
        setState(303);
        match(NON_OVERLAPPING);
        setState(304);
        match(LPAREN);
        setState(305);
        ((IntervalNonOverlappingContext) _localctx).minuend = intervalFun();
        setState(306);
        ((IntervalNonOverlappingContext) _localctx).subtrahend = intervalFun();
        setState(307);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalWildcardContext extends ParserRuleContext {
    public Token wildcard;
    public Token maxExpansions;

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode WILDCARD() {
      return getToken(StandardSyntaxAntlrParser.WILDCARD, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public TerminalNode TERM() {
      return getToken(StandardSyntaxAntlrParser.TERM, 0);
    }

    public List<TerminalNode> NUMBER() {
      return getTokens(StandardSyntaxAntlrParser.NUMBER);
    }

    public TerminalNode NUMBER(int i) {
      return getToken(StandardSyntaxAntlrParser.NUMBER, i);
    }

    public TerminalNode QUOTED() {
      return getToken(StandardSyntaxAntlrParser.QUOTED, 0);
    }

    public IntervalWildcardContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalWildcard;
    }
  }

  public final IntervalWildcardContext intervalWildcard() throws RecognitionException {
    IntervalWildcardContext _localctx = new IntervalWildcardContext(_ctx, getState());
    enterRule(_localctx, 58, RULE_intervalWildcard);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(309);
        match(FN_PREFIX);
        setState(310);
        match(WILDCARD);
        setState(311);
        match(LPAREN);
        setState(312);
        ((IntervalWildcardContext) _localctx).wildcard = _input.LT(1);
        _la = _input.LA(1);
        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 1835008L) != 0))) {
          ((IntervalWildcardContext) _localctx).wildcard = (Token) _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF) matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
        setState(314);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == NUMBER) {
          {
            setState(313);
            ((IntervalWildcardContext) _localctx).maxExpansions = match(NUMBER);
          }
        }

        setState(316);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalFuzzyTermContext extends ParserRuleContext {
    public Token fuzzyTerm;
    public Token maxEdits;
    public Token maxExpansions;

    public TerminalNode FN_PREFIX() {
      return getToken(StandardSyntaxAntlrParser.FN_PREFIX, 0);
    }

    public TerminalNode FUZZYTERM() {
      return getToken(StandardSyntaxAntlrParser.FUZZYTERM, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(StandardSyntaxAntlrParser.LPAREN, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(StandardSyntaxAntlrParser.RPAREN, 0);
    }

    public TerminalNode TERM() {
      return getToken(StandardSyntaxAntlrParser.TERM, 0);
    }

    public List<TerminalNode> NUMBER() {
      return getTokens(StandardSyntaxAntlrParser.NUMBER);
    }

    public TerminalNode NUMBER(int i) {
      return getToken(StandardSyntaxAntlrParser.NUMBER, i);
    }

    public TerminalNode QUOTED() {
      return getToken(StandardSyntaxAntlrParser.QUOTED, 0);
    }

    public IntervalFuzzyTermContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalFuzzyTerm;
    }
  }

  public final IntervalFuzzyTermContext intervalFuzzyTerm() throws RecognitionException {
    IntervalFuzzyTermContext _localctx = new IntervalFuzzyTermContext(_ctx, getState());
    enterRule(_localctx, 60, RULE_intervalFuzzyTerm);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(318);
        match(FN_PREFIX);
        setState(319);
        match(FUZZYTERM);
        setState(320);
        match(LPAREN);
        setState(321);
        ((IntervalFuzzyTermContext) _localctx).fuzzyTerm = _input.LT(1);
        _la = _input.LA(1);
        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 1835008L) != 0))) {
          ((IntervalFuzzyTermContext) _localctx).fuzzyTerm =
              (Token) _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF) matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
        setState(326);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == NUMBER) {
          {
            setState(322);
            ((IntervalFuzzyTermContext) _localctx).maxEdits = match(NUMBER);
            setState(324);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == NUMBER) {
              {
                setState(323);
                ((IntervalFuzzyTermContext) _localctx).maxExpansions = match(NUMBER);
              }
            }
          }
        }

        setState(328);
        match(RPAREN);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class IntervalTextContext extends ParserRuleContext {
    public TerminalNode QUOTED() {
      return getToken(StandardSyntaxAntlrParser.QUOTED, 0);
    }

    public TerminalNode TERM() {
      return getToken(StandardSyntaxAntlrParser.TERM, 0);
    }

    public TerminalNode NUMBER() {
      return getToken(StandardSyntaxAntlrParser.NUMBER, 0);
    }

    public IntervalTextContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_intervalText;
    }
  }

  public final IntervalTextContext intervalText() throws RecognitionException {
    IntervalTextContext _localctx = new IntervalTextContext(_ctx, getState());
    enterRule(_localctx, 62, RULE_intervalText);
    int _la;
    try {
      setState(332);
      _errHandler.sync(this);
      switch (_input.LA(1)) {
        case QUOTED:
          enterOuterAlt(_localctx, 1);
          {
            setState(330);
            match(QUOTED);
          }
          break;
        case NUMBER:
        case TERM:
          enterOuterAlt(_localctx, 2);
          {
            setState(331);
            _la = _input.LA(1);
            if (!(_la == NUMBER || _la == TERM)) {
              _errHandler.recoverInline(this);
            } else {
              if (_input.LA(1) == Token.EOF) matchedEOF = true;
              _errHandler.reportMatch(this);
              consume();
            }
          }
          break;
        default:
          throw new NoViableAltException(this);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class BoostContext extends ParserRuleContext {
    public TerminalNode CARAT() {
      return getToken(StandardSyntaxAntlrParser.CARAT, 0);
    }

    public TerminalNode NUMBER() {
      return getToken(StandardSyntaxAntlrParser.NUMBER, 0);
    }

    public BoostContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_boost;
    }
  }

  public final BoostContext boost() throws RecognitionException {
    BoostContext _localctx = new BoostContext(_ctx, getState());
    enterRule(_localctx, 64, RULE_boost);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(334);
        match(CARAT);
        setState(335);
        match(NUMBER);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class FuzzyOpContext extends ParserRuleContext {
    public Token similarity;

    public TerminalNode TILDE() {
      return getToken(StandardSyntaxAntlrParser.TILDE, 0);
    }

    public TerminalNode NUMBER() {
      return getToken(StandardSyntaxAntlrParser.NUMBER, 0);
    }

    public FuzzyOpContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_fuzzyOp;
    }
  }

  public final FuzzyOpContext fuzzyOp() throws RecognitionException {
    FuzzyOpContext _localctx = new FuzzyOpContext(_ctx, getState());
    enterRule(_localctx, 66, RULE_fuzzyOp);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(337);
        match(TILDE);
        setState(340);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 20, _ctx)) {
          case 1:
            {
              setState(338);
              ((FuzzyOpContext) _localctx).similarity = match(NUMBER);
            }
            break;
          case 2:
            {
              setState(339);
              if (!(_input.LA(1) != NUMBER))
                throw new FailedPredicateException(this, "_input.LA(1) != NUMBER");
            }
            break;
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class FieldRangeExprContext extends ParserRuleContext {
    public Token operator;
    public Token value;

    public FieldNameContext fieldName() {
      return getRuleContext(FieldNameContext.class, 0);
    }

    public TerminalNode OP_LESSTHAN() {
      return getToken(StandardSyntaxAntlrParser.OP_LESSTHAN, 0);
    }

    public TerminalNode OP_LESSTHANEQ() {
      return getToken(StandardSyntaxAntlrParser.OP_LESSTHANEQ, 0);
    }

    public TerminalNode OP_MORETHAN() {
      return getToken(StandardSyntaxAntlrParser.OP_MORETHAN, 0);
    }

    public TerminalNode OP_MORETHANEQ() {
      return getToken(StandardSyntaxAntlrParser.OP_MORETHANEQ, 0);
    }

    public TerminalNode TERM() {
      return getToken(StandardSyntaxAntlrParser.TERM, 0);
    }

    public TerminalNode QUOTED() {
      return getToken(StandardSyntaxAntlrParser.QUOTED, 0);
    }

    public TerminalNode NUMBER() {
      return getToken(StandardSyntaxAntlrParser.NUMBER, 0);
    }

    public FieldRangeExprContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_fieldRangeExpr;
    }
  }

  public final FieldRangeExprContext fieldRangeExpr() throws RecognitionException {
    FieldRangeExprContext _localctx = new FieldRangeExprContext(_ctx, getState());
    enterRule(_localctx, 68, RULE_fieldRangeExpr);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(342);
        fieldName();
        setState(343);
        ((FieldRangeExprContext) _localctx).operator = _input.LT(1);
        _la = _input.LA(1);
        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 30720L) != 0))) {
          ((FieldRangeExprContext) _localctx).operator = (Token) _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF) matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
        setState(344);
        ((FieldRangeExprContext) _localctx).value = _input.LT(1);
        _la = _input.LA(1);
        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 1835008L) != 0))) {
          ((FieldRangeExprContext) _localctx).value = (Token) _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF) matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class TermContext extends ParserRuleContext {
    public TerminalNode REGEXPTERM() {
      return getToken(StandardSyntaxAntlrParser.REGEXPTERM, 0);
    }

    public TermRangeExprContext termRangeExpr() {
      return getRuleContext(TermRangeExprContext.class, 0);
    }

    public QuotedTermContext quotedTerm() {
      return getRuleContext(QuotedTermContext.class, 0);
    }

    public TerminalNode TERM() {
      return getToken(StandardSyntaxAntlrParser.TERM, 0);
    }

    public TerminalNode NUMBER() {
      return getToken(StandardSyntaxAntlrParser.NUMBER, 0);
    }

    public BoostContext boost() {
      return getRuleContext(BoostContext.class, 0);
    }

    public FuzzyOpContext fuzzyOp() {
      return getRuleContext(FuzzyOpContext.class, 0);
    }

    public TermContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_term;
    }
  }

  public final TermContext term() throws RecognitionException {
    TermContext _localctx = new TermContext(_ctx, getState());
    enterRule(_localctx, 70, RULE_term);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(353);
        _errHandler.sync(this);
        switch (_input.LA(1)) {
          case REGEXPTERM:
            {
              setState(346);
              match(REGEXPTERM);
            }
            break;
          case NUMBER:
          case TERM:
            {
              setState(347);
              _la = _input.LA(1);
              if (!(_la == NUMBER || _la == TERM)) {
                _errHandler.recoverInline(this);
              } else {
                if (_input.LA(1) == Token.EOF) matchedEOF = true;
                _errHandler.reportMatch(this);
                consume();
              }
              setState(349);
              _errHandler.sync(this);
              _la = _input.LA(1);
              if (_la == TILDE) {
                {
                  setState(348);
                  fuzzyOp();
                }
              }
            }
            break;
          case RANGEIN_START:
          case RANGEEX_START:
            {
              setState(351);
              termRangeExpr();
            }
            break;
          case QUOTED:
            {
              setState(352);
              quotedTerm();
            }
            break;
          default:
            throw new NoViableAltException(this);
        }
        setState(356);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == CARAT) {
          {
            setState(355);
            boost();
          }
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class QuotedTermContext extends ParserRuleContext {
    public Token slop;

    public TerminalNode QUOTED() {
      return getToken(StandardSyntaxAntlrParser.QUOTED, 0);
    }

    public TerminalNode TILDE() {
      return getToken(StandardSyntaxAntlrParser.TILDE, 0);
    }

    public TerminalNode NUMBER() {
      return getToken(StandardSyntaxAntlrParser.NUMBER, 0);
    }

    public QuotedTermContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_quotedTerm;
    }
  }

  public final QuotedTermContext quotedTerm() throws RecognitionException {
    QuotedTermContext _localctx = new QuotedTermContext(_ctx, getState());
    enterRule(_localctx, 72, RULE_quotedTerm);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(358);
        match(QUOTED);
        setState(361);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == TILDE) {
          {
            setState(359);
            match(TILDE);
            setState(360);
            ((QuotedTermContext) _localctx).slop = match(NUMBER);
          }
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  @SuppressWarnings("CheckReturnValue")
  public static class TermRangeExprContext extends ParserRuleContext {
    public Token open;
    public Token left;
    public Token right;
    public Token close;

    public List<TerminalNode> RANGE_TO() {
      return getTokens(StandardSyntaxAntlrParser.RANGE_TO);
    }

    public TerminalNode RANGE_TO(int i) {
      return getToken(StandardSyntaxAntlrParser.RANGE_TO, i);
    }

    public TerminalNode RANGEIN_START() {
      return getToken(StandardSyntaxAntlrParser.RANGEIN_START, 0);
    }

    public TerminalNode RANGEEX_START() {
      return getToken(StandardSyntaxAntlrParser.RANGEEX_START, 0);
    }

    public List<TerminalNode> RANGE_GOOP() {
      return getTokens(StandardSyntaxAntlrParser.RANGE_GOOP);
    }

    public TerminalNode RANGE_GOOP(int i) {
      return getToken(StandardSyntaxAntlrParser.RANGE_GOOP, i);
    }

    public List<TerminalNode> RANGE_QUOTED() {
      return getTokens(StandardSyntaxAntlrParser.RANGE_QUOTED);
    }

    public TerminalNode RANGE_QUOTED(int i) {
      return getToken(StandardSyntaxAntlrParser.RANGE_QUOTED, i);
    }

    public TerminalNode RANGEIN_END() {
      return getToken(StandardSyntaxAntlrParser.RANGEIN_END, 0);
    }

    public TerminalNode RANGEEX_END() {
      return getToken(StandardSyntaxAntlrParser.RANGEEX_END, 0);
    }

    public TermRangeExprContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_termRangeExpr;
    }
  }

  public final TermRangeExprContext termRangeExpr() throws RecognitionException {
    TermRangeExprContext _localctx = new TermRangeExprContext(_ctx, getState());
    enterRule(_localctx, 74, RULE_termRangeExpr);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(363);
        ((TermRangeExprContext) _localctx).open = _input.LT(1);
        _la = _input.LA(1);
        if (!(_la == RANGEIN_START || _la == RANGEEX_START)) {
          ((TermRangeExprContext) _localctx).open = (Token) _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF) matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
        setState(364);
        ((TermRangeExprContext) _localctx).left = _input.LT(1);
        _la = _input.LA(1);
        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 7036874417766400L) != 0))) {
          ((TermRangeExprContext) _localctx).left = (Token) _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF) matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
        setState(365);
        match(RANGE_TO);
        setState(366);
        ((TermRangeExprContext) _localctx).right = _input.LT(1);
        _la = _input.LA(1);
        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 7036874417766400L) != 0))) {
          ((TermRangeExprContext) _localctx).right = (Token) _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF) matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
        setState(367);
        ((TermRangeExprContext) _localctx).close = _input.LT(1);
        _la = _input.LA(1);
        if (!(_la == RANGEIN_END || _la == RANGEEX_END)) {
          ((TermRangeExprContext) _localctx).close = (Token) _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF) matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
    switch (ruleIndex) {
      case 33:
        return fuzzyOp_sempred((FuzzyOpContext) _localctx, predIndex);
    }
    return true;
  }

  private boolean fuzzyOp_sempred(FuzzyOpContext _localctx, int predIndex) {
    switch (predIndex) {
      case 0:
        return _input.LA(1) != NUMBER;
    }
    return true;
  }

  public static final String _serializedATN =
      "\u0004\u00014\u0172\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"
          + "\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"
          + "\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"
          + "\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"
          + "\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"
          + "\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"
          + "\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"
          + "\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"
          + "\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"
          + "\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"
          + "\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002"
          + "#\u0007#\u0002$\u0007$\u0002%\u0007%\u0001\u0000\u0001\u0000\u0001\u0000"
          + "\u0001\u0001\u0004\u0001Q\b\u0001\u000b\u0001\f\u0001R\u0001\u0002\u0001"
          + "\u0002\u0001\u0002\u0005\u0002X\b\u0002\n\u0002\f\u0002[\t\u0002\u0001"
          + "\u0003\u0001\u0003\u0001\u0003\u0005\u0003`\b\u0003\n\u0003\f\u0003c\t"
          + "\u0003\u0001\u0004\u0003\u0004f\b\u0004\u0001\u0004\u0001\u0004\u0001"
          + "\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u0005n\b\u0005\u0001"
          + "\u0005\u0001\u0005\u0001\u0005\u0003\u0005s\b\u0005\u0003\u0005u\b\u0005"
          + "\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"
          + "\u0003\u0007}\b\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0081\b\u0007"
          + "\u0001\b\u0001\b\u0003\b\u0085\b\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001"
          + "\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"
          + "\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u009c"
          + "\b\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0004\n\u00a3\b\n\u000b\n"
          + "\f\n\u00a4\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"
          + "\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001"
          + "\f\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001\r\u0004\r\u00bb\b\r\u000b"
          + "\r\f\r\u00bc\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001"
          + "\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001"
          + "\u000f\u0001\u000f\u0004\u000f\u00cc\b\u000f\u000b\u000f\f\u000f\u00cd"
          + "\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010"
          + "\u0004\u0010\u00d6\b\u0010\u000b\u0010\f\u0010\u00d7\u0001\u0010\u0001"
          + "\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0004\u0011\u00e0"
          + "\b\u0011\u000b\u0011\f\u0011\u00e1\u0001\u0011\u0001\u0011\u0001\u0012"
          + "\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"
          + "\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"
          + "\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014"
          + "\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015"
          + "\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0016"
          + "\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017"
          + "\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0018"
          + "\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018"
          + "\u0001\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019"
          + "\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u001a\u0001\u001a\u0001\u001a"
          + "\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001b"
          + "\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"
          + "\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"
          + "\u0001\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d"
          + "\u0003\u001d\u013b\b\u001d\u0001\u001d\u0001\u001d\u0001\u001e\u0001\u001e"
          + "\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0003\u001e\u0145\b\u001e"
          + "\u0003\u001e\u0147\b\u001e\u0001\u001e\u0001\u001e\u0001\u001f\u0001\u001f"
          + "\u0003\u001f\u014d\b\u001f\u0001 \u0001 \u0001 \u0001!\u0001!\u0001!\u0003"
          + "!\u0155\b!\u0001\"\u0001\"\u0001\"\u0001\"\u0001#\u0001#\u0001#\u0003"
          + "#\u015e\b#\u0001#\u0001#\u0003#\u0162\b#\u0001#\u0003#\u0165\b#\u0001"
          + "$\u0001$\u0001$\u0003$\u016a\b$\u0001%\u0001%\u0001%\u0001%\u0001%\u0001"
          + "%\u0001%\u0000\u0000&\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012"
          + "\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJ\u0000\b\u0002"
          + "\u0000\u0004\u0004\u0006\u0007\u0001\u0000\t\n\u0001\u0000\u0012\u0014"
          + "\u0001\u0000\u0013\u0014\u0001\u0000\u000b\u000e\u0001\u0000\u0016\u0017"
          + "\u0002\u00000034\u0001\u000012\u017a\u0000L\u0001\u0000\u0000\u0000\u0002"
          + "P\u0001\u0000\u0000\u0000\u0004T\u0001\u0000\u0000\u0000\u0006\\\u0001"
          + "\u0000\u0000\u0000\be\u0001\u0000\u0000\u0000\nt\u0001\u0000\u0000\u0000"
          + "\fv\u0001\u0000\u0000\u0000\u000ex\u0001\u0000\u0000\u0000\u0010\u0084"
          + "\u0001\u0000\u0000\u0000\u0012\u009b\u0001\u0000\u0000\u0000\u0014\u009d"
          + "\u0001\u0000\u0000\u0000\u0016\u00a8\u0001\u0000\u0000\u0000\u0018\u00af"
          + "\u0001\u0000\u0000\u0000\u001a\u00b6\u0001\u0000\u0000\u0000\u001c\u00c0"
          + "\u0001\u0000\u0000\u0000\u001e\u00c7\u0001\u0000\u0000\u0000 \u00d1\u0001"
          + "\u0000\u0000\u0000\"\u00db\u0001\u0000\u0000\u0000$\u00e5\u0001\u0000"
          + "\u0000\u0000&\u00ec\u0001\u0000\u0000\u0000(\u00f3\u0001\u0000\u0000\u0000"
          + "*\u00fa\u0001\u0000\u0000\u0000,\u0101\u0001\u0000\u0000\u0000.\u0108"
          + "\u0001\u0000\u0000\u00000\u010f\u0001\u0000\u0000\u00002\u0117\u0001\u0000"
          + "\u0000\u00004\u011f\u0001\u0000\u0000\u00006\u0127\u0001\u0000\u0000\u0000"
          + "8\u012e\u0001\u0000\u0000\u0000:\u0135\u0001\u0000\u0000\u0000<\u013e"
          + "\u0001\u0000\u0000\u0000>\u014c\u0001\u0000\u0000\u0000@\u014e\u0001\u0000"
          + "\u0000\u0000B\u0151\u0001\u0000\u0000\u0000D\u0156\u0001\u0000\u0000\u0000"
          + "F\u0161\u0001\u0000\u0000\u0000H\u0166\u0001\u0000\u0000\u0000J\u016b"
          + "\u0001\u0000\u0000\u0000LM\u0003\u0002\u0001\u0000MN\u0005\u0000\u0000"
          + "\u0001N\u0001\u0001\u0000\u0000\u0000OQ\u0003\u0004\u0002\u0000PO\u0001"
          + "\u0000\u0000\u0000QR\u0001\u0000\u0000\u0000RP\u0001\u0000\u0000\u0000"
          + "RS\u0001\u0000\u0000\u0000S\u0003\u0001\u0000\u0000\u0000TY\u0003\u0006"
          + "\u0003\u0000UV\u0005\u0003\u0000\u0000VX\u0003\u0006\u0003\u0000WU\u0001"
          + "\u0000\u0000\u0000X[\u0001\u0000\u0000\u0000YW\u0001\u0000\u0000\u0000"
          + "YZ\u0001\u0000\u0000\u0000Z\u0005\u0001\u0000\u0000\u0000[Y\u0001\u0000"
          + "\u0000\u0000\\a\u0003\b\u0004\u0000]^\u0005\u0002\u0000\u0000^`\u0003"
          + "\b\u0004\u0000_]\u0001\u0000\u0000\u0000`c\u0001\u0000\u0000\u0000a_\u0001"
          + "\u0000\u0000\u0000ab\u0001\u0000\u0000\u0000b\u0007\u0001\u0000\u0000"
          + "\u0000ca\u0001\u0000\u0000\u0000df\u0007\u0000\u0000\u0000ed\u0001\u0000"
          + "\u0000\u0000ef\u0001\u0000\u0000\u0000fg\u0001\u0000\u0000\u0000gh\u0003"
          + "\n\u0005\u0000h\t\u0001\u0000\u0000\u0000iu\u0003D\"\u0000jk\u0003\f\u0006"
          + "\u0000kl\u0007\u0001\u0000\u0000ln\u0001\u0000\u0000\u0000mj\u0001\u0000"
          + "\u0000\u0000mn\u0001\u0000\u0000\u0000nr\u0001\u0000\u0000\u0000os\u0003"
          + "F#\u0000ps\u0003\u000e\u0007\u0000qs\u0003\u0012\t\u0000ro\u0001\u0000"
          + "\u0000\u0000rp\u0001\u0000\u0000\u0000rq\u0001\u0000\u0000\u0000su\u0001"
          + "\u0000\u0000\u0000ti\u0001\u0000\u0000\u0000tm\u0001\u0000\u0000\u0000"
          + "u\u000b\u0001\u0000\u0000\u0000vw\u0005\u0014\u0000\u0000w\r\u0001\u0000"
          + "\u0000\u0000xy\u0005\u0018\u0000\u0000yz\u0003\u0002\u0001\u0000z|\u0005"
          + "\b\u0000\u0000{}\u0003@ \u0000|{\u0001\u0000\u0000\u0000|}\u0001\u0000"
          + "\u0000\u0000}\u0080\u0001\u0000\u0000\u0000~\u007f\u0005\u0011\u0000\u0000"
          + "\u007f\u0081\u0005\u0013\u0000\u0000\u0080~\u0001\u0000\u0000\u0000\u0080"
          + "\u0081\u0001\u0000\u0000\u0000\u0081\u000f\u0001\u0000\u0000\u0000\u0082"
          + "\u0085\u0003\u0012\t\u0000\u0083\u0085\u0003>\u001f\u0000\u0084\u0082"
          + "\u0001\u0000\u0000\u0000\u0084\u0083\u0001\u0000\u0000\u0000\u0085\u0011"
          + "\u0001\u0000\u0000\u0000\u0086\u009c\u0003\u0014\n\u0000\u0087\u009c\u0003"
          + "\u0016\u000b\u0000\u0088\u009c\u0003\u0018\f\u0000\u0089\u009c\u0003\u001e"
          + "\u000f\u0000\u008a\u009c\u0003\u001a\r\u0000\u008b\u009c\u0003\u001c\u000e"
          + "\u0000\u008c\u009c\u0003 \u0010\u0000\u008d\u009c\u0003:\u001d\u0000\u008e"
          + "\u009c\u0003&\u0013\u0000\u008f\u009c\u0003$\u0012\u0000\u0090\u009c\u0003"
          + "\"\u0011\u0000\u0091\u009c\u0003(\u0014\u0000\u0092\u009c\u0003*\u0015"
          + "\u0000\u0093\u009c\u0003,\u0016\u0000\u0094\u009c\u0003.\u0017\u0000\u0095"
          + "\u009c\u00030\u0018\u0000\u0096\u009c\u00034\u001a\u0000\u0097\u009c\u0003"
          + "6\u001b\u0000\u0098\u009c\u00038\u001c\u0000\u0099\u009c\u00032\u0019"
          + "\u0000\u009a\u009c\u0003<\u001e\u0000\u009b\u0086\u0001\u0000\u0000\u0000"
          + "\u009b\u0087\u0001\u0000\u0000\u0000\u009b\u0088\u0001\u0000\u0000\u0000"
          + "\u009b\u0089\u0001\u0000\u0000\u0000\u009b\u008a\u0001\u0000\u0000\u0000"
          + "\u009b\u008b\u0001\u0000\u0000\u0000\u009b\u008c\u0001\u0000\u0000\u0000"
          + "\u009b\u008d\u0001\u0000\u0000\u0000\u009b\u008e\u0001\u0000\u0000\u0000"
          + "\u009b\u008f\u0001\u0000\u0000\u0000\u009b\u0090\u0001\u0000\u0000\u0000"
          + "\u009b\u0091\u0001\u0000\u0000\u0000\u009b\u0092\u0001\u0000\u0000\u0000"
          + "\u009b\u0093\u0001\u0000\u0000\u0000\u009b\u0094\u0001\u0000\u0000\u0000"
          + "\u009b\u0095\u0001\u0000\u0000\u0000\u009b\u0096\u0001\u0000\u0000\u0000"
          + "\u009b\u0097\u0001\u0000\u0000\u0000\u009b\u0098\u0001\u0000\u0000\u0000"
          + "\u009b\u0099\u0001\u0000\u0000\u0000\u009b\u009a\u0001\u0000\u0000\u0000"
          + "\u009c\u0013\u0001\u0000\u0000\u0000\u009d\u009e\u0005\u0005\u0000\u0000"
          + "\u009e\u009f\u0005\u001a\u0000\u0000\u009f\u00a0\u0005\u0018\u0000\u0000"
          + "\u00a0\u00a2\u0005\u0013\u0000\u0000\u00a1\u00a3\u0003\u0010\b\u0000\u00a2"
          + "\u00a1\u0001\u0000\u0000\u0000\u00a3\u00a4\u0001\u0000\u0000\u0000\u00a4"
          + "\u00a2\u0001\u0000\u0000\u0000\u00a4\u00a5\u0001\u0000\u0000\u0000\u00a5"
          + "\u00a6\u0001\u0000\u0000\u0000\u00a6\u00a7\u0005\b\u0000\u0000\u00a7\u0015"
          + "\u0001\u0000\u0000\u0000\u00a8\u00a9\u0005\u0005\u0000\u0000\u00a9\u00aa"
          + "\u0005#\u0000\u0000\u00aa\u00ab\u0005\u0018\u0000\u0000\u00ab\u00ac\u0005"
          + "\u0013\u0000\u0000\u00ac\u00ad\u0003\u0010\b\u0000\u00ad\u00ae\u0005\b"
          + "\u0000\u0000\u00ae\u0017\u0001\u0000\u0000\u0000\u00af\u00b0\u0005\u0005"
          + "\u0000\u0000\u00b0\u00b1\u0005\"\u0000\u0000\u00b1\u00b2\u0005\u0018\u0000"
          + "\u0000\u00b2\u00b3\u0005\u0013\u0000\u0000\u00b3\u00b4\u0003\u0010\b\u0000"
          + "\u00b4\u00b5\u0005\b\u0000\u0000\u00b5\u0019\u0001\u0000\u0000\u0000\u00b6"
          + "\u00b7\u0005\u0005\u0000\u0000\u00b7\u00b8\u0005+\u0000\u0000\u00b8\u00ba"
          + "\u0005\u0018\u0000\u0000\u00b9\u00bb\u0003\u0010\b\u0000\u00ba\u00b9\u0001"
          + "\u0000\u0000\u0000\u00bb\u00bc\u0001\u0000\u0000\u0000\u00bc\u00ba\u0001"
          + "\u0000\u0000\u0000\u00bc\u00bd\u0001\u0000\u0000\u0000\u00bd\u00be\u0001"
          + "\u0000\u0000\u0000\u00be\u00bf\u0005\b\u0000\u0000\u00bf\u001b\u0001\u0000"
          + "\u0000\u0000\u00c0\u00c1\u0005\u0005\u0000\u0000\u00c1\u00c2\u0005,\u0000"
          + "\u0000\u00c2\u00c3\u0005\u0018\u0000\u0000\u00c3\u00c4\u0003\u0010\b\u0000"
          + "\u00c4\u00c5\u0003\u0010\b\u0000\u00c5\u00c6\u0005\b\u0000\u0000\u00c6"
          + "\u001d\u0001\u0000\u0000\u0000\u00c7\u00c8\u0005\u0005\u0000\u0000\u00c8"
          + "\u00c9\u0005(\u0000\u0000\u00c9\u00cb\u0005\u0018\u0000\u0000\u00ca\u00cc"
          + "\u0003\u0010\b\u0000\u00cb\u00ca\u0001\u0000\u0000\u0000\u00cc\u00cd\u0001"
          + "\u0000\u0000\u0000\u00cd\u00cb\u0001\u0000\u0000\u0000\u00cd\u00ce\u0001"
          + "\u0000\u0000\u0000\u00ce\u00cf\u0001\u0000\u0000\u0000\u00cf\u00d0\u0005"
          + "\b\u0000\u0000\u00d0\u001f\u0001\u0000\u0000\u0000\u00d1\u00d2\u0005\u0005"
          + "\u0000\u0000\u00d2\u00d3\u0005 \u0000\u0000\u00d3\u00d5\u0005\u0018\u0000"
          + "\u0000\u00d4\u00d6\u0003\u0010\b\u0000\u00d5\u00d4\u0001\u0000\u0000\u0000"
          + "\u00d6\u00d7\u0001\u0000\u0000\u0000\u00d7\u00d5\u0001\u0000\u0000\u0000"
          + "\u00d7\u00d8\u0001\u0000\u0000\u0000\u00d8\u00d9\u0001\u0000\u0000\u0000"
          + "\u00d9\u00da\u0005\b\u0000\u0000\u00da!\u0001\u0000\u0000\u0000\u00db"
          + "\u00dc\u0005\u0005\u0000\u0000\u00dc\u00dd\u0005*\u0000\u0000\u00dd\u00df"
          + "\u0005\u0018\u0000\u0000\u00de\u00e0\u0003\u0010\b\u0000\u00df\u00de\u0001"
          + "\u0000\u0000\u0000\u00e0\u00e1\u0001\u0000\u0000\u0000\u00e1\u00df\u0001"
          + "\u0000\u0000\u0000\u00e1\u00e2\u0001\u0000\u0000\u0000\u00e2\u00e3\u0001"
          + "\u0000\u0000\u0000\u00e3\u00e4\u0005\b\u0000\u0000\u00e4#\u0001\u0000"
          + "\u0000\u0000\u00e5\u00e6\u0005\u0005\u0000\u0000\u00e6\u00e7\u0005\u001c"
          + "\u0000\u0000\u00e7\u00e8\u0005\u0018\u0000\u0000\u00e8\u00e9\u0003\u0010"
          + "\b\u0000\u00e9\u00ea\u0003\u0010\b\u0000\u00ea\u00eb\u0005\b\u0000\u0000"
          + "\u00eb%\u0001\u0000\u0000\u0000\u00ec\u00ed\u0005\u0005\u0000\u0000\u00ed"
          + "\u00ee\u0005\u001b\u0000\u0000\u00ee\u00ef\u0005\u0018\u0000\u0000\u00ef"
          + "\u00f0\u0003\u0010\b\u0000\u00f0\u00f1\u0003\u0010\b\u0000\u00f1\u00f2"
          + "\u0005\b\u0000\u0000\u00f2\'\u0001\u0000\u0000\u0000\u00f3\u00f4\u0005"
          + "\u0005\u0000\u0000\u00f4\u00f5\u0005\u001e\u0000\u0000\u00f5\u00f6\u0005"
          + "\u0018\u0000\u0000\u00f6\u00f7\u0003\u0010\b\u0000\u00f7\u00f8\u0003\u0010"
          + "\b\u0000\u00f8\u00f9\u0005\b\u0000\u0000\u00f9)\u0001\u0000\u0000\u0000"
          + "\u00fa\u00fb\u0005\u0005\u0000\u0000\u00fb\u00fc\u0005&\u0000\u0000\u00fc"
          + "\u00fd\u0005\u0018\u0000\u0000\u00fd\u00fe\u0003\u0010\b\u0000\u00fe\u00ff"
          + "\u0003\u0010\b\u0000\u00ff\u0100\u0005\b\u0000\u0000\u0100+\u0001\u0000"
          + "\u0000\u0000\u0101\u0102\u0005\u0005\u0000\u0000\u0102\u0103\u0005\u001d"
          + "\u0000\u0000\u0103\u0104\u0005\u0018\u0000\u0000\u0104\u0105\u0003\u0010"
          + "\b\u0000\u0105\u0106\u0003\u0010\b\u0000\u0106\u0107\u0005\b\u0000\u0000"
          + "\u0107-\u0001\u0000\u0000\u0000\u0108\u0109\u0005\u0005\u0000\u0000\u0109"
          + "\u010a\u0005%\u0000\u0000\u010a\u010b\u0005\u0018\u0000\u0000\u010b\u010c"
          + "\u0003\u0010\b\u0000\u010c\u010d\u0003\u0010\b\u0000\u010d\u010e\u0005"
          + "\b\u0000\u0000\u010e/\u0001\u0000\u0000\u0000\u010f\u0110\u0005\u0005"
          + "\u0000\u0000\u0110\u0111\u0005.\u0000\u0000\u0111\u0112\u0005\u0018\u0000"
          + "\u0000\u0112\u0113\u0003\u0010\b\u0000\u0113\u0114\u0005\u0013\u0000\u0000"
          + "\u0114\u0115\u0003\u0010\b\u0000\u0115\u0116\u0005\b\u0000\u0000\u0116"
          + "1\u0001\u0000\u0000\u0000\u0117\u0118\u0005\u0005\u0000\u0000\u0118\u0119"
          + "\u0005\u001f\u0000\u0000\u0119\u011a\u0005\u0018\u0000\u0000\u011a\u011b"
          + "\u0003\u0010\b\u0000\u011b\u011c\u0005\u0013\u0000\u0000\u011c\u011d\u0005"
          + "\u0013\u0000\u0000\u011d\u011e\u0005\b\u0000\u0000\u011e3\u0001\u0000"
          + "\u0000\u0000\u011f\u0120\u0005\u0005\u0000\u0000\u0120\u0121\u0005\'\u0000"
          + "\u0000\u0121\u0122\u0005\u0018\u0000\u0000\u0122\u0123\u0003\u0010\b\u0000"
          + "\u0123\u0124\u0005\u0013\u0000\u0000\u0124\u0125\u0003\u0010\b\u0000\u0125"
          + "\u0126\u0005\b\u0000\u0000\u01265\u0001\u0000\u0000\u0000\u0127\u0128"
          + "\u0005\u0005\u0000\u0000\u0128\u0129\u0005)\u0000\u0000\u0129\u012a\u0005"
          + "\u0018\u0000\u0000\u012a\u012b\u0003\u0010\b\u0000\u012b\u012c\u0003\u0010"
          + "\b\u0000\u012c\u012d\u0005\b\u0000\u0000\u012d7\u0001\u0000\u0000\u0000"
          + "\u012e\u012f\u0005\u0005\u0000\u0000\u012f\u0130\u0005$\u0000\u0000\u0130"
          + "\u0131\u0005\u0018\u0000\u0000\u0131\u0132\u0003\u0010\b\u0000\u0132\u0133"
          + "\u0003\u0010\b\u0000\u0133\u0134\u0005\b\u0000\u0000\u01349\u0001\u0000"
          + "\u0000\u0000\u0135\u0136\u0005\u0005\u0000\u0000\u0136\u0137\u0005-\u0000"
          + "\u0000\u0137\u0138\u0005\u0018\u0000\u0000\u0138\u013a\u0007\u0002\u0000"
          + "\u0000\u0139\u013b\u0005\u0013\u0000\u0000\u013a\u0139\u0001\u0000\u0000"
          + "\u0000\u013a\u013b\u0001\u0000\u0000\u0000\u013b\u013c\u0001\u0000\u0000"
          + "\u0000\u013c\u013d\u0005\b\u0000\u0000\u013d;\u0001\u0000\u0000\u0000"
          + "\u013e\u013f\u0005\u0005\u0000\u0000\u013f\u0140\u0005!\u0000\u0000\u0140"
          + "\u0141\u0005\u0018\u0000\u0000\u0141\u0146\u0007\u0002\u0000\u0000\u0142"
          + "\u0144\u0005\u0013\u0000\u0000\u0143\u0145\u0005\u0013\u0000\u0000\u0144"
          + "\u0143\u0001\u0000\u0000\u0000\u0144\u0145\u0001\u0000\u0000\u0000\u0145"
          + "\u0147\u0001\u0000\u0000\u0000\u0146\u0142\u0001\u0000\u0000\u0000\u0146"
          + "\u0147\u0001\u0000\u0000\u0000\u0147\u0148\u0001\u0000\u0000\u0000\u0148"
          + "\u0149\u0005\b\u0000\u0000\u0149=\u0001\u0000\u0000\u0000\u014a\u014d"
          + "\u0005\u0012\u0000\u0000\u014b\u014d\u0007\u0003\u0000\u0000\u014c\u014a"
          + "\u0001\u0000\u0000\u0000\u014c\u014b\u0001\u0000\u0000\u0000\u014d?\u0001"
          + "\u0000\u0000\u0000\u014e\u014f\u0005\u000f\u0000\u0000\u014f\u0150\u0005"
          + "\u0013\u0000\u0000\u0150A\u0001\u0000\u0000\u0000\u0151\u0154\u0005\u0010"
          + "\u0000\u0000\u0152\u0155\u0005\u0013\u0000\u0000\u0153\u0155\u0004!\u0000"
          + "\u0000\u0154\u0152\u0001\u0000\u0000\u0000\u0154\u0153\u0001\u0000\u0000"
          + "\u0000\u0155C\u0001\u0000\u0000\u0000\u0156\u0157\u0003\f\u0006\u0000"
          + "\u0157\u0158\u0007\u0004\u0000\u0000\u0158\u0159\u0007\u0002\u0000\u0000"
          + "\u0159E\u0001\u0000\u0000\u0000\u015a\u0162\u0005\u0015\u0000\u0000\u015b"
          + "\u015d\u0007\u0003\u0000\u0000\u015c\u015e\u0003B!\u0000\u015d\u015c\u0001"
          + "\u0000\u0000\u0000\u015d\u015e\u0001\u0000\u0000\u0000\u015e\u0162\u0001"
          + "\u0000\u0000\u0000\u015f\u0162\u0003J%\u0000\u0160\u0162\u0003H$\u0000"
          + "\u0161\u015a\u0001\u0000\u0000\u0000\u0161\u015b\u0001\u0000\u0000\u0000"
          + "\u0161\u015f\u0001\u0000\u0000\u0000\u0161\u0160\u0001\u0000\u0000\u0000"
          + "\u0162\u0164\u0001\u0000\u0000\u0000\u0163\u0165\u0003@ \u0000\u0164\u0163"
          + "\u0001\u0000\u0000\u0000\u0164\u0165\u0001\u0000\u0000\u0000\u0165G\u0001"
          + "\u0000\u0000\u0000\u0166\u0169\u0005\u0012\u0000\u0000\u0167\u0168\u0005"
          + "\u0010\u0000\u0000\u0168\u016a\u0005\u0013\u0000\u0000\u0169\u0167\u0001"
          + "\u0000\u0000\u0000\u0169\u016a\u0001\u0000\u0000\u0000\u016aI\u0001\u0000"
          + "\u0000\u0000\u016b\u016c\u0007\u0005\u0000\u0000\u016c\u016d\u0007\u0006"
          + "\u0000\u0000\u016d\u016e\u00050\u0000\u0000\u016e\u016f\u0007\u0006\u0000"
          + "\u0000\u016f\u0170\u0007\u0007\u0000\u0000\u0170K\u0001\u0000\u0000\u0000"
          + "\u0019RYaemrt|\u0080\u0084\u009b\u00a4\u00bc\u00cd\u00d7\u00e1\u013a\u0144"
          + "\u0146\u014c\u0154\u015d\u0161\u0164\u0169";
  public static final ATN _ATN = new ATNDeserializer().deserialize(_serializedATN.toCharArray());

  static {
    _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
    for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
      _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
    }
  }
}
