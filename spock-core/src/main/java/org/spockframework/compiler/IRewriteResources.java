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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;

import org.spockframework.compiler.condition.IConditionErrorRecorders;
import org.spockframework.compiler.model.Block;
import org.spockframework.compiler.model.Method;

/**
 *
 * @author Peter Niederwieser
 */
public interface IRewriteResources {
  /**
   * The AST expression that yields the {@code Specification} instance at this
   * rewrite site (the "spec reference"). Real specs return {@code this};
   * external mechanisms return an expression that locates the owning spec.
   */
  Expression getSpecificationReference();

  /**
   * Whether interactions may be declared in a {@code static} method here. The
   * default is {@code false}: the spec reference normally depends on {@code this}
   * (a real spec, or {@code this.getSpecification()} for a
   * {@code MockInteractionSupport}), which is unavailable in static scope. It is
   * {@code true} only when the spec reference is instance-independent, i.e. an
   * injected parameter such as a {@code @Interactions} companion's
   * {@code $spec}.
   */
  default boolean isStaticInteractionScopeAllowed() {
    return false;
  }

  Method getCurrentMethod();

  Block getCurrentBlock();

  VariableExpression captureOldValue(Expression oldValue);

  MethodCallExpression getMockInvocationMatcher();

  AstNodeCache getAstNodeCache();

  String getSourceText(ASTNode node);

  ErrorReporter getErrorReporter();

  IConditionErrorRecorders getErrorRecorders();

}
