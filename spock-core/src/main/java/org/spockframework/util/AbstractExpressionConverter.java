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

package org.spockframework.util;

import java.util.*;

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.*;

// IDEA: abstract method that provides default conversion if result not set
public abstract class AbstractExpressionConverter<T> implements GroovyCodeVisitor {
  protected T result;

  // tries to detect all situations where a visitXXXExpression
  // method doesn't set a result, as this would corrupt the
  // conversion
  public T convert(Expression expr) {
    result = null;
    expr.visit(this);
    if (result == null)
      Assert.fail("No result set for expression: " + expr);

    T temp = result;
    result = null;
    return temp;
  }

  protected List<T> convertAll(List<? extends Expression> expressions) {
    List<T> converted = new ArrayList<>(expressions.size());
    // do not convert the implicit this expression like when calling the constructor of a non-static inner class
    for (Expression expr : expressions) {
      if (expr instanceof VariableExpression) {
        VariableExpression varExpr = (VariableExpression) expr;
        if (varExpr.isThisExpression() && varExpr.isDynamicTyped()) {
          converted.add((T) varExpr);
          continue;
        }
      }
      converted.add(convert(expr));
    }
    return converted;
  }

  protected void unsupported() {
    throw new UnsupportedOperationException();
  }

  // remaining methods are statement callbacks of GroovyCodeVisitor

  @Override
  public final void visitBlockStatement(BlockStatement statement) {
    unsupported();
  }

  @Override
  public final void visitForLoop(ForStatement forLoop) {
    unsupported();
  }

  @Override
  public final void visitWhileLoop(WhileStatement loop) {
    unsupported();
  }

  @Override
  public final void visitDoWhileLoop(DoWhileStatement loop) {
    unsupported();
  }

  @Override
  public final void visitIfElse(IfStatement ifElse) {
    unsupported();
  }

  @Override
  public final void visitExpressionStatement(ExpressionStatement statement) {
    unsupported();
  }

  @Override
  public final void visitReturnStatement(ReturnStatement statement) {
    unsupported();
  }

  @Override
  public final void visitAssertStatement(AssertStatement statement) {
    unsupported();
  }

  @Override
  public final void visitTryCatchFinally(TryCatchStatement finally1) {
    unsupported();
  }

  @Override
  public final void visitSwitch(SwitchStatement statement) {
    unsupported();
  }

  @Override
  public final void visitCaseStatement(CaseStatement statement) {
    unsupported();
  }

  @Override
  public final void visitBreakStatement(BreakStatement statement) {
    unsupported();
  }

  @Override
  public final void visitContinueStatement(ContinueStatement statement) {
    unsupported();
  }

  @Override
  public final void visitThrowStatement(ThrowStatement statement) {
    unsupported();
  }

  @Override
  public final void visitSynchronizedStatement(SynchronizedStatement statement) {
    unsupported();
  }

  @Override
  public final void visitCatchStatement(CatchStatement statement) {
    unsupported();
  }
}

