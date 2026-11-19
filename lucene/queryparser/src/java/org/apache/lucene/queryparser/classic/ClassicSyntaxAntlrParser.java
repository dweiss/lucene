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

package org.apache.lucene.queryparser.classic;

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
class ClassicSyntaxAntlrParser extends Parser {
  static {
    RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION);
  }

  protected static final DFA[] _decisionToDFA;
  protected static final PredictionContextCache _sharedContextCache = new PredictionContextCache();
  public static final int WS = 1,
      AND = 2,
      OR = 3,
      NOT = 4,
      PLUS = 5,
      MINUS = 6,
      BAREOPER = 7,
      LPAREN = 8,
      RPAREN = 9,
      COLON = 10,
      STAR = 11,
      CARAT = 12,
      QUOTED = 13,
      TERM = 14,
      FUZZY_SLOP = 15,
      PREFIXTERM = 16,
      WILDTERM = 17,
      REGEXPTERM = 18,
      RANGEIN_START = 19,
      RANGEEX_START = 20,
      NUMBER = 21,
      RANGE_WS = 22,
      RANGE_TO = 23,
      RANGEIN_END = 24,
      RANGEEX_END = 25,
      RANGE_QUOTED = 26,
      RANGE_GOOP = 27;
  public static final int RULE_topLevelQuery = 0,
      RULE_query = 1,
      RULE_conjClause = 2,
      RULE_modClause = 3,
      RULE_clause = 4,
      RULE_term = 5;

  private static String[] makeRuleNames() {
    return new String[] {"topLevelQuery", "query", "conjClause", "modClause", "clause", "term"};
  }

  public static final String[] ruleNames = makeRuleNames();

  private static String[] makeLiteralNames() {
    return new String[] {
      null, null, null, null, null, "'+'", "'-'", null, "'('", "')'", "':'", "'*'", "'^'", null,
      null, null, null, null, null, "'['", "'{'", null, null, "'TO'", "']'", "'}'"
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
      "PLUS",
      "MINUS",
      "BAREOPER",
      "LPAREN",
      "RPAREN",
      "COLON",
      "STAR",
      "CARAT",
      "QUOTED",
      "TERM",
      "FUZZY_SLOP",
      "PREFIXTERM",
      "WILDTERM",
      "REGEXPTERM",
      "RANGEIN_START",
      "RANGEEX_START",
      "NUMBER",
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
    return "ClassicSyntaxAntlrParser.g4";
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

  public ClassicSyntaxAntlrParser(TokenStream input) {
    super(input);
    _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
  }

  @SuppressWarnings("CheckReturnValue")
  public static class TopLevelQueryContext extends ParserRuleContext {
    public QueryContext query() {
      return getRuleContext(QueryContext.class, 0);
    }

    public TerminalNode EOF() {
      return getToken(ClassicSyntaxAntlrParser.EOF, 0);
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
        setState(12);
        query();
        setState(13);
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
    public ModClauseContext modClause() {
      return getRuleContext(ModClauseContext.class, 0);
    }

    public List<ConjClauseContext> conjClause() {
      return getRuleContexts(ConjClauseContext.class);
    }

    public ConjClauseContext conjClause(int i) {
      return getRuleContext(ConjClauseContext.class, i);
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
        setState(15);
        modClause();
        setState(19);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4155900L) != 0)) {
          {
            {
              setState(16);
              conjClause();
            }
          }
          setState(21);
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
  public static class ConjClauseContext extends ParserRuleContext {
    public Token conjunction;

    public ModClauseContext modClause() {
      return getRuleContext(ModClauseContext.class, 0);
    }

    public TerminalNode AND() {
      return getToken(ClassicSyntaxAntlrParser.AND, 0);
    }

    public TerminalNode OR() {
      return getToken(ClassicSyntaxAntlrParser.OR, 0);
    }

    public ConjClauseContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_conjClause;
    }
  }

  public final ConjClauseContext conjClause() throws RecognitionException {
    ConjClauseContext _localctx = new ConjClauseContext(_ctx, getState());
    enterRule(_localctx, 4, RULE_conjClause);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(23);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == AND || _la == OR) {
          {
            setState(22);
            ((ConjClauseContext) _localctx).conjunction = _input.LT(1);
            _la = _input.LA(1);
            if (!(_la == AND || _la == OR)) {
              ((ConjClauseContext) _localctx).conjunction = (Token) _errHandler.recoverInline(this);
            } else {
              if (_input.LA(1) == Token.EOF) matchedEOF = true;
              _errHandler.reportMatch(this);
              consume();
            }
          }
        }

        setState(25);
        modClause();
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
      return getToken(ClassicSyntaxAntlrParser.PLUS, 0);
    }

    public TerminalNode MINUS() {
      return getToken(ClassicSyntaxAntlrParser.MINUS, 0);
    }

    public TerminalNode NOT() {
      return getToken(ClassicSyntaxAntlrParser.NOT, 0);
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
    enterRule(_localctx, 6, RULE_modClause);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(28);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 112L) != 0)) {
          {
            setState(27);
            ((ModClauseContext) _localctx).modifier = _input.LT(1);
            _la = _input.LA(1);
            if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 112L) != 0))) {
              ((ModClauseContext) _localctx).modifier = (Token) _errHandler.recoverInline(this);
            } else {
              if (_input.LA(1) == Token.EOF) matchedEOF = true;
              _errHandler.reportMatch(this);
              consume();
            }
          }
        }

        setState(30);
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
    public Token field;
    public Token boost;

    public TermContext term() {
      return getRuleContext(TermContext.class, 0);
    }

    public TerminalNode LPAREN() {
      return getToken(ClassicSyntaxAntlrParser.LPAREN, 0);
    }

    public QueryContext query() {
      return getRuleContext(QueryContext.class, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(ClassicSyntaxAntlrParser.RPAREN, 0);
    }

    public TerminalNode COLON() {
      return getToken(ClassicSyntaxAntlrParser.COLON, 0);
    }

    public TerminalNode TERM() {
      return getToken(ClassicSyntaxAntlrParser.TERM, 0);
    }

    public TerminalNode STAR() {
      return getToken(ClassicSyntaxAntlrParser.STAR, 0);
    }

    public TerminalNode CARAT() {
      return getToken(ClassicSyntaxAntlrParser.CARAT, 0);
    }

    public TerminalNode NUMBER() {
      return getToken(ClassicSyntaxAntlrParser.NUMBER, 0);
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
    enterRule(_localctx, 8, RULE_clause);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(34);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 3, _ctx)) {
          case 1:
            {
              setState(32);
              ((ClauseContext) _localctx).field = _input.LT(1);
              _la = _input.LA(1);
              if (!(_la == STAR || _la == TERM)) {
                ((ClauseContext) _localctx).field = (Token) _errHandler.recoverInline(this);
              } else {
                if (_input.LA(1) == Token.EOF) matchedEOF = true;
                _errHandler.reportMatch(this);
                consume();
              }
              setState(33);
              match(COLON);
            }
            break;
        }
        setState(44);
        _errHandler.sync(this);
        switch (_input.LA(1)) {
          case BAREOPER:
          case STAR:
          case QUOTED:
          case TERM:
          case PREFIXTERM:
          case WILDTERM:
          case REGEXPTERM:
          case RANGEIN_START:
          case RANGEEX_START:
          case NUMBER:
            {
              setState(36);
              term();
            }
            break;
          case LPAREN:
            {
              setState(37);
              match(LPAREN);
              setState(38);
              query();
              setState(39);
              match(RPAREN);
              setState(42);
              _errHandler.sync(this);
              _la = _input.LA(1);
              if (_la == CARAT) {
                {
                  setState(40);
                  match(CARAT);
                  setState(41);
                  ((ClauseContext) _localctx).boost = match(NUMBER);
                }
              }
            }
            break;
          default:
            throw new NoViableAltException(this);
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
    public Token bare;
    public Token boost;
    public Token fuzzySlop;
    public Token open;
    public Token left;
    public Token right;
    public Token close;
    public Token quoted;

    public TerminalNode TERM() {
      return getToken(ClassicSyntaxAntlrParser.TERM, 0);
    }

    public TerminalNode STAR() {
      return getToken(ClassicSyntaxAntlrParser.STAR, 0);
    }

    public TerminalNode PREFIXTERM() {
      return getToken(ClassicSyntaxAntlrParser.PREFIXTERM, 0);
    }

    public TerminalNode WILDTERM() {
      return getToken(ClassicSyntaxAntlrParser.WILDTERM, 0);
    }

    public TerminalNode REGEXPTERM() {
      return getToken(ClassicSyntaxAntlrParser.REGEXPTERM, 0);
    }

    public List<TerminalNode> NUMBER() {
      return getTokens(ClassicSyntaxAntlrParser.NUMBER);
    }

    public TerminalNode NUMBER(int i) {
      return getToken(ClassicSyntaxAntlrParser.NUMBER, i);
    }

    public TerminalNode BAREOPER() {
      return getToken(ClassicSyntaxAntlrParser.BAREOPER, 0);
    }

    public TerminalNode CARAT() {
      return getToken(ClassicSyntaxAntlrParser.CARAT, 0);
    }

    public TerminalNode FUZZY_SLOP() {
      return getToken(ClassicSyntaxAntlrParser.FUZZY_SLOP, 0);
    }

    public List<TerminalNode> RANGE_TO() {
      return getTokens(ClassicSyntaxAntlrParser.RANGE_TO);
    }

    public TerminalNode RANGE_TO(int i) {
      return getToken(ClassicSyntaxAntlrParser.RANGE_TO, i);
    }

    public TerminalNode RANGEIN_START() {
      return getToken(ClassicSyntaxAntlrParser.RANGEIN_START, 0);
    }

    public TerminalNode RANGEEX_START() {
      return getToken(ClassicSyntaxAntlrParser.RANGEEX_START, 0);
    }

    public List<TerminalNode> RANGE_GOOP() {
      return getTokens(ClassicSyntaxAntlrParser.RANGE_GOOP);
    }

    public TerminalNode RANGE_GOOP(int i) {
      return getToken(ClassicSyntaxAntlrParser.RANGE_GOOP, i);
    }

    public List<TerminalNode> RANGE_QUOTED() {
      return getTokens(ClassicSyntaxAntlrParser.RANGE_QUOTED);
    }

    public TerminalNode RANGE_QUOTED(int i) {
      return getToken(ClassicSyntaxAntlrParser.RANGE_QUOTED, i);
    }

    public TerminalNode RANGEIN_END() {
      return getToken(ClassicSyntaxAntlrParser.RANGEIN_END, 0);
    }

    public TerminalNode RANGEEX_END() {
      return getToken(ClassicSyntaxAntlrParser.RANGEEX_END, 0);
    }

    public TerminalNode QUOTED() {
      return getToken(ClassicSyntaxAntlrParser.QUOTED, 0);
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
    enterRule(_localctx, 10, RULE_term);
    int _la;
    try {
      setState(81);
      _errHandler.sync(this);
      switch (_input.LA(1)) {
        case BAREOPER:
        case STAR:
        case TERM:
        case PREFIXTERM:
        case WILDTERM:
        case REGEXPTERM:
        case NUMBER:
          enterOuterAlt(_localctx, 1);
          {
            setState(46);
            ((TermContext) _localctx).bare = _input.LT(1);
            _la = _input.LA(1);
            if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 2574464L) != 0))) {
              ((TermContext) _localctx).bare = (Token) _errHandler.recoverInline(this);
            } else {
              if (_input.LA(1) == Token.EOF) matchedEOF = true;
              _errHandler.reportMatch(this);
              consume();
            }
            setState(57);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
              case CARAT:
                {
                  setState(47);
                  match(CARAT);
                  setState(48);
                  ((TermContext) _localctx).boost = match(NUMBER);
                  setState(50);
                  _errHandler.sync(this);
                  _la = _input.LA(1);
                  if (_la == FUZZY_SLOP) {
                    {
                      setState(49);
                      ((TermContext) _localctx).fuzzySlop = match(FUZZY_SLOP);
                    }
                  }
                }
                break;
              case FUZZY_SLOP:
                {
                  setState(52);
                  ((TermContext) _localctx).fuzzySlop = match(FUZZY_SLOP);
                  setState(55);
                  _errHandler.sync(this);
                  _la = _input.LA(1);
                  if (_la == CARAT) {
                    {
                      setState(53);
                      match(CARAT);
                      setState(54);
                      ((TermContext) _localctx).boost = match(NUMBER);
                    }
                  }
                }
                break;
              case EOF:
              case AND:
              case OR:
              case NOT:
              case PLUS:
              case MINUS:
              case BAREOPER:
              case LPAREN:
              case RPAREN:
              case STAR:
              case QUOTED:
              case TERM:
              case PREFIXTERM:
              case WILDTERM:
              case REGEXPTERM:
              case RANGEIN_START:
              case RANGEEX_START:
              case NUMBER:
                break;
              default:
                break;
            }
          }
          break;
        case RANGEIN_START:
        case RANGEEX_START:
          enterOuterAlt(_localctx, 2);
          {
            setState(59);
            ((TermContext) _localctx).open = _input.LT(1);
            _la = _input.LA(1);
            if (!(_la == RANGEIN_START || _la == RANGEEX_START)) {
              ((TermContext) _localctx).open = (Token) _errHandler.recoverInline(this);
            } else {
              if (_input.LA(1) == Token.EOF) matchedEOF = true;
              _errHandler.reportMatch(this);
              consume();
            }
            setState(60);
            ((TermContext) _localctx).left = _input.LT(1);
            _la = _input.LA(1);
            if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 209715200L) != 0))) {
              ((TermContext) _localctx).left = (Token) _errHandler.recoverInline(this);
            } else {
              if (_input.LA(1) == Token.EOF) matchedEOF = true;
              _errHandler.reportMatch(this);
              consume();
            }
            setState(61);
            match(RANGE_TO);
            setState(62);
            ((TermContext) _localctx).right = _input.LT(1);
            _la = _input.LA(1);
            if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 209715200L) != 0))) {
              ((TermContext) _localctx).right = (Token) _errHandler.recoverInline(this);
            } else {
              if (_input.LA(1) == Token.EOF) matchedEOF = true;
              _errHandler.reportMatch(this);
              consume();
            }
            setState(63);
            ((TermContext) _localctx).close = _input.LT(1);
            _la = _input.LA(1);
            if (!(_la == RANGEIN_END || _la == RANGEEX_END)) {
              ((TermContext) _localctx).close = (Token) _errHandler.recoverInline(this);
            } else {
              if (_input.LA(1) == Token.EOF) matchedEOF = true;
              _errHandler.reportMatch(this);
              consume();
            }
            setState(66);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == CARAT) {
              {
                setState(64);
                match(CARAT);
                setState(65);
                ((TermContext) _localctx).boost = match(NUMBER);
              }
            }
          }
          break;
        case QUOTED:
          enterOuterAlt(_localctx, 3);
          {
            setState(68);
            ((TermContext) _localctx).quoted = match(QUOTED);
            setState(79);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
              case CARAT:
                {
                  setState(69);
                  match(CARAT);
                  setState(70);
                  ((TermContext) _localctx).boost = match(NUMBER);
                  setState(72);
                  _errHandler.sync(this);
                  _la = _input.LA(1);
                  if (_la == FUZZY_SLOP) {
                    {
                      setState(71);
                      ((TermContext) _localctx).fuzzySlop = match(FUZZY_SLOP);
                    }
                  }
                }
                break;
              case FUZZY_SLOP:
                {
                  setState(74);
                  ((TermContext) _localctx).fuzzySlop = match(FUZZY_SLOP);
                  setState(77);
                  _errHandler.sync(this);
                  _la = _input.LA(1);
                  if (_la == CARAT) {
                    {
                      setState(75);
                      match(CARAT);
                      setState(76);
                      ((TermContext) _localctx).boost = match(NUMBER);
                    }
                  }
                }
                break;
              case EOF:
              case AND:
              case OR:
              case NOT:
              case PLUS:
              case MINUS:
              case BAREOPER:
              case LPAREN:
              case RPAREN:
              case STAR:
              case QUOTED:
              case TERM:
              case PREFIXTERM:
              case WILDTERM:
              case REGEXPTERM:
              case RANGEIN_START:
              case RANGEEX_START:
              case NUMBER:
                break;
              default:
                break;
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

  public static final String _serializedATN =
      "\u0004\u0001\u001bT\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"
          + "\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"
          + "\u0005\u0007\u0005\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001"
          + "\u0001\u0005\u0001\u0012\b\u0001\n\u0001\f\u0001\u0015\t\u0001\u0001\u0002"
          + "\u0003\u0002\u0018\b\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0003\u0003"
          + "\u001d\b\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0003\u0004"
          + "#\b\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"
          + "\u0001\u0004\u0003\u0004+\b\u0004\u0003\u0004-\b\u0004\u0001\u0005\u0001"
          + "\u0005\u0001\u0005\u0001\u0005\u0003\u00053\b\u0005\u0001\u0005\u0001"
          + "\u0005\u0001\u0005\u0003\u00058\b\u0005\u0003\u0005:\b\u0005\u0001\u0005"
          + "\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"
          + "\u0003\u0005C\b\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"
          + "\u0003\u0005I\b\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u0005"
          + "N\b\u0005\u0003\u0005P\b\u0005\u0003\u0005R\b\u0005\u0001\u0005\u0000"
          + "\u0000\u0006\u0000\u0002\u0004\u0006\b\n\u0000\u0007\u0001\u0000\u0002"
          + "\u0003\u0001\u0000\u0004\u0006\u0002\u0000\u000b\u000b\u000e\u000e\u0005"
          + "\u0000\u0007\u0007\u000b\u000b\u000e\u000e\u0010\u0012\u0015\u0015\u0001"
          + "\u0000\u0013\u0014\u0002\u0000\u0017\u0017\u001a\u001b\u0001\u0000\u0018"
          + "\u0019^\u0000\f\u0001\u0000\u0000\u0000\u0002\u000f\u0001\u0000\u0000"
          + "\u0000\u0004\u0017\u0001\u0000\u0000\u0000\u0006\u001c\u0001\u0000\u0000"
          + "\u0000\b\"\u0001\u0000\u0000\u0000\nQ\u0001\u0000\u0000\u0000\f\r\u0003"
          + "\u0002\u0001\u0000\r\u000e\u0005\u0000\u0000\u0001\u000e\u0001\u0001\u0000"
          + "\u0000\u0000\u000f\u0013\u0003\u0006\u0003\u0000\u0010\u0012\u0003\u0004"
          + "\u0002\u0000\u0011\u0010\u0001\u0000\u0000\u0000\u0012\u0015\u0001\u0000"
          + "\u0000\u0000\u0013\u0011\u0001\u0000\u0000\u0000\u0013\u0014\u0001\u0000"
          + "\u0000\u0000\u0014\u0003\u0001\u0000\u0000\u0000\u0015\u0013\u0001\u0000"
          + "\u0000\u0000\u0016\u0018\u0007\u0000\u0000\u0000\u0017\u0016\u0001\u0000"
          + "\u0000\u0000\u0017\u0018\u0001\u0000\u0000\u0000\u0018\u0019\u0001\u0000"
          + "\u0000\u0000\u0019\u001a\u0003\u0006\u0003\u0000\u001a\u0005\u0001\u0000"
          + "\u0000\u0000\u001b\u001d\u0007\u0001\u0000\u0000\u001c\u001b\u0001\u0000"
          + "\u0000\u0000\u001c\u001d\u0001\u0000\u0000\u0000\u001d\u001e\u0001\u0000"
          + "\u0000\u0000\u001e\u001f\u0003\b\u0004\u0000\u001f\u0007\u0001\u0000\u0000"
          + "\u0000 !\u0007\u0002\u0000\u0000!#\u0005\n\u0000\u0000\" \u0001\u0000"
          + "\u0000\u0000\"#\u0001\u0000\u0000\u0000#,\u0001\u0000\u0000\u0000$-\u0003"
          + "\n\u0005\u0000%&\u0005\b\u0000\u0000&\'\u0003\u0002\u0001\u0000\'*\u0005"
          + "\t\u0000\u0000()\u0005\f\u0000\u0000)+\u0005\u0015\u0000\u0000*(\u0001"
          + "\u0000\u0000\u0000*+\u0001\u0000\u0000\u0000+-\u0001\u0000\u0000\u0000"
          + ",$\u0001\u0000\u0000\u0000,%\u0001\u0000\u0000\u0000-\t\u0001\u0000\u0000"
          + "\u0000.9\u0007\u0003\u0000\u0000/0\u0005\f\u0000\u000002\u0005\u0015\u0000"
          + "\u000013\u0005\u000f\u0000\u000021\u0001\u0000\u0000\u000023\u0001\u0000"
          + "\u0000\u00003:\u0001\u0000\u0000\u000047\u0005\u000f\u0000\u000056\u0005"
          + "\f\u0000\u000068\u0005\u0015\u0000\u000075\u0001\u0000\u0000\u000078\u0001"
          + "\u0000\u0000\u00008:\u0001\u0000\u0000\u00009/\u0001\u0000\u0000\u0000"
          + "94\u0001\u0000\u0000\u00009:\u0001\u0000\u0000\u0000:R\u0001\u0000\u0000"
          + "\u0000;<\u0007\u0004\u0000\u0000<=\u0007\u0005\u0000\u0000=>\u0005\u0017"
          + "\u0000\u0000>?\u0007\u0005\u0000\u0000?B\u0007\u0006\u0000\u0000@A\u0005"
          + "\f\u0000\u0000AC\u0005\u0015\u0000\u0000B@\u0001\u0000\u0000\u0000BC\u0001"
          + "\u0000\u0000\u0000CR\u0001\u0000\u0000\u0000DO\u0005\r\u0000\u0000EF\u0005"
          + "\f\u0000\u0000FH\u0005\u0015\u0000\u0000GI\u0005\u000f\u0000\u0000HG\u0001"
          + "\u0000\u0000\u0000HI\u0001\u0000\u0000\u0000IP\u0001\u0000\u0000\u0000"
          + "JM\u0005\u000f\u0000\u0000KL\u0005\f\u0000\u0000LN\u0005\u0015\u0000\u0000"
          + "MK\u0001\u0000\u0000\u0000MN\u0001\u0000\u0000\u0000NP\u0001\u0000\u0000"
          + "\u0000OE\u0001\u0000\u0000\u0000OJ\u0001\u0000\u0000\u0000OP\u0001\u0000"
          + "\u0000\u0000PR\u0001\u0000\u0000\u0000Q.\u0001\u0000\u0000\u0000Q;\u0001"
          + "\u0000\u0000\u0000QD\u0001\u0000\u0000\u0000R\u000b\u0001\u0000\u0000"
          + "\u0000\u000e\u0013\u0017\u001c\"*,279BHMOQ";
  public static final ATN _ATN = new ATNDeserializer().deserialize(_serializedATN.toCharArray());

  static {
    _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
    for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
      _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
    }
  }
}
