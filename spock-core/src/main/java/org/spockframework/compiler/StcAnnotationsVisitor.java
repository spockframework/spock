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

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static org.spockframework.util.ObjectUtil.uncheckedCast;

public class StcAnnotationsVisitor extends ClassCodeVisitorSupport {
  private final AstNodeCache nodeCache;
  private final SourceUnit sourceUnit;

  public StcAnnotationsVisitor(AstNodeCache nodeCache, SourceUnit sourceUnit) {
    this.nodeCache = nodeCache;
    this.sourceUnit = sourceUnit;
  }

  @Override
  protected SourceUnit getSourceUnit() {
    return sourceUnit;
  }

  @Override
  public void visitAnnotations(AnnotatedNode node) {
    List<AnnotationNode> originalAnnotations = uncheckedCast(node.getNodeMetaData(MarkAnnotationsAlreadyPresentInConversionPhase.class));
    if (originalAnnotations != null) {
      for (AnnotationNode an : originalAnnotations) {
        if ((nodeCache.CompileDynamic.equals(an.getClassNode())
          || nodeCache.CompileStatic.equals(an.getClassNode())
          || nodeCache.TypeChecked.equals(an.getClassNode()))) {

          sourceUnit.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(
            new SyntaxException("CompileDynamic, CompileStatic, and TypeChecked annotations are not allowed within Spock specifications as the IDE would majorly misreport type problems, use SpockCompileDynamic, SpockCompileStatic, or SpockTypeChecked instead", an),
            sourceUnit));
        }
      }
    }

    super.visitAnnotations(node);

    List<AnnotationNode> annotations = node.getAnnotations();
    annotations
      .stream()
      .map(annotation ->
      {
        ClassNode builtInAnnotation = null;
        if (nodeCache.SpockCompileDynamic.equals(annotation.getClassNode())) {
          builtInAnnotation = nodeCache.CompileDynamic;
        } else if (nodeCache.SpockCompileStatic.equals(annotation.getClassNode())) {
          builtInAnnotation = nodeCache.CompileStatic;
        } else if (nodeCache.SpockTypeChecked.equals(annotation.getClassNode())) {
          builtInAnnotation = nodeCache.TypeChecked;
        }

        if (builtInAnnotation == null) {
          return null;
        }

        AnnotationNode newAnnotation = new AnnotationNode(builtInAnnotation);
        newAnnotation.setSourcePosition(annotation);
        annotation.getMembers().forEach(newAnnotation::setMember);

        return newAnnotation;
      })
      .filter(Objects::nonNull)
      .collect(toList())
      .forEach(node::addAnnotation);
  }
}
