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
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;

import org.spockframework.compiler.model.Block;
import org.spockframework.compiler.model.Method;
import org.spockframework.util.UnreachableCodeError;

import spock.lang.Shared;

import java.util.Collection;

// we don't currently check this.x and super.x because
// it's not as clear how this should be done (for example,
// 'x' could refer to a property)
public class InstanceFieldAccessChecker extends ClassCodeVisitorSupport {
  private final IRewriteResources resources;

  public InstanceFieldAccessChecker(IRewriteResources resources) {
    this.resources = resources;
  }

  public void check(Expression expr) {
    expr.visit(this);
  }

  public void check(Collection<Statement> stats) {
    stats.forEach(stat -> stat.visit(this));
  }

  public void check(Method method) {
    for (Block block : method.getBlocks())
      for (Statement stat : block.getAst())
        stat.visit(this);
  }

  @Override
  public void visitVariableExpression(VariableExpression expr) {
    super.visitVariableExpression(expr);

    Variable var = expr.getAccessedVariable();
    if (!(var instanceof FieldNode)) return;

    checkFieldAccess(expr, (FieldNode) var);
  }

  @Override
  public void visitFieldExpression(FieldExpression expr) {
    super.visitFieldExpression(expr);

    checkFieldAccess(expr, expr.getField());
  }

  @Override
  protected SourceUnit getSourceUnit() {
    throw new UnreachableCodeError();
  }

  private void checkFieldAccess(ASTNode context, FieldNode field) {
    if (AstUtil.hasAnnotation(field, Shared.class) || field.isStatic()) return;

    resources.getErrorReporter().error(context, "Only @Shared and static fields may be accessed from here");
  }
}
