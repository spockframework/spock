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

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.control.CompilePhase.CONVERSION;

/**
 * Mark annotations as already present in conversion phase as for example trait transform adds
 * code with STC annotations and we don't want to complain about those later on.
 * We cannot complain here directly, as in conversion phase only the explicitly written annotation
 * name is known, not the proper type.
 * We can also not simply mark the annotations themselves, because the annotation collection
 * could already been done between conversion and semantic analysis phases, which would replace
 * the marked dynamic compilation annotation with an unmarked static compilation skip annotation.
 */
@GroovyASTTransformation(phase = CONVERSION)
public class MarkAnnotationsAlreadyPresentInConversionPhase implements ASTTransformation {
  @Override
  public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
    List<ClassNode> classes = sourceUnit.getAST().getClasses();

    for (ClassNode classNode : classes) {
      new ClassCodeVisitorSupport() {
        @Override
        protected SourceUnit getSourceUnit() {
          return sourceUnit;
        }

        @Override
        public void visitAnnotations(AnnotatedNode node) {
          List<AnnotationNode> annotations = node.getAnnotations();
          if (!annotations.isEmpty()) {
            node.putNodeMetaData(MarkAnnotationsAlreadyPresentInConversionPhase.class, new ArrayList<>(annotations));
          }

          super.visitAnnotations(node);
        }
      }.visitClass(classNode);
    }
  }
}
