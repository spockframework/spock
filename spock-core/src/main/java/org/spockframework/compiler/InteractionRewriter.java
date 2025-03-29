/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.compiler;

import org.spockframework.lang.Wildcard;
import org.spockframework.util.*;

import java.util.*;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.syntax.Types;

import static java.util.Arrays.asList;
import static org.spockframework.compiler.AstUtil.createDirectMethodCall;
import static org.spockframework.compiler.AstUtil.primitiveConstExpression;

/**
 * Creates the AST representation of an InteractionBuilder build sequence.
 *
 * @author Peter Niederwieser
 */
public class InteractionRewriter {
  private final ISpecRewriteResources resources;
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

  public InteractionRewriter(ISpecRewriteResources resources, @Nullable ClosureExpression activeWithOrMockClosure) {
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

    if (expr instanceof VariableExpression
      && ((VariableExpression) expr).getAccessedVariable() instanceof DynamicVariable) {

      if (activeWithOrMockClosure == null || !AstUtil.hasImplicitParameter(activeWithOrMockClosure)) {
        throw new InvalidSpecCompileException(call, "Interaction is missing a target");
      }
      call = new PropertyExpression(new VariableExpression(new DynamicVariable("it", false)), ((VariableExpression) expr).getName());
      call.setSourcePosition(expr);
      implicitTarget = true;

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
            asList(
              primitiveConstExpression(expr.getLineNumber()),
              primitiveConstExpression(expr.getColumnNumber()),
              new ConstantExpression(resources.getSourceText(expr)))));
  }

  private void setCount() {
    if (count == null) return;

    if (count instanceof RangeExpression) {
      RangeExpression range = (RangeExpression)count;
      call(resources.getAstNodeCache().InteractionBuilder_SetRangeCount,
        range.getFrom(),
        range.getTo(),
        primitiveConstExpression(range.isInclusive()));

      return;
    }

    call(resources.getAstNodeCache().InteractionBuilder_SetFixedCount, count);
  }

  private void setCall() {
    if (wildcardCall) {
      if (implicitTarget) {
        call(resources.getAstNodeCache().InteractionBuilder_AddEqualTarget, AstUtil.getImplicitParameterRef(activeWithOrMockClosure));
      } else {
        // turn _ into _._
        // gives better better behavior for stub with required interaction
        call(resources.getAstNodeCache().InteractionBuilder_AddWildcardTarget);
      }
      // not same as leaving it out - doesn't match Object.toString() etc.
      call(resources.getAstNodeCache().InteractionBuilder_AddEqualMethodName, new ConstantExpression(Wildcard.INSTANCE.toString()));
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
    MethodNode constraint = selectNameConstraint(propertyNameExpr,
      resources.getAstNodeCache().InteractionBuilder_AddEqualPropertyName, resources.getAstNodeCache().InteractionBuilder_AddRegexPropertyName);
    call(constraint, propertyNameExpr);
  }

  private void setMethodCall() {
    setTarget();
    setMethodName();
    addArgs();
  }

  private void setConstructorCall() {
    setTarget();
    call(resources.getAstNodeCache().InteractionBuilder_AddEqualMethodName, new ConstantExpression("<init>"));
    addArgs();
  }

  private void setTarget()  {
    if (implicitTarget) {
      call(resources.getAstNodeCache().InteractionBuilder_AddEqualTarget, AstUtil.getImplicitParameterRef(activeWithOrMockClosure));
    } else {
      call(resources.getAstNodeCache().InteractionBuilder_AddEqualTarget, AstUtil.getInvocationTarget(call));
    }
  }

  private void setMethodName() {
    Expression methodNameExpr = ((MethodCallExpression) call).getMethod();
    MethodNode constraint = selectNameConstraint(methodNameExpr,
      resources.getAstNodeCache().InteractionBuilder_AddEqualMethodName, resources.getAstNodeCache().InteractionBuilder_AddRegexMethodName);
    call(constraint, methodNameExpr);
  }

  private MethodNode selectNameConstraint(Expression nameExpr, MethodNode constraint1, MethodNode constraint2) {
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
    call(resources.getAstNodeCache().InteractionBuilder_SetArgListKind_boolean_boolean, ConstantExpression.PRIM_FALSE, primitiveConstExpression(isMixed));
  }

  private void usePositionalArgs(boolean isMixed) {
    call(resources.getAstNodeCache().InteractionBuilder_SetArgListKind_boolean_boolean, ConstantExpression.PRIM_TRUE, primitiveConstExpression(isMixed));
  }

  private void addName(Expression name) {
    call(resources.getAstNodeCache().InteractionBuilder_AddArgName, name);
  }

  private void addArg(Expression arg) {
    if (arg instanceof NotExpression) {
      NotExpression not = (NotExpression)arg;
      addArg(not.getExpression());
      call(resources.getAstNodeCache().InteractionBuilder_NegateLastArg);
      return;
    }

    if (arg instanceof CastExpression) {
      CastExpression cast = (CastExpression)arg;

      if (cast.getExpression() instanceof ListExpression) {
        // keep casting expression so that `[] as Set` will still be correctly cast
        call(resources.getAstNodeCache().InteractionBuilder_AddEqualArg, arg);
      } else {
        addArg(cast.getExpression());
      }

      call(resources.getAstNodeCache().InteractionBuilder_TypeLastArg, new ClassExpression(cast.getType()));
      return;
    }

    if (arg instanceof ClosureExpression) {
      call(resources.getAstNodeCache().InteractionBuilder_AddCodeArg, arg);
      return;
    }

    call(resources.getAstNodeCache().InteractionBuilder_AddEqualArg, arg);
  }

  private void addResponses() {
    for (InteractionResponse response : responses) {
      if (response.iterable) {
        call(resources.getAstNodeCache().InteractionBuilder_AddIterableResponse, response.expr);
      } else if (response.expr instanceof ClosureExpression) {
        call(resources.getAstNodeCache().InteractionBuilder_AddCodeResponse, response.expr);
      } else {
        call(resources.getAstNodeCache().InteractionBuilder_AddConstantResponse, response.expr);
      }
    }
  }

  private void build() {
    call(resources.getAstNodeCache().InteractionBuilder_Build);
  }

  private ExpressionStatement register() {
    ExpressionStatement result = new ExpressionStatement(
      createDirectMethodCall(
        resources.getMockInvocationMatcher(),
        resources.getAstNodeCache().MockController_AddInteraction,
        new ArgumentListExpression(builderExpr)));
    result.setSourcePosition(stat);
    return result;
  }

  private void call(MethodNode method, Expression... args) {
    builderExpr = createDirectMethodCall(builderExpr, method, new ArgumentListExpression(args));
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
