/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.compiler;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.syntax.SyntaxException;

/**
 * Indicates that a spec was found to contain a (syntactic or semantic)
 * error during compilation. As such, this is the compile-time equivalent
 * of InvalidSpecException.
 * Inherits from SyntaxException so that line information can
 * be exploited by integrators (IDEs etc.).
 *
 * Most of the time it is not necessary to use this class directly (see
 * documentation of class ErrorReporter).
 *
 * @author Peter Niederwieser
 */
public class InvalidSpecCompileException extends SyntaxException {
  public InvalidSpecCompileException(int line, int column, String msg, Object... args) {
    super(String.format(msg, args), line, column);
  }

  public InvalidSpecCompileException(ASTNode node, String msg, Object... args) {
    this(getLine(node), getColumn(node), msg, args);
  }

  private static int getLine(ASTNode node) {
    return node.getLineNumber() > 0 ? node.getLineNumber() : node.getLastLineNumber();
  }

  private static int getColumn(ASTNode node) {
    return node.getColumnNumber() > 0 ? node.getColumnNumber() : node.getLastColumnNumber();
  }
}
