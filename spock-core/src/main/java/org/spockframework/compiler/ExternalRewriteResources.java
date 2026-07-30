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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;

import org.spockframework.compiler.condition.IConditionErrorRecorders;
import org.spockframework.compiler.model.Block;
import org.spockframework.compiler.model.Method;

import java.util.function.Supplier;

import static org.spockframework.compiler.AstUtil.createDirectMethodCall;

/**
 * Standalone {@link IRewriteResources} for rewriting interactions in code that
 * does not run on a {@code Specification} instance. Modeled on
 * {@code DefaultConditionRewriterResources}: it merely holds its dependencies.
 *
 * <p>The spec reference is supplied by the caller and is the only thing that
 * differs from the in-spec rewrite ({@code this} for a real spec,
 * {@code this.getSpecification()} for a {@code MockInteractionSupport} class,
 * or the injected {@code $spec} parameter for a {@code @Interactions}
 * companion). Everything that registers an interaction layers on top of it.
 */
public class ExternalRewriteResources implements IRewriteResources {
  private final Supplier<Expression> specificationReferenceFactory;
  private final Method currentMethod;
  private final AstNodeCache nodeCache;
  private final SourceLookup sourceLookup;
  private final ErrorReporter errorReporter;

  /**
   * @param specificationReferenceFactory produces a fresh spec-reference
   *        expression on each call; a fresh node is required because the
   *        reference is embedded into a new AST location for every interaction
   *        or creation it locates.
   */
  public ExternalRewriteResources(Supplier<Expression> specificationReferenceFactory, Method currentMethod,
      AstNodeCache nodeCache, SourceLookup sourceLookup, ErrorReporter errorReporter) {
    this.specificationReferenceFactory = specificationReferenceFactory;
    this.currentMethod = currentMethod;
    this.nodeCache = nodeCache;
    this.sourceLookup = sourceLookup;
    this.errorReporter = errorReporter;
  }

  @Override
  public Expression getSpecificationReference() {
    return specificationReferenceFactory.get();
  }

  @Override
  public Method getCurrentMethod() {
    return currentMethod;
  }

  @Override
  public Block getCurrentBlock() {
    throw new UnsupportedOperationException("External interaction rewriting has no block structure");
  }

  @Override
  public VariableExpression captureOldValue(Expression oldValue) {
    throw new UnsupportedOperationException("old() is not supported outside a then-block");
  }

  @Override
  public MethodCallExpression getMockInvocationMatcher() {
    MethodCallExpression specificationContext = createDirectMethodCall(specificationReferenceFactory.get(),
        nodeCache.Specification_GetSpecificationContext, ArgumentListExpression.EMPTY_ARGUMENTS);
    return createDirectMethodCall(specificationContext,
        nodeCache.SpecificationContext_GetMockController, ArgumentListExpression.EMPTY_ARGUMENTS);
  }

  @Override
  public AstNodeCache getAstNodeCache() {
    return nodeCache;
  }

  @Override
  public String getSourceText(ASTNode node) {
    return sourceLookup.lookup(node);
  }

  @Override
  public ErrorReporter getErrorReporter() {
    return errorReporter;
  }

  @Override
  public IConditionErrorRecorders getErrorRecorders() {
    throw new UnsupportedOperationException("External interaction rewriting does not record conditions");
  }
}
