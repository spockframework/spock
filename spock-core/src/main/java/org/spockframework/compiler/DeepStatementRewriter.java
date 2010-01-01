/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.compiler;

import java.util.List;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;

import org.spockframework.compiler.model.*;

/**
 * Walks the statement and expression tree to:
 * - rewrite explicit conditions,
 * - rewrite interactions,
 * - rewrite core language primitives (members of class Specification)
 * - Forbid
 * 
 * Also records whether conditions and interactions were found.
 *
 * @author Peter Niederwieser
 */
public class DeepStatementRewriter extends StatementReplacingVisitorSupport {
  private final IRewriteResourceProvider resourceProvider;

  private boolean conditionFound = false;
  private boolean interactionFound = false;
  // scope for the current closure; null if not in a closure
  private VariableScope closureScope;

  public DeepStatementRewriter(IRewriteResourceProvider resourceProvider) {
    this.resourceProvider = resourceProvider;
  }
  
  public boolean isConditionFound() {
    return conditionFound;
  }

  public boolean isInteractionFound() {
    return interactionFound;
  }

  public void visitBlock(Block block) {
    replaceAll(block.getAst());
  }
  
  @Override
  public void visitAssertStatement(AssertStatement stat) {
    super.visitAssertStatement(stat);
    conditionFound = true;
    replaceVisitedStatementWith(
        ConditionRewriter.rewriteExplicitCondition(stat, resourceProvider));
  }

  @Override
  public void visitExpressionStatement(ExpressionStatement stat) {
    super.visitExpressionStatement(stat);
    if (!AstUtil.isInteraction(stat)) return;

    interactionFound = true;
    replaceVisitedStatementWith(InteractionRewriter.rewrite(stat, resourceProvider));
  }

  @Override
  public void visitClosureExpression(ClosureExpression expr) {
    // because a closure might be executed asynchronously, its conditions
    // and interactions are handled independently from the conditions
    // and interactions of the closure's context

    boolean oldConditionFound = conditionFound;
    boolean oldInteractionFound = interactionFound;
    VariableScope oldClosureScope = closureScope;
    conditionFound = false;
    interactionFound = false;
    closureScope = expr.getVariableScope();

    fixupClosureScope();
    super.visitClosureExpression(expr);
    if (conditionFound) defineValueRecorder(expr);

    conditionFound = oldConditionFound;
    interactionFound = oldInteractionFound;
    closureScope = oldClosureScope;
  }

  private void defineValueRecorder(ClosureExpression expr) {
    resourceProvider.defineValueRecorder(AstUtil.getStatements(expr));
  }

  private void fixupClosureScope() {
    Method method = resourceProvider.getCurrentMethod();
    if (!(method instanceof FeatureMethod)) return;

    // if this is a parameterized feature method w/o explicit parameter list,
    // update any references to parameterization variables
    // (parameterization variables used to be free variables,
    // but have been changed to method parameters by WhereBlockRewriter)
    for (Parameter param : method.getAst().getParameters()) {
      Variable var = closureScope.getReferencedClassVariable(param.getName());
      if (var instanceof DynamicVariable) {
        closureScope.removeReferencedClassVariable(param.getName());
        closureScope.putReferencedLocalVariable(param);
        param.setClosureSharedVariable(true);
      }
    }
  }

  @Override
  public void visitDeclarationExpression(DeclarationExpression expr) {
    visitBinaryExpression(expr);
  }

  @Override
  public void visitBinaryExpression(BinaryExpression expr) {
    if (AstUtil.isBuiltinMemberDecl(expr, Identifiers.MOCK, 0, 1))
      try {
        AstUtil.expandBuiltinMemberDecl(expr, resourceProvider.getMockControllerRef());
      } catch (SpecCompileException e) {
        resourceProvider.getErrorReporter().error(e);
        return;
      }

    // only descend after we have expanded Specification.Mock so that it's not
    // expanded by visit(Static)MethodCallExpression instead
    super.visitBinaryExpression(expr);
  }

  @Override
  public void visitMethodCallExpression(MethodCallExpression expr) {
    super.visitMethodCallExpression(expr);
    forbidUseOfSuperInFixtureMethod(expr);
    handlePredefMockAndPredefOld(expr);
  }

  // Flag the use of super.foo() in fixture method foo,
  // because it is most likely a mistake (user thinks he is overriding
  // the base method and doesn't know that it will be run automatically).
  private void forbidUseOfSuperInFixtureMethod(MethodCallExpression expr) {
    Method currMethod = resourceProvider.getCurrentMethod();
    Expression target = expr.getObjectExpression();
    
    if (currMethod instanceof FixtureMethod
        && target instanceof VariableExpression
        && ((VariableExpression)target).isSuperExpression()
        && currMethod.getName().equals(expr.getMethodAsString())) {
      resourceProvider.getErrorReporter().error(expr,
        "A base class fixture method should not be called explicitely " +
        "because it is always run automatically by the framework");
    }
  }

  private void handlePredefMockAndPredefOld(Expression expr) {
    if (AstUtil.isBuiltinMemberCall(expr, Identifiers.MOCK, 0, 1))
      handlePredefMock(expr);
    else if (AstUtil.isBuiltinMemberCall(expr, Identifiers.OLD, 1, 1))
      handlePredefOld(expr);
  }

  private void handlePredefMock(Expression expr) {
    try {
      AstUtil.expandBuiltinMemberCall(expr, resourceProvider.getMockControllerRef());
    } catch (SpecCompileException e) {
      resourceProvider.getErrorReporter().error(e);
    }
  }

  private void handlePredefOld(Expression expr) {
    if (!(resourceProvider.getCurrentBlock() instanceof ThenBlock)) {
      resourceProvider.getErrorReporter().error(expr, "old() may only be used in a 'then' block");
      return;
    }

    List<Expression> args = AstUtil.getArguments(expr);
    VariableExpression oldValue = resourceProvider.captureOldValue(args.get(0));
    args.set(0, oldValue);
    args.add(ConstantExpression.FALSE); // dummy arg

    if (closureScope != null) {
      oldValue.setClosureSharedVariable(true);
      closureScope.putReferencedLocalVariable(oldValue);
    }
  }
}
