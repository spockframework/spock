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

import static org.spockframework.compiler.Constants.*;
import org.spockframework.compiler.model.*;
import org.spockframework.compiler.model.Speck;
import org.spockframework.dsl.*;
import org.spockframework.util.SyntaxException;

/**
 * Given the abstract syntax tree of a Groovy class representing a Spock
 * specification, builds an object model of the specification.
 *
 * @author Peter Niederwieser
 */
// IDEA: run SpeckParser in phase conversion s.t. all compiler code can share
// the same model; model could be passed between phases by adding/removing
// a special ASTNode (e.g. subclass of EmptyStatement) that holds the model
// TODO: check that each named block has at least one statement (otherwise block labels get lost)
// is this possible given the current Groovy implementation?
// TODO: should we allow multiple setup/given blocks? generally bad style bad sometimes useful
// TODO: disallow inheriting from other Specks (directly or indirectly) until we have agreed on the semantics
// TODO: disallow static methods (e.g. conditions in static helper methods won't work)
// TODO: semantics of interactions in setupSpeck()?
// TODO: implement Ignore on Speck level
public class SpeckParser implements GroovyClassVisitor {
  private Speck speck;

  public Speck build(ClassNode clazz) {
    speck = new Speck(clazz);
    clazz.visitContents(this);
    return speck;
  }

  public void visitClass(ClassNode clazz) {
   throw new UnsupportedOperationException("visitClass");
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
    return AstUtil.isSynthetic(method)
        || !method.getDeclaringClass().equals(speck.getAst())
        || AstUtil.getAnnotation(method, Ignore.class) != null;
  }

  // IDEA: check for misspellings other than wrong capitalization
  private static boolean isFixtureMethod(MethodNode method) {
    String name = method.getName();

    for (String fmName : FIXTURE_METHODS) {
      if (!fmName.equalsIgnoreCase(name)) continue;

      if (!fmName.equals(name))
        throw new SyntaxException(method, "Misspelled '%s()' method (wrong capitalization)", fmName);
      if (method.isStatic())
        throw new SyntaxException(method, "Fixture methods must not be static");

      return true;
    }

    return false;
  }

  private void buildFixtureMethod(MethodNode method) {
    FixtureMethod fixtureMethod = new FixtureMethod(speck, method);

    Block block = new AnonymousBlock(fixtureMethod);
    fixtureMethod.addBlock(block);
    List<Statement> stats = AstUtil.getStatements(method);
    block.getAst().addAll(stats);
    stats.clear();

    String name = method.getName();
    if (name.equals(SETUP)) speck.setSetup(fixtureMethod);
    else if (name.equals(CLEANUP)) speck.setCleanup(fixtureMethod);
    else if (name.equals(SETUP_SPECK)) speck.setSetupSpeck(fixtureMethod);
    else speck.setCleanupSpeck(fixtureMethod);
  }

  // IDEA: recognize feature methods by looking at signature only
  // rationale: current solution can sometimes be unintuitive, e.g.
  // for methods with empty body, or for methods with single assert
  // potential indicators for feature methods:
  // - public visibility (and no fixture method)
  // - no visibility modifier (and no fixture method) (source lookup required?)
  // - method name given as string literal (requires source lookup)
  private static boolean isFeatureMethod(MethodNode method) {
    for (Statement stat : AstUtil.getStatements(method)) {
      String label = stat.getStatementLabel();
      if (label == null) continue;

      if (method.isStatic())
        throw new SyntaxException(method, "Feature methods must not be static");

      return true;
    }

    return false;
  }

  private void buildFeatureMethod(MethodNode method) {
    Method feature = new FeatureMethod(speck, method);
    speck.getMethods().add(feature);
    buildBlocks(feature);
  }

  private void buildHelperMethod(MethodNode method) {  
    Method helper = new HelperMethod(speck, method);
    speck.getMethods().add(helper);

    Block block = helper.addBlock(new AnonymousBlock(helper));
    List<Statement> stats = AstUtil.getStatements(method);
    block.getAst().addAll(stats);
    stats.clear();
  }

  public void visitConstructor(ConstructorNode constructor) {
    if (AstUtil.isSynthetic(constructor)) return;
    throw new SyntaxException(constructor,
"Constructors are not allowed; instead, define a 'setup()' or 'setupSpeck()' method");
   }

  public void visitProperty(PropertyNode node) {}

  public void visitField(FieldNode field) {}

  private void buildBlocks(Method method) {
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

  private Block addBlock(Method method, Statement stat) {
    String label = stat.getStatementLabel();

    for (BlockParseInfo blockInfo: BlockParseInfo.values()) {
	  	if (!label.equals(blockInfo.toString())) continue;

      checkIsValidSuccessor(method, blockInfo, stat.getLineNumber(), stat.getColumnNumber());
      Block block = blockInfo.addNewBlock(method);
      String description = getDescription(stat);
      if (description == null)
        block.getAst().add(stat);
      else
        block.getDescriptions().add(description);

      return block;
		}

		throw new SyntaxException(stat, "Unrecognized block label: " + label);
  }

  private String getDescription(Statement stat) {
    ConstantExpression constExpr = AstUtil.getExpression(stat, ConstantExpression.class);
    return constExpr == null || !(constExpr.getValue() instanceof String) ?
        null : (String)constExpr.getValue();
  }

  private void checkIsValidSuccessor(Method method, BlockParseInfo blockInfo, int line, int column) {
    BlockParseInfo oldBlockInfo = method.getLastBlock().getParseInfo();
    if (!oldBlockInfo.getSuccessors(method).contains(blockInfo))
      throw new SyntaxException(line, column, "'%s' is not allowed here; instead, use one of: %s",
          blockInfo, oldBlockInfo.getSuccessors(method), method.getName(), oldBlockInfo, blockInfo);
  }
}
