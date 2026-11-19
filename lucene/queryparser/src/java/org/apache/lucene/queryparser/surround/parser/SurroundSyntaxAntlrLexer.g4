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

/*
 * ANTLRv4 lexer grammar for the surround query language.
 *
 * Query language operators: OR, AND, NOT, W, N, (, ), ^, *, ?, " and comma.
 *
 * Rule order matters: ANTLR picks the longest match and, on ties, the rule declared first
 * (within the current mode).
 */

lexer grammar SurroundSyntaxAntlrLexer;

// Default mode.

WS           : WHITESPACE -> skip ;

// Operator tokens (in increasing order of precedence).
OR           : 'OR' | 'or' ;
AND          : 'AND' | 'and' ;
NOT          : 'NOT' | 'not' ;
W            : DISTOP_NUM? [Ww] ;
N            : DISTOP_NUM? [Nn] ;

// These are excluded in TERM_CHAR.
LPAREN       : '(' ;
RPAREN       : ')' ;
COMMA        : ',' ;
COLON        : ':' ;
CARAT        : '^' -> mode(Boost) ;

// Literal non empty term between quotes, escape quoted quote or backslash by backslash.
// Evt. truncated.
TRUNCQUOTED  : '"' ~'"'+ '"' STAR ;
QUOTED       : '"' ( ~["\\] | '\\' [\\"] )+ '"' ;
SUFFIXTERM   : TERM_CHAR+ STAR ;
// At least one * or ?.
TRUNCTERM    : TERM_CHAR+ ( STAR | ONE_CHAR )+ ( TERM_CHAR | STAR | ONE_CHAR )* ;
TERM         : TERM_CHAR+ ;

// Entered after '^'. A number is the only thing recognized here (not even whitespace).

mode Boost;

NUMBER       : NUM_CHAR+ ( '.' NUM_CHAR+ )? -> mode(DEFAULT_MODE) ;

// Fragments, shared by all modes.

fragment NUM_CHAR
    : [0-9]
    ;

// Everything except whitespace and operators.
fragment TERM_CHAR
    : ~[ \t\n\r,?*():^"]
    ;

fragment WHITESPACE
    : [ \t\n\r]
    ;

// Term truncation.
fragment STAR
    : '*'
    ;

// Precisely one character in a term.
fragment ONE_CHAR
    : '?'
    ;

// 2..99 prefix for distance operators.
fragment DISTOP_NUM
    : [2-9] [0-9]?
    | '1' [0-9]
    ;
