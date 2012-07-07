package org.spockframework.compiler;

import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.Collection;

public interface IBuiltInMethodCall {
  boolean isMethodName(String name);

  boolean isOneOfMethodNames(Collection<String> names);

  boolean isExceptionCondition();

  boolean isThrownCall();

  boolean isOldCall();

  boolean isInteractionCall();

  boolean isWithCall();

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
