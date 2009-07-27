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

import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.syntax.Types;

import org.spockframework.mock.InteractionBuilder;
import org.spockframework.mock.MockController;
import org.spockframework.util.Assert;
import org.spockframework.util.SyntaxException;

/**
 * Creates the AST representation of an InteractionBuilder build sequence.
 *
 * @author Peter Niederwieser
 */
public class InteractionRewriter {
  private final IRewriteResourceProvider resourceProvider;

  // information about the interaction; filled in by parse()
  private ExpressionStatement stat;
  private Expression count;
  private Expression call;
  private Expression result;
  private boolean iterableResult;

  // holds the incrementally constructed expression, which looks roughly as follows:
  // "new InteractionBuilder(..).setCount(..).setTarget(..).setMethod(..).addArg(..).setResult(..).build()"
  private Expression builderExpr;

  private InteractionRewriter(IRewriteResourceProvider resourceProvider) {
    this.resourceProvider = resourceProvider;
  }

  public static Statement rewrite(ExpressionStatement stat, IRewriteResourceProvider resourceProvider) {
    return new InteractionRewriter(resourceProvider).rewrite(stat);
  }

  private Statement rewrite(ExpressionStatement stat) {
    assert AstUtil.isInteraction(stat);

    parse(stat);
    createBuilder();
    setCount();
    setTarget();
    setMethod();
    addArgs();
    setResult();
    build();
    return register();
  }

  private void parse(ExpressionStatement stat) {
    this.stat = stat;
    Expression expr = stat.getExpression();

    // handle result generators (>> and >>>)
    if (expr instanceof BinaryExpression) {
      BinaryExpression binExpr = (BinaryExpression)expr;
      int type = binExpr.getOperation().getType();
      if (type == Types.RIGHT_SHIFT || type == Types.RIGHT_SHIFT_UNSIGNED) {
        expr = binExpr.getLeftExpression();
        result = binExpr.getRightExpression();
        iterableResult = type == Types.RIGHT_SHIFT_UNSIGNED;
      }
    }

    if (expr instanceof BinaryExpression) { // explicit invocation count
      BinaryExpression binExpr = (BinaryExpression)expr;
      boolean leftIsInvocation = isPotentialMockInvocation(binExpr.getLeftExpression());
      boolean rightIsInvocation = isPotentialMockInvocation(binExpr.getRightExpression());
      if (leftIsInvocation && rightIsInvocation)
        throw new SyntaxException(binExpr,
"Ambiguous interaction definition: cannot tell count from call. Help me by introducing a variable for count.");
      if (leftIsInvocation) {
        expr = binExpr.getLeftExpression();
        count = binExpr.getRightExpression();
      } else if (rightIsInvocation) {
        expr = binExpr.getRightExpression();
        count = binExpr.getLeftExpression();
      } else throw new SyntaxException(binExpr,
"* indicates an interaction definition, but neither the left nor the right side looks like a method call to me");
    }

    assert result != null || count != null;

    call = expr;
  }

  private void createBuilder() {
    // ExpressionStatements w/ label have wrong source position,
    // but source position of the contained expression is OK
    Expression expr = stat.getExpression();

    builderExpr = new ConstructorCallExpression(
        resourceProvider.getAstNodeCache().InteractionBuilder,
        new ArgumentListExpression(
            Arrays.asList(
                new ConstantExpression(expr.getLineNumber()),
                new ConstantExpression(expr.getColumnNumber()),
                new ConstantExpression(resourceProvider.getSourceText(expr)))));
  }

  private void setCount() {
    if (count == null) return;

    if (count instanceof RangeExpression) {
      RangeExpression range = (RangeExpression)count;
      call(InteractionBuilder.SET_RANGE_COUNT,
          range.getFrom(),
          range.getTo(),
          new ConstantExpression(range.isInclusive()));

      return;
    }

    call(InteractionBuilder.SET_FIXED_COUNT, count);
  }

  private void setTarget() {
    // syntax already checked earlier
    call(InteractionBuilder.ADD_EQUAL_TARGET, AstUtil.getInvocationTarget(call));
  }

  private void setMethod() {
    if (call instanceof PropertyExpression) return; // must be _ (checked earlier)

    Expression methodExpr = ((MethodCallExpression)call).getMethod();
    call(chooseMethodMatcher(methodExpr), methodExpr);
  }

  private String chooseMethodMatcher(Expression methodExpr) {
    if (!(methodExpr instanceof ConstantExpression)) // dynamically generated method name
      return InteractionBuilder.ADD_EQUAL_METHOD_NAME;

    String method = (String)((ConstantExpression)methodExpr).getValue();
    // NOTE: we cannot tell from the AST if a method name is defined using
    // slashy string syntax ("/somePattern/"); hence, we consider any name
    // that isn't a valid Java identifier a pattern. While this isn't entirely
    // safe (Groovy allows almost all characters in method names), it should
    // work out well in practice because it's very unlikely that a
    // collaborator has a method name that is not a valid Java identifier.
    return AstUtil.isJavaIdentifier(method) ?
        InteractionBuilder.ADD_EQUAL_METHOD_NAME :
        InteractionBuilder.ADD_REGEX_METHOD_NAME;
  }

  private void addArgs() {
    if (call instanceof PropertyExpression) return;

    Expression args = ((MethodCallExpression)call).getArguments();
    if (args == ArgumentListExpression.EMPTY_ARGUMENTS) return; // fast lane
    
    call(InteractionBuilder.SET_ARG_LIST_KIND,
        new ConstantExpression(args instanceof ArgumentListExpression));
    
    if (args instanceof ArgumentListExpression)
      addPositionalArgs((ArgumentListExpression)args);
    else if (args instanceof NamedArgumentListExpression)
      addNamedArgs((NamedArgumentListExpression)args);
    else Assert.that(false, "unknown kind of argument list: " + args);
  }

  @SuppressWarnings("unchecked")
  private void addPositionalArgs(ArgumentListExpression args) {
    for (Expression arg: (List<Expression>)args.getExpressions())
      addArg(arg);
  }

  @SuppressWarnings("unchecked")
  private void addNamedArgs(NamedArgumentListExpression args) {
    for (MapEntryExpression arg : (List<MapEntryExpression>)args.getMapEntryExpressions()) {
      addName(arg.getKeyExpression());
      addArg(arg.getValueExpression());
    }
  }

  private void addName(Expression name) {
    call(InteractionBuilder.ADD_ARG_NAME, name);
  }

  private void addArg(Expression arg) {
    if (arg instanceof NotExpression) {
      NotExpression not = (NotExpression)arg;
      addArg(not.getExpression());
      call(InteractionBuilder.NEGATE_LAST_ARG);
      return;
    }

    if (arg instanceof CastExpression) {
      CastExpression cast = (CastExpression)arg;
      addArg(cast.getExpression());
      call(InteractionBuilder.TYPE_LAST_ARG, new ClassExpression(cast.getType()));
      return;
    }

    if (arg instanceof ClosureExpression) {
      call(InteractionBuilder.ADD_CODE_ARG, arg);
      return;
    }

    call(InteractionBuilder.ADD_EQUAL_ARG, arg);
  }

  private void setResult() {
    if (result == null) return;

    if (iterableResult) {
      call(InteractionBuilder.SET_ITERABLE_RESULT, result);
      return;
    }

    if (result instanceof ClosureExpression) {
      call(InteractionBuilder.SET_CODE_RESULT, result);
      return;
    }

    call(InteractionBuilder.SET_CONSTANT_RESULT, result);
  }

  private void build() {
    call(InteractionBuilder.BUILD);
  }

  private Statement register() {
    Statement result =
        new ExpressionStatement(
            new MethodCallExpression(
                resourceProvider.getMockControllerRef(),
                MockController.ADD,
                new ArgumentListExpression(builderExpr)));

    result.setSourcePosition(stat);
    return result;
  }

  private void call(String method, Expression... args) {
    builderExpr = new MethodCallExpression(
        builderExpr,
        method,
        new ArgumentListExpression(args));
  }

  private static boolean isPotentialMockInvocation(Expression expr) {
    if (expr instanceof PropertyExpression) {
      PropertyExpression propExpr = (PropertyExpression)expr;
      return !propExpr.isImplicitThis()
          && !(propExpr.getObjectExpression() instanceof ClassExpression);
    }
    if (expr instanceof MethodCallExpression) {
      MethodCallExpression mcExpr = (MethodCallExpression)expr;
      return !mcExpr.isImplicitThis()
          && !(mcExpr.getObjectExpression() instanceof ClassExpression);
    }
    return false;
  }
}
