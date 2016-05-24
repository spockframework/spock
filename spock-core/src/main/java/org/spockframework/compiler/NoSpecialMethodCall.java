/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.compiler;

import java.util.Collection;

import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.Statement;

public class NoSpecialMethodCall implements ISpecialMethodCall {
  public static final ISpecialMethodCall INSTANCE = new NoSpecialMethodCall();

  public boolean isMethodName(String name) {
    return false;
  }

  public boolean isOneOfMethodNames(Collection<String> names) {
    return false;
  }

  public boolean isExceptionCondition() {
    return false;
  }

  public boolean isThrownCall() {
    return false;
  }

  public boolean isOldCall() {
    return false;
  }

  public boolean isInteractionCall() {
    return false;
  }

  public boolean isWithCall() {
    return false;
  }

  public boolean isConditionBlock() {
    return false;
  }

  public boolean isGroupConditionBlock() {
    return false;
  }

  public boolean isTestDouble() {
    return false;
  }

  public boolean isExceptionCondition(MethodCallExpression expr) {
    return false;
  }

  public boolean isThrownCall(MethodCallExpression expr) {
    return false;
  }

  public boolean isOldCall(MethodCallExpression expr) {
    return false;
  }

  public boolean isInteractionCall(MethodCallExpression expr) {
    return false;
  }

  public boolean isWithCall(MethodCallExpression expr) {
    return false;
  }

  public boolean isTestDouble(MethodCallExpression expr) {
    return false;
  }

  public boolean isMatch(Statement stat) {
    return false;
  }

  public boolean isMatch(ClosureExpression closureExpr) {
    return false;
  }

  public ClosureExpression getClosureExpr() {
    throw new UnsupportedOperationException();
  }

  public void expand() {
    throw new UnsupportedOperationException();
  }
}
