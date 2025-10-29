/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spockframework.compiler.condition;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.spockframework.compiler.AstNodeCache;
import org.spockframework.compiler.ErrorReporter;
import org.spockframework.compiler.IRewriteResources;
import org.spockframework.compiler.SourceLookup;
import org.spockframework.compiler.model.Block;
import org.spockframework.compiler.model.Method;

import static org.spockframework.util.Assert.notNull;

class DefaultConditionRewriterResources implements IRewriteResources {

  private final Method method;
  private final AstNodeCache nodeCache;
  private final SourceLookup lookup;
  private final ErrorReporter errorReporter;
  private final IConditionErrorRecorders errorRecorders;

  DefaultConditionRewriterResources(
    Method method,
    AstNodeCache nodeCache,
    SourceLookup lookup,
    ErrorReporter errorReporter,
    IConditionErrorRecorders errorRecorders
  ) {
    this.method = notNull(method);
    this.nodeCache = notNull(nodeCache);
    this.lookup = notNull(lookup);
    this.errorReporter = notNull(errorReporter);
    this.errorRecorders = notNull(errorRecorders);
  }

  @Override
  public Method getCurrentMethod() {
    return method;
  }

  @Override
  public Block getCurrentBlock() {
    return method.getFirstBlock();
  }

  @Override
  public VariableExpression captureOldValue(Expression oldValue) {
    throw new UnsupportedOperationException("captureOldValue");
  }

  @Override
  public MethodCallExpression getMockInvocationMatcher() {
    return null;
  }

  @Override
  public AstNodeCache getAstNodeCache() {
    return nodeCache;
  }

  @Override
  public String getSourceText(ASTNode node) {
    return lookup.lookup(node);
  }

  @Override
  public ErrorReporter getErrorReporter() {
    return errorReporter;
  }

  @Override
  public IConditionErrorRecorders getErrorRecorders() {
    return errorRecorders;
  }
}
