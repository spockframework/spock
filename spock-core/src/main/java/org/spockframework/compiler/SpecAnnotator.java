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

import org.spockframework.compiler.model.*;
import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.RepeatedExtensionAnnotations;
import org.spockframework.runtime.model.*;

import java.io.File;
import java.lang.annotation.Repeatable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;

import static java.util.stream.Collectors.*;
import static org.spockframework.compiler.AstUtil.*;
import static org.spockframework.util.ObjectUtil.asInstance;

/**
 * Puts all spec information required at runtime into annotations
 * attached to class members.
 *
 * @author Peter Niederwieser
 */
public class SpecAnnotator extends AbstractSpecVisitor {
  private final AstNodeCache nodeCache;
  private ListExpression blockAnnElems;

  public SpecAnnotator(AstNodeCache nodeCache) {
    this.nodeCache = nodeCache;
  }

  @Override
  public void visitSpec(Spec spec) throws Exception {
    addSpecMetadata(spec);
    addRepeatedExtensionAnnotations(spec.getAst());
  }

  private void addSpecMetadata(Spec spec) {
    AnnotationNode ann = new AnnotationNode(nodeCache.SpecMetadata);
    String pathname = spec.getAst().getModule().getContext().getName();
    String filename = new File(pathname).getName();
    ann.setMember(SpecMetadata.FILENAME, new ConstantExpression(filename));
    ann.setMember(SpecMetadata.LINE, primitiveConstExpression(spec.getAst().getLineNumber()));
    spec.getAst().addAnnotation(ann);
  }

  @Override
  public void visitField(Field field) throws Exception {
    addFieldMetadata(field);
    addRepeatedExtensionAnnotations(field.getAst());
  }

  private void addFieldMetadata(Field field) {
    AnnotationNode ann = new AnnotationNode(nodeCache.FieldMetadata);
    ann.setMember(FieldMetadata.NAME, new ConstantExpression(field.getName()));
    ann.setMember(FieldMetadata.ORDINAL, primitiveConstExpression(field.getOrdinal()));
    ann.setMember(FieldMetadata.LINE, primitiveConstExpression(field.getAst().getLineNumber()));
    ann.setMember(FieldMetadata.INITIALIZER, primitiveConstExpression(field.hasInitialExpression()));
    field.getAst().addAnnotation(ann);
  }

  @Override
  public void visitMethod(Method method) throws Exception {
    if (method instanceof FeatureMethod)
      addFeatureMetadata((FeatureMethod)method);
    if ((method instanceof FeatureMethod) || (method instanceof FixtureMethod))
      addRepeatedExtensionAnnotations(method.getAst());
  }

  private void addRepeatedExtensionAnnotations(AnnotatedNode annotatedNode) {
    ListExpression repeatedExtensionAnnotations = annotatedNode
      // get all repeatable extension annotations flattened
      .getAnnotations()
      .stream()
      .flatMap(this::flattenRepeatableExtensionAnnotationContainer)
      .filter(this::isRepeatableExtensionAnnotation)
      // find the annotations that occur multiple times
      .collect(groupingBy(AnnotationNode::getClassNode))
      .entrySet()
      .stream()
      .filter(entry -> entry.getValue().size() > 1)
      // put them to a ListExpression
      .map(Map.Entry::getKey)
      .map(ClassExpression::new)
      .collect(collectingAndThen(Collectors.<Expression>toList(), ListExpression::new));

    // if any were found, put them to a RepeatedExtensionAnnotations annotation as runtime hint
    if (repeatedExtensionAnnotations.getExpressions().size() != 0) {
      AnnotationNode ann = new AnnotationNode(nodeCache.RepeatedExtensionAnnotations);
      ann.setMember(RepeatedExtensionAnnotations.VALUE, repeatedExtensionAnnotations);
      annotatedNode.addAnnotation(ann);
    }
  }

  private Stream<? extends AnnotationNode> flattenRepeatableExtensionAnnotationContainer(AnnotationNode container) {
    // supply the container itself, it could also be a valid extension annotation
    Stream<? extends AnnotationNode> result = Stream.of(container);

    Expression value = container.getMember("value");
    if (value instanceof ListExpression) {
      List<Expression> valueExpressions = asInstance(value, ListExpression.class).getExpressions();
      switch (valueExpressions.size()) {
        case 0:
          // no value, nothing to do
          break;

        case 1:
          result = handleSingleContainedAnnotation(container, result, valueExpressions.get(0));
          break;

        default:
          result = handleMultipleContainedAnnotations(container, result, valueExpressions);
          break;
      }
    } else if (value instanceof AnnotationConstantExpression) {
      result = handleSingleContainedAnnotation(container, result, value);
    }

    return result;
  }

  private Stream<? extends AnnotationNode> handleSingleContainedAnnotation(AnnotationNode container,
                                                                           Stream<? extends AnnotationNode> result,
                                                                           Expression value) {
    if (notRepeatedAnnotation(value, container)) {
      return result;
    }

    AnnotationNode annotationNode = asInstance(
      asInstance(value, AnnotationConstantExpression.class).getValue(),
      AnnotationNode.class);
    // supply annotation twice in case there is only the container annotation
    // with one contained annotation and no annotation besides it
    // in that case we also need the RepeatedExtensionAnnotations at runtime
    // to unwrap the container
    return Stream.concat(result, Stream.of(annotationNode, annotationNode));
  }

  private Stream<? extends AnnotationNode> handleMultipleContainedAnnotations(AnnotationNode container,
                                                                              Stream<? extends AnnotationNode> result,
                                                                              List<Expression> valueExpressions) {
    if (notRepeatedAnnotation(valueExpressions.get(0), container)) {
      return result;
    }

    return Stream.concat(result, valueExpressions
      .stream()
      .map(AnnotationConstantExpression.class::cast)
      .map(AnnotationConstantExpression::getValue)
      .map(AnnotationNode.class::cast));
  }

  private boolean notRepeatedAnnotation(Expression clazz, AnnotationNode container) {
    AnnotationNode repeatableAnnotation = getAnnotation(clazz.getType(), Repeatable.class);
    return (repeatableAnnotation == null) ||
      !repeatableAnnotation.getMember("value").getType().equals(container.getClassNode());
  }

  private boolean isRepeatableExtensionAnnotation(AnnotationNode annotation) {
    ClassNode annotationClass = annotation.getClassNode();
    return hasAnnotation(annotationClass, ExtensionAnnotation.class) &&
      hasAnnotation(annotationClass, Repeatable.class);
  }

  private void addFeatureMetadata(FeatureMethod feature) {
    AnnotationNode ann = new AnnotationNode(nodeCache.FeatureMetadata);
    ann.setMember(FeatureMetadata.NAME, new ConstantExpression(feature.getName()));
    ann.setMember(FeatureMetadata.ORDINAL, primitiveConstExpression(feature.getOrdinal()));
    ann.setMember(FeatureMetadata.LINE, primitiveConstExpression(feature.getAst().getLineNumber()));
    ann.setMember(FeatureMetadata.BLOCKS, blockAnnElems = new ListExpression());

    ListExpression paramNames = new ListExpression();
    for (Parameter param : feature.getAst().getParameters())
      paramNames.addExpression(new ConstantExpression(param.getName()));
    ann.setMember(FeatureMetadata.PARAMETER_NAMES, paramNames);

    feature.getAst().addAnnotation(ann);
  }

  private void addBlockMetadata(Block block, BlockKind kind) {
    AnnotationNode blockAnn = new AnnotationNode(nodeCache.BlockMetadata);
    blockAnn.setMember(BlockMetadata.KIND, new PropertyExpression(
        new ClassExpression(nodeCache.BlockKind), kind.name()));
    ListExpression textExprs = new ListExpression();
    for (String text : block.getDescriptions())
      textExprs.addExpression(new ConstantExpression(text));
    blockAnn.setMember(BlockMetadata.TEXTS, textExprs);
    blockAnnElems.addExpression(new AnnotationConstantExpression(blockAnn));
  }

  @Override
  public void visitSetupBlock(SetupBlock block) throws Exception {
    addBlockMetadata(block, BlockKind.SETUP);
  }

  @Override
  public void visitExpectBlock(ExpectBlock block) throws Exception {
    addBlockMetadata(block, BlockKind.EXPECT);
  }

  @Override
  public void visitWhenBlock(WhenBlock block) throws Exception {
    addBlockMetadata(block, BlockKind.WHEN);
  }

  @Override
  public void visitThenBlock(ThenBlock block) throws Exception {
    addBlockMetadata(block, BlockKind.THEN);
  }

  @Override
  public void visitCleanupBlock(CleanupBlock block) throws Exception {
    addBlockMetadata(block, BlockKind.CLEANUP);
  }

  @Override
  public void visitWhereBlock(WhereBlock block) throws Exception {
    addBlockMetadata(block, BlockKind.WHERE);
  }

  public void visitFilterBlock(FilterBlock block) throws Exception {
    addBlockMetadata(block, BlockKind.FILTER);
  }
}
