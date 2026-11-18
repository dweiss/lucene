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
package org.apache.lucene.queryparser.flexible.standard.parser;

import org.apache.lucene.queryparser.flexible.core.QueryNodeParseException;
import org.apache.lucene.queryparser.flexible.core.messages.QueryParserMessages;
import org.apache.lucene.queryparser.flexible.messages.Message;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;

/** This exception is thrown when parse errors are encountered. */
public class ParseException extends QueryNodeParseException {
  private static final long serialVersionUID = 1L;

  /** Creates an exception with a generic syntax error message. */
  public ParseException() {
    super(new MessageImpl(QueryParserMessages.INVALID_SYNTAX, "Error"));
  }

  /** Constructor with message. */
  public ParseException(Message message) {
    super(message);
  }

  /**
   * Creates an exception for a syntax error in {@code query}.
   *
   * @param query the query that failed to parse
   * @param details the description of the problem
   * @param line the line of the error (1-based) or -1 if unknown
   * @param column the column of the error within the line (0-based) or -1 if unknown
   * @param errorToken the text of the offending token or an empty string if unknown
   */
  ParseException(CharSequence query, String details, int line, int column, String errorToken) {
    super(new MessageImpl(QueryParserMessages.INVALID_SYNTAX_CANNOT_PARSE, query, details));
    setQuery(query);
    // setQuery resets the message, dropping the details.
    setNonLocalizedMessage(
        new MessageImpl(QueryParserMessages.INVALID_SYNTAX_CANNOT_PARSE, query, details));
    setBeginLine(line);
    setBeginColumn(column);
    setErrorToken(errorToken);
  }
}
