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

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({
  "all",
  "warnings",
  "unchecked",
  "unused",
  "cast",
  "CheckReturnValue",
  "this-escape"
})
class ClassicSyntaxAntlrLexer extends Lexer {
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
  public static final int Boost = 1, Range = 2;
  public static String[] channelNames = {"DEFAULT_TOKEN_CHANNEL", "HIDDEN"};

  public static String[] modeNames = {"DEFAULT_MODE", "Boost", "Range"};

  private static String[] makeRuleNames() {
    return new String[] {
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
      "RANGE_GOOP",
      "NUM_CHAR",
      "ESCAPED_CHAR",
      "TERM_START_CHAR",
      "TERM_CHAR",
      "WHITESPACE",
      "QUOTED_CHAR"
    };
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

  public ClassicSyntaxAntlrLexer(CharStream input) {
    super(input);
    _interp = new LexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
  }

  @Override
  public String getGrammarFileName() {
    return "ClassicSyntaxAntlrLexer.g4";
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
  public String[] getChannelNames() {
    return channelNames;
  }

  @Override
  public String[] getModeNames() {
    return modeNames;
  }

  @Override
  public ATN getATN() {
    return _ATN;
  }

  public static final String _serializedATN =
      "\u0004\u0000\u001b\u0102\u0006\uffff\uffff\u0006\uffff\uffff\u0006\uffff"
          + "\uffff\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002\u0002\u0007"
          + "\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002\u0005\u0007"
          + "\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002\b\u0007\b"
          + "\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002\f\u0007"
          + "\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f\u0002"
          + "\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012\u0002"
          + "\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015\u0002"
          + "\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018\u0002"
          + "\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b\u0002"
          + "\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e\u0002"
          + "\u001f\u0007\u001f\u0002 \u0007 \u0001\u0000\u0001\u0000\u0001\u0000\u0001"
          + "\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003"
          + "\u0001O\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003"
          + "\u0002U\b\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003"
          + "\u0003[\b\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001"
          + "\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b"
          + "\u0001\t\u0001\t\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001"
          + "\u000b\u0001\f\u0001\f\u0005\fr\b\f\n\f\f\fu\t\f\u0001\f\u0001\f\u0001"
          + "\r\u0001\r\u0005\r{\b\r\n\r\f\r~\t\r\u0001\u000e\u0001\u000e\u0004\u000e"
          + "\u0082\b\u000e\u000b\u000e\f\u000e\u0083\u0001\u000e\u0001\u000e\u0004"
          + "\u000e\u0088\b\u000e\u000b\u000e\f\u000e\u0089\u0003\u000e\u008c\b\u000e"
          + "\u0001\u000e\u0005\u000e\u008f\b\u000e\n\u000e\f\u000e\u0092\t\u000e\u0001"
          + "\u000e\u0005\u000e\u0095\b\u000e\n\u000e\f\u000e\u0098\t\u000e\u0003\u000e"
          + "\u009a\b\u000e\u0001\u000f\u0001\u000f\u0005\u000f\u009e\b\u000f\n\u000f"
          + "\f\u000f\u00a1\t\u000f\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010"
          + "\u0003\u0010\u00a7\b\u0010\u0001\u0010\u0001\u0010\u0005\u0010\u00ab\b"
          + "\u0010\n\u0010\f\u0010\u00ae\t\u0010\u0001\u0011\u0001\u0011\u0001\u0011"
          + "\u0001\u0011\u0005\u0011\u00b4\b\u0011\n\u0011\f\u0011\u00b7\t\u0011\u0001"
          + "\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001"
          + "\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0014\u0004\u0014\u00c4"
          + "\b\u0014\u000b\u0014\f\u0014\u00c5\u0001\u0014\u0001\u0014\u0004\u0014"
          + "\u00ca\b\u0014\u000b\u0014\f\u0014\u00cb\u0003\u0014\u00ce\b\u0014\u0001"
          + "\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"
          + "\u0016\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001"
          + "\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0019\u0001"
          + "\u0019\u0001\u0019\u0001\u0019\u0004\u0019\u00e5\b\u0019\u000b\u0019\f"
          + "\u0019\u00e6\u0001\u0019\u0001\u0019\u0001\u001a\u0004\u001a\u00ec\b\u001a"
          + "\u000b\u001a\f\u001a\u00ed\u0001\u001b\u0001\u001b\u0001\u001c\u0001\u001c"
          + "\u0001\u001c\u0001\u001d\u0001\u001d\u0003\u001d\u00f7\b\u001d\u0001\u001e"
          + "\u0001\u001e\u0003\u001e\u00fb\b\u001e\u0001\u001f\u0001\u001f\u0001 "
          + "\u0001 \u0003 \u0101\b \u0000\u0000!\u0003\u0001\u0005\u0002\u0007\u0003"
          + "\t\u0004\u000b\u0005\r\u0006\u000f\u0007\u0011\b\u0013\t\u0015\n\u0017"
          + "\u000b\u0019\f\u001b\r\u001d\u000e\u001f\u000f!\u0010#\u0011%\u0012\'"
          + "\u0013)\u0014+\u0015-\u0016/\u00171\u00183\u00195\u001a7\u001b9\u0000"
          + ";\u0000=\u0000?\u0000A\u0000C\u0000\u0003\u0000\u0001\u0002\n\u0003\u0000"
          + "!!++--\u0002\u0000**??\u0001\u0000//\u0001\u0000\"\"\u0003\u0000  ]]}"
          + "}\u0001\u000009\f\u0000\t\n\r\r \"(+--//::??[^{{}~\u3000\u3000\u0002\u0000"
          + "++--\u0004\u0000\t\n\r\r  \u3000\u3000\u0002\u0000\"\"\\\\\u0113\u0000"
          + "\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000"
          + "\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b"
          + "\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001"
          + "\u0000\u0000\u0000\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001"
          + "\u0000\u0000\u0000\u0000\u0015\u0001\u0000\u0000\u0000\u0000\u0017\u0001"
          + "\u0000\u0000\u0000\u0000\u0019\u0001\u0000\u0000\u0000\u0000\u001b\u0001"
          + "\u0000\u0000\u0000\u0000\u001d\u0001\u0000\u0000\u0000\u0000\u001f\u0001"
          + "\u0000\u0000\u0000\u0000!\u0001\u0000\u0000\u0000\u0000#\u0001\u0000\u0000"
          + "\u0000\u0000%\u0001\u0000\u0000\u0000\u0000\'\u0001\u0000\u0000\u0000"
          + "\u0000)\u0001\u0000\u0000\u0000\u0001+\u0001\u0000\u0000\u0000\u0002-"
          + "\u0001\u0000\u0000\u0000\u0002/\u0001\u0000\u0000\u0000\u00021\u0001\u0000"
          + "\u0000\u0000\u00023\u0001\u0000\u0000\u0000\u00025\u0001\u0000\u0000\u0000"
          + "\u00027\u0001\u0000\u0000\u0000\u0003E\u0001\u0000\u0000\u0000\u0005N"
          + "\u0001\u0000\u0000\u0000\u0007T\u0001\u0000\u0000\u0000\tZ\u0001\u0000"
          + "\u0000\u0000\u000b\\\u0001\u0000\u0000\u0000\r^\u0001\u0000\u0000\u0000"
          + "\u000f`\u0001\u0000\u0000\u0000\u0011c\u0001\u0000\u0000\u0000\u0013e"
          + "\u0001\u0000\u0000\u0000\u0015g\u0001\u0000\u0000\u0000\u0017i\u0001\u0000"
          + "\u0000\u0000\u0019k\u0001\u0000\u0000\u0000\u001bo\u0001\u0000\u0000\u0000"
          + "\u001dx\u0001\u0000\u0000\u0000\u001f\u007f\u0001\u0000\u0000\u0000!\u009b"
          + "\u0001\u0000\u0000\u0000#\u00a6\u0001\u0000\u0000\u0000%\u00af\u0001\u0000"
          + "\u0000\u0000\'\u00ba\u0001\u0000\u0000\u0000)\u00be\u0001\u0000\u0000"
          + "\u0000+\u00c3\u0001\u0000\u0000\u0000-\u00d1\u0001\u0000\u0000\u0000/"
          + "\u00d5\u0001\u0000\u0000\u00001\u00d8\u0001\u0000\u0000\u00003\u00dc\u0001"
          + "\u0000\u0000\u00005\u00e0\u0001\u0000\u0000\u00007\u00eb\u0001\u0000\u0000"
          + "\u00009\u00ef\u0001\u0000\u0000\u0000;\u00f1\u0001\u0000\u0000\u0000="
          + "\u00f6\u0001\u0000\u0000\u0000?\u00fa\u0001\u0000\u0000\u0000A\u00fc\u0001"
          + "\u0000\u0000\u0000C\u0100\u0001\u0000\u0000\u0000EF\u0003A\u001f\u0000"
          + "FG\u0001\u0000\u0000\u0000GH\u0006\u0000\u0000\u0000H\u0004\u0001\u0000"
          + "\u0000\u0000IJ\u0005A\u0000\u0000JK\u0005N\u0000\u0000KO\u0005D\u0000"
          + "\u0000LM\u0005&\u0000\u0000MO\u0005&\u0000\u0000NI\u0001\u0000\u0000\u0000"
          + "NL\u0001\u0000\u0000\u0000O\u0006\u0001\u0000\u0000\u0000PQ\u0005O\u0000"
          + "\u0000QU\u0005R\u0000\u0000RS\u0005|\u0000\u0000SU\u0005|\u0000\u0000"
          + "TP\u0001\u0000\u0000\u0000TR\u0001\u0000\u0000\u0000U\b\u0001\u0000\u0000"
          + "\u0000VW\u0005N\u0000\u0000WX\u0005O\u0000\u0000X[\u0005T\u0000\u0000"
          + "Y[\u0005!\u0000\u0000ZV\u0001\u0000\u0000\u0000ZY\u0001\u0000\u0000\u0000"
          + "[\n\u0001\u0000\u0000\u0000\\]\u0005+\u0000\u0000]\f\u0001\u0000\u0000"
          + "\u0000^_\u0005-\u0000\u0000_\u000e\u0001\u0000\u0000\u0000`a\u0007\u0000"
          + "\u0000\u0000ab\u0003A\u001f\u0000b\u0010\u0001\u0000\u0000\u0000cd\u0005"
          + "(\u0000\u0000d\u0012\u0001\u0000\u0000\u0000ef\u0005)\u0000\u0000f\u0014"
          + "\u0001\u0000\u0000\u0000gh\u0005:\u0000\u0000h\u0016\u0001\u0000\u0000"
          + "\u0000ij\u0005*\u0000\u0000j\u0018\u0001\u0000\u0000\u0000kl\u0005^\u0000"
          + "\u0000lm\u0001\u0000\u0000\u0000mn\u0006\u000b\u0001\u0000n\u001a\u0001"
          + "\u0000\u0000\u0000os\u0005\"\u0000\u0000pr\u0003C \u0000qp\u0001\u0000"
          + "\u0000\u0000ru\u0001\u0000\u0000\u0000sq\u0001\u0000\u0000\u0000st\u0001"
          + "\u0000\u0000\u0000tv\u0001\u0000\u0000\u0000us\u0001\u0000\u0000\u0000"
          + "vw\u0005\"\u0000\u0000w\u001c\u0001\u0000\u0000\u0000x|\u0003=\u001d\u0000"
          + "y{\u0003?\u001e\u0000zy\u0001\u0000\u0000\u0000{~\u0001\u0000\u0000\u0000"
          + "|z\u0001\u0000\u0000\u0000|}\u0001\u0000\u0000\u0000}\u001e\u0001\u0000"
          + "\u0000\u0000~|\u0001\u0000\u0000\u0000\u007f\u0099\u0005~\u0000\u0000"
          + "\u0080\u0082\u00039\u001b\u0000\u0081\u0080\u0001\u0000\u0000\u0000\u0082"
          + "\u0083\u0001\u0000\u0000\u0000\u0083\u0081\u0001\u0000\u0000\u0000\u0083"
          + "\u0084\u0001\u0000\u0000\u0000\u0084\u008b\u0001\u0000\u0000\u0000\u0085"
          + "\u0087\u0005.\u0000\u0000\u0086\u0088\u00039\u001b\u0000\u0087\u0086\u0001"
          + "\u0000\u0000\u0000\u0088\u0089\u0001\u0000\u0000\u0000\u0089\u0087\u0001"
          + "\u0000\u0000\u0000\u0089\u008a\u0001\u0000\u0000\u0000\u008a\u008c\u0001"
          + "\u0000\u0000\u0000\u008b\u0085\u0001\u0000\u0000\u0000\u008b\u008c\u0001"
          + "\u0000\u0000\u0000\u008c\u0090\u0001\u0000\u0000\u0000\u008d\u008f\u0003"
          + "?\u001e\u0000\u008e\u008d\u0001\u0000\u0000\u0000\u008f\u0092\u0001\u0000"
          + "\u0000\u0000\u0090\u008e\u0001\u0000\u0000\u0000\u0090\u0091\u0001\u0000"
          + "\u0000\u0000\u0091\u009a\u0001\u0000\u0000\u0000\u0092\u0090\u0001\u0000"
          + "\u0000\u0000\u0093\u0095\u0003?\u001e\u0000\u0094\u0093\u0001\u0000\u0000"
          + "\u0000\u0095\u0098\u0001\u0000\u0000\u0000\u0096\u0094\u0001\u0000\u0000"
          + "\u0000\u0096\u0097\u0001\u0000\u0000\u0000\u0097\u009a\u0001\u0000\u0000"
          + "\u0000\u0098\u0096\u0001\u0000\u0000\u0000\u0099\u0081\u0001\u0000\u0000"
          + "\u0000\u0099\u0096\u0001\u0000\u0000\u0000\u009a \u0001\u0000\u0000\u0000"
          + "\u009b\u009f\u0003=\u001d\u0000\u009c\u009e\u0003?\u001e\u0000\u009d\u009c"
          + "\u0001\u0000\u0000\u0000\u009e\u00a1\u0001\u0000\u0000\u0000\u009f\u009d"
          + "\u0001\u0000\u0000\u0000\u009f\u00a0\u0001\u0000\u0000\u0000\u00a0\u00a2"
          + "\u0001\u0000\u0000\u0000\u00a1\u009f\u0001\u0000\u0000\u0000\u00a2\u00a3"
          + "\u0005*\u0000\u0000\u00a3\"\u0001\u0000\u0000\u0000\u00a4\u00a7\u0003"
          + "=\u001d\u0000\u00a5\u00a7\u0007\u0001\u0000\u0000\u00a6\u00a4\u0001\u0000"
          + "\u0000\u0000\u00a6\u00a5\u0001\u0000\u0000\u0000\u00a7\u00ac\u0001\u0000"
          + "\u0000\u0000\u00a8\u00ab\u0003?\u001e\u0000\u00a9\u00ab\u0007\u0001\u0000"
          + "\u0000\u00aa\u00a8\u0001\u0000\u0000\u0000\u00aa\u00a9\u0001\u0000\u0000"
          + "\u0000\u00ab\u00ae\u0001\u0000\u0000\u0000\u00ac\u00aa\u0001\u0000\u0000"
          + "\u0000\u00ac\u00ad\u0001\u0000\u0000\u0000\u00ad$\u0001\u0000\u0000\u0000"
          + "\u00ae\u00ac\u0001\u0000\u0000\u0000\u00af\u00b5\u0005/\u0000\u0000\u00b0"
          + "\u00b4\b\u0002\u0000\u0000\u00b1\u00b2\u0005\\\u0000\u0000\u00b2\u00b4"
          + "\u0005/\u0000\u0000\u00b3\u00b0\u0001\u0000\u0000\u0000\u00b3\u00b1\u0001"
          + "\u0000\u0000\u0000\u00b4\u00b7\u0001\u0000\u0000\u0000\u00b5\u00b3\u0001"
          + "\u0000\u0000\u0000\u00b5\u00b6\u0001\u0000\u0000\u0000\u00b6\u00b8\u0001"
          + "\u0000\u0000\u0000\u00b7\u00b5\u0001\u0000\u0000\u0000\u00b8\u00b9\u0005"
          + "/\u0000\u0000\u00b9&\u0001\u0000\u0000\u0000\u00ba\u00bb\u0005[\u0000"
          + "\u0000\u00bb\u00bc\u0001\u0000\u0000\u0000\u00bc\u00bd\u0006\u0012\u0002"
          + "\u0000\u00bd(\u0001\u0000\u0000\u0000\u00be\u00bf\u0005{\u0000\u0000\u00bf"
          + "\u00c0\u0001\u0000\u0000\u0000\u00c0\u00c1\u0006\u0013\u0002\u0000\u00c1"
          + "*\u0001\u0000\u0000\u0000\u00c2\u00c4\u00039\u001b\u0000\u00c3\u00c2\u0001"
          + "\u0000\u0000\u0000\u00c4\u00c5\u0001\u0000\u0000\u0000\u00c5\u00c3\u0001"
          + "\u0000\u0000\u0000\u00c5\u00c6\u0001\u0000\u0000\u0000\u00c6\u00cd\u0001"
          + "\u0000\u0000\u0000\u00c7\u00c9\u0005.\u0000\u0000\u00c8\u00ca\u00039\u001b"
          + "\u0000\u00c9\u00c8\u0001\u0000\u0000\u0000\u00ca\u00cb\u0001\u0000\u0000"
          + "\u0000\u00cb\u00c9\u0001\u0000\u0000\u0000\u00cb\u00cc\u0001\u0000\u0000"
          + "\u0000\u00cc\u00ce\u0001\u0000\u0000\u0000\u00cd\u00c7\u0001\u0000\u0000"
          + "\u0000\u00cd\u00ce\u0001\u0000\u0000\u0000\u00ce\u00cf\u0001\u0000\u0000"
          + "\u0000\u00cf\u00d0\u0006\u0014\u0003\u0000\u00d0,\u0001\u0000\u0000\u0000"
          + "\u00d1\u00d2\u0003A\u001f\u0000\u00d2\u00d3\u0001\u0000\u0000\u0000\u00d3"
          + "\u00d4\u0006\u0015\u0000\u0000\u00d4.\u0001\u0000\u0000\u0000\u00d5\u00d6"
          + "\u0005T\u0000\u0000\u00d6\u00d7\u0005O\u0000\u0000\u00d70\u0001\u0000"
          + "\u0000\u0000\u00d8\u00d9\u0005]\u0000\u0000\u00d9\u00da\u0001\u0000\u0000"
          + "\u0000\u00da\u00db\u0006\u0017\u0003\u0000\u00db2\u0001\u0000\u0000\u0000"
          + "\u00dc\u00dd\u0005}\u0000\u0000\u00dd\u00de\u0001\u0000\u0000\u0000\u00de"
          + "\u00df\u0006\u0018\u0003\u0000\u00df4\u0001\u0000\u0000\u0000\u00e0\u00e4"
          + "\u0005\"\u0000\u0000\u00e1\u00e5\b\u0003\u0000\u0000\u00e2\u00e3\u0005"
          + "\\\u0000\u0000\u00e3\u00e5\u0005\"\u0000\u0000\u00e4\u00e1\u0001\u0000"
          + "\u0000\u0000\u00e4\u00e2\u0001\u0000\u0000\u0000\u00e5\u00e6\u0001\u0000"
          + "\u0000\u0000\u00e6\u00e4\u0001\u0000\u0000\u0000\u00e6\u00e7\u0001\u0000"
          + "\u0000\u0000\u00e7\u00e8\u0001\u0000\u0000\u0000\u00e8\u00e9\u0005\"\u0000"
          + "\u0000\u00e96\u0001\u0000\u0000\u0000\u00ea\u00ec\b\u0004\u0000\u0000"
          + "\u00eb\u00ea\u0001\u0000\u0000\u0000\u00ec\u00ed\u0001\u0000\u0000\u0000"
          + "\u00ed\u00eb\u0001\u0000\u0000\u0000\u00ed\u00ee\u0001\u0000\u0000\u0000"
          + "\u00ee8\u0001\u0000\u0000\u0000\u00ef\u00f0\u0007\u0005\u0000\u0000\u00f0"
          + ":\u0001\u0000\u0000\u0000\u00f1\u00f2\u0005\\\u0000\u0000\u00f2\u00f3"
          + "\t\u0000\u0000\u0000\u00f3<\u0001\u0000\u0000\u0000\u00f4\u00f7\b\u0006"
          + "\u0000\u0000\u00f5\u00f7\u0003;\u001c\u0000\u00f6\u00f4\u0001\u0000\u0000"
          + "\u0000\u00f6\u00f5\u0001\u0000\u0000\u0000\u00f7>\u0001\u0000\u0000\u0000"
          + "\u00f8\u00fb\u0003=\u001d\u0000\u00f9\u00fb\u0007\u0007\u0000\u0000\u00fa"
          + "\u00f8\u0001\u0000\u0000\u0000\u00fa\u00f9\u0001\u0000\u0000\u0000\u00fb"
          + "@\u0001\u0000\u0000\u0000\u00fc\u00fd\u0007\b\u0000\u0000\u00fdB\u0001"
          + "\u0000\u0000\u0000\u00fe\u0101\b\t\u0000\u0000\u00ff\u0101\u0003;\u001c"
          + "\u0000\u0100\u00fe\u0001\u0000\u0000\u0000\u0100\u00ff\u0001\u0000\u0000"
          + "\u0000\u0101D\u0001\u0000\u0000\u0000\u001d\u0000\u0001\u0002NTZs|\u0083"
          + "\u0089\u008b\u0090\u0096\u0099\u009f\u00a6\u00aa\u00ac\u00b3\u00b5\u00c5"
          + "\u00cb\u00cd\u00e4\u00e6\u00ed\u00f6\u00fa\u0100\u0004\u0006\u0000\u0000"
          + "\u0002\u0001\u0000\u0002\u0002\u0000\u0002\u0000\u0000";
  public static final ATN _ATN = new ATNDeserializer().deserialize(_serializedATN.toCharArray());

  static {
    _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
    for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
      _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
    }
  }
}
