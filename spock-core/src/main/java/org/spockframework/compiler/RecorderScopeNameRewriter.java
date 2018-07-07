package org.spockframework.compiler;

import org.spockframework.runtime.SpockRuntime;

import java.util.*;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.control.SourceUnit;

/**
 * Rewrites the declaration and usage of $spock_valueRecorder and $spock_errorCollector to be unique in their scope.
 * See https://github.com/spockframework/spock/issues/783
 */
public class RecorderScopeNameRewriter extends ClassCodeVisitorSupport {

  private final AstNodeCache astNodeCache;
  private int valueRecorderIndex = -1;
  private int errorCollectorIndex = -1;

  public RecorderScopeNameRewriter(AstNodeCache astNodeCache) {
    this.astNodeCache = astNodeCache;
  }

  @Override
  public void visitClosureExpression(ClosureExpression expression) {
    int oldValueRecorderIndex = valueRecorderIndex;
    int oldErrorCollectorIndex = errorCollectorIndex;
    super.visitClosureExpression(expression);
    valueRecorderIndex = oldValueRecorderIndex;
    errorCollectorIndex = oldErrorCollectorIndex;
  }

  @Override
  public void visitDeclarationExpression(DeclarationExpression expression) {
    if(expression.isMultipleAssignmentDeclaration()) return;

    VariableExpression variableExpression = expression.getVariableExpression();
    if (isValueRecorderExpression(variableExpression)) {
      valueRecorderIndex++;
      expression.setLeftExpression(valueRecorderExpression());
    } else if (isErrorCollectorExpression(variableExpression)) {
      errorCollectorIndex++;
      expression.setLeftExpression(errorCollectorExpression());
    }
    super.visitDeclarationExpression(expression);
  }

  @Override
  public void visitMethodCallExpression(MethodCallExpression call) {
    Expression objectExpression = call.getObjectExpression();
    if (objectExpression instanceof VariableExpression) {
      rewriteMethodObject(call, (VariableExpression)objectExpression);
    } else if (objectExpression instanceof ClassExpression) {
      rewriteArgumentList(call, (ClassExpression)objectExpression);
    }
    super.visitMethodCallExpression(call);
  }

  private void rewriteMethodObject(MethodCallExpression call, VariableExpression objectExpression) {
    if (isValueRecorderExpression(objectExpression)) {
      call.setObjectExpression(valueRecorderExpression());
    } else if (isErrorCollectorExpression(objectExpression)) {
      call.setObjectExpression(errorCollectorExpression());
    }
  }

  private void rewriteArgumentList(MethodCallExpression call, ClassExpression objectExpression) {
    if (SpockRuntime.class.getName().equals(objectExpression.getType().getName())) {
      String methodName = call.getMethod().getText();
      if ("verifyCondition".equals(methodName)
        || "conditionFailedWithException".equals(methodName)
        || "verifyMethodCondition".equals(methodName)) {
        List<Expression> arguments = new ArrayList<>(((ArgumentListExpression)call.getArguments()).getExpressions());
        Expression expression = arguments.get(0);
        if (expression instanceof VariableExpression && isErrorCollectorExpression(((VariableExpression)expression))) {
          arguments.set(0, errorCollectorExpression());
        }
        expression = arguments.get(1);
        if (expression instanceof VariableExpression && isValueRecorderExpression(((VariableExpression)expression))) {
          arguments.set(1, valueRecorderExpression());
        }
        call.setArguments(new ArgumentListExpression(arguments));
      }
    }
  }

  @Override
  protected SourceUnit getSourceUnit() {
    throw new UnsupportedOperationException("getSourceUnit");
  }

  private VariableExpression errorCollectorExpression() {
    return new VariableExpression(SpockNames.ERROR_COLLECTOR +
      (errorCollectorIndex == 0 ? "" : String.valueOf(errorCollectorIndex)),
      astNodeCache.ErrorCollector);
  }

  private VariableExpression valueRecorderExpression() {
    return new VariableExpression(SpockNames.VALUE_RECORDER +
      (valueRecorderIndex == 0 ? "" : String.valueOf(valueRecorderIndex)),
      astNodeCache.ValueRecorder);
  }

  private boolean isErrorCollectorExpression(VariableExpression variableExpression) {
    return SpockNames.ERROR_COLLECTOR.equals(variableExpression.getName());
  }

  private boolean isValueRecorderExpression(VariableExpression variableExpression) {
    return SpockNames.VALUE_RECORDER.equals(variableExpression.getName());
  }
  // this would be so much cleaner with java8

}
