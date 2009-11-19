/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.util;

import org.codehaus.groovy.ast.ASTNode;

/**
 * Indicates a syntax error in a Spec. Used by both compiler and runtime.
 *
 * @author Peter Niederwieser
 */
// IDEA: throwing this exception causes compilation to end immediately;
// should we be more graceful (i.e. use errorCollector.add...)?
public class SyntaxException extends RuntimeException {
  private final int line;
  private final int column;

  public SyntaxException(int line, int column, String msg, Object... args) {
    super(String.format(msg, args));
    this.line = line;
    this.column = column;
  }

  // should only be used for SyntaxException's that are thrown at runtime
  public SyntaxException(String msg, Object... args) {
    this(-1, -1, msg, args);
  }

  public SyntaxException(ASTNode node, String msg, Object... args) {
    this(getLine(node), getColumn(node), msg, args);
  }

  private static int getLine(ASTNode node) {
    return node.getLineNumber() > 0 ? node.getLineNumber() : node.getLastLineNumber();
  }

  private static int getColumn(ASTNode node) {
    return node.getColumnNumber() > 0 ? node.getColumnNumber() : node.getLastColumnNumber();
  }

  public SpockSyntaxException toSpockSyntaxException() {
    return new SpockSyntaxException(getMessage(), line, column);
  }
}
