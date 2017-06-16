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
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.stmt.Statement;

import static org.spockframework.util.Identifiers.*;
import org.spockframework.compiler.model.*;

import spock.lang.Shared;

/**
 * Given the abstract syntax tree of a Groovy class representing a Spock
 * specification, builds an object model of the specification.
 *
 * @author Peter Niederwieser
 */
public class SpecParser implements GroovyClassVisitor {
  private final ErrorReporter errorReporter;

  private Spec spec;
  private int fieldCount = 0;
  private int featureMethodCount = 0;

  public SpecParser(ErrorReporter errorReporter) {
    this.errorReporter = errorReporter;
  }

  public Spec build(ClassNode clazz) {
    spec = new Spec(clazz);
    clazz.visitContents(this);
    return spec;
  }

  public void visitClass(ClassNode clazz) {
   throw new UnsupportedOperationException("visitClass");
  }

  // might only want to include fields relating to a user-provided
  // definition, but it's hard to tell them apart; for example,
  // field.isSynthetic() is true for a property's backing field
  // although it IS related to a user-provided definition
  public void visitField(FieldNode gField) {
    PropertyNode owner = spec.getAst().getProperty(gField.getName());
    if (gField.isStatic()) return;

    Field field = new Field(spec, gField, fieldCount++);
    field.setShared(AstUtil.hasAnnotation(gField, Shared.class));
    field.setOwner(owner);
    spec.getFields().add(field);
  }

  public void visitProperty(PropertyNode node) {}

  public void visitConstructor(ConstructorNode constructor) {
    if (AstUtil.isSynthetic(constructor)) return;
    if (constructorMayHaveBeenAddedByCompiler(constructor)) return;

    errorReporter.error(constructor,
"Constructors are not allowed; instead, define a 'setup()' or 'setupSpec()' method");
  }

  // In case of joint compilation, Verifier - which may add a default constructor
  // - is run in phase CONVERSION. Additionally, groovyc 1.7 and above no longer
  // marks default constructors as synthetic (following javac). Therefore, we have
  // to add special logic to detect this case.
  private boolean constructorMayHaveBeenAddedByCompiler(ConstructorNode constructor) {
    Parameter[] params = constructor.getParameters();
    Statement firstStat = constructor.getFirstStatement();
    return AstUtil.isJointCompiled(spec.getAst()) && constructor.isPublic()
      && params != null && params.length == 0 && firstStat == null;
  }

  public void visitMethod(MethodNode method) {
    if (isIgnoredMethod(method)) return;

    if (isFixtureMethod(method))
      buildFixtureMethod(method);
    else if (isFeatureMethod(method))
      buildFeatureMethod(method);
    else buildHelperMethod(method);
  }

  private boolean isIgnoredMethod(MethodNode method) {
    return AstUtil.isSynthetic(method);
  }

  // IDEA: check for misspellings other than wrong capitalization
  private boolean isFixtureMethod(MethodNode method) {
    String name = method.getName();

    for (String fmName : FIXTURE_METHODS) {
      if (!fmName.equalsIgnoreCase(name)) continue;

      // assertion: is (meant to be) a fixture method, so we'll return true in the end

      if (method.isStatic())
        errorReporter.error(method, "Fixture methods must not be static");
      if (!fmName.equals(name))
        errorReporter.error(method, "Misspelled '%s()' method (wrong capitalization)", fmName);

      return true;
    }

    return false;
  }

  private void buildFixtureMethod(MethodNode method) {
    FixtureMethod fixtureMethod = new FixtureMethod(spec, method);

    Block block = new AnonymousBlock(fixtureMethod);
    fixtureMethod.addBlock(block);
    List<Statement> stats = AstUtil.getStatements(method);
    block.getAst().addAll(stats);
    stats.clear();

    String name = method.getName();
    if (name.equals(SETUP)) spec.setSetupMethod(fixtureMethod);
    else if (name.equals(CLEANUP)) spec.setCleanupMethod(fixtureMethod);
    else if (name.equals(SETUP_SPEC_METHOD)) spec.setSetupSpecMethod(fixtureMethod);
    else spec.setCleanupSpecMethod(fixtureMethod);
  }

  // IDEA: recognize feature methods by looking at signature only
  // rationale: current solution can sometimes be unintuitive, e.g.
  // for methods with empty body, or for methods with single assert
  // potential indicators for feature methods:
  // - public visibility (and no fixture method)
  // - no visibility modifier (and no fixture method) (source lookup required?)
  // - method name given as string literal (requires source lookup)
  private boolean isFeatureMethod(MethodNode method) {
    for (Statement stat : AstUtil.getStatements(method)) {
      String label = stat.getStatementLabel();
      if (label == null) continue;

      // assertion: is (meant to be) a feature method, so we'll return true in the end
      if (method.isStatic())
        errorReporter.error(method, "Feature methods must not be static");

      return true;
    }

    return false;
  }

  private void buildFeatureMethod(MethodNode method) {
    Method feature = new FeatureMethod(spec, method, featureMethodCount++);
    try {
      buildBlocks(feature);
    } catch (InvalidSpecCompileException e) {
      errorReporter.error(e);
      return;
    }
    spec.getMethods().add(feature);
  }

  private void buildHelperMethod(MethodNode method) {
    Method helper = new HelperMethod(spec, method);
    spec.getMethods().add(helper);

    Block block = helper.addBlock(new AnonymousBlock(helper));
    List<Statement> stats = AstUtil.getStatements(method);
    block.getAst().addAll(stats);
    stats.clear();
  }

  private void buildBlocks(Method method) throws InvalidSpecCompileException {
    List<Statement> stats = AstUtil.getStatements(method.getAst());
    Block currBlock = method.addBlock(new AnonymousBlock(method));

    for (Statement stat : stats) {
      if (stat.getStatementLabel() == null)
        currBlock.getAst().add(stat);
      else
        currBlock = addBlock(method, stat);
    }

    checkIsValidSuccessor(method, BlockParseInfo.METHOD_END,
        method.getAst().getLastLineNumber(), method.getAst().getLastColumnNumber());

    // now that statements have been copied to blocks, the original statement
    // list is cleared; statements will be copied back after rewriting is done
    stats.clear();
  }

  private Block addBlock(Method method, Statement stat) throws InvalidSpecCompileException {
    String label = stat.getStatementLabel();

    for (BlockParseInfo blockInfo: BlockParseInfo.values()) {
	  	if (!label.equals(blockInfo.toString())) continue;

      checkIsValidSuccessor(method, blockInfo, stat.getLineNumber(), stat.getColumnNumber());
      Block block = blockInfo.addNewBlock(method);
      String description = getDescription(stat);
      if (description == null)
        block.getAst().add(stat);
      else {
        if(block.getName().equalsIgnoreCase("when") || block.getName().equalsIgnoreCase("then")
          || block.getName().equalsIgnoreCase("setup")) {
          block.getAst().add(stat);
        }
        block.getDescriptions().add(description);
      }

      return block;
		}

		throw new InvalidSpecCompileException(stat, "Unrecognized block label: " + label);
  }

  private String getDescription(Statement stat) {
    ConstantExpression constExpr = AstUtil.getExpression(stat, ConstantExpression.class);
    return constExpr == null || !(constExpr.getValue() instanceof String) ?
        null : (String)constExpr.getValue();
  }

  private void checkIsValidSuccessor(Method method, BlockParseInfo blockInfo, int line, int column)
      throws InvalidSpecCompileException {
    BlockParseInfo oldBlockInfo = method.getLastBlock().getParseInfo();
    if (!oldBlockInfo.getSuccessors(method).contains(blockInfo))
      throw new InvalidSpecCompileException(line, column, "'%s' is not allowed here; instead, use one of: %s",
          blockInfo, oldBlockInfo.getSuccessors(method), method.getName(), oldBlockInfo, blockInfo);
  }
}
