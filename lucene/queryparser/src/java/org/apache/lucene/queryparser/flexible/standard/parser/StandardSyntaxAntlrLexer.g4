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
 * ANTLRv4 lexer grammar for the standard Lucene query syntax (flexible query parser).
 *
 * Rule order matters: ANTLR picks the longest match and, on ties, the rule declared first
 * (within the current mode).
 */

lexer grammar StandardSyntaxAntlrLexer;

// Default mode.

WS             : WHITESPACE -> skip ;

AND            : 'AND' | '&&' ;
OR             : 'OR' | '||' ;
NOT            : 'NOT' | '!' ;
FN_PREFIX      : 'fn:' -> mode(Function) ;
PLUS           : '+' ;
MINUS          : '-' ;
RPAREN         : ')' ;
OP_COLON       : ':' ;
OP_EQUAL       : '=' ;
OP_LESSTHAN    : '<' ;
OP_LESSTHANEQ  : '<=' ;
OP_MORETHAN    : '>' ;
OP_MORETHANEQ  : '>=' ;
CARAT          : '^' ;
TILDE          : '~' ;
AT             : '@' ;
QUOTED         : '"' QUOTED_CHAR* '"' ;
NUMBER         : NUM_CHAR+ ( '.' NUM_CHAR+ )? ;
TERM           : TERM_START_CHAR TERM_CHAR* ;
REGEXPTERM     : '/' ( ~'/' | '\\/' )* '/' ;
RANGEIN_START  : '[' -> mode(Range) ;
RANGEEX_START  : '{' -> mode(Range) ;
LPAREN         : '(' ;

// Entered after 'fn:'. Only the function's name (and optional whitespace) is recognized
// here; the opening parenthesis switches back to the default mode.

mode Function;

FUNCTION_WS            : WHITESPACE -> skip ;
FUNCTION_LPAREN        : '(' -> type(LPAREN), mode(DEFAULT_MODE) ;

ATLEAST                : 'atleast' | 'atLeast' ;
AFTER                  : 'after' ;
BEFORE                 : 'before' ;
CONTAINED_BY           : 'containedBy' | 'containedby' ;
CONTAINING             : 'containing' ;
EXTEND                 : 'extend' ;
FN_OR                  : 'or' ;
FUZZYTERM              : 'fuzzyterm' | 'fuzzyTerm' ;
MAXGAPS                : 'maxgaps' | 'maxGaps' ;
MAXWIDTH               : 'maxwidth' | 'maxWidth' ;
NON_OVERLAPPING        : 'nonOverlapping' | 'nonoverlapping' ;
NOT_CONTAINED_BY       : 'notContainedBy' | 'notcontainedby' ;
NOT_CONTAINING         : 'notContaining' | 'notcontaining' ;
NOT_WITHIN             : 'notWithin' | 'notwithin' ;
ORDERED                : 'ordered' ;
OVERLAPPING            : 'overlapping' ;
PHRASE                 : 'phrase' ;
UNORDERED              : 'unordered' ;
UNORDERED_NO_OVERLAPS  : 'unorderedNoOverlaps' | 'unorderednooverlaps' ;
WILDCARD               : 'wildcard' ;
WITHIN                 : 'within' ;

// Entered after '[' or '{', left on ']' or '}'.

mode Range;

// Must precede RANGE_GOOP, which also matches all whitespace characters but ' '.
RANGE_WS      : WHITESPACE -> skip ;

RANGE_TO      : 'TO' ;
RANGEIN_END   : ']' -> mode(DEFAULT_MODE) ;
RANGEEX_END   : '}' -> mode(DEFAULT_MODE) ;
RANGE_QUOTED  : '"' ( ~'"' | '\\"' )+ '"' ;
RANGE_GOOP    : ~[ \]}]+ ;

// Fragments, shared by all modes.

fragment NUM_CHAR
    : [0-9]
    ;

// Every character that follows a backslash is considered as an escaped character
fragment ESCAPED_CHAR
    : '\\' .
    ;

fragment TERM_START_CHAR
    : ~[ \t\n\r　+\-!():^@<>=[\]"{}~\\/]
    | ESCAPED_CHAR
    ;

fragment TERM_CHAR
    : TERM_START_CHAR
    | ESCAPED_CHAR
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
