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
class SurroundSyntaxAntlrLexer extends Lexer {
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
  public static final int Boost = 1;
  public static String[] channelNames = {"DEFAULT_TOKEN_CHANNEL", "HIDDEN"};

  public static String[] modeNames = {"DEFAULT_MODE", "Boost"};

  private static String[] makeRuleNames() {
    return new String[] {
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
      "NUMBER",
      "NUM_CHAR",
      "TERM_CHAR",
      "WHITESPACE",
      "STAR",
      "ONE_CHAR",
      "DISTOP_NUM"
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

  public SurroundSyntaxAntlrLexer(CharStream input) {
    super(input);
    _interp = new LexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
  }

  @Override
  public String getGrammarFileName() {
    return "SurroundSyntaxAntlrLexer.g4";
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
      "\u0004\u0000\u0011\u00b3\u0006\uffff\uffff\u0006\uffff\uffff\u0002\u0000"
          + "\u0007\u0000\u0002\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003"
          + "\u0007\u0003\u0002\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006"
          + "\u0007\u0006\u0002\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002"
          + "\n\u0007\n\u0002\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002"
          + "\u000e\u0007\u000e\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002"
          + "\u0011\u0007\u0011\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002"
          + "\u0014\u0007\u0014\u0002\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0001"
          + "\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001"
          + "\u0001\u0001\u0001\u0003\u00019\b\u0001\u0001\u0002\u0001\u0002\u0001"
          + "\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002A\b\u0002\u0001"
          + "\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003"
          + "\u0003I\b\u0003\u0001\u0004\u0003\u0004L\b\u0004\u0001\u0004\u0001\u0004"
          + "\u0001\u0005\u0003\u0005Q\b\u0005\u0001\u0005\u0001\u0005\u0001\u0006"
          + "\u0001\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\t\u0001\t\u0001"
          + "\n\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0004\u000bc\b\u000b"
          + "\u000b\u000b\f\u000bd\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001"
          + "\f\u0001\f\u0001\f\u0004\fn\b\f\u000b\f\f\fo\u0001\f\u0001\f\u0001\r\u0004"
          + "\ru\b\r\u000b\r\f\rv\u0001\r\u0001\r\u0001\u000e\u0004\u000e|\b\u000e"
          + "\u000b\u000e\f\u000e}\u0001\u000e\u0001\u000e\u0004\u000e\u0082\b\u000e"
          + "\u000b\u000e\f\u000e\u0083\u0001\u000e\u0001\u000e\u0001\u000e\u0005\u000e"
          + "\u0089\b\u000e\n\u000e\f\u000e\u008c\t\u000e\u0001\u000f\u0004\u000f\u008f"
          + "\b\u000f\u000b\u000f\f\u000f\u0090\u0001\u0010\u0004\u0010\u0094\b\u0010"
          + "\u000b\u0010\f\u0010\u0095\u0001\u0010\u0001\u0010\u0004\u0010\u009a\b"
          + "\u0010\u000b\u0010\f\u0010\u009b\u0003\u0010\u009e\b\u0010\u0001\u0010"
          + "\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0013"
          + "\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0016"
          + "\u0001\u0016\u0003\u0016\u00ae\b\u0016\u0001\u0016\u0001\u0016\u0003\u0016"
          + "\u00b2\b\u0016\u0000\u0000\u0017\u0002\u0001\u0004\u0002\u0006\u0003\b"
          + "\u0004\n\u0005\f\u0006\u000e\u0007\u0010\b\u0012\t\u0014\n\u0016\u000b"
          + "\u0018\f\u001a\r\u001c\u000e\u001e\u000f \u0010\"\u0011$\u0000&\u0000"
          + "(\u0000*\u0000,\u0000.\u0000\u0002\u0000\u0001\b\u0002\u0000WWww\u0002"
          + "\u0000NNnn\u0001\u0000\"\"\u0002\u0000\"\"\\\\\u0001\u000009\t\u0000\t"
          + "\n\r\r  \"\"(*,,::??^^\u0003\u0000\t\n\r\r  \u0001\u000029\u00c0\u0000"
          + "\u0002\u0001\u0000\u0000\u0000\u0000\u0004\u0001\u0000\u0000\u0000\u0000"
          + "\u0006\u0001\u0000\u0000\u0000\u0000\b\u0001\u0000\u0000\u0000\u0000\n"
          + "\u0001\u0000\u0000\u0000\u0000\f\u0001\u0000\u0000\u0000\u0000\u000e\u0001"
          + "\u0000\u0000\u0000\u0000\u0010\u0001\u0000\u0000\u0000\u0000\u0012\u0001"
          + "\u0000\u0000\u0000\u0000\u0014\u0001\u0000\u0000\u0000\u0000\u0016\u0001"
          + "\u0000\u0000\u0000\u0000\u0018\u0001\u0000\u0000\u0000\u0000\u001a\u0001"
          + "\u0000\u0000\u0000\u0000\u001c\u0001\u0000\u0000\u0000\u0000\u001e\u0001"
          + "\u0000\u0000\u0000\u0000 \u0001\u0000\u0000\u0000\u0001\"\u0001\u0000"
          + "\u0000\u0000\u00020\u0001\u0000\u0000\u0000\u00048\u0001\u0000\u0000\u0000"
          + "\u0006@\u0001\u0000\u0000\u0000\bH\u0001\u0000\u0000\u0000\nK\u0001\u0000"
          + "\u0000\u0000\fP\u0001\u0000\u0000\u0000\u000eT\u0001\u0000\u0000\u0000"
          + "\u0010V\u0001\u0000\u0000\u0000\u0012X\u0001\u0000\u0000\u0000\u0014Z"
          + "\u0001\u0000\u0000\u0000\u0016\\\u0001\u0000\u0000\u0000\u0018`\u0001"
          + "\u0000\u0000\u0000\u001ai\u0001\u0000\u0000\u0000\u001ct\u0001\u0000\u0000"
          + "\u0000\u001e{\u0001\u0000\u0000\u0000 \u008e\u0001\u0000\u0000\u0000\""
          + "\u0093\u0001\u0000\u0000\u0000$\u00a1\u0001\u0000\u0000\u0000&\u00a3\u0001"
          + "\u0000\u0000\u0000(\u00a5\u0001\u0000\u0000\u0000*\u00a7\u0001\u0000\u0000"
          + "\u0000,\u00a9\u0001\u0000\u0000\u0000.\u00b1\u0001\u0000\u0000\u00000"
          + "1\u0003(\u0013\u000012\u0001\u0000\u0000\u000023\u0006\u0000\u0000\u0000"
          + "3\u0003\u0001\u0000\u0000\u000045\u0005O\u0000\u000059\u0005R\u0000\u0000"
          + "67\u0005o\u0000\u000079\u0005r\u0000\u000084\u0001\u0000\u0000\u00008"
          + "6\u0001\u0000\u0000\u00009\u0005\u0001\u0000\u0000\u0000:;\u0005A\u0000"
          + "\u0000;<\u0005N\u0000\u0000<A\u0005D\u0000\u0000=>\u0005a\u0000\u0000"
          + ">?\u0005n\u0000\u0000?A\u0005d\u0000\u0000@:\u0001\u0000\u0000\u0000@"
          + "=\u0001\u0000\u0000\u0000A\u0007\u0001\u0000\u0000\u0000BC\u0005N\u0000"
          + "\u0000CD\u0005O\u0000\u0000DI\u0005T\u0000\u0000EF\u0005n\u0000\u0000"
          + "FG\u0005o\u0000\u0000GI\u0005t\u0000\u0000HB\u0001\u0000\u0000\u0000H"
          + "E\u0001\u0000\u0000\u0000I\t\u0001\u0000\u0000\u0000JL\u0003.\u0016\u0000"
          + "KJ\u0001\u0000\u0000\u0000KL\u0001\u0000\u0000\u0000LM\u0001\u0000\u0000"
          + "\u0000MN\u0007\u0000\u0000\u0000N\u000b\u0001\u0000\u0000\u0000OQ\u0003"
          + ".\u0016\u0000PO\u0001\u0000\u0000\u0000PQ\u0001\u0000\u0000\u0000QR\u0001"
          + "\u0000\u0000\u0000RS\u0007\u0001\u0000\u0000S\r\u0001\u0000\u0000\u0000"
          + "TU\u0005(\u0000\u0000U\u000f\u0001\u0000\u0000\u0000VW\u0005)\u0000\u0000"
          + "W\u0011\u0001\u0000\u0000\u0000XY\u0005,\u0000\u0000Y\u0013\u0001\u0000"
          + "\u0000\u0000Z[\u0005:\u0000\u0000[\u0015\u0001\u0000\u0000\u0000\\]\u0005"
          + "^\u0000\u0000]^\u0001\u0000\u0000\u0000^_\u0006\n\u0001\u0000_\u0017\u0001"
          + "\u0000\u0000\u0000`b\u0005\"\u0000\u0000ac\b\u0002\u0000\u0000ba\u0001"
          + "\u0000\u0000\u0000cd\u0001\u0000\u0000\u0000db\u0001\u0000\u0000\u0000"
          + "de\u0001\u0000\u0000\u0000ef\u0001\u0000\u0000\u0000fg\u0005\"\u0000\u0000"
          + "gh\u0003*\u0014\u0000h\u0019\u0001\u0000\u0000\u0000im\u0005\"\u0000\u0000"
          + "jn\b\u0003\u0000\u0000kl\u0005\\\u0000\u0000ln\u0007\u0003\u0000\u0000"
          + "mj\u0001\u0000\u0000\u0000mk\u0001\u0000\u0000\u0000no\u0001\u0000\u0000"
          + "\u0000om\u0001\u0000\u0000\u0000op\u0001\u0000\u0000\u0000pq\u0001\u0000"
          + "\u0000\u0000qr\u0005\"\u0000\u0000r\u001b\u0001\u0000\u0000\u0000su\u0003"
          + "&\u0012\u0000ts\u0001\u0000\u0000\u0000uv\u0001\u0000\u0000\u0000vt\u0001"
          + "\u0000\u0000\u0000vw\u0001\u0000\u0000\u0000wx\u0001\u0000\u0000\u0000"
          + "xy\u0003*\u0014\u0000y\u001d\u0001\u0000\u0000\u0000z|\u0003&\u0012\u0000"
          + "{z\u0001\u0000\u0000\u0000|}\u0001\u0000\u0000\u0000}{\u0001\u0000\u0000"
          + "\u0000}~\u0001\u0000\u0000\u0000~\u0081\u0001\u0000\u0000\u0000\u007f"
          + "\u0082\u0003*\u0014\u0000\u0080\u0082\u0003,\u0015\u0000\u0081\u007f\u0001"
          + "\u0000\u0000\u0000\u0081\u0080\u0001\u0000\u0000\u0000\u0082\u0083\u0001"
          + "\u0000\u0000\u0000\u0083\u0081\u0001\u0000\u0000\u0000\u0083\u0084\u0001"
          + "\u0000\u0000\u0000\u0084\u008a\u0001\u0000\u0000\u0000\u0085\u0089\u0003"
          + "&\u0012\u0000\u0086\u0089\u0003*\u0014\u0000\u0087\u0089\u0003,\u0015"
          + "\u0000\u0088\u0085\u0001\u0000\u0000\u0000\u0088\u0086\u0001\u0000\u0000"
          + "\u0000\u0088\u0087\u0001\u0000\u0000\u0000\u0089\u008c\u0001\u0000\u0000"
          + "\u0000\u008a\u0088\u0001\u0000\u0000\u0000\u008a\u008b\u0001\u0000\u0000"
          + "\u0000\u008b\u001f\u0001\u0000\u0000\u0000\u008c\u008a\u0001\u0000\u0000"
          + "\u0000\u008d\u008f\u0003&\u0012\u0000\u008e\u008d\u0001\u0000\u0000\u0000"
          + "\u008f\u0090\u0001\u0000\u0000\u0000\u0090\u008e\u0001\u0000\u0000\u0000"
          + "\u0090\u0091\u0001\u0000\u0000\u0000\u0091!\u0001\u0000\u0000\u0000\u0092"
          + "\u0094\u0003$\u0011\u0000\u0093\u0092\u0001\u0000\u0000\u0000\u0094\u0095"
          + "\u0001\u0000\u0000\u0000\u0095\u0093\u0001\u0000\u0000\u0000\u0095\u0096"
          + "\u0001\u0000\u0000\u0000\u0096\u009d\u0001\u0000\u0000\u0000\u0097\u0099"
          + "\u0005.\u0000\u0000\u0098\u009a\u0003$\u0011\u0000\u0099\u0098\u0001\u0000"
          + "\u0000\u0000\u009a\u009b\u0001\u0000\u0000\u0000\u009b\u0099\u0001\u0000"
          + "\u0000\u0000\u009b\u009c\u0001\u0000\u0000\u0000\u009c\u009e\u0001\u0000"
          + "\u0000\u0000\u009d\u0097\u0001\u0000\u0000\u0000\u009d\u009e\u0001\u0000"
          + "\u0000\u0000\u009e\u009f\u0001\u0000\u0000\u0000\u009f\u00a0\u0006\u0010"
          + "\u0002\u0000\u00a0#\u0001\u0000\u0000\u0000\u00a1\u00a2\u0007\u0004\u0000"
          + "\u0000\u00a2%\u0001\u0000\u0000\u0000\u00a3\u00a4\b\u0005\u0000\u0000"
          + "\u00a4\'\u0001\u0000\u0000\u0000\u00a5\u00a6\u0007\u0006\u0000\u0000\u00a6"
          + ")\u0001\u0000\u0000\u0000\u00a7\u00a8\u0005*\u0000\u0000\u00a8+\u0001"
          + "\u0000\u0000\u0000\u00a9\u00aa\u0005?\u0000\u0000\u00aa-\u0001\u0000\u0000"
          + "\u0000\u00ab\u00ad\u0007\u0007\u0000\u0000\u00ac\u00ae\u0007\u0004\u0000"
          + "\u0000\u00ad\u00ac\u0001\u0000\u0000\u0000\u00ad\u00ae\u0001\u0000\u0000"
          + "\u0000\u00ae\u00b2\u0001\u0000\u0000\u0000\u00af\u00b0\u00051\u0000\u0000"
          + "\u00b0\u00b2\u0007\u0004\u0000\u0000\u00b1\u00ab\u0001\u0000\u0000\u0000"
          + "\u00b1\u00af\u0001\u0000\u0000\u0000\u00b2/\u0001\u0000\u0000\u0000\u0016"
          + "\u0000\u00018@HKPdmov}\u0081\u0083\u0088\u008a\u0090\u0095\u009b\u009d"
          + "\u00ad\u00b1\u0003\u0006\u0000\u0000\u0002\u0001\u0000\u0002\u0000\u0000";
  public static final ATN _ATN = new ATNDeserializer().deserialize(_serializedATN.toCharArray());

  static {
    _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
    for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
      _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
    }
  }
}
