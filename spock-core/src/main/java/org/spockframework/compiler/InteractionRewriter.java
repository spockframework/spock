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

import org.spockframework.lang.Wildcard;
import org.spockframework.mock.runtime.*;
import org.spockframework.util.*;

import java.util.*;

import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.syntax.Types;

/**
 * Creates the AST representation of an InteractionBuilder build sequence.
 *
 * @author Peter Niederwieser
 */
public class InteractionRewriter {
  private final IRewriteResources resources;
  private final ClosureExpression activeWithOrMockClosure;

  // information about the interaction; filled in by parse()
  private ExpressionStatement stat;
  private Expression count;
  private Expression call;
  private boolean wildcardCall;
  private boolean implicitTarget;
  private List<InteractionResponse> responses = new ArrayList<>();
  private Boolean scanResult;

  // holds the incrementally constructed expression, which looks roughly as follows:
  // "new InteractionBuilder(..).setCount(..).setTarget(..).setMethod(..).addArg(..).addResult(..).build()"
  private Expression builderExpr;

  public InteractionRewriter(IRewriteResources resources, @Nullable ClosureExpression activeWithOrMockClosure) {
    this.resources = resources;
    this.activeWithOrMockClosure = activeWithOrMockClosure;
  }

  /**
   * If the given statement is a valid interaction definition, returns the rewritten statement.
   * If the given statement is not an interaction definition, returns null.
   * If the given statement is an invalid interaction definition, records a compile error
   * and returns null.
   */
  @Nullable
  public ExpressionStatement rewrite(ExpressionStatement stat) {
    try {
      if (!isInteraction(stat)) return null;

      createBuilder();
      setCount();
      setCall();
      addResponses();
      build();
      return register();
    } catch (InvalidSpecCompileException e) {
      resources.getErrorReporter().error(e);
      return null;
    }
  }

  public boolean isInteraction(ExpressionStatement stat) throws InvalidSpecCompileException {
    if (scanResult != null) {
      if (stat != this.stat) {
        throw new InvalidSpecCompileException(stat, "InteractionRewriter was reused");
      }
      return scanResult;
    }

    this.stat = stat;

    Expression expr = parseCount(parseResults(stat.getExpression()));
    boolean interaction = (count != null || !responses.isEmpty()) && parseCall(expr);
    if (interaction && resources.getCurrentMethod().getAst().isStatic()) {
      throw new InvalidSpecCompileException(stat, "Interactions cannot be declared in static scope");
    }

    return scanResult = interaction;
  }

  private Expression parseResults(Expression expr) {
    while (expr instanceof BinaryExpression) {
      BinaryExpression binExpr = (BinaryExpression) expr;
      int type = binExpr.getOperation().getType();
      if (type != Types.RIGHT_SHIFT && type != Types.RIGHT_SHIFT_UNSIGNED) break;
      responses.add(new InteractionResponse(binExpr.getRightExpression(), type == Types.RIGHT_SHIFT_UNSIGNED));
      expr = binExpr.getLeftExpression();
    }
    return expr;
  }

  private Expression parseCount(Expression expr) {
    BinaryExpression binExpr = ObjectUtil.asInstance(expr, BinaryExpression.class);
    if (binExpr == null || binExpr.getOperation().getType() != Types.MULTIPLY) return expr;
    count = binExpr.getLeftExpression();
    return binExpr.getRightExpression();
  }

  private boolean parseCall(Expression expr) throws InvalidSpecCompileException {
    call = expr;

    if (AstUtil.isWildcardRef(expr)) {
      wildcardCall = true;
      implicitTarget = activeWithOrMockClosure != null;
      return true;
    }

    if (expr instanceof PropertyExpression
        || expr instanceof MethodCallExpression
        || expr instanceof ConstructorCallExpression) {
      if (AstUtil.isInvocationWithImplicitThis(expr)) {
        if (activeWithOrMockClosure == null || !AstUtil.hasImplicitParameter(activeWithOrMockClosure)) {
          throw new InvalidSpecCompileException(call, "Interaction is missing a target");
        }
        implicitTarget = true;
      }

      return true;
    }

    // StaticMethodCallExpression is only used for unqualified calls to static methods
    if (expr instanceof StaticMethodCallExpression) return false;

    return false;
  }

  private void createBuilder() {
    // ExpressionStatements w/ label have wrong source position,
    // but source position of the contained expression is OK
    Expression expr = stat.getExpression();

    builderExpr = new ConstructorCallExpression(
        resources.getAstNodeCache().InteractionBuilder,
        new ArgumentListExpression(
            Arrays.<Expression> asList(
                new ConstantExpression(expr.getLineNumber()),
                new ConstantExpression(expr.getColumnNumber()),
                new ConstantExpression(resources.getSourceText(expr)))));
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

  private void setCall() {
    if (wildcardCall) {
      if (implicitTarget) {
        call(InteractionBuilder.ADD_EQUAL_TARGET, AstUtil.getImplicitParameterRef(activeWithOrMockClosure));
      } else {
        // turn _ into _._
        // gives better better behavior for stub with required interaction
        call(InteractionBuilder.ADD_WILDCARD_TARGET);
      }
      // not same as leaving it out - doesn't match Object.toString() etc.
      call(InteractionBuilder.ADD_EQUAL_METHOD_NAME, new ConstantExpression(Wildcard.INSTANCE.toString()));
    } else if (call instanceof PropertyExpression) {
      setPropertyCall();
    } else if (call instanceof MethodCallExpression) {
      setMethodCall();
    } else if (call instanceof ConstructorCallExpression) {
      setConstructorCall();
    } else {
      throw new UnreachableCodeError();
    }
  }

  private void setPropertyCall() {
    setTarget();
    setPropertyName();
  }

  private void setPropertyName() {
    Expression propertyNameExpr = ((PropertyExpression) call).getProperty();
    String constraint = selectNameConstraint(propertyNameExpr,
        InteractionBuilder.ADD_EQUAL_PROPERTY_NAME, InteractionBuilder.ADD_REGEX_PROPERTY_NAME);
    call(constraint, propertyNameExpr);
  }

  private void setMethodCall() {
    setTarget();
    setMethodName();
    addArgs();
  }

  private void setConstructorCall() {
    setTarget();
    call(InteractionBuilder.ADD_EQUAL_METHOD_NAME, new ConstantExpression("<init>"));
    addArgs();
  }

  private void setTarget()  {
    if (implicitTarget) {
      call(InteractionBuilder.ADD_EQUAL_TARGET, AstUtil.getImplicitParameterRef(activeWithOrMockClosure));
    } else {
      call(InteractionBuilder.ADD_EQUAL_TARGET, AstUtil.getInvocationTarget(call));
    }
  }

  private void setMethodName() {
    Expression methodNameExpr = ((MethodCallExpression) call).getMethod();
    String constraint = selectNameConstraint(methodNameExpr,
        InteractionBuilder.ADD_EQUAL_METHOD_NAME, InteractionBuilder.ADD_REGEX_METHOD_NAME);
    call(constraint, methodNameExpr);
  }

  private String selectNameConstraint(Expression nameExpr, String constraint1, String constraint2) {
    if (!(nameExpr instanceof ConstantExpression)) // dynamically generated name
      return constraint1;

    String method = (String)((ConstantExpression) nameExpr).getValue();
    // NOTE: we cannot tell from the AST if a method (or property) name is defined using
    // slashy string syntax ("/somePattern/"); hence, we consider any name
    // that isn't a valid Java identifier a pattern. While this isn't entirely
    // safe (the JVM allows almost all characters in method names), it should
    // work out well in practice because it's very unlikely that a
    // collaborator has a method name that is not a valid Java identifier.
    return AstUtil.isJavaIdentifier(method) ? constraint1 : constraint2;
  }

  private void addArgs() {
    if (call instanceof PropertyExpression) return;

    Expression args = AstUtil.getArguments(call);
    if (args == ArgumentListExpression.EMPTY_ARGUMENTS) return; // fast lane

    if (args instanceof ArgumentListExpression)
      addPositionalArgs((ArgumentListExpression) args);
    else if (args instanceof NamedArgumentListExpression)
      addNamedArgs((NamedArgumentListExpression) args, false);
    else if (args instanceof TupleExpression
        && ((TupleExpression)args).getExpression(0) instanceof NamedArgumentListExpression)
      //for some reason groovy wraps NamedArgumentListExpression into a TupleExpression
      addNamedArgs((NamedArgumentListExpression) ((TupleExpression)args).getExpression(0), false);
    else Assert.that(false, "unknown kind of argument list: " + args);
  }

  @SuppressWarnings("unchecked")
  private void addPositionalArgs(ArgumentListExpression args) {
    List<Expression> expressions = args.getExpressions();

    if (expressions.size() > 0 && expressions.get(0) instanceof MapExpression) {
      boolean isMixed = expressions.size() > 1;
      addNamedArgs((MapExpression) expressions.get(0), isMixed);
      if (isMixed) {
        addPositionalListArgs(expressions.subList(1, expressions.size()), true);
      }
    } else {
      addPositionalListArgs(expressions, false);
    }
  }

  private void addPositionalListArgs(List<Expression> expressions, boolean isMixed) {
    usePositionalArgs(isMixed);
    for (Expression arg : expressions) {
      addArg(arg);
    }
  }

  @SuppressWarnings("unchecked")
  private void addNamedArgs(MapExpression args, boolean isMixed) {
    useNamedArgs(isMixed);
    for (MapEntryExpression arg : args.getMapEntryExpressions()) {
      addName(arg.getKeyExpression());
      addArg(arg.getValueExpression());
    }
  }

  private void useNamedArgs(boolean isMixed) {
    call(InteractionBuilder.SET_ARG_LIST_KIND, ConstantExpression.PRIM_FALSE, new ConstantExpression(isMixed,true));
  }

  private void usePositionalArgs(boolean isMixed) {
    call(InteractionBuilder.SET_ARG_LIST_KIND, ConstantExpression.PRIM_TRUE, new ConstantExpression(isMixed, true));
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

  private void addResponses() {
    for (InteractionResponse response : responses) {
      if (response.iterable) {
        call(InteractionBuilder.ADD_ITERABLE_RESPONSE, response.expr);
      } else if (response.expr instanceof ClosureExpression) {
        call(InteractionBuilder.ADD_CODE_RESPONSE, response.expr);
      } else {
        call(InteractionBuilder.ADD_CONSTANT_RESPONSE, response.expr);
      }
    }
  }

  private void build() {
    call(InteractionBuilder.BUILD);
  }

  private ExpressionStatement register() {
    ExpressionStatement result =
        new ExpressionStatement(
            new MethodCallExpression(
                resources.getMockInvocationMatcher(),
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

  private static class InteractionResponse {
    final Expression expr;
    final boolean iterable;

    private InteractionResponse(Expression expr, boolean iterable) {
      this.expr = expr;
      this.iterable = iterable;
    }
  }
}
