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
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.spockframework.compiler.*;
import org.spockframework.compiler.model.Method;
import org.spockframework.compiler.model.VerifyMethod;
import spock.lang.Verify;
import spock.lang.VerifyAll;

import static org.spockframework.compiler.AstUtil.hasAnnotation;

abstract class BaseVerifyMethodTransform implements ASTTransformation {

  static final AstNodeCache nodeCache = new AstNodeCache();

  abstract IVerifyMethodRewriter createRewriter(Method method, IRewriteResources resources);

  abstract Method createVerifyMethod(MethodNode methodNode);

  @Override
  public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
    ErrorReporter errorReporter = new ErrorReporter(sourceUnit);

    try (SourceLookup sourceLookup = new SourceLookup(sourceUnit)) {
      for (ASTNode node : astNodes) {
        if (!(node instanceof MethodNode)) {
          continue;
        }
        MethodNode methodNode = (MethodNode) node;

        if (methodNode.getDeclaringClass().isDerivedFrom(nodeCache.Specification)) {
          // SpecRewriter already handles the method, so don't transform it again
          continue;
        }

        if (hasAnnotation(node, Verify.class) && hasAnnotation(node, VerifyAll.class)) {
          errorReporter.error(node, "Method '%s' cannot be annotated with both @Verify and @VerifyAll.", methodNode.getName());
        }

        processVerificationHelperMethod(createVerifyMethod(methodNode), errorReporter, sourceLookup);
      }
    }
  }

  private void processVerificationHelperMethod(Method method, ErrorReporter errorReporter, SourceLookup sourceLookup) {
    MethodNode methodAst = method.getAst();
    if (!VerifyMethod.verifyReturnType(methodAst, errorReporter)) {
      return;
    }

    methodAst.setReturnType(nodeCache.Void);

    IVerifyMethodRewriter rewriter = createRewriter(
        method,
        new DefaultConditionRewriterResources(method, nodeCache, sourceLookup, errorReporter, new DefaultConditionErrorRecorders(nodeCache))
    );

    try {
      rewriter.rewrite();
    } catch (Exception e) {
      errorReporter.error(
          "Unexpected error during compilation of verification helper method '%s'. Maybe you have used invalid Spock syntax? Anyway, please file a bug report at https://issues.spockframework.org.",
          e, method.getName());
    }
  }
}
