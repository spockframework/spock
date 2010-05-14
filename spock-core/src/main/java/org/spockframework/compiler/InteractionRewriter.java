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
import org.spockframework.util.Nullable;

import spock.lang.Specification;

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
  private boolean wildcardCall;
  private Expression result;
  private boolean iterableResult;

  // holds the incrementally constructed expression, which looks roughly as follows:
  // "new InteractionBuilder(..).setCount(..).setTarget(..).setMethod(..).addArg(..).setResult(..).build()"
  private Expression builderExpr;

  public InteractionRewriter(IRewriteResourceProvider resourceProvider) {
    this.resourceProvider = resourceProvider;
  }

  /**
   * If the given statement is a valid interaction definition, returns the rewritten statement.
   * If the given statement is not an interaction definition, returns null.
   * If the given statement is an invalid interaction definition, records a compile error
   * and returns null.
   */
  public @Nullable Statement rewrite(ExpressionStatement stat) {
    try {
      if (!parse(stat)) return null;

      createBuilder();
      setCount();
      setCall();
      setResult();
      build();
      return register();
    } catch (InvalidSpecCompileException e) {
      resourceProvider.getErrorReporter().error(e);
      return null;
    }
  }

  private boolean parse(ExpressionStatement stat) throws InvalidSpecCompileException {
    this.stat = stat;

    BinaryExpression binExpr = AstUtil.getExpression(stat, BinaryExpression.class);
    if (binExpr == null) return false;

    int type = binExpr.getOperation().getType();
    if (type == Types.RIGHT_SHIFT || type == Types.RIGHT_SHIFT_UNSIGNED) {
      result = binExpr.getRightExpression();
      iterableResult = type == Types.RIGHT_SHIFT_UNSIGNED;
      return parseCount(binExpr.getLeftExpression());
    }

    return type == Types.MULTIPLY && parseCount(binExpr);
  }

  private boolean parseCount(Expression expr) throws InvalidSpecCompileException {
    BinaryExpression binExpr = AstUtil.asInstance(expr, BinaryExpression.class);
    if (binExpr == null) {
      return parseCall(expr);
    }

    if (binExpr.getOperation().getType() != Types.MULTIPLY) return false;

    count = binExpr.getLeftExpression();
    return parseCall(binExpr.getRightExpression());
  }

  private boolean parseCall(Expression expr) throws InvalidSpecCompileException {
    call = expr;

    if (AstUtil.isWildcardRef(expr)) {
      wildcardCall = true;
      return true;
    }

    if (expr instanceof PropertyExpression) {
      PropertyExpression propExpr = (PropertyExpression)expr;
      if (!Specification._.toString().equals(propExpr.getPropertyAsString()))
        propertySyntaxNotAllowed(expr);
      return true;
    }

    if (expr instanceof MethodCallExpression) {
      MethodCallExpression mcExpr = (MethodCallExpression)expr;
      if (mcExpr.isImplicitThis()) missingTarget(expr);
      if (mcExpr.getObjectExpression() instanceof ClassExpression)
        staticMethodsNotSupported(expr);
      return true;
    }

    if (expr instanceof StaticMethodCallExpression)
      // might be more helpful to issue missingTarget than staticMethodsNotSupported here
      missingTarget(expr);

    return false;
  }

  private void propertySyntaxNotAllowed(Expression expr) throws InvalidSpecCompileException {
    throw new InvalidSpecCompileException(expr,
          "Property syntax is not allowed in interaction definitions. Instead, use getter/setter syntax.");
  }

  private void staticMethodsNotSupported(Expression expr) throws InvalidSpecCompileException {
    throw new InvalidSpecCompileException(expr,
        "Stubbing/mocking of static methods is not supported.");
  }

  private void missingTarget(Expression expr) throws InvalidSpecCompileException {
    throw new InvalidSpecCompileException(expr,
        "Interaction definition is missing the expected target of the interaction");
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

  private void setCall() throws InvalidSpecCompileException {
    if (wildcardCall)
      call(InteractionBuilder.ADD_EQUAL_METHOD_NAME, new ConstantExpression(Specification._.toString()));
    else if (call instanceof PropertyExpression)
      setPropertyCall();
    else
      setMethodCall();
  }

  private void setPropertyCall() throws InvalidSpecCompileException {
    setTarget();
    PropertyExpression propExpr = (PropertyExpression)call;
    Assert.that(Specification._.toString().equals(propExpr.getPropertyAsString())); // already checked in parseCall()
    call(InteractionBuilder.ADD_EQUAL_METHOD_NAME, propExpr.getProperty());
  }

  private void setMethodCall() {
    setTarget();
    setMethodName();
    addArgs();
  }

  private void setTarget() {
    call(InteractionBuilder.ADD_EQUAL_TARGET, AstUtil.getInvocationTarget(call));
  }

  private void setMethodName() {
    Expression methodNameExpr = ((MethodCallExpression)call).getMethod();
    call(chooseMethodNameConstraint(methodNameExpr), methodNameExpr);
  }

  private String chooseMethodNameConstraint(Expression methodNameExpr) {
    if (!(methodNameExpr instanceof ConstantExpression)) // dynamically generated method name
      return InteractionBuilder.ADD_EQUAL_METHOD_NAME;

    String method = (String)((ConstantExpression)methodNameExpr).getValue();
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
                MockController.ADD_INTERACTION,
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
}
