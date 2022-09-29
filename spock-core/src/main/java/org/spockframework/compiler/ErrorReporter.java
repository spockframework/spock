/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.compiler;

import org.spockframework.util.TextUtil;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.*;

/**
 * Reporting facility for problems found during compilation.
 * In general, error(ASTNode) is the preferred method to use.
 * error(InvalidSpecCompileException) should only be used if compilation cannot
 * continue in the same method where the error was found (because some
 * steps need to be skipped). In that case, a InvalidSpecCompileException should be
 * thrown at the point where the error is detected, and an outer method
 * should catch the exception and pass it on to ErrorReporter.
 *
 * @author Peter Niederwieser
 */
public class ErrorReporter {
  private final SourceUnit sourceUnit;

  public ErrorReporter(SourceUnit sourceUnit) {
    this.sourceUnit = sourceUnit;
  }

  public void error(String msg, Object... args) {
    sourceUnit.getErrorCollector().addErrorAndContinue(
        new SimpleMessage(String.format(msg, args), sourceUnit));
  }

  public void error(String msg, Throwable cause, Object... args) {
    sourceUnit.getErrorCollector().addErrorAndContinue(
        new SimpleMessage(String.format(msg, args) + "\n\n" + TextUtil.printStackTrace(cause), sourceUnit));
  }

  public void error(ASTNode node, String msg, Object... args) {
    error(new InvalidSpecCompileException(node, msg, args));
  }

  public void error(int line, int column, String msg, Object... args) {
    error(new InvalidSpecCompileException(line, column, msg, args));
  }

  public void error(InvalidSpecCompileException e) {
    sourceUnit.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(e, sourceUnit));
  }
}
