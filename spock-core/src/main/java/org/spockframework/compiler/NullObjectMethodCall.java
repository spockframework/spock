package org.spockframework.compiler;

import java.util.Collection;

import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.Statement;

public class NullObjectMethodCall implements IBuiltInMethodCall {
  public static final IBuiltInMethodCall INSTANCE = new NullObjectMethodCall();

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
