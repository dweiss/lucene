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
 * ANTLRv4 lexer grammar for the classic Lucene query syntax.
 *
 * Rule order matters: ANTLR picks the longest match and, on ties, the rule declared first
 * (within the current mode).
 */

lexer grammar ClassicSyntaxAntlrLexer;

// Default mode.

WS             : WHITESPACE -> skip ;

AND            : 'AND' | '&&' ;
OR             : 'OR' | '||' ;
NOT            : 'NOT' | '!' ;
PLUS           : '+' ;
MINUS          : '-' ;
// An operator followed by whitespace is a term.
BAREOPER       : [+\-!] WHITESPACE ;
LPAREN         : '(' ;
RPAREN         : ')' ;
COLON          : ':' ;
STAR           : '*' ;
CARAT          : '^' -> mode(Boost) ;
QUOTED         : '"' QUOTED_CHAR* '"' ;
TERM           : TERM_START_CHAR TERM_CHAR* ;
FUZZY_SLOP     : '~' ( NUM_CHAR+ ( '.' NUM_CHAR+ )? TERM_CHAR* | TERM_CHAR* ) ;
// A single '*' is always STAR.
PREFIXTERM     : TERM_START_CHAR TERM_CHAR* '*' ;
WILDTERM       : ( TERM_START_CHAR | [*?] ) ( TERM_CHAR | [*?] )* ;
REGEXPTERM     : '/' ( ~'/' | '\\/' )* '/' ;
RANGEIN_START  : '[' -> mode(Range) ;
RANGEEX_START  : '{' -> mode(Range) ;

// Entered after '^'. A number is the only thing recognized here (not even whitespace).

mode Boost;

NUMBER         : NUM_CHAR+ ( '.' NUM_CHAR+ )? -> mode(DEFAULT_MODE) ;

// Entered after '[' or '{', left on ']' or '}'.

mode Range;

// Must precede RANGE_GOOP, which also matches all whitespace characters but ' '.
RANGE_WS       : WHITESPACE -> skip ;

RANGE_TO       : 'TO' ;
RANGEIN_END    : ']' -> mode(DEFAULT_MODE) ;
RANGEEX_END    : '}' -> mode(DEFAULT_MODE) ;
RANGE_QUOTED   : '"' ( ~'"' | '\\"' )+ '"' ;
RANGE_GOOP     : ~[ \]}]+ ;

// Fragments, shared by all modes.

fragment NUM_CHAR
    : [0-9]
    ;

// Every character that follows a backslash is considered as an escaped character
fragment ESCAPED_CHAR
    : '\\' .
    ;

fragment TERM_START_CHAR
    : ~[ \t\n\r　+\-!():^[\]"{}~*?\\/]
    | ESCAPED_CHAR
    ;

fragment TERM_CHAR
    : TERM_START_CHAR
    | '-'
    | '+'
    ;

fragment WHITESPACE
    : [ \t\n\r　]
    ;

fragment QUOTED_CHAR
    : ~["\\]
    | ESCAPED_CHAR
    ;
