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

import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.Collection;

public interface ISpecialMethodCall {
  boolean isMethodName(String name);

  boolean isOneOfMethodNames(Collection<String> names);

  boolean isExceptionCondition();

  boolean isThrownCall();

  boolean isOldCall();

  boolean isInteractionCall();

  boolean isWithCall();

  boolean isConditionBlock();

  boolean isGroupConditionBlock();

  boolean isTestDouble();

  boolean isExceptionCondition(MethodCallExpression expr);

  boolean isThrownCall(MethodCallExpression expr);

  boolean isOldCall(MethodCallExpression expr);

  boolean isInteractionCall(MethodCallExpression expr);

  boolean isWithCall(MethodCallExpression expr);

  boolean isTestDouble(MethodCallExpression expr);

  boolean isMatch(Statement stat);

  boolean isMatch(ClosureExpression closureExpr);

  ClosureExpression getClosureExpr();

  void expand();
}
