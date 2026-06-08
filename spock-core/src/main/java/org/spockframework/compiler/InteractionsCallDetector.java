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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import spock.lang.Interactions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Detects calls to {@code @Interactions}-annotated helper methods, using the
 * same static-type resolution as {@code @ConditionBlock} detection
 * ({@code SpecialMethodCall#checkIsConditionBlock}). Works for implicit-this and
 * strongly-typed receivers; silently misses {@code def}-typed receivers (which
 * fall through to the throwing original at runtime, by design).
 */
final class InteractionsCallDetector {
  private InteractionsCallDetector() {}

  static boolean isInteractionsCall(MethodCallExpression call, ClassNode enclosingType) {
    String methodName = call.getMethodAsString();
    if (methodName == null) return false;
    // an implicit-this (or explicit this/super) receiver carries no useful static
    // type at this phase; the enclosing class is the receiver type then
    ClassNode targetType = AstUtil.isThisOrSuperExpression(call.getObjectExpression())
        ? enclosingType
        : call.getObjectExpression().getType();
    if (targetType == null) return false;
    try {
      // resolving methods may fail with NoClassDefFoundError if a parameter or
      // return type is not on the compile classpath; treat that as "no match"
      for (MethodNode method : targetType.getMethods(methodName))
        if (AstUtil.hasAnnotation(method, Interactions.class)) return true;
    } catch (NoClassDefFoundError e) {
      // no match
    }
    return false;
  }

  /**
   * If {@code call} targets an {@code @Interactions} helper and does not already
   * pass a {@code Specification} as its first argument, prepends the spec
   * reference produced by {@code specificationReferenceFactory} so the call
   * dispatches to the synthesized companion overload.
   *
   * @return whether the call was rewritten
   */
  static boolean rewriteToCompanionCall(MethodCallExpression call, Supplier<Expression> specificationReferenceFactory,
      AstNodeCache nodeCache, ClassNode enclosingType) {
    if (!isInteractionsCall(call, enclosingType)) return false;
    if (firstArgIsSpecification(call, nodeCache)) return false; // already an explicit-spec overload call

    List<Expression> args = new ArrayList<>();
    args.add(specificationReferenceFactory.get());
    args.addAll(AstUtil.getArgumentList(call));
    ArgumentListExpression newArgs = new ArgumentListExpression(args);
    AstUtil.copySourcePosition(call.getArguments(), newArgs);
    call.setArguments(newArgs);
    return true;
  }

  private static boolean firstArgIsSpecification(MethodCallExpression expr, AstNodeCache nodeCache) {
    List<Expression> args = AstUtil.getArgumentList(expr);
    if (args.isEmpty()) return false;
    Expression first = args.get(0);
    // an explicit-spec overload call inside a spec passes `this`/`super`, or a
    // variable whose static type is a Specification
    return AstUtil.isThisOrSuperExpression(first)
        || first.getType().isDerivedFrom(nodeCache.Specification);
  }
}
