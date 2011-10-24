/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.compiler;

import java.io.File;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.*;

import org.spockframework.compiler.model.*;
import org.spockframework.runtime.model.*;

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
  }

  private void addSpecMetadata(Spec spec) {
    AnnotationNode ann = new AnnotationNode(nodeCache.SpecMetadata);
    String pathname = spec.getAst().getModule().getContext().getName();
    String filename = new File(pathname).getName();
    ann.setMember(SpecMetadata.FILENAME, new ConstantExpression(filename));
    ann.setMember(SpecMetadata.LINE, new ConstantExpression(spec.getAst().getLineNumber()));
    spec.getAst().addAnnotation(ann);
  }

  @Override
  public void visitField(Field field) throws Exception {
    addFieldMetadata(field);
  }

  private void addFieldMetadata(Field field) {
    AnnotationNode ann = new AnnotationNode(nodeCache.FieldMetadata);
    ann.setMember(FieldMetadata.NAME, new ConstantExpression(field.getName()));
    ann.setMember(FieldMetadata.ORDINAL, new ConstantExpression(field.getOrdinal()));
    ann.setMember(FieldMetadata.LINE, new ConstantExpression(field.getAst().getLineNumber()));
    field.getAst().addAnnotation(ann);
  }

  @Override
  public void visitMethod(Method method) throws Exception {
    if (method instanceof FeatureMethod)
      addFeatureMetadata((FeatureMethod)method);
  }

  private void addFeatureMetadata(FeatureMethod feature) {
    AnnotationNode ann = new AnnotationNode(nodeCache.FeatureMetadata);
    ann.setMember(FeatureMetadata.NAME, new ConstantExpression(feature.getName()));
    ann.setMember(FeatureMetadata.ORDINAL, new ConstantExpression(feature.getOrdinal()));
    ann.setMember(FeatureMetadata.LINE, new ConstantExpression(feature.getAst().getLineNumber()));
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
}
