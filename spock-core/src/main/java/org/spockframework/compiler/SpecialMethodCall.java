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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

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
  private final AstNodeCache nodeCache;

  public SpecialMethodCall(String methodName, AstNodeCache nodeCache) {
    this(methodName, null, null, null, null, null, false, nodeCache);
  }

  public SpecialMethodCall(String methodName, Expression inferredName, Expression inferredType,
      MethodCallExpression methodCallExpr, @Nullable BinaryExpression binaryExpr,
      @Nullable ClosureExpression closureExpr, boolean conditionBlock, AstNodeCache nodeCache) {
    this.methodName = methodName;
    this.inferredName = inferredName;
    this.inferredType = inferredType;
    this.binaryExpr = binaryExpr;
    this.methodCallExpr = methodCallExpr;
    this.closureExpr = closureExpr;
    this.conditionBlock = conditionBlock;
    this.nodeCache = nodeCache;
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
  public boolean isConditionMethodCall() {
    return isMethodName(Identifiers.WITH) || isMethodName(Identifiers.VERIFY_EACH);
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
  public boolean isConditionMethodCall(MethodCallExpression expr) {
    return expr == methodCallExpr && isConditionMethodCall();
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
    args.add(VariableExpression.THIS_EXPRESSION);
    args.add(inferredName);
    args.add(inferredType);
    args.addAll(AstUtil.getArgumentList(methodCallExpr));

    ArgumentListExpression argsExpr = new ArgumentListExpression(args);
    AstUtil.copySourcePosition(methodCallExpr.getArguments(), argsExpr);
    methodCallExpr.setArguments(argsExpr);
    methodCallExpr.setObjectExpression(new ClassExpression(nodeCache.SpecInternals));
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

    return new SpecialMethodCall(methodName, inferredName, inferredType, methodCallExpr, binaryExpr, closureExpr, conditionBlock, nodeCache);
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

  static boolean checkIsConditionMethodCall(MethodCallExpression expr) {
    if (!AstUtil.isThisOrSuperExpression(expr.getObjectExpression())) return false;
    return Identifiers.CONDITION_METHODS.contains(expr.getMethodAsString());
  }

  private static boolean checkIsBuiltInMethodCall(MethodCallExpression expr) {
    if (!AstUtil.isThisOrSuperExpression(expr.getObjectExpression())) return false;
    return Identifiers.BUILT_IN_METHODS.contains(expr.getMethodAsString());
  }

  private static boolean checkIsConditionBlock(MethodCallExpression methodCallExpr) {
    ClassNode targetType = methodCallExpr.getObjectExpression().getType();
    String methodName = methodCallExpr.getMethodAsString();

    try {
      // if targetType has any method with a parameter or return type that is not in the
      // compile classpath this call will fail with a NoClassDefFoundError
      List<MethodNode> methods = targetType.getMethods(methodName);
      for (MethodNode method : methods) {
        for (AnnotationNode annotation : method.getAnnotations()) {
          if (annotation.getClassNode().getName().equals(ConditionBlock.class.getName())) return true;
        }
      }
    } catch (NoClassDefFoundError e) {
      // just assume there is no condition block and return false
    }

    return false;
  }
}
