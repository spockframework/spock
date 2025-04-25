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
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import spock.lang.Verify;
import spock.lang.VerifyAll;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spockframework.compiler.AstNodeCache;
import org.spockframework.compiler.ErrorReporter;
import org.spockframework.compiler.IRewriteResources;
import org.spockframework.compiler.SourceLookup;

abstract class BaseVerifyMethodTransform implements ASTTransformation {

  private static final Set<String> ANNOTATIONS = Stream.of(Verify.class, VerifyAll.class).map(Class::getName).collect(Collectors.toSet());
  static final AstNodeCache nodeCache = new AstNodeCache();

  private final Class<? extends Annotation> expectedAnnotation;

  public BaseVerifyMethodTransform(Class<? extends Annotation> expectedAnnotation) {
    this.expectedAnnotation = expectedAnnotation;
  }

  abstract IVerifyMethodRewriter createRewriter(MethodNode methodNode, IRewriteResources resources);

  @Override
  public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
    ErrorReporter errorReporter = new ErrorReporter(sourceUnit);

    try (SourceLookup sourceLookup = new SourceLookup(sourceUnit)) {
      for (ASTNode node : astNodes) {
        if (!(node instanceof MethodNode)) {
          continue;
        }

        processVerificationHelperMethod((MethodNode) node, errorReporter, sourceLookup);
      }
    }
  }

  private void processVerificationHelperMethod(MethodNode method, ErrorReporter errorReporter, SourceLookup sourceLookup) {
    if (!method.isVoidMethod()) {
      errorReporter.error("Verification helper method '%s' must have a void return type.", method.getName());
      return;
    }

    for (AnnotationNode annotation : method.getAnnotations()) {
      String name = annotation.getClassNode().getName();
      if (name.equals(expectedAnnotation.getName())) continue;
      if (ANNOTATIONS.contains(name)) {
        errorReporter.error("Verification helper annotations can't be combined on '%s', '@%s' conflicts with '@%s'.", method.getName(), expectedAnnotation.getName(), name);
        return;
      }
    }

    if (!method.isStatic() && method.getDeclaringClass().isDerivedFrom(nodeCache.Specification)) {
      // This will be handled by the SpockTransform
      return;
    }

    IVerifyMethodRewriter rewriter = createRewriter(
        method,
        new DefaultConditionRewriterResources(nodeCache, sourceLookup, errorReporter, new DefaultConditionErrorRecorders(nodeCache))
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
