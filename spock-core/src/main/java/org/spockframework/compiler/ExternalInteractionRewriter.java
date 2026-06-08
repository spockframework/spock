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
 * expanded (interface mechanism) or rejected as a compile error (annotation
 * mechanism), controlled by {@code allowCreation}.
 *
 * <p>Only top-level statements are rewritten; closure-nested interaction forms
 * ({@code with(mock) { ... }}) are not handled here.
 */
public class ExternalInteractionRewriter {
  private final AstNodeCache nodeCache;
  private final ErrorReporter errorReporter;
  private final SourceLookup sourceLookup;
  private final boolean allowCreation;

  /**
   * @param allowCreation whether built-in creation calls may be expanded
   *                      (interface mechanism) or are rejected (annotation
   *                      mechanism).
   */
  public ExternalInteractionRewriter(AstNodeCache nodeCache, ErrorReporter errorReporter,
      SourceLookup sourceLookup, boolean allowCreation) {
    this.nodeCache = nodeCache;
    this.errorReporter = errorReporter;
    this.sourceLookup = sourceLookup;
    this.allowCreation = allowCreation;
  }

  /**
   * Rewrites {@code method}'s body in place. {@code specificationReferenceFactory}
   * produces a fresh spec-locator expression on each call, used for both
   * interaction registration and (when allowed) mock creation.
   */
  public void rewriteInPlace(MethodNode method, Supplier<Expression> specificationReferenceFactory) {
    if (!(method.getCode() instanceof BlockStatement)) return;

    ExternalRewriteResources resources = new ExternalRewriteResources(
        specificationReferenceFactory, new ExternalInteractionMethod(method), nodeCache, sourceLookup, errorReporter);

    // the spec is located through `this`, which is unavailable in static scope;
    // interactions in static methods are rejected by InteractionRewriter itself
    boolean staticScopeViolation = method.isStatic();

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

    // guard the located spec: anything we rewrote depends on it being non-null
    if (rewrote) {
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
   * Builds {@code Checks.notNull(<specRef>, "...")}, guarding against a missing
   * owning spec (e.g. a {@code MockInteractionSupport} whose
   * {@code getSpecification()} was never attached).
   */
  private Statement createSpecificationNotNullCheck(Expression specificationReference) {
    MethodCallExpression check = createDirectMethodCall(
        new ClassExpression(nodeCache.Checks),
        nodeCache.Checks_NotNull,
        new ArgumentListExpression(specificationReference, new ConstantExpression(SPEC_NULL_MESSAGE)));
    return new ExpressionStatement(check);
  }

  private static final String SPEC_NULL_MESSAGE =
      "Cannot declare mock interactions: the owning Specification is null. Attach the MockInteractionSupport to a "
          + "running Specification through a constructor field.";
}
