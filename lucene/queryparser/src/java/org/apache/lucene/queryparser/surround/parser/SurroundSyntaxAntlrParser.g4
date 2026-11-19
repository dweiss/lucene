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
 * ANTLRv4 parser grammar for the surround query language.
 *
 * The grammar contains no embedded actions: QueryParser walks the parse tree and builds the
 * query. Element labels mark the tokens it refers to.
 */

parser grammar SurroundSyntaxAntlrParser;

options { tokenVocab = SurroundSyntaxAntlrLexer; }

/*
 * The top-level rule ensures that there is no garbage after the query string.
 */
topSrndQuery
    : fieldsQuery EOF
    ;

/*
 * A query, optionally restricted to one or more fields.
 */
fieldsQuery
    : ( fieldNames+=TERM COLON )* orQuery
    ;

/*
 * Infix operators, in increasing order of precedence. All are left associative.
 */
orQuery
    : andQuery ( operators+=OR andQuery )*
    ;

andQuery
    : notQuery ( operators+=AND notQuery )*
    ;

notQuery
    : nQuery ( operators+=NOT nQuery )*
    ;

nQuery
    : wQuery ( operators+=N wQuery )*
    ;

wQuery
    : primaryQuery ( operators+=W primaryQuery )*
    ;

/*
 * A bracketed weighted query or a weighted term.
 */
primaryQuery
    : ( LPAREN fieldsQuery RPAREN | prefixOperatorQuery | simpleTerm ) ( CARAT weights+=NUMBER )*
    ;

/*
 * An operator in prefix notation: 'OR(a, b, c)'.
 */
prefixOperatorQuery
    : operator=( OR | AND | N | W ) fieldsQueryList
    ;

fieldsQueryList
    : LPAREN fieldsQuery ( COMMA fieldsQuery )+ RPAREN
    ;

simpleTerm
    : term=( TERM | QUOTED | SUFFIXTERM | TRUNCTERM | TRUNCQUOTED )
    ;
