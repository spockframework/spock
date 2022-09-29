/*
 * Copyright 2012 the original author or authors.
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

import org.spockframework.lang.ConditionBlock;
import org.spockframework.util.*;

import java.util.*;
import java.util.stream.Stream;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.syntax.Types;

import static java.util.stream.Collectors.toList;

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

  @Override
  public boolean isMethodName(String name) {
    return name.equals(methodName);
  }

  @Override
  public boolean isOneOfMethodNames(Collection<String> names) {
    return names.contains(methodName);
  }

  @Override
  public boolean isExceptionCondition() {
    return isOneOfMethodNames(Identifiers.EXCEPTION_CONDITION_METHODS);
  }

  @Override
  public boolean isThrownCall() {
    return isMethodName(Identifiers.THROWN);
  }

  @Override
  public boolean isOldCall() {
    return isMethodName(Identifiers.OLD);
  }

  @Override
  public boolean isInteractionCall() {
    return isMethodName(Identifiers.INTERACTION);
  }

  @Override
  public boolean isWithCall() {
    return isMethodName(Identifiers.WITH);
  }

  @Override
  public boolean isConditionBlock() {
    return conditionBlock;
  }

  @Override
  public boolean isGroupConditionBlock() {
    return isMethodName(Identifiers.VERIFY_ALL);
  }

  @Override
  public boolean isTestDouble() {
    return isOneOfMethodNames(Identifiers.TEST_DOUBLE_METHODS);
  }

  @Override
  public boolean isExceptionCondition(MethodCallExpression expr) {
    return expr == methodCallExpr && isExceptionCondition();
  }

  @Override
  public boolean isThrownCall(MethodCallExpression expr) {
    return expr == methodCallExpr && isThrownCall();
  }

  @Override
  public boolean isOldCall(MethodCallExpression expr) {
    return expr == methodCallExpr && isOldCall();
  }

  @Override
  public boolean isInteractionCall(MethodCallExpression expr) {
    return expr == methodCallExpr && isInteractionCall();
  }

  @Override
  public boolean isWithCall(MethodCallExpression expr) {
    return expr == methodCallExpr && isWithCall();
  }

  public boolean isConditionBlock(MethodCallExpression expr) {
    return expr == methodCallExpr && isConditionBlock();
  }

  @Override
  public boolean isTestDouble(MethodCallExpression expr) {
    return expr == methodCallExpr && isTestDouble();
  }

  @Override
  public boolean isMatch(Statement stat) {
    ExpressionStatement exprStat = ObjectUtil.asInstance(stat, ExpressionStatement.class);
    if (exprStat == null) return false;
    Expression expr = exprStat.getExpression();
    return expr == binaryExpr || expr == methodCallExpr;
  }

  @Override
  public boolean isMatch(ClosureExpression expr) {
    return expr == closureExpr;
  }

  @Override
  @Nullable
  public ClosureExpression getClosureExpr() {
    return closureExpr;
  }

  @Override
  public void expand() {
    List<Expression> args = new ArrayList<>();
    args.add(inferredName);
    args.add(inferredType);
    args.addAll(AstUtil.getArgumentList(methodCallExpr));

    ArgumentListExpression argsExpr = new ArgumentListExpression(args);
    AstUtil.copySourcePosition(methodCallExpr.getArguments(), argsExpr);
    methodCallExpr.setArguments(argsExpr);
    methodCallExpr.setMethod(new ConstantExpression(methodName + "Impl"));
  }

  public static SpecialMethodCall parse(MethodCallExpression methodCallExpr, @Nullable BinaryExpression binaryExpr,
                                        AstNodeCache nodeCache) {
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
    wrapCastedConstructorArgs(arguments, nodeCache);

    return new SpecialMethodCall(methodName, inferredName, inferredType, methodCallExpr, binaryExpr, closureExpr, conditionBlock);
  }

  private static void wrapCastedConstructorArgs(List<Expression> arguments, AstNodeCache nodeCache) {
    arguments
      .stream()
      .filter(MapExpression.class::isInstance)
      .map(MapExpression.class::cast)
      .flatMap(SpecialMethodCall::entriesWithCastedConstructorArg)
      .forEach(constructorArgsEntry -> wrapCastedConstructorArgs(constructorArgsEntry, nodeCache));
  }

  private static void wrapCastedConstructorArgs(MapEntryExpression constructorArgsEntry, AstNodeCache nodeCache) {
    constructorArgsEntry
      .setValueExpression(new ListExpression(
        // get the constructor args
        ((ListExpression) constructorArgsEntry.getValueExpression())
          .getExpressions()
          .stream()
          .map(constructorArg -> {
            // wrap them in a PojoWrapper if casted to transport
            // the cast type to the constructor selection
            // for example for "null as String"
            return constructorArg instanceof CastExpression
              ? wrapExpression(constructorArg, nodeCache)
              : constructorArg;
          })
          .collect(toList())));
  }

  private static Expression wrapExpression(Expression expression, AstNodeCache nodeCache) {
    ConstructorCallExpression wrapExpression = new ConstructorCallExpression(
      nodeCache.PojoWrapper,
      new ArgumentListExpression(
        expression,
        new ClassExpression(expression.getType())));
    wrapExpression.setSourcePosition(expression);
    return wrapExpression;
  }

  private static Stream<MapEntryExpression> entriesWithCastedConstructorArg(MapExpression map) {
    return map
      .getMapEntryExpressions()
      .stream()
      .filter(SpecialMethodCall::hasCastedConstructorArg);
  }

  private static boolean hasCastedConstructorArg(MapEntryExpression mapEntry) {
    Expression key = mapEntry.getKeyExpression();
    Expression value = mapEntry.getValueExpression();
    return (key instanceof ConstantExpression)
      && ((ConstantExpression) key).getValue().equals("constructorArgs")
      && (value instanceof ListExpression)
      && ((ListExpression) value).getExpressions().stream().anyMatch(CastExpression.class::isInstance);
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
