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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.syntax.Types;
import org.spockframework.lang.ConditionBlock;
import org.spockframework.util.CollectionUtil;
import org.spockframework.util.Identifiers;
import org.spockframework.util.Nullable;
import org.spockframework.util.ObjectUtil;

public class SpecialMethodCall implements ISpecialMethodCall {
  private final String methodName;
  private final Expression inferredName;
  private final Expression inferredType;
  private final MethodCallExpression methodCallExpr;
  @Nullable
  private final BinaryExpression binaryExpr;
  @Nullable
  private final ClosureExpression closureExpr;
  private final boolean conditionBlock;

  public SpecialMethodCall(String methodName, Expression inferredName, Expression inferredType,
      MethodCallExpression methodCallExpr, @Nullable BinaryExpression binaryExpr,
      @Nullable ClosureExpression closureExpr, boolean conditionBlock) {
    this.methodName = methodName;
    this.inferredName = inferredName;
    this.inferredType = inferredType;
    this.binaryExpr = binaryExpr;
    this.methodCallExpr = methodCallExpr;
    this.closureExpr = closureExpr;
    this.conditionBlock = conditionBlock;
  }

  public boolean isMethodName(String name) {
    return name.equals(methodName);
  }

  public boolean isOneOfMethodNames(Collection<String> names) {
    return names.contains(methodName);
  }

  public boolean isExceptionCondition() {
    return isOneOfMethodNames(Identifiers.EXCEPTION_CONDITION_METHODS);
  }

  public boolean isThrownCall() {
    return isMethodName(Identifiers.THROWN);
  }

  public boolean isOldCall() {
    return isMethodName(Identifiers.OLD);
  }

  public boolean isInteractionCall() {
    return isMethodName(Identifiers.INTERACTION);
  }

  public boolean isWithCall() {
    return isMethodName(Identifiers.WITH);
  }

  public boolean isConditionBlock() {
    return conditionBlock;
  }

  public boolean isGroupConditionBlock() {
    return isMethodName(Identifiers.VERIFY_ALL);
  }

  public boolean isTestDouble() {
    return isOneOfMethodNames(Identifiers.TEST_DOUBLE_METHODS);
  }

  public boolean isExceptionCondition(MethodCallExpression expr) {
    return expr == methodCallExpr && isExceptionCondition();
  }

  public boolean isThrownCall(MethodCallExpression expr) {
    return expr == methodCallExpr && isThrownCall();
  }

  public boolean isOldCall(MethodCallExpression expr) {
    return expr == methodCallExpr && isOldCall();
  }

  public boolean isInteractionCall(MethodCallExpression expr) {
    return expr == methodCallExpr && isInteractionCall();
  }

  public boolean isWithCall(MethodCallExpression expr) {
    return expr == methodCallExpr && isWithCall();
  }

  public boolean isConditionBlock(MethodCallExpression expr) {
    return expr == methodCallExpr && isConditionBlock();
  }

  public boolean isTestDouble(MethodCallExpression expr) {
    return expr == methodCallExpr && isTestDouble();
  }

  public boolean isMatch(Statement stat) {
    ExpressionStatement exprStat = ObjectUtil.asInstance(stat, ExpressionStatement.class);
    if (exprStat == null) return false;
    Expression expr = exprStat.getExpression();
    return expr == binaryExpr || expr == methodCallExpr;
  }

  public boolean isMatch(ClosureExpression expr) {
    return expr == closureExpr;
  }

  @Nullable
  public ClosureExpression getClosureExpr() {
    return closureExpr;
  }

  public void expand() {
    List<Expression> args = new ArrayList<Expression>();
    args.add(inferredName);
    args.add(inferredType);
    args.addAll(AstUtil.getArgumentList(methodCallExpr));

    ArgumentListExpression argsExpr = new ArgumentListExpression(args);
    AstUtil.copySourcePosition(methodCallExpr.getArguments(), argsExpr);
    methodCallExpr.setArguments(argsExpr);
    methodCallExpr.setMethod(new ConstantExpression(methodName + "Impl"));
  }

  public static SpecialMethodCall parse(MethodCallExpression methodCallExpr, @Nullable BinaryExpression binaryExpr) {
    boolean builtInCall = checkIsBuiltInMethodCall(methodCallExpr);
    boolean conditionBlock = checkIsConditionBlock(methodCallExpr);

    if (!(builtInCall || conditionBlock)) return null;

    String methodName = methodCallExpr.getMethodAsString();
    Expression inferredName;
    Expression inferredType;

    if (binaryExpr != null && binaryExpr.getOperation().getType() == Types.ASSIGN && binaryExpr.getRightExpression() == methodCallExpr) {
      inferredName = AstUtil.getVariableName(binaryExpr);
      inferredType = AstUtil.getVariableType(binaryExpr);
    } else {
      binaryExpr = null; // not part of this special method call
      inferredName = ConstantExpression.NULL;
      inferredType = ConstantExpression.NULL;
    }

    ClosureExpression closureExpr = null;
    List<Expression> arguments = AstUtil.getArgumentList(methodCallExpr);
    if (!arguments.isEmpty()) {
      Expression lastArg = CollectionUtil.getLastElement(arguments);
      if (lastArg instanceof ClosureExpression) {
        closureExpr = (ClosureExpression) lastArg;
      }
    }

    return new SpecialMethodCall(methodName, inferredName, inferredType, methodCallExpr, binaryExpr, closureExpr, conditionBlock);
  }

  public String toString() {
    return String.format("method name: %s\ninferred name: %s\ninferred type: %s\nmethod call:%s\nclosure: %s\ncondition block: %s\n",
        methodName, inferredName, inferredType, methodCallExpr, closureExpr, conditionBlock);
  }

  private static boolean checkIsBuiltInMethodCall(MethodCallExpression expr) {
    if (!AstUtil.isThisOrSuperExpression(expr.getObjectExpression())) return false;
    return Identifiers.BUILT_IN_METHODS.contains(expr.getMethodAsString());
  }

  private static boolean checkIsConditionBlock(MethodCallExpression methodCallExpr) {
    ClassNode targetType = methodCallExpr.getObjectExpression().getType();
    String methodName = methodCallExpr.getMethodAsString();

    List<MethodNode> methods = targetType.getMethods(methodName);
    for (MethodNode method : methods) {
      for (AnnotationNode annotation : method.getAnnotations()) {
        if (annotation.getClassNode().getName().equals(ConditionBlock.class.getName())) return true;
      }
    }

    return false;
  }
}
