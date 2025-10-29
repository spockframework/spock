/*
 * Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.compiler;

import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.Collection;

public class NoSpecialMethodCall implements ISpecialMethodCall {
  public static final ISpecialMethodCall INSTANCE = new NoSpecialMethodCall();
  public static final ISpecialMethodCall CLOSURE_INSTANCE = new NoSpecialMethodCall();

  @Override
  public boolean isMethodName(String name) {
    return false;
  }

  @Override
  public boolean isOneOfMethodNames(Collection<String> names) {
    return false;
  }

  @Override
  public boolean isExceptionCondition() {
    return false;
  }

  @Override
  public boolean isThrownCall() {
    return false;
  }

  @Override
  public boolean isOldCall() {
    return false;
  }

  @Override
  public boolean isInteractionCall() {
    return false;
  }

  @Override
  public boolean isWithCall() {
    return false;
  }

  @Override
  public boolean isConditionMethodCall() {
    return false;
  }

  @Override
  public boolean isConditionBlock() {
    return false;
  }

  @Override
  public boolean isGroupConditionBlock() {
    return false;
  }

  @Override
  public boolean isTestDouble() {
    return false;
  }

  @Override
  public boolean isExceptionCondition(MethodCallExpression expr) {
    return false;
  }

  @Override
  public boolean isThrownCall(MethodCallExpression expr) {
    return false;
  }

  @Override
  public boolean isOldCall(MethodCallExpression expr) {
    return false;
  }

  @Override
  public boolean isInteractionCall(MethodCallExpression expr) {
    return false;
  }

  @Override
  public boolean isConditionMethodCall(MethodCallExpression expr) {
    return false;
  }

  @Override
  public boolean isTestDouble(MethodCallExpression expr) {
    return false;
  }

  @Override
  public boolean isMatch(Statement stat) {
    return false;
  }

  @Override
  public boolean isMatch(ClosureExpression closureExpr) {
    return false;
  }

  @Override
  public ClosureExpression getClosureExpr() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void expand() {
    throw new UnsupportedOperationException();
  }
}
