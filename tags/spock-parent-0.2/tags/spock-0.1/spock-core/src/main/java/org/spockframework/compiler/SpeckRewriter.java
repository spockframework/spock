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

import java.util.*;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

import spock.lang.Shared;

import org.spockframework.compiler.model.*;
import org.spockframework.compiler.model.Speck;
import org.spockframework.mock.MockController;
import org.spockframework.runtime.SpockRuntime;
import org.spockframework.util.SyntaxException;

/**
 * A Speck visitor responsible for most of the rewriting of a Speck's AST.
 * 
 * @author Peter Niederwieser
 */
// IDEA: should we impose an order on statements in except and when blocks?
// e.g. interactions/exception conditions/conditions
// this order complies with verification order and makes it obvious which other
// verifications have passed if one verification fails
public class SpeckRewriter extends AbstractSpeckVisitor implements IRewriteResourceProvider {
  private final AstNodeCache nodeCache;
  private final SourceLookup lookup;

  private Speck speck;
  private Method method;

  private VariableExpression thrownExceptionRef; // reference to speck.__thrown42
  private VariableExpression mockControllerRef; // reference to speck.__mockController42

  private boolean methodHasCondition; // TODO: try to make this like for thrownField
  private boolean movedStatsBackToMethod;
  private int featureMethodCount = 0;

  public SpeckRewriter(AstNodeCache nodeCache, SourceLookup lookup) {
    this.nodeCache = nodeCache;
    this.lookup = lookup;
  }

  public void visitSpeck(Speck speck) {
    this.speck = speck;
    rewriteFields();
    addMockController();
  }

  // IDEA: we could omit creating mock controller if whole Speck doesn't
  // contain any interaction definition
  private void addMockController() {
    mockControllerRef =
        new VariableExpression(
            speck.getAst().addField(
                "__mockController42",
                Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC,
                ClassHelper.DYNAMIC_TYPE,
                null));

    // mock controller needs to be initialized before all other fields
    getSetup().getFirstBlock().getAst().add(0,
        new ExpressionStatement(
            new BinaryExpression(
                mockControllerRef,
                Token.newSymbol(Types.ASSIGN, -1, -1),
                new ConstructorCallExpression(
                    nodeCache.MockController,
                    new FieldExpression(nodeCache.DefaultMockFactory_INSTANCE)))));
  }

  private void rewriteFields() {
    List<FieldNode> fields = speck.getAst().getFields();
    // iterate backwards so that moved initializers in fixture methods are in original order
    // TODO: why do we get a ConcurrentModificationException unless we copy the list of fields?
    ListIterator<FieldNode> iter = new ArrayList<FieldNode>(fields).listIterator(fields.size());
    while (iter.hasPrevious()) {
      FieldNode field = iter.previous();
      // filter out fields internal to the Groovy implementation (e.g. $ownClass)
      // since Groovy 1.6-beta-2, field.isSynthetic() can no longer be used for this purpose,
      // because fields underlying simple property definitions are now also considered synthetic
      if (field.getName().startsWith("$") || field.isStatic()) continue;

      if (AstUtil.hasAnnotation(field, Shared.class))
        moveInitializer(field, getSetupSpeck());
      else
        moveInitializer(field, getSetup());
    }
  }

  /*
   * Moves initialization of the given field to the beginning of the first block of the given method.
   */
  private static void moveInitializer(FieldNode field, Method method) {
    method.getFirstBlock().getAst().add(0,
        new ExpressionStatement(
            new FieldInitializerExpression(field)));

    field.setInitialValueExpression(null);
  }

  public void visitMethod(Method method) {
    this.method = method;
    methodHasCondition = false;
    movedStatsBackToMethod = false;

    makeMethodPublic(method);
    transplantFeatureMethod(method);
    rewriteWhereBlock(method);
  }

  private void makeMethodPublic(Method method) {
    if (method instanceof HelperMethod) return;

    int modifiers = method.getAst().getModifiers();
    modifiers &= ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED);
    method.getAst().setModifiers(modifiers | Opcodes.ACC_PUBLIC);
  }

  private void transplantFeatureMethod(Method method) {
    if (!(method instanceof FeatureMethod)) return;

    MethodNode oldAst = method.getAst();
    MethodNode newAst = copyMethod(oldAst, "__feature" + featureMethodCount++);
    speck.getAst().addMethod(newAst);
    method.setAst(newAst);
    deactivateMethod(oldAst);
  }

  private MethodNode copyMethod(MethodNode method, String newName) {
    MethodNode newMethod = new MethodNode(newName, method.getModifiers(),
        method.getReturnType(), method.getParameters(), method.getExceptions(), method.getCode());
    newMethod.addAnnotations(method.getAnnotations());
    newMethod.setSynthetic(method.isSynthetic());
    newMethod.setDeclaringClass(method.getDeclaringClass());
    newMethod.setSourcePosition(method);
    newMethod.setVariableScope(method.getVariableScope());
    newMethod.setGenericsTypes(method.getGenericsTypes());
    newMethod.setAnnotationDefault(method.hasAnnotationDefault());

    return newMethod;
  }

  private void deactivateMethod(MethodNode method) {
    Statement stat = new ExpressionStatement(
        new MethodCallExpression(
            new ClassExpression(nodeCache.SpockRuntime),
            new ConstantExpression(SpockRuntime.FEATURE_METHOD_CALLED),
            ArgumentListExpression.EMPTY_ARGUMENTS));

    BlockStatement block = new BlockStatement();
    block.addStatement(stat);
    method.setCode(block);
    method.setReturnType(ClassHelper.VOID_TYPE);
    method.setVariableScope(new VariableScope());
  }

  // where block must be rewritten before all other blocks
  // s.t. missing method parameters are added; these parameters
  // will then be used by DeepStatementRewriter.fixupVariableScope()
  private void rewriteWhereBlock(Method method) {
    if (method.getLastBlock() instanceof WhereBlock)
      WhereBlockRewriter.rewrite((WhereBlock)method.getLastBlock());
  }

  public void visitMethodAgain(Method method) {
    if (methodHasCondition)
      defineValueRecorder(AstUtil.getStatements(method.getAst()));

    if (!movedStatsBackToMethod)
      for (Block b : method.getBlocks())
        method.getStatements().addAll(b.getAst());
  }

  public void visitAnyBlock(Block block) {
    if (block instanceof ExpectBlock || block instanceof ThenBlock) return;

    DeepStatementRewriter deep = new DeepStatementRewriter(this);
    deep.visitBlock(block);
    methodHasCondition |= deep.isConditionFound();
  }

  public void visitExpectBlock(ExpectBlock block) {
    ListIterator<Statement> iter = block.getAst().listIterator();

    while(iter.hasNext()) {
      Statement stat = iter.next();

      DeepStatementRewriter deep = new DeepStatementRewriter(this);
      Statement newStat = deep.visit(stat);
      methodHasCondition |= deep.isConditionFound();

      if (stat instanceof AssertStatement)
        iter.set(newStat);
      else if (isExceptionCondition(newStat)) {
        // TODO (not allowed)
      } else if (statHasInteraction(newStat, deep)) {
        // TODO (not allowed)
      } else if (isImplicitCondition(newStat)) {
        iter.set(rewriteImplicitCondition(newStat));
        methodHasCondition = true;
      }
    }
  }

  // TODO: should we allow sth. other than assert statements
  // and expression statements as top-level statements in then-blocks?
  public void visitThenBlock(ThenBlock block) {
    ListIterator<Statement> iter = block.getAst().listIterator();
    boolean blockHasExceptionCondition = false;
    List<Statement> interactions = new ArrayList<Statement>();

    while(iter.hasNext()) {
      Statement stat = iter.next();
      DeepStatementRewriter deep = new DeepStatementRewriter(this);
      Statement newStat = deep.visit(stat);
      methodHasCondition |= deep.isConditionFound();

      if (stat instanceof AssertStatement)
        iter.set(newStat);
      else if (isExceptionCondition(newStat)) {
        rewriteExceptionCondition(newStat, blockHasExceptionCondition);
        rewriteWhenBlockForExceptionCondition(block.getPrevious());
        blockHasExceptionCondition = true;
      } else if (statHasInteraction(newStat, deep)) {
        interactions.add(newStat);
        iter.remove();
      } else if (isImplicitCondition(newStat)) {
        iter.set(rewriteImplicitCondition(newStat));
        methodHasCondition = true;
      }
    }
    
    insertInteractions(interactions, block.getPrevious());
  }

  private boolean isImplicitCondition(Statement stat) {
    return stat instanceof ExpressionStatement
        && !(((ExpressionStatement)stat).getExpression() instanceof DeclarationExpression);
  }

  private Statement rewriteImplicitCondition(Statement stat) {
    return ConditionRewriter.rewriteImplicitCondition((ExpressionStatement)stat, this);
  }

  private boolean isExceptionCondition(Statement stat) {
    Expression expr = AstUtil.getExpression(stat, Expression.class);
    return expr != null && AstUtil.isPredefDeclOrCall(expr, Constants.THROWN, 1);
  }

  private void rewriteExceptionCondition(Statement stat, boolean hasExceptionCondition) {
    if (hasExceptionCondition)
      throw new SyntaxException(stat, "a (group of) 'then' block(s) may only contain one exception condition");

    Expression expr = AstUtil.getExpression(stat, Expression.class);
    assert expr != null;   
    AstUtil.expandPredefDeclOrCall(expr, getThrownExceptionRef());
  }

  private static boolean statHasInteraction(Statement stat, DeepStatementRewriter deep) {
    if (deep.isInteractionFound()) return true;

    Expression expr = AstUtil.getExpression(stat, Expression.class);
    return expr != null && AstUtil.isPredefCall(expr, Constants.INTERACTION, 1);
  }

  private void insertInteractions(List<Statement> interactions, WhenBlock whenBlock) {
    if (interactions.isEmpty()) return;

    List<Statement> prevStats = whenBlock.getPrevious().getAst();
    prevStats.add(createMockControllerCall(MockController.ENTER_SCOPE));
    prevStats.addAll(interactions);

    // insert at beginning of then-block rather than end of when-block
    // s.t. it's outside of try-block inserted for exception conditions
    List<Statement> thenStats = whenBlock.getNext().getAst();
    thenStats.add(0, createMockControllerCall(MockController.LEAVE_SCOPE));
  }

  private Statement createMockControllerCall(String methodName) {
    return new ExpressionStatement(
        new MethodCallExpression(
            getMockControllerRef(),
            methodName,
            ArgumentListExpression.EMPTY_ARGUMENTS));
  }

  // TODO: what should we do if cleanup-block throws an exception?
  public void visitCleanupBlock(CleanupBlock block) {
    for (Block b : method.getBlocks()) {
      if (b == block) break;
      moveVariableDeclarations(b.getAst(), method.getStatements());
    }

    List<Statement> tryStats = new ArrayList<Statement>();
    for (Block b : method.getBlocks()) {
      if (b == block) break;
      tryStats.addAll(b.getAst());
    }

    List<Statement> finallyStats = new ArrayList<Statement>();
    finallyStats.addAll(block.getAst());

    TryCatchStatement tryFinally =
        new TryCatchStatement(
            new BlockStatement(tryStats, new VariableScope()),
            new BlockStatement(finallyStats, new VariableScope()));

    method.getStatements().add(tryFinally);

    // a cleanup-block may only be followed by a where-block, whose
    // statements are copied to newly generated methods rather than
    // the original method
    movedStatsBackToMethod = true;
  }

  // IRewriteResourceProvider members

  public Method getCurrentMethod() {
    return method;
  }

   public void defineValueRecorder(List<Statement> stats) {
    // recorder variable needs to be defined in outermost scope,
    // hence we insert it at the beginning of the block
    // NOTE: when the following two statements are combined into one,
    // a MissingPropertyException is thrown at runtime - why?
    stats.add(0,
        new ExpressionStatement(
            new DeclarationExpression(
                new VariableExpression("__valueRecorder42"),
                Token.newSymbol(Types.ASSIGN, -1, -1),
                new ConstantExpression(null))));

    stats.add(1,
        new ExpressionStatement(
            new BinaryExpression(
                new VariableExpression("__valueRecorder42"),
                Token.newSymbol(Types.ASSIGN, -1, -1),
                new ConstructorCallExpression(
                    nodeCache.ValueRecorder,
                    ArgumentListExpression.EMPTY_ARGUMENTS))));
  }

  public VariableExpression getThrownExceptionRef() {
    if (thrownExceptionRef == null)
      thrownExceptionRef =
          new VariableExpression(
              speck.getAst().addField(
                  "__thrown42",
                  Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC,
                  ClassHelper.DYNAMIC_TYPE,
                  null));

    return thrownExceptionRef;
  }

  public VariableExpression getMockControllerRef() {
    return mockControllerRef;
  }

  public AstNodeCache getAstNodeCache() {
    return nodeCache;
  }

  private FixtureMethod getSetup() {
    if (speck.getSetup() == null) {
      MethodNode gMethod = new MethodNode(Constants.SETUP, Opcodes.ACC_PUBLIC,
          ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());
      speck.getAst().addMethod(gMethod);
      FixtureMethod setup = new FixtureMethod(speck, gMethod);
      setup.addBlock(new AnonymousBlock(setup));
      speck.setSetup(setup);
    }

    return speck.getSetup();
  }

  private FixtureMethod getSetupSpeck() {
    if (speck.getSetupSpeck() == null) {
      MethodNode gMethod = new MethodNode(Constants.SETUP_SPECK, Opcodes.ACC_PUBLIC,
          ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());
      speck.getAst().addMethod(gMethod);
      FixtureMethod setupSpeck = new FixtureMethod(speck, gMethod);
      setupSpeck.addBlock(new AnonymousBlock(setupSpeck));
      speck.setSetupSpeck(setupSpeck);
    }

    return speck.getSetupSpeck();
  }

  public String getSourceText(ASTNode node) {
    return lookup.lookup(node);
  }

  private void rewriteWhenBlockForExceptionCondition(WhenBlock block) {
    List<Statement> tryStats = block.getAst();
    List<Statement> blockStats = new ArrayList<Statement>();
    block.setAst(blockStats);

    blockStats.add(
        new ExpressionStatement(
            new BinaryExpression(
                getThrownExceptionRef(),
                Token.newSymbol(Types.ASSIGN, -1, -1),
                ConstantExpression.NULL)));

    // ensure variables remain in same scope
    // by moving variable defs to the very beginning of the method, they don't
    // need to be moved again if feature method also has a cleanup-block
    moveVariableDeclarations(tryStats, method.getStatements());

    TryCatchStatement tryCatchStat =
        new TryCatchStatement(
            new BlockStatement(tryStats, new VariableScope()),
            new BlockStatement());

    blockStats.add(tryCatchStat);

    tryCatchStat.addCatch(
        new CatchStatement(
            new Parameter(nodeCache.Throwable, "__e42"),
            new BlockStatement(
                Arrays.asList(
                    new ExpressionStatement(
                    new BinaryExpression(
                        getThrownExceptionRef(),
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        new VariableExpression("__e42")))),
                new VariableScope())));
  }

  /*
   * Moves variable declarations from one statement list to another. Initializer
   * expressions are kept at their former place.
   */
  private static void moveVariableDeclarations(List<Statement> from, List<Statement> to) {
    for (Statement stat : from) {
      if (!(stat instanceof ExpressionStatement)) continue;
      ExpressionStatement exprStat = (ExpressionStatement)stat;
      if (!(exprStat.getExpression() instanceof DeclarationExpression)) continue;
      DeclarationExpression declExpr = (DeclarationExpression)exprStat.getExpression();

      exprStat.setExpression(
          new BinaryExpression(
              new VariableExpression(declExpr.getVariableExpression().getName()),
              Token.newSymbol(Types.ASSIGN, -1, -1),
              declExpr.getRightExpression()));

      declExpr.setRightExpression(ConstantExpression.NULL);
      to.add(new ExpressionStatement(declExpr));
    }
  }
}
