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

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.expr.*;

import org.spockframework.compiler.model.*;
import org.spockframework.runtime.model.*;

/**
 * Puts all speck information required at runtime into annotations
 * attached to class members.
 * 
 * @author Peter Niederwieser
 */
public class SpeckAnnotator extends AbstractSpeckVisitor {
  private final AstNodeCache nodeCache;
  private ListExpression blockAnnElems;
  private int methodIndex = 0;

  public SpeckAnnotator(AstNodeCache nodeCache) {
    this.nodeCache = nodeCache;
  }

  @Override
  public void visitSpeck(Speck speck) throws Exception {
    addSpeckAnnotation(speck);
    addFixtureMethodAnnotations(speck);
  }

  private void addSpeckAnnotation(Speck speck) {
    AnnotationNode ann2 = new AnnotationNode(nodeCache.SpeckMetadata);
    speck.getAst().addAnnotation(ann2);
  }

  private void addFixtureMethodAnnotations(Speck speck) {
    addMethodAnnotation(speck.getSetup(), MethodKind.SETUP);
    addMethodAnnotation(speck.getCleanup(), MethodKind.CLEANUP);
    addMethodAnnotation(speck.getSetupSpeck(), MethodKind.SETUP_SPECK);
    addMethodAnnotation(speck.getCleanupSpeck(), MethodKind.CLEANUP_SPECK);
  }

  private void addMethodAnnotation(Method method, MethodKind kind) {
    if (method == null) return; // fixture methods may not be present
    AnnotationNode ann = new AnnotationNode(nodeCache.MethodMetadata);
    ann.setMember(MethodMetadata.INDEX, new ConstantExpression(methodIndex++));
    ann.setMember(MethodMetadata.NAME, new ConstantExpression(method.getName()));
    ann.setMember(MethodMetadata.KIND, new PropertyExpression(
        new ClassExpression(nodeCache.MethodKind), kind.name()));
    ann.setMember(MethodMetadata.BLOCKS, blockAnnElems = new ListExpression());
    method.getAst().addAnnotation(ann);
  }

  @Override
  public void visitMethod(Method method) throws Exception {
    if (method instanceof FeatureMethod)
      addMethodAnnotation(method, MethodKind.FEATURE);
  }

  private void addBlockAnnotation(Block block, BlockKind kind) {
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
    addBlockAnnotation(block, BlockKind.SETUP);
  }

  @Override
  public void visitExpectBlock(ExpectBlock block) throws Exception {
    addBlockAnnotation(block, BlockKind.EXPECT);
  }

  @Override
  public void visitWhenBlock(WhenBlock block) throws Exception {
    addBlockAnnotation(block, BlockKind.WHEN);
  }

  @Override
  public void visitThenBlock(ThenBlock block) throws Exception {
    addBlockAnnotation(block, BlockKind.THEN);
  }

  @Override
  public void visitCleanupBlock(CleanupBlock block) throws Exception {
    addBlockAnnotation(block, BlockKind.CLEANUP);
  }

  @Override
  public void visitWhereBlock(WhereBlock block) throws Exception {
    addBlockAnnotation(block, BlockKind.WHERE);
  }
}
