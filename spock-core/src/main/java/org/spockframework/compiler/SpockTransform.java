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

import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.spockframework.compiler.model.Spec;
import org.spockframework.util.VersionChecker;

import java.util.List;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.control.*;
import org.codehaus.groovy.transform.*;

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
@SuppressWarnings("UnusedDeclaration")
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class SpockTransform implements ASTTransformation {
  public SpockTransform() {
    new VersionChecker().checkGroovyVersion("compiler plugin");
  }

  @Override
  public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
    new Impl().visit(nodes, sourceUnit);
  }

  // use of nested class defers linking until after groovy version check
  private static class Impl {
    static final AstNodeCache nodeCache = new AstNodeCache();

    void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
      ErrorReporter errorReporter = new ErrorReporter(sourceUnit);

      try (SourceLookup sourceLookup = new SourceLookup(sourceUnit)) {
        List<ClassNode> classes = sourceUnit.getAST().getClasses();

        for (ClassNode clazz : classes)
          if (isSpec(clazz)) processSpec(sourceUnit, clazz, errorReporter, sourceLookup);
      }
    }

    boolean isSpec(ClassNode clazz) {
      return clazz.isDerivedFrom(nodeCache.Specification);
    }

    void processSpec(SourceUnit sourceUnit, ClassNode clazz, ErrorReporter errorReporter, SourceLookup sourceLookup) {
      try {
        Spec spec = new SpecParser(nodeCache, errorReporter).build(clazz);
        spec.accept(new SpecRewriter(nodeCache, sourceLookup, errorReporter));
        spec.accept(new SpecAnnotator(nodeCache));
        // if there were no errors so far, let the variable scope visitor fix up variable scopes
        if (!sourceUnit.getErrorCollector().hasErrors()) {
          new VariableScopeVisitor(sourceUnit).visitClass(clazz);
        }
      } catch (Exception e) {
        errorReporter.error(
            "Unexpected error during compilation of spec '%s'. Maybe you have used invalid Spock syntax? Anyway, please file a bug report at https://issues.spockframework.org.",
            e, clazz.getName());
      }
    }
  }
}
