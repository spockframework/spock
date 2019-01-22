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

import org.spockframework.compiler.model.*;
import org.spockframework.util.*;

import java.util.*;

import groovy.lang.Closure;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.syntax.Types;

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
public class DeepBlockRewriter extends AbstractDeepBlockRewriter {
  private final IRewriteResources resources;
  private boolean insideInteraction = false;

  public DeepBlockRewriter(IRewriteResources resources) {
    super(resources.getCurrentBlock());
    this.resources = resources;
  }

  @Override
  public void visit(Block block) {
    super.visit(block);
  }

  @Override
  public void visitAssertStatement(AssertStatement stat) {
    super.visitAssertStatement(stat);
    conditionFound = true;
    replaceVisitedStatementWith(
        ConditionRewriter.rewriteExplicitCondition(stat, resources));
  }

  @Override
  protected void doVisitExpressionStatement(ExpressionStatement stat) {
    InteractionRewriter rewriter = visitInteractionAwareExpressionStatement(stat);

    boolean handled = (stat == lastSpecialMethodCallStat && !(currSpecialMethodCall.isWithCall() || currSpecialMethodCall.isGroupConditionBlock())) // don't process further
        || handleInteraction(rewriter, stat)
        || handleImplicitCondition(stat);
  }

  private InteractionRewriter visitInteractionAwareExpressionStatement(ExpressionStatement stat) {
    InteractionRewriter rewriter = new InteractionRewriter(resources, getCurrentWithOrMockClosure());
    if (isInteractionExpression(rewriter, stat)) {
      insideInteraction = true;
      super.doVisitExpressionStatement(stat);
      insideInteraction = false;
    } else {
      super.doVisitExpressionStatement(stat);
    }
    return rewriter;
  }

  @Override
  protected void doVisitBinaryExpression(BinaryExpression expression) {
    if(insideInteraction) {
      /* We are inside an interaction expression, e.g., mock.foo({it > 0}) >> { result }
         We don't want the closures to the right of the right shift to be treated
         as condition closures, whereas the method argument closures should be treated
         as conditions.
       */
      Expression expr = expression;
      insideInteraction = false;
      boolean found = false;
      while (expr instanceof BinaryExpression) {
        BinaryExpression binExpr = (BinaryExpression) expr;
        int type = binExpr.getOperation().getType();
        if (type != Types.RIGHT_SHIFT && type != Types.RIGHT_SHIFT_UNSIGNED) break;
        found = true;
        binExpr.getRightExpression().visit(this);
        expr = binExpr.getLeftExpression();
      }
      insideInteraction = true;
      if (found) {
        expr.visit(this);
      } else {
        super.doVisitBinaryExpression(expression);
      }
    } else {
      super.doVisitBinaryExpression(expression);
    }
  }

  @Override
  protected void doVisitClosureExpression(ClosureExpression expr) {
    if (resources.getCurrentMethod() instanceof FeatureMethod) {
      AstUtil.fixUpLocalVariables(resources.getCurrentMethod().getAst().getParameters(), expr.getVariableScope(), true);
    }
    super.doVisitClosureExpression(expr);
    if (conditionFound || groupConditionFound) defineRecorders(expr, groupConditionFound);
  }

  @Override
  public void visitBlockStatement(BlockStatement stat) {
    super.visitBlockStatement(stat);
    if (resources.getCurrentMethod() instanceof FeatureMethod) {
      AstUtil.fixUpLocalVariables(resources.getCurrentMethod().getAst().getParameters(), stat.getVariableScope(), false);
    }
  }

  @Override
  public void visitDeclarationExpression(DeclarationExpression expr) {
    visitBinaryExpression(expr);
  }

  @Override
  protected void doVisitMethodCallExpression(MethodCallExpression expr) {
    super.doVisitMethodCallExpression(expr);

    boolean handled = handleMockCall(expr)
        || handleThrownCall(expr)
        || handleOldCall(expr)
        || handleInteractionBlockCall(expr)
        || handleImplicitCallOnMethodParam(expr)
        || forbidUseOfSuperInFixtureMethod(expr);
  }

  private boolean handleImplicitCallOnMethodParam(MethodCallExpression expr) {
    if (!expr.isImplicitThis()) return false;

    String methodName = expr.getMethodAsString();
    List<Parameter> params = Arrays.asList(resources.getCurrentMethod().getAst().getParameters());

    for (Parameter param : params) {
      if (param.getName().equals(methodName)) {
        expr.setMethod(new ConstantExpression("call"));
        expr.setObjectExpression(new VariableExpression(methodName));
        return true;
      }
    }

    return false;
  }

  private boolean handleInteraction(InteractionRewriter rewriter, ExpressionStatement stat) {
    ExpressionStatement interaction = rewriter.rewrite(stat);
    if (interaction == null) return false;

    // would also want to enforce this for when-blocks, but unfortunately it's not that uncommon
    // for projects to have interactions in when-blocks (Gradle, Tapestry). Before we enforce this,
    // we should at least support multiple setup-blocks.
    if (block instanceof ExpectBlock) {
      resources.getErrorReporter().error(stat, "Interactions are not allowed in '%s' blocks. " +
          "Put them before the '%s' block or into a 'then' block.", block.getName(), block.getName());
      return true;
    }

    replaceVisitedStatementWith(interaction);
    interactionFound = true;
    return true;
  }

  private boolean handleImplicitCondition(ExpressionStatement stat) {
    if (!(stat == currTopLevelStat && isThenOrExpectBlock()
        || currSpecialMethodCall.isWithCall()
        || currSpecialMethodCall.isConditionBlock()
        || currSpecialMethodCall.isGroupConditionBlock()
        || insideInteraction)) {
      return false;
    }
    if (!isImplicitCondition(stat)) return false;

    checkIsValidImplicitCondition(stat);
    conditionFound = true;
    groupConditionFound = currSpecialMethodCall.isGroupConditionBlock();

    if ((currSpecialMethodCall.isWithCall() || currSpecialMethodCall.isGroupConditionBlock())
      && AstUtil.isInvocationWithImplicitThis(stat.getExpression())
      && !(Identifiers.WITH.equals(AstUtil.getMethodName(stat.getExpression()))
            || Identifiers.VERIFY_ALL.equals(AstUtil.getMethodName(stat.getExpression())))) {
      replaceObjectExpressionWithCurrentClosure(stat);
    }

    Statement condition = ConditionRewriter.rewriteImplicitCondition(stat, resources);
    replaceVisitedStatementWith(condition);
    return true;
  }

  private void replaceObjectExpressionWithCurrentClosure(ExpressionStatement stat) {
    MethodCallExpression methodCall = AstUtil.getExpression(stat, MethodCallExpression.class);
    if (methodCall == null) return;

    MethodCallExpression target = referenceToCurrentClosure();
    methodCall.setObjectExpression(target);
  }

  private MethodCallExpression referenceToCurrentClosure() {
    return new MethodCallExpression(
      new VariableExpression("this"),
      new ConstantExpression("each"),
      new ArgumentListExpression(
        new PropertyExpression(
          new ClassExpression(ClassHelper.makeWithoutCaching(Closure.class)),
          new ConstantExpression("IDENTITY")
        )
      )
    );
  }

  private boolean handleMockCall(MethodCallExpression expr) {
    if (!currSpecialMethodCall.isTestDouble(expr)) return false;

    if (resources.getCurrentMethod().getAst().isStatic()) {
      resources.getErrorReporter().error(expr, "Mocks cannot be created in static scope");
      // expand nevertheless so that inner scope (if any) won't trip over this again
    }

    currSpecialMethodCall.expand();
    return true;
  }

  private boolean handleThrownCall(MethodCallExpression expr) {
    if (!currSpecialMethodCall.isExceptionCondition(expr)) return false;

    if (!(block instanceof ThenBlock)) {
      resources.getErrorReporter().error(expr, "Exception conditions are only allowed in 'then' blocks");
      return true;
    }
    if (isExceptionConditionFound()) {
      resources.getErrorReporter().error(expr, "Only one exception condition is allowed per 'then' block");
      return true;
    }
    if (!currSpecialMethodCall.isMatch(currTopLevelStat)) {
      resources.getErrorReporter().error(expr, "Exception conditions are only allowed as top-level statements");
      return true;
    }

    foundExceptionCondition = expr;
    if (currSpecialMethodCall.isThrownCall()) {
      currSpecialMethodCall.expand();
    }
    return true;
  }

  private boolean handleOldCall(MethodCallExpression expr) {
    if (!currSpecialMethodCall.isOldCall(expr)) return false;

    if (!(block instanceof ThenBlock)) {
      resources.getErrorReporter().error(expr, "old() is only allowed in 'then' blocks");
      return true;
    }

    expr.setMethod(new ConstantExpression(expr.getMethodAsString() + "Impl"));
    List<Expression> args = AstUtil.getArgumentList(expr);
    VariableExpression oldValue = resources.captureOldValue(args.get(0));
    args.set(0, oldValue);

    if (currClosure != null) {
      oldValue.setClosureSharedVariable(true);
      currClosure.getVariableScope().putReferencedLocalVariable(oldValue);
    }

    return true;
  }

  private boolean handleInteractionBlockCall(MethodCallExpression expr) {
    if (!currSpecialMethodCall.isInteractionCall(expr)) return false;

    interactionFound = true;
    return true;
  }

  private void defineRecorders(ClosureExpression expr, boolean enableErrorCollector) {
    resources.defineRecorders(AstUtil.getStatements(expr), enableErrorCollector);
  }

  // Forbid the use of super.foo() in fixture method foo,
  // because it is most likely a mistake (user thinks he is overriding
  // the base method and doesn't know that it will be run automatically)
  private boolean forbidUseOfSuperInFixtureMethod(MethodCallExpression expr) {
    Method currMethod = resources.getCurrentMethod();
    Expression target = expr.getObjectExpression();

    if (currMethod instanceof FixtureMethod
        && target instanceof VariableExpression
        && ((VariableExpression)target).isSuperExpression()
        && currMethod.getName().equals(expr.getMethodAsString())) {
      resources.getErrorReporter().error(expr,
          "A base class fixture method should not be called explicitly " +
              "because it is always invoked automatically by the framework");
      return true;
    }

    return false;
  }

  @Nullable
  private ClosureExpression getCurrentWithOrMockClosure() {
    if (currSpecialMethodCall.isWithCall() || currSpecialMethodCall.isTestDouble()) {
      return currSpecialMethodCall.getClosureExpr();
    }
    return null;
  }

  private boolean isThenOrExpectBlock() {
    return (block instanceof ThenBlock || block instanceof ExpectBlock);
  }

  private boolean isInteractionExpression(InteractionRewriter rewriter, ExpressionStatement stat) {
    try {
      return rewriter.isInteraction(stat);
    } catch (InvalidSpecCompileException e) {
      return false;
    }
  }

  // assumption: not already an interaction
  public static boolean isImplicitCondition(Statement stat) {
    return stat instanceof ExpressionStatement
        && !(((ExpressionStatement) stat).getExpression() instanceof DeclarationExpression);
  }

  private void checkIsValidImplicitCondition(Statement stat) {
    BinaryExpression binExpr = AstUtil.getExpression(stat, BinaryExpression.class);
    if (binExpr == null) return;

    if (Types.ofType(binExpr.getOperation().getType(), Types.ASSIGNMENT_OPERATOR)) {
      resources.getErrorReporter().error(stat, "Expected a condition, but found an assignment. Did you intend to write '==' ?");
    }
  }
}
