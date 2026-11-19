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

package org.apache.lucene.queryparser.surround.parser;

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
class SurroundSyntaxAntlrParser extends Parser {
  static {
    RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION);
  }

  protected static final DFA[] _decisionToDFA;
  protected static final PredictionContextCache _sharedContextCache = new PredictionContextCache();
  public static final int WS = 1,
      OR = 2,
      AND = 3,
      NOT = 4,
      W = 5,
      N = 6,
      LPAREN = 7,
      RPAREN = 8,
      COMMA = 9,
      COLON = 10,
      CARAT = 11,
      TRUNCQUOTED = 12,
      QUOTED = 13,
      SUFFIXTERM = 14,
      TRUNCTERM = 15,
      TERM = 16,
      NUMBER = 17;
  public static final int RULE_topSrndQuery = 0,
      RULE_fieldsQuery = 1,
      RULE_orQuery = 2,
      RULE_andQuery = 3,
      RULE_notQuery = 4,
      RULE_nQuery = 5,
      RULE_wQuery = 6,
      RULE_primaryQuery = 7,
      RULE_prefixOperatorQuery = 8,
      RULE_fieldsQueryList = 9,
      RULE_simpleTerm = 10;

  private static String[] makeRuleNames() {
    return new String[] {
      "topSrndQuery",
      "fieldsQuery",
      "orQuery",
      "andQuery",
      "notQuery",
      "nQuery",
      "wQuery",
      "primaryQuery",
      "prefixOperatorQuery",
      "fieldsQueryList",
      "simpleTerm"
    };
  }

  public static final String[] ruleNames = makeRuleNames();

  private static String[] makeLiteralNames() {
    return new String[] {
      null, null, null, null, null, null, null, "'('", "')'", "','", "':'", "'^'"
    };
  }

  private static final String[] _LITERAL_NAMES = makeLiteralNames();

  private static String[] makeSymbolicNames() {
    return new String[] {
      null,
      "WS",
      "OR",
      "AND",
      "NOT",
      "W",
      "N",
      "LPAREN",
      "RPAREN",
      "COMMA",
      "COLON",
      "CARAT",
      "TRUNCQUOTED",
      "QUOTED",
      "SUFFIXTERM",
      "TRUNCTERM",
      "TERM",
      "NUMBER"
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
    return "SurroundSyntaxAntlrParser.g4";
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

  public SurroundSyntaxAntlrParser(TokenStream input) {
    super(input);
    _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
  }

  @SuppressWarnings("CheckReturnValue")
  public static class TopSrndQueryContext extends ParserRuleContext {
    public FieldsQueryContext fieldsQuery() {
      return getRuleContext(FieldsQueryContext.class, 0);
    }

    public TerminalNode EOF() {
      return getToken(SurroundSyntaxAntlrParser.EOF, 0);
    }

    public TopSrndQueryContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_topSrndQuery;
    }
  }

  public final TopSrndQueryContext topSrndQuery() throws RecognitionException {
    TopSrndQueryContext _localctx = new TopSrndQueryContext(_ctx, getState());
    enterRule(_localctx, 0, RULE_topSrndQuery);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(22);
        fieldsQuery();
        setState(23);
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
  public static class FieldsQueryContext extends ParserRuleContext {
    public Token TERM;
    public List<Token> fieldNames = new ArrayList<Token>();

    public OrQueryContext orQuery() {
      return getRuleContext(OrQueryContext.class, 0);
    }

    public List<TerminalNode> COLON() {
      return getTokens(SurroundSyntaxAntlrParser.COLON);
    }

    public TerminalNode COLON(int i) {
      return getToken(SurroundSyntaxAntlrParser.COLON, i);
    }

    public List<TerminalNode> TERM() {
      return getTokens(SurroundSyntaxAntlrParser.TERM);
    }

    public TerminalNode TERM(int i) {
      return getToken(SurroundSyntaxAntlrParser.TERM, i);
    }

    public FieldsQueryContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_fieldsQuery;
    }
  }

  public final FieldsQueryContext fieldsQuery() throws RecognitionException {
    FieldsQueryContext _localctx = new FieldsQueryContext(_ctx, getState());
    enterRule(_localctx, 2, RULE_fieldsQuery);
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(29);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 0, _ctx);
        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            {
              {
                setState(25);
                ((FieldsQueryContext) _localctx).TERM = match(TERM);
                ((FieldsQueryContext) _localctx)
                    .fieldNames.add(((FieldsQueryContext) _localctx).TERM);
                setState(26);
                match(COLON);
              }
            }
          }
          setState(31);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 0, _ctx);
        }
        setState(32);
        orQuery();
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
  public static class OrQueryContext extends ParserRuleContext {
    public Token OR;
    public List<Token> operators = new ArrayList<Token>();

    public List<AndQueryContext> andQuery() {
      return getRuleContexts(AndQueryContext.class);
    }

    public AndQueryContext andQuery(int i) {
      return getRuleContext(AndQueryContext.class, i);
    }

    public List<TerminalNode> OR() {
      return getTokens(SurroundSyntaxAntlrParser.OR);
    }

    public TerminalNode OR(int i) {
      return getToken(SurroundSyntaxAntlrParser.OR, i);
    }

    public OrQueryContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_orQuery;
    }
  }

  public final OrQueryContext orQuery() throws RecognitionException {
    OrQueryContext _localctx = new OrQueryContext(_ctx, getState());
    enterRule(_localctx, 4, RULE_orQuery);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(34);
        andQuery();
        setState(39);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == OR) {
          {
            {
              setState(35);
              ((OrQueryContext) _localctx).OR = match(OR);
              ((OrQueryContext) _localctx).operators.add(((OrQueryContext) _localctx).OR);
              setState(36);
              andQuery();
            }
          }
          setState(41);
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
  public static class AndQueryContext extends ParserRuleContext {
    public Token AND;
    public List<Token> operators = new ArrayList<Token>();

    public List<NotQueryContext> notQuery() {
      return getRuleContexts(NotQueryContext.class);
    }

    public NotQueryContext notQuery(int i) {
      return getRuleContext(NotQueryContext.class, i);
    }

    public List<TerminalNode> AND() {
      return getTokens(SurroundSyntaxAntlrParser.AND);
    }

    public TerminalNode AND(int i) {
      return getToken(SurroundSyntaxAntlrParser.AND, i);
    }

    public AndQueryContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_andQuery;
    }
  }

  public final AndQueryContext andQuery() throws RecognitionException {
    AndQueryContext _localctx = new AndQueryContext(_ctx, getState());
    enterRule(_localctx, 6, RULE_andQuery);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(42);
        notQuery();
        setState(47);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == AND) {
          {
            {
              setState(43);
              ((AndQueryContext) _localctx).AND = match(AND);
              ((AndQueryContext) _localctx).operators.add(((AndQueryContext) _localctx).AND);
              setState(44);
              notQuery();
            }
          }
          setState(49);
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
  public static class NotQueryContext extends ParserRuleContext {
    public Token NOT;
    public List<Token> operators = new ArrayList<Token>();

    public List<NQueryContext> nQuery() {
      return getRuleContexts(NQueryContext.class);
    }

    public NQueryContext nQuery(int i) {
      return getRuleContext(NQueryContext.class, i);
    }

    public List<TerminalNode> NOT() {
      return getTokens(SurroundSyntaxAntlrParser.NOT);
    }

    public TerminalNode NOT(int i) {
      return getToken(SurroundSyntaxAntlrParser.NOT, i);
    }

    public NotQueryContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_notQuery;
    }
  }

  public final NotQueryContext notQuery() throws RecognitionException {
    NotQueryContext _localctx = new NotQueryContext(_ctx, getState());
    enterRule(_localctx, 8, RULE_notQuery);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(50);
        nQuery();
        setState(55);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == NOT) {
          {
            {
              setState(51);
              ((NotQueryContext) _localctx).NOT = match(NOT);
              ((NotQueryContext) _localctx).operators.add(((NotQueryContext) _localctx).NOT);
              setState(52);
              nQuery();
            }
          }
          setState(57);
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
  public static class NQueryContext extends ParserRuleContext {
    public Token N;
    public List<Token> operators = new ArrayList<Token>();

    public List<WQueryContext> wQuery() {
      return getRuleContexts(WQueryContext.class);
    }

    public WQueryContext wQuery(int i) {
      return getRuleContext(WQueryContext.class, i);
    }

    public List<TerminalNode> N() {
      return getTokens(SurroundSyntaxAntlrParser.N);
    }

    public TerminalNode N(int i) {
      return getToken(SurroundSyntaxAntlrParser.N, i);
    }

    public NQueryContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_nQuery;
    }
  }

  public final NQueryContext nQuery() throws RecognitionException {
    NQueryContext _localctx = new NQueryContext(_ctx, getState());
    enterRule(_localctx, 10, RULE_nQuery);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(58);
        wQuery();
        setState(63);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == N) {
          {
            {
              setState(59);
              ((NQueryContext) _localctx).N = match(N);
              ((NQueryContext) _localctx).operators.add(((NQueryContext) _localctx).N);
              setState(60);
              wQuery();
            }
          }
          setState(65);
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
  public static class WQueryContext extends ParserRuleContext {
    public Token W;
    public List<Token> operators = new ArrayList<Token>();

    public List<PrimaryQueryContext> primaryQuery() {
      return getRuleContexts(PrimaryQueryContext.class);
    }

    public PrimaryQueryContext primaryQuery(int i) {
      return getRuleContext(PrimaryQueryContext.class, i);
    }

    public List<TerminalNode> W() {
      return getTokens(SurroundSyntaxAntlrParser.W);
    }

    public TerminalNode W(int i) {
      return getToken(SurroundSyntaxAntlrParser.W, i);
    }

    public WQueryContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_wQuery;
    }
  }

  public final WQueryContext wQuery() throws RecognitionException {
    WQueryContext _localctx = new WQueryContext(_ctx, getState());
    enterRule(_localctx, 12, RULE_wQuery);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(66);
        primaryQuery();
        setState(71);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == W) {
          {
            {
              setState(67);
              ((WQueryContext) _localctx).W = match(W);
              ((WQueryContext) _localctx).operators.add(((WQueryContext) _localctx).W);
              setState(68);
              primaryQuery();
            }
          }
          setState(73);
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
  public static class PrimaryQueryContext extends ParserRuleContext {
    public Token NUMBER;
    public List<Token> weights = new ArrayList<Token>();

    public TerminalNode LPAREN() {
      return getToken(SurroundSyntaxAntlrParser.LPAREN, 0);
    }

    public FieldsQueryContext fieldsQuery() {
      return getRuleContext(FieldsQueryContext.class, 0);
    }

    public TerminalNode RPAREN() {
      return getToken(SurroundSyntaxAntlrParser.RPAREN, 0);
    }

    public PrefixOperatorQueryContext prefixOperatorQuery() {
      return getRuleContext(PrefixOperatorQueryContext.class, 0);
    }

    public SimpleTermContext simpleTerm() {
      return getRuleContext(SimpleTermContext.class, 0);
    }

    public List<TerminalNode> CARAT() {
      return getTokens(SurroundSyntaxAntlrParser.CARAT);
    }

    public TerminalNode CARAT(int i) {
      return getToken(SurroundSyntaxAntlrParser.CARAT, i);
    }

    public List<TerminalNode> NUMBER() {
      return getTokens(SurroundSyntaxAntlrParser.NUMBER);
    }

    public TerminalNode NUMBER(int i) {
      return getToken(SurroundSyntaxAntlrParser.NUMBER, i);
    }

    public PrimaryQueryContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_primaryQuery;
    }
  }

  public final PrimaryQueryContext primaryQuery() throws RecognitionException {
    PrimaryQueryContext _localctx = new PrimaryQueryContext(_ctx, getState());
    enterRule(_localctx, 14, RULE_primaryQuery);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(80);
        _errHandler.sync(this);
        switch (_input.LA(1)) {
          case LPAREN:
            {
              setState(74);
              match(LPAREN);
              setState(75);
              fieldsQuery();
              setState(76);
              match(RPAREN);
            }
            break;
          case OR:
          case AND:
          case W:
          case N:
            {
              setState(78);
              prefixOperatorQuery();
            }
            break;
          case TRUNCQUOTED:
          case QUOTED:
          case SUFFIXTERM:
          case TRUNCTERM:
          case TERM:
            {
              setState(79);
              simpleTerm();
            }
            break;
          default:
            throw new NoViableAltException(this);
        }
        setState(86);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == CARAT) {
          {
            {
              setState(82);
              match(CARAT);
              setState(83);
              ((PrimaryQueryContext) _localctx).NUMBER = match(NUMBER);
              ((PrimaryQueryContext) _localctx)
                  .weights.add(((PrimaryQueryContext) _localctx).NUMBER);
            }
          }
          setState(88);
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
  public static class PrefixOperatorQueryContext extends ParserRuleContext {
    public Token operator;

    public FieldsQueryListContext fieldsQueryList() {
      return getRuleContext(FieldsQueryListContext.class, 0);
    }

    public TerminalNode OR() {
      return getToken(SurroundSyntaxAntlrParser.OR, 0);
    }

    public TerminalNode AND() {
      return getToken(SurroundSyntaxAntlrParser.AND, 0);
    }

    public TerminalNode N() {
      return getToken(SurroundSyntaxAntlrParser.N, 0);
    }

    public TerminalNode W() {
      return getToken(SurroundSyntaxAntlrParser.W, 0);
    }

    public PrefixOperatorQueryContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_prefixOperatorQuery;
    }
  }

  public final PrefixOperatorQueryContext prefixOperatorQuery() throws RecognitionException {
    PrefixOperatorQueryContext _localctx = new PrefixOperatorQueryContext(_ctx, getState());
    enterRule(_localctx, 16, RULE_prefixOperatorQuery);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(89);
        ((PrefixOperatorQueryContext) _localctx).operator = _input.LT(1);
        _la = _input.LA(1);
        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 108L) != 0))) {
          ((PrefixOperatorQueryContext) _localctx).operator =
              (Token) _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF) matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
        setState(90);
        fieldsQueryList();
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
  public static class FieldsQueryListContext extends ParserRuleContext {
    public TerminalNode LPAREN() {
      return getToken(SurroundSyntaxAntlrParser.LPAREN, 0);
    }

    public List<FieldsQueryContext> fieldsQuery() {
      return getRuleContexts(FieldsQueryContext.class);
    }

    public FieldsQueryContext fieldsQuery(int i) {
      return getRuleContext(FieldsQueryContext.class, i);
    }

    public TerminalNode RPAREN() {
      return getToken(SurroundSyntaxAntlrParser.RPAREN, 0);
    }

    public List<TerminalNode> COMMA() {
      return getTokens(SurroundSyntaxAntlrParser.COMMA);
    }

    public TerminalNode COMMA(int i) {
      return getToken(SurroundSyntaxAntlrParser.COMMA, i);
    }

    public FieldsQueryListContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_fieldsQueryList;
    }
  }

  public final FieldsQueryListContext fieldsQueryList() throws RecognitionException {
    FieldsQueryListContext _localctx = new FieldsQueryListContext(_ctx, getState());
    enterRule(_localctx, 18, RULE_fieldsQueryList);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(92);
        match(LPAREN);
        setState(93);
        fieldsQuery();
        setState(96);
        _errHandler.sync(this);
        _la = _input.LA(1);
        do {
          {
            {
              setState(94);
              match(COMMA);
              setState(95);
              fieldsQuery();
            }
          }
          setState(98);
          _errHandler.sync(this);
          _la = _input.LA(1);
        } while (_la == COMMA);
        setState(100);
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
  public static class SimpleTermContext extends ParserRuleContext {
    public Token term;

    public TerminalNode TERM() {
      return getToken(SurroundSyntaxAntlrParser.TERM, 0);
    }

    public TerminalNode QUOTED() {
      return getToken(SurroundSyntaxAntlrParser.QUOTED, 0);
    }

    public TerminalNode SUFFIXTERM() {
      return getToken(SurroundSyntaxAntlrParser.SUFFIXTERM, 0);
    }

    public TerminalNode TRUNCTERM() {
      return getToken(SurroundSyntaxAntlrParser.TRUNCTERM, 0);
    }

    public TerminalNode TRUNCQUOTED() {
      return getToken(SurroundSyntaxAntlrParser.TRUNCQUOTED, 0);
    }

    public SimpleTermContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_simpleTerm;
    }
  }

  public final SimpleTermContext simpleTerm() throws RecognitionException {
    SimpleTermContext _localctx = new SimpleTermContext(_ctx, getState());
    enterRule(_localctx, 20, RULE_simpleTerm);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(102);
        ((SimpleTermContext) _localctx).term = _input.LT(1);
        _la = _input.LA(1);
        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & 126976L) != 0))) {
          ((SimpleTermContext) _localctx).term = (Token) _errHandler.recoverInline(this);
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

  public static final String _serializedATN =
      "\u0004\u0001\u0011i\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"
          + "\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"
          + "\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"
          + "\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0001\u0000\u0001\u0000\u0001"
          + "\u0000\u0001\u0001\u0001\u0001\u0005\u0001\u001c\b\u0001\n\u0001\f\u0001"
          + "\u001f\t\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002"
          + "\u0005\u0002&\b\u0002\n\u0002\f\u0002)\t\u0002\u0001\u0003\u0001\u0003"
          + "\u0001\u0003\u0005\u0003.\b\u0003\n\u0003\f\u00031\t\u0003\u0001\u0004"
          + "\u0001\u0004\u0001\u0004\u0005\u00046\b\u0004\n\u0004\f\u00049\t\u0004"
          + "\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005>\b\u0005\n\u0005\f\u0005"
          + "A\t\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0005\u0006F\b\u0006\n\u0006"
          + "\f\u0006I\t\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"
          + "\u0007\u0001\u0007\u0003\u0007Q\b\u0007\u0001\u0007\u0001\u0007\u0005"
          + "\u0007U\b\u0007\n\u0007\f\u0007X\t\u0007\u0001\b\u0001\b\u0001\b\u0001"
          + "\t\u0001\t\u0001\t\u0001\t\u0004\ta\b\t\u000b\t\f\tb\u0001\t\u0001\t\u0001"
          + "\n\u0001\n\u0001\n\u0000\u0000\u000b\u0000\u0002\u0004\u0006\b\n\f\u000e"
          + "\u0010\u0012\u0014\u0000\u0002\u0002\u0000\u0002\u0003\u0005\u0006\u0001"
          + "\u0000\f\u0010g\u0000\u0016\u0001\u0000\u0000\u0000\u0002\u001d\u0001"
          + "\u0000\u0000\u0000\u0004\"\u0001\u0000\u0000\u0000\u0006*\u0001\u0000"
          + "\u0000\u0000\b2\u0001\u0000\u0000\u0000\n:\u0001\u0000\u0000\u0000\fB"
          + "\u0001\u0000\u0000\u0000\u000eP\u0001\u0000\u0000\u0000\u0010Y\u0001\u0000"
          + "\u0000\u0000\u0012\\\u0001\u0000\u0000\u0000\u0014f\u0001\u0000\u0000"
          + "\u0000\u0016\u0017\u0003\u0002\u0001\u0000\u0017\u0018\u0005\u0000\u0000"
          + "\u0001\u0018\u0001\u0001\u0000\u0000\u0000\u0019\u001a\u0005\u0010\u0000"
          + "\u0000\u001a\u001c\u0005\n\u0000\u0000\u001b\u0019\u0001\u0000\u0000\u0000"
          + "\u001c\u001f\u0001\u0000\u0000\u0000\u001d\u001b\u0001\u0000\u0000\u0000"
          + "\u001d\u001e\u0001\u0000\u0000\u0000\u001e \u0001\u0000\u0000\u0000\u001f"
          + "\u001d\u0001\u0000\u0000\u0000 !\u0003\u0004\u0002\u0000!\u0003\u0001"
          + "\u0000\u0000\u0000\"\'\u0003\u0006\u0003\u0000#$\u0005\u0002\u0000\u0000"
          + "$&\u0003\u0006\u0003\u0000%#\u0001\u0000\u0000\u0000&)\u0001\u0000\u0000"
          + "\u0000\'%\u0001\u0000\u0000\u0000\'(\u0001\u0000\u0000\u0000(\u0005\u0001"
          + "\u0000\u0000\u0000)\'\u0001\u0000\u0000\u0000*/\u0003\b\u0004\u0000+,"
          + "\u0005\u0003\u0000\u0000,.\u0003\b\u0004\u0000-+\u0001\u0000\u0000\u0000"
          + ".1\u0001\u0000\u0000\u0000/-\u0001\u0000\u0000\u0000/0\u0001\u0000\u0000"
          + "\u00000\u0007\u0001\u0000\u0000\u00001/\u0001\u0000\u0000\u000027\u0003"
          + "\n\u0005\u000034\u0005\u0004\u0000\u000046\u0003\n\u0005\u000053\u0001"
          + "\u0000\u0000\u000069\u0001\u0000\u0000\u000075\u0001\u0000\u0000\u0000"
          + "78\u0001\u0000\u0000\u00008\t\u0001\u0000\u0000\u000097\u0001\u0000\u0000"
          + "\u0000:?\u0003\f\u0006\u0000;<\u0005\u0006\u0000\u0000<>\u0003\f\u0006"
          + "\u0000=;\u0001\u0000\u0000\u0000>A\u0001\u0000\u0000\u0000?=\u0001\u0000"
          + "\u0000\u0000?@\u0001\u0000\u0000\u0000@\u000b\u0001\u0000\u0000\u0000"
          + "A?\u0001\u0000\u0000\u0000BG\u0003\u000e\u0007\u0000CD\u0005\u0005\u0000"
          + "\u0000DF\u0003\u000e\u0007\u0000EC\u0001\u0000\u0000\u0000FI\u0001\u0000"
          + "\u0000\u0000GE\u0001\u0000\u0000\u0000GH\u0001\u0000\u0000\u0000H\r\u0001"
          + "\u0000\u0000\u0000IG\u0001\u0000\u0000\u0000JK\u0005\u0007\u0000\u0000"
          + "KL\u0003\u0002\u0001\u0000LM\u0005\b\u0000\u0000MQ\u0001\u0000\u0000\u0000"
          + "NQ\u0003\u0010\b\u0000OQ\u0003\u0014\n\u0000PJ\u0001\u0000\u0000\u0000"
          + "PN\u0001\u0000\u0000\u0000PO\u0001\u0000\u0000\u0000QV\u0001\u0000\u0000"
          + "\u0000RS\u0005\u000b\u0000\u0000SU\u0005\u0011\u0000\u0000TR\u0001\u0000"
          + "\u0000\u0000UX\u0001\u0000\u0000\u0000VT\u0001\u0000\u0000\u0000VW\u0001"
          + "\u0000\u0000\u0000W\u000f\u0001\u0000\u0000\u0000XV\u0001\u0000\u0000"
          + "\u0000YZ\u0007\u0000\u0000\u0000Z[\u0003\u0012\t\u0000[\u0011\u0001\u0000"
          + "\u0000\u0000\\]\u0005\u0007\u0000\u0000]`\u0003\u0002\u0001\u0000^_\u0005"
          + "\t\u0000\u0000_a\u0003\u0002\u0001\u0000`^\u0001\u0000\u0000\u0000ab\u0001"
          + "\u0000\u0000\u0000b`\u0001\u0000\u0000\u0000bc\u0001\u0000\u0000\u0000"
          + "cd\u0001\u0000\u0000\u0000de\u0005\b\u0000\u0000e\u0013\u0001\u0000\u0000"
          + "\u0000fg\u0007\u0001\u0000\u0000g\u0015\u0001\u0000\u0000\u0000\t\u001d"
          + "\'/7?GPVb";
  public static final ATN _ATN = new ATNDeserializer().deserialize(_serializedATN.toCharArray());

  static {
    _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
    for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
      _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
    }
  }
}
