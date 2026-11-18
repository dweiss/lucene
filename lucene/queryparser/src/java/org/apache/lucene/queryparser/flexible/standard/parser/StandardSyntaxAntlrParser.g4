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
 * ANTLRv4 parser grammar for the standard Lucene query syntax (flexible query parser).
 *
 * The grammar contains no embedded actions: StandardSyntaxParser walks the parse tree and
 * builds the query node tree. Element labels mark the tokens and subrules it refers to.
 */

parser grammar StandardSyntaxAntlrParser;

options { tokenVocab = StandardSyntaxAntlrLexer; }

/*
 * The top-level rule ensures that there is no garbage after the query string.
 */
topLevelQuery
    : query EOF
    ;

/*
 * A query consists of one or more disjunction queries (solves operator precedence).
 *
 * A "pure" negation query (a single clause with the NOT modifier) needs to be wrapped as
 * a boolean query, otherwise the negation is dropped.
 */
query
    : disjQuery+
    ;

/*
 * A disjoint clause consists of one or more conjunction clauses.
 */
disjQuery
    : conjQuery ( OR conjQuery )*
    ;

/*
 * A conjunction clause consists of one or more modifier-clause pairs.
 */
conjQuery
    : modClause ( AND modClause )*
    ;

/*
 * A modifier-atomic clause pair.
 */
modClause
    : modifier=( PLUS | MINUS | NOT )? clause
    ;

/*
 * An atomic clause consists of a field range expression, a potentially
 * field-qualified term, a group or an interval function.
 */
clause
    : fieldRangeExpr
    | ( fieldName ( OP_COLON | OP_EQUAL ) )? ( term | groupingExpr | intervalFunction )
    ;

/*
 * A field name. Escape characters need to be stripped from field names.
 */
fieldName
    : TERM
    ;

/*
 * A grouping expression is a Query with potential boost and minimum-should-match
 * applied to it.
 */
groupingExpr
    : LPAREN query RPAREN boost? ( AT minShouldMatch=NUMBER )?
    ;

/*
 * An argument of an interval function: a nested function or analyzed text.
 */
intervalFun
    : intervalFunction
    | intervalText
    ;

/*
 * An interval function ('fn:name(...)'). This is also an atomic clause: plain text at the
 * clause level is always a term, so only the function form is allowed there.
 */
intervalFunction
    : intervalAtLeast
    | intervalMaxWidth
    | intervalMaxGaps
    | intervalOrdered
    | intervalUnordered
    | intervalUnorderedNoOverlaps
    | intervalOr
    | intervalWildcard
    | intervalAfter
    | intervalBefore
    | intervalPhrase
    | intervalContaining
    | intervalNotContaining
    | intervalContainedBy
    | intervalNotContainedBy
    | intervalWithin
    | intervalNotWithin
    | intervalOverlapping
    | intervalNonOverlapping
    | intervalExtend
    | intervalFuzzyTerm
    ;

intervalAtLeast
    : FN_PREFIX ATLEAST LPAREN minShouldMatch=NUMBER sources+=intervalFun+ RPAREN
    ;

intervalMaxWidth
    : FN_PREFIX MAXWIDTH LPAREN maxWidth=NUMBER source=intervalFun RPAREN
    ;

intervalMaxGaps
    : FN_PREFIX MAXGAPS LPAREN maxGaps=NUMBER source=intervalFun RPAREN
    ;

intervalUnordered
    : FN_PREFIX UNORDERED LPAREN sources+=intervalFun+ RPAREN
    ;

intervalUnorderedNoOverlaps
    : FN_PREFIX UNORDERED_NO_OVERLAPS LPAREN a=intervalFun b=intervalFun RPAREN
    ;

intervalOrdered
    : FN_PREFIX ORDERED LPAREN sources+=intervalFun+ RPAREN
    ;

intervalOr
    : FN_PREFIX FN_OR LPAREN sources+=intervalFun+ RPAREN
    ;

intervalPhrase
    : FN_PREFIX PHRASE LPAREN sources+=intervalFun+ RPAREN
    ;

intervalBefore
    : FN_PREFIX BEFORE LPAREN source=intervalFun reference=intervalFun RPAREN
    ;

intervalAfter
    : FN_PREFIX AFTER LPAREN source=intervalFun reference=intervalFun RPAREN
    ;

intervalContaining
    : FN_PREFIX CONTAINING LPAREN big=intervalFun small=intervalFun RPAREN
    ;

intervalNotContaining
    : FN_PREFIX NOT_CONTAINING LPAREN minuend=intervalFun subtrahend=intervalFun RPAREN
    ;

intervalContainedBy
    : FN_PREFIX CONTAINED_BY LPAREN small=intervalFun big=intervalFun RPAREN
    ;

intervalNotContainedBy
    : FN_PREFIX NOT_CONTAINED_BY LPAREN small=intervalFun big=intervalFun RPAREN
    ;

intervalWithin
    : FN_PREFIX WITHIN LPAREN source=intervalFun positions=NUMBER reference=intervalFun RPAREN
    ;

intervalExtend
    : FN_PREFIX EXTEND LPAREN source=intervalFun before=NUMBER after=NUMBER RPAREN
    ;

intervalNotWithin
    : FN_PREFIX NOT_WITHIN LPAREN minuend=intervalFun positions=NUMBER subtrahend=intervalFun RPAREN
    ;

intervalOverlapping
    : FN_PREFIX OVERLAPPING LPAREN source=intervalFun reference=intervalFun RPAREN
    ;

intervalNonOverlapping
    : FN_PREFIX NON_OVERLAPPING LPAREN minuend=intervalFun subtrahend=intervalFun RPAREN
    ;

intervalWildcard
    : FN_PREFIX WILDCARD LPAREN wildcard=( TERM | NUMBER | QUOTED ) maxExpansions=NUMBER? RPAREN
    ;

// A single number is maxEdits.
intervalFuzzyTerm
    : FN_PREFIX FUZZYTERM LPAREN fuzzyTerm=( TERM | NUMBER | QUOTED )
        ( maxEdits=NUMBER maxExpansions=NUMBER? )? RPAREN
    ;

intervalText
    : QUOTED
    | ( TERM | NUMBER )
    ;

/*
 * Score boost modifier.
 */
boost
    : CARAT NUMBER
    ;

/*
 * Fuzzy term modifier.
 *
 * A number following the tilde could be the fuzzy similarity or the next clause's term. It is
 * always the similarity: the predicate prevents the parser from choosing the other reading
 * when only that one leads to a successful parse ('a~ 0.8~' is an error, not 'a~' and '0.8~').
 */
fuzzyOp
    : TILDE ( similarity=NUMBER | {_input.LA(1) != NUMBER}? )
    ;

/*
 * A field range expression selects all field values larger/ smaller (or equal) than a given one.
 */
fieldRangeExpr
    : fieldName
        operator=( OP_LESSTHAN | OP_LESSTHANEQ | OP_MORETHAN | OP_MORETHANEQ )
        value=( TERM | QUOTED | NUMBER )
    ;

/*
 * A term expression.
 */
term
    : ( REGEXPTERM | ( TERM | NUMBER ) fuzzyOp? | termRangeExpr | quotedTerm ) boost?
    ;

/*
 * A quoted term (phrase).
 */
quotedTerm
    : QUOTED ( TILDE slop=NUMBER )?
    ;

/*
 * A value range expression.
 *
 * RANGE_TO can be consumed as range start/end because this needs to be accepted as a valid range:
 * [TO TO TO]
 */
termRangeExpr
    : open=( RANGEIN_START | RANGEEX_START )
        left=( RANGE_GOOP | RANGE_QUOTED | RANGE_TO )
        RANGE_TO
        right=( RANGE_GOOP | RANGE_QUOTED | RANGE_TO )
        close=( RANGEIN_END | RANGEEX_END )
    ;
