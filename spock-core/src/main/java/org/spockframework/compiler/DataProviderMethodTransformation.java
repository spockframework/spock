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
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

/**
 * AST transformation for rewriting standalone {@code @DataProvider} methods.
 * <p>
 * This is a local transform attached to the {@code spock.lang.DataProvider} annotation, so it
 * fires regardless of the enclosing class. Within the {@code SEMANTIC_ANALYSIS} phase the global
 * {@link SpockTransform} runs first; it leaves {@code @DataProvider} methods untouched (see
 * {@link SpecParser}), so this transform always receives the raw, unrewritten method body,
 * whether the method is declared inside a specification or on a plain class.
 *
 * @since 2.5
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class DataProviderMethodTransformation implements ASTTransformation {
  static final AstNodeCache nodeCache = new AstNodeCache();

  @Override
  public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
    ErrorReporter errorReporter = new ErrorReporter(sourceUnit);

    try (SourceLookup sourceLookup = new SourceLookup(sourceUnit)) {
      for (ASTNode node : nodes) {
        if (!(node instanceof MethodNode)) {
          continue;
        }
        MethodNode methodNode = (MethodNode) node;
        try {
          new DataProviderMethodRewriter(methodNode, nodeCache, sourceUnit, errorReporter, sourceLookup).rewrite();
        } catch (Exception e) {
          errorReporter.error(
            "Unexpected error during compilation of @DataProvider method '%s'. Maybe you have used invalid Spock syntax? Anyway, please file a bug report at https://issues.spockframework.org.",
            e, methodNode.getName());
        }
      }
    }
  }
}
