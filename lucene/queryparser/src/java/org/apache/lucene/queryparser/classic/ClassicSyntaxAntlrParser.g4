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
 * ANTLRv4 parser grammar for the classic Lucene query syntax.
 *
 * The grammar contains no embedded actions: QueryParser walks the parse tree and builds the
 * query. Element labels mark the tokens it refers to.
 */

parser grammar ClassicSyntaxAntlrParser;

options { tokenVocab = ClassicSyntaxAntlrLexer; }

/*
 * The top-level rule ensures that there is no garbage after the query string.
 */
topLevelQuery
    : query EOF
    ;

/*
 * A query is a series of clauses, each optionally preceded by a conjunction (other than
 * the first one) and a modifier.
 *
 * Adjacent plain terms ('multi-terms') are analyzed together unless the parser splits on
 * whitespace. They are ordinary clauses here; QueryParser groups them.
 */
query
    : modClause conjClause*
    ;

conjClause
    : conjunction=( AND | OR )? modClause
    ;

modClause
    : modifier=( PLUS | MINUS | NOT )? clause
    ;

/*
 * A clause is a term or a parenthesized query, optionally qualified with a field.
 */
clause
    : ( field=( TERM | STAR ) COLON )? ( term | LPAREN query RPAREN ( CARAT boost=NUMBER )? )
    ;

/*
 * A term: a single (bare) token, a range or a quoted phrase.
 */
term
    : bare=( TERM | STAR | PREFIXTERM | WILDTERM | REGEXPTERM | NUMBER | BAREOPER )
        ( CARAT boost=NUMBER fuzzySlop=FUZZY_SLOP? | fuzzySlop=FUZZY_SLOP ( CARAT boost=NUMBER )? )?
    | open=( RANGEIN_START | RANGEEX_START )
        left=( RANGE_GOOP | RANGE_QUOTED | RANGE_TO )
        RANGE_TO
        right=( RANGE_GOOP | RANGE_QUOTED | RANGE_TO )
        close=( RANGEIN_END | RANGEEX_END )
        ( CARAT boost=NUMBER )?
    | quoted=QUOTED
        ( CARAT boost=NUMBER fuzzySlop=FUZZY_SLOP? | fuzzySlop=FUZZY_SLOP ( CARAT boost=NUMBER )? )?
    ;
