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
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;

import java.util.ArrayList;

/**
 * Builds the companion overload for a {@code @Interactions} method and the
 * throwing replacement body for the original method.
 *
 * <p>The companion has the same name, return type, generics, varargs, thrown
 * exceptions and visibility as the original, with a {@code Specification $spec}
 * parameter prepended. It is NOT marked synthetic, so it is a first-class,
 * directly-callable overload.
 */
public class CompanionMethodBuilder {
  public static final String SPEC_PARAM_NAME = "$spec";

  private final AstNodeCache nodeCache;

  public CompanionMethodBuilder(AstNodeCache nodeCache) {
    this.nodeCache = nodeCache;
  }

  public MethodNode buildCompanion(MethodNode original) {
    Parameter[] originalParams = original.getParameters();
    Parameter[] params = new Parameter[originalParams.length + 1];
    params[0] = new Parameter(nodeCache.Specification, SPEC_PARAM_NAME);
    System.arraycopy(originalParams, 0, params, 1, originalParams.length);

    MethodNode companion = new MethodNode(
        original.getName(),
        original.getModifiers(),
        original.getReturnType(),
        params,
        original.getExceptions(),
        new BlockStatement());
    companion.setGenericsTypes(original.getGenericsTypes());
    companion.addAnnotations(new ArrayList<>(original.getAnnotations()));
    companion.setSourcePosition(original);
    return companion;
  }

  public BlockStatement buildThrowingBody(MethodNode original) {
    String message = "Method '" + original.getName() + "' is annotated with @Interactions and can only declare "
        + "interactions when called from a Specification with a strongly-typed receiver, or when its explicit-spec "
        + "overload (with a leading Specification argument) is called directly.";
    ConstructorCallExpression exception = new ConstructorCallExpression(
        nodeCache.InvalidSpecException,
        new ArgumentListExpression(new ConstantExpression(message)));
    BlockStatement body = new BlockStatement();
    body.addStatement(new ThrowStatement(exception));
    return body;
  }
}
