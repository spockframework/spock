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
import org.codehaus.groovy.transform.trait.Traits;
import org.spockframework.compiler.model.Spec;
import org.spockframework.util.VersionChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import spock.lang.Interactions;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
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

    static final String SUPPORT_SPEC_NULL_MESSAGE =
        "Cannot declare mock interactions: the owning Specification is null. Attach the MockInteractionSupport to a "
            + "running Specification through a constructor field.";

    static final String INTERACTIONS_SPEC_NULL_MESSAGE =
        "Cannot declare mock interactions: the Specification passed to this @Interactions method is null.";

    void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
      ErrorReporter errorReporter = new ErrorReporter(sourceUnit);

      try (SourceLookup sourceLookup = new SourceLookup(sourceUnit)) {
        List<ClassNode> classes = sourceUnit.getAST().getClasses();

        // Pass 1: synthesize @Interactions companions across the whole unit,
        // so same-unit call sites can be rewritten to the companion in pass 2.
        for (ClassNode clazz : classes)
          processInteractionsMethods(clazz, errorReporter, sourceLookup);

        // Pass 2: process specs and MockInteractionSupport classes.
        for (ClassNode clazz : classes) {
          boolean spec = isSpec(clazz);
          boolean support = isMockInteractionSupport(clazz);

          if (spec && support) {
            errorReporter.error(
                "Class '%s' must not be both a Specification and a MockInteractionSupport; a spec already supports interactions directly.",
                clazz.getName());
            continue;
          }

          if (spec) {
            processSpec(sourceUnit, clazz, errorReporter, sourceLookup);
          } else if (support) {
            processMockInteractionSupport(sourceUnit, clazz, errorReporter, sourceLookup);
          }
        }
      }
    }

    void processInteractionsMethods(ClassNode clazz, ErrorReporter errorReporter, SourceLookup sourceLookup) {
      // copy to avoid concurrent modification while adding the companion; also lets
      // us skip the common (no-@Interactions) class before allocating anything
      List<MethodNode> methods = new ArrayList<>(clazz.getMethods());
      if (methods.stream().noneMatch(this::hasInteractions)) return;

      boolean onSpec = isSpec(clazz);
      boolean onTrait = Traits.isTrait(clazz);
      boolean onSupport = isMockInteractionSupport(clazz);
      CompanionMethodBuilder builder = new CompanionMethodBuilder(nodeCache);
      // allowCreation=false (mocks are passed in); allowStaticScope=true (the
      // companion receives the spec as the injected $spec parameter, so a static
      // companion can still reach it)
      ExternalInteractionRewriter rewriter =
          new ExternalInteractionRewriter(nodeCache, errorReporter, sourceLookup, false, true,
              INTERACTIONS_SPEC_NULL_MESSAGE);

      for (MethodNode method : methods) {
        if (!hasInteractions(method)) continue;

        if (onSpec) {
          errorReporter.error(
              "Method '%s' is annotated with @Interactions but declared on a Specification; declare interactions directly in a block instead.",
              method.getName());
          continue;
        }
        if (onTrait) {
          errorReporter.error(
              "Method '%s' is annotated with @Interactions but declared in a trait, which is not supported.",
              method.getName());
          continue;
        }
        if (onSupport) {
          // class-level processing rewrites the body in place; annotation is redundant here
          continue;
        }
        if (method.isAbstract()) {
          errorReporter.error(
              "Method '%s' is annotated with @Interactions but is abstract; @Interactions requires a method body.",
              method.getName());
          continue;
        }
        if (hasLeadingSpecificationParameter(method)) {
          errorReporter.error(
              "Method '%s' is annotated with @Interactions and must not declare a leading Specification parameter; "
                  + "the Spock compiler adds the Specification parameter to the synthesized companion overload automatically.",
              method.getName());
          continue;
        }
        MethodNode unannotatedOverload = findUnannotatedOverload(clazz, method);
        if (unannotatedOverload != null) {
          errorReporter.error(
              "Method '%s' is annotated with @Interactions, but the overload with parameter types %s is not; "
                  + "calls to @Interactions helpers are matched by method name, so all overloads must be annotated if one is.",
              method.getName(), parameterTypeNames(unannotatedOverload));
          continue;
        }

        MethodNode companion = builder.buildCompanion(method);
        if (signatureCollides(clazz, companion, method)) {
          errorReporter.error(
              "Cannot synthesize a companion for @Interactions method '%s': a method with the companion signature (leading Specification parameter) already exists.",
              method.getName());
          continue;
        }

        BlockStatement companionBody = (BlockStatement) companion.getCode();
        if (method.getCode() instanceof BlockStatement) {
          companionBody.addStatements(((BlockStatement) method.getCode()).getStatements());
        }
        // add before rewriting, so the rewrite sees the companion's declaring class
        // (needed to resolve implicit-this calls to sibling @Interactions helpers)
        clazz.addMethod(companion);
        rewriter.rewriteInPlace(companion,
            () -> new VariableExpression(CompanionMethodBuilder.SPEC_PARAM_NAME, nodeCache.Specification));

        // original body becomes a diagnostic throw
        method.setCode(builder.buildThrowingBody(method));
      }
    }

    boolean hasInteractions(MethodNode method) {
      return AstUtil.hasAnnotation(method, Interactions.class);
    }

    boolean hasLeadingSpecificationParameter(MethodNode method) {
      Parameter[] parameters = method.getParameters();
      return parameters.length > 0 && parameters[0].getType().isDerivedFrom(nodeCache.Specification);
    }

    /**
     * Finds a same-name overload (declared or inherited) that lacks the
     * {@code @Interactions} annotation. Call sites are matched by name alone, so
     * such an overload would have the spec prepended to its arguments and fail
     * to dispatch.
     */
    MethodNode findUnannotatedOverload(ClassNode clazz, MethodNode original) {
      for (MethodNode candidate : clazz.getMethods(original.getName())) {
        if (candidate == original) continue;
        if (!hasInteractions(candidate)) return candidate;
      }
      return null;
    }

    String parameterTypeNames(MethodNode method) {
      List<String> names = new ArrayList<>();
      for (Parameter parameter : method.getParameters()) {
        names.add(parameter.getType().getNameWithoutPackage());
      }
      return names.toString();
    }

    boolean signatureCollides(ClassNode clazz, MethodNode companion, MethodNode original) {
      for (MethodNode existing : clazz.getMethods(companion.getName())) {
        if (existing == original) continue;
        if (parametersMatch(existing.getParameters(), companion.getParameters())) return true;
      }
      return false;
    }

    boolean parametersMatch(Parameter[] a, Parameter[] b) {
      if (a.length != b.length) return false;
      for (int i = 0; i < a.length; i++)
        if (!a[i].getType().equals(b[i].getType())) return false;
      return true;
    }

    boolean isSpec(ClassNode clazz) {
      return clazz.isDerivedFrom(nodeCache.Specification);
    }

    boolean isMockInteractionSupport(ClassNode clazz) {
      return clazz.implementsInterface(nodeCache.MockInteractionSupport);
    }

    void processMockInteractionSupport(SourceUnit sourceUnit, ClassNode clazz, ErrorReporter errorReporter, SourceLookup sourceLookup) {
      Supplier<Expression> specRef = () -> AstUtil.createDirectMethodCall(
          VariableExpression.THIS_EXPRESSION, nodeCache.MockInteractionSupport_GetSpecification,
          ArgumentListExpression.EMPTY_ARGUMENTS);
      // allowCreation=true (mocks can be created here); allowStaticScope=false
      // (the spec is reached via this.getSpecification(), so static methods that
      // declare interactions/creation get a clear "static scope" compile error).
      // The located spec is always guarded with Checks.notNull, since
      // getSpecification() may be null if the fixture was never attached.
      ExternalInteractionRewriter rewriter =
          new ExternalInteractionRewriter(nodeCache, errorReporter, sourceLookup, true, false,
              SUPPORT_SPEC_NULL_MESSAGE);
      rewriteDeclaredMethods(clazz, rewriter, specRef);
      if (!sourceUnit.getErrorCollector().hasErrors()) {
        new VariableScopeVisitor(sourceUnit).visitClass(clazz);
      }
    }

    /** Rewrites every concrete method declared directly on {@code clazz} in place. */
    void rewriteDeclaredMethods(ClassNode clazz, ExternalInteractionRewriter rewriter, Supplier<Expression> specRef) {
      for (MethodNode method : clazz.getMethods()) {
        if (method.isAbstract() || method.getDeclaringClass() != clazz) continue;
        rewriter.rewriteInPlace(method, specRef);
      }
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
