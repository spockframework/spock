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

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.spockframework.util.UnreachableCodeError;

import java.util.Collection;

public class ErrorRethrowerUsageDetector extends ClassCodeVisitorSupport {
  private boolean errorRethrowerUsageDetected;

  public boolean detectedErrorRethrowerUsage(Expression expr) {
    errorRethrowerUsageDetected = false;
    expr.visit(this);
    return errorRethrowerUsageDetected;
  }

  public boolean detectedErrorRethrowerUsage(Collection<Statement> stats) {
    errorRethrowerUsageDetected = false;
    stats.forEach(stat -> stat.visit(this));
    return errorRethrowerUsageDetected;
  }

  @Override
  public void visitVariableExpression(VariableExpression expr) {
    super.visitVariableExpression(expr);

    Variable var = expr.getAccessedVariable();
    errorRethrowerUsageDetected |= (var == null) && expr.getName().equals(SpockNames.ERROR_COLLECTOR);
  }

  @Override
  protected SourceUnit getSourceUnit() {
    throw new UnreachableCodeError();
  }
}
