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

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.control.*;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import org.spockframework.compiler.model.Spec;

/**
 * AST transformation for rewriting Spock specifications. Runs after phase
 * SEMANTIC_ANALYSIS, which means that the AST is semantically accurate
 * and already decorated with reflection information. On the flip side,
 * because types and variables have already been resolved,
 * program elements like import statements and variable definitions
 * can no longer be manipulated at will.
 *
 * @author Peter Niederwieser
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class SpockTransform implements ASTTransformation {
  private static final AstNodeCache nodeCache = new AstNodeCache();

  public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
    ErrorReporter errorReporter = new ErrorReporter(sourceUnit);
    SourceLookup sourceLookup = new SourceLookup(sourceUnit, new Janitor());

    try {
      ModuleNode module = (ModuleNode)nodes[0];
      @SuppressWarnings("unchecked")
      List<ClassNode> classes =  module.getClasses();

      for (ClassNode clazz : classes)
        if (isSpec(clazz)) processSpec(clazz, errorReporter, sourceLookup);     
    } finally {
      sourceLookup.close();
    }
  }

  private boolean isSpec(ClassNode clazz) {
    return clazz.isDerivedFrom(nodeCache.Specification) && !AstUtil.isJavaStub(clazz);
  }

  private void processSpec(ClassNode clazz, ErrorReporter errorReporter, SourceLookup sourceLookup) {
    try {
      Spec spec = new SpecParser(errorReporter).build(clazz);
      spec.accept(new SpecRewriter(nodeCache, sourceLookup, errorReporter));
      spec.accept(new SpecAnnotator(nodeCache));
    } catch (Exception e) {
      errorReporter.error(
"Unexpected error during compilation of specification '%s'. Maybe you have used invalid Spock syntax? Anyway, this should not happen. Please file a bug report at http://issues.spockframework.org.",
          clazz.getName());
    }
  }
}
