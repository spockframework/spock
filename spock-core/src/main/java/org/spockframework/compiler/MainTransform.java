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

import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.Janitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import org.spockframework.compiler.model.Speck;
import org.spockframework.util.SyntaxException;
import spock.lang.*;

/**
 *
 * @author Peter Niederwieser
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class MainTransform implements ASTTransformation {
  private final AstNodeCache nodeCache = new AstNodeCache();

  public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
    SourceLookup lookup = null;

    try {
      ModuleNode module = (ModuleNode)nodes[0];

      lookup = new SourceLookup(sourceUnit, new Janitor());
      @SuppressWarnings("unchecked")
      List<ClassNode> classes =  (List<ClassNode>)module.getClasses();

      for (ClassNode clazz : classes) {
        if (!isSpeck(clazz)) continue;
        
        Speck speck = new SpeckParser().build(clazz);
        speck.accept(new SpeckRewriter(nodeCache, lookup));
        speck.accept(new SpeckAnnotator(nodeCache));
      }
    } catch (SyntaxException e) {
      sourceUnit.getErrorCollector().addError(e.toSpockSyntaxException(), sourceUnit);
    } catch (Exception e) {
      // NOTE: this produces an uninformative error message in IDEA
      // ("an unknown error has occurred")
      sourceUnit.getErrorCollector().addError(
          new TransformErrorMessage(sourceUnit, e, true));
    } finally {
      if (lookup != null) lookup.close();
    }
  }

  private static boolean isSpeck(ClassNode clazz) {
    return hasSpeckAnnotation(clazz) || isDerivedFromSpecification(clazz);
  }

  private static boolean hasSpeckAnnotation(ClassNode clazz) {
    return AstUtil.hasAnnotation(clazz, spock.lang.Speck.class);
  }

  private static boolean isDerivedFromSpecification(ClassNode clazz) {
    for (ClassNode node = clazz; node != null; node = node.getSuperClass())
      if (node.getName().equals(Specification.class.getName()))
        return true;

    return false;
  }
}
