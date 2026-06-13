/*
 * Copyright 2026 the original author or authors.
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

import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.spockframework.compiler.model.ExternalInteractionMethod;
import org.spockframework.util.Nullable;
import org.spockframework.util.ObjectUtil;

import java.util.List;
import java.util.function.Supplier;

import static org.spockframework.compiler.AstUtil.createDirectMethodCall;

/**
 * Rewrites the top-level interaction statements of an instance method that runs
 * outside a {@code Specification}. Each top-level {@link ExpressionStatement} is
 * offered to {@link InteractionRewriter}; matched statements are replaced with
 * the registration call ({@code mockController.addInteraction(...)}). Built-in
 * creation calls ({@code Mock()}/{@code Stub()}/{@code Spy()} ...) are either
 * expanded ({@code MockInteractionSupport} classes and
 * {@code @SelfType(Specification)} traits) or rejected as a compile error
 * ({@code @Interactions} methods), controlled by {@code allowCreation}. Calls to other
 * {@code @Interactions} helpers anywhere in the body are rewritten to pass the
 * located spec along, so helpers compose.
 *
 * <p>Only top-level statements are rewritten; closure-nested interaction forms
 * ({@code with(mock) { ... }}) are not handled here.
 */
public class ExternalInteractionRewriter {
  private final AstNodeCache nodeCache;
  private final ErrorReporter errorReporter;
  private final SourceLookup sourceLookup;
  private final boolean allowCreation;
  private final boolean allowStaticScope;
  private final String specNullMessage;

  /**
   * @param allowCreation    whether built-in creation calls may be expanded
   *                         (support classes and spec traits) or are rejected
   *                         ({@code @Interactions} methods).
   * @param allowStaticScope see {@link IRewriteResources#isStaticInteractionScopeAllowed()}.
   * @param specNullMessage  the message of the {@code Checks.notNull} guard that
   *                         is prepended to every rewritten method, protecting
   *                         against a {@code null} located spec; mechanism-specific
   *                         so the failure tells the user what to fix. May be
   *                         {@code null} to skip the guard when the reference is
   *                         non-null by construction (a
   *                         {@code @SelfType(Specification)} trait's {@code this}).
   */
  public ExternalInteractionRewriter(AstNodeCache nodeCache, ErrorReporter errorReporter,
      SourceLookup sourceLookup, boolean allowCreation, boolean allowStaticScope, @Nullable String specNullMessage) {
    this.nodeCache = nodeCache;
    this.errorReporter = errorReporter;
    this.sourceLookup = sourceLookup;
    this.allowCreation = allowCreation;
    this.allowStaticScope = allowStaticScope;
    this.specNullMessage = specNullMessage;
  }

  /**
   * Rewrites {@code method}'s body in place. {@code specificationReferenceFactory}
   * produces a fresh spec-locator expression on each call, used for both
   * interaction registration and (when allowed) mock creation.
   */
  public void rewriteInPlace(MethodNode method, Supplier<Expression> specificationReferenceFactory) {
    if (!(method.getCode() instanceof BlockStatement)) return;

    ExternalRewriteResources resources = new ExternalRewriteResources(
        specificationReferenceFactory, new ExternalInteractionMethod(method), nodeCache, sourceLookup, errorReporter,
        allowStaticScope);

    boolean staticScopeViolation = method.isStatic() && !allowStaticScope;

    BlockStatement body = (BlockStatement) method.getCode();
    List<Statement> statements = body.getStatements();
    boolean rewrote = false;
    for (int i = 0; i < statements.size(); i++) {
      Statement stat = statements.get(i);
      ExpressionStatement exprStat = ObjectUtil.asInstance(stat, ExpressionStatement.class);
      if (exprStat == null) continue;

      // expand or reject built-in creation calls (Mock/Stub/Spy ...) first
      rewrote |= handleCreation(exprStat, specificationReferenceFactory, staticScopeViolation);

      ExpressionStatement rewritten = new InteractionRewriter(resources, null).rewrite(exprStat);
      if (rewritten != null) {
        statements.set(i, rewritten);
        rewrote = true;
      }
    }

    // route calls to other @Interactions helpers through the located spec, so
    // helpers compose (the helper's $spec parameter has no user-visible name)
    rewrote |= rewriteNestedInteractionsCalls(method, body, specificationReferenceFactory);

    // guard the located spec: anything we rewrote depends on it being non-null
    if (rewrote && specNullMessage != null) {
      statements.add(0, createSpecificationNotNullCheck(specificationReferenceFactory.get()));
    }
  }

  private boolean handleCreation(ExpressionStatement exprStat, Supplier<Expression> specificationReferenceFactory,
      boolean staticScopeViolation) {
    Expression expr = exprStat.getExpression();
    BinaryExpression binaryExpr = ObjectUtil.asInstance(expr, BinaryExpression.class);
    Expression callCandidate = binaryExpr != null ? binaryExpr.getRightExpression() : expr;
    MethodCallExpression call = ObjectUtil.asInstance(callCandidate, MethodCallExpression.class);
    if (call == null) return false;

    SpecialMethodCall smc = SpecialMethodCall.parse(call, binaryExpr, nodeCache);
    if (smc == null || !smc.isTestDouble()) return false;

    if (staticScopeViolation) {
      errorReporter.error(call, "Mocks cannot be created in static scope");
      return false;
    }

    if (!allowCreation) {
      errorReporter.error(call,
          "Mock/Stub/Spy creation is not allowed in an @Interactions method; pass the mock in as a parameter, or use MockInteractionSupport.");
      return false;
    }
    smc.expand(specificationReferenceFactory.get());
    return true;
  }

  /**
   * Rewrites every call to an {@code @Interactions} helper anywhere in
   * {@code body} (including nested statements and closures) to pass the located
   * spec as the leading argument, selecting the helper's companion overload.
   */
  private boolean rewriteNestedInteractionsCalls(MethodNode method, BlockStatement body,
      Supplier<Expression> specificationReferenceFactory) {
    boolean[] rewrote = {false};
    new CodeVisitorSupport() {
      @Override
      public void visitMethodCallExpression(MethodCallExpression call) {
        super.visitMethodCallExpression(call);
        rewrote[0] |= InteractionsCallDetector.rewriteToCompanionCall(
            call, specificationReferenceFactory, nodeCache, method.getDeclaringClass());
      }
    }.visitBlockStatement(body);
    return rewrote[0];
  }

  /**
   * Builds {@code Checks.notNull(<specRef>, "...")}, guarding against a missing
   * owning spec (e.g. a {@code MockInteractionSupport} whose
   * {@code getSpecification()} was never attached, or a {@code null} passed for
   * the companion's {@code $spec} parameter).
   */
  private Statement createSpecificationNotNullCheck(Expression specificationReference) {
    MethodCallExpression check = createDirectMethodCall(
        new ClassExpression(nodeCache.Checks),
        nodeCache.Checks_NotNull,
        new ArgumentListExpression(specificationReference, new ConstantExpression(specNullMessage)));
    return new ExpressionStatement(check);
  }
}
