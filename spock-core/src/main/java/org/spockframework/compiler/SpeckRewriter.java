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
public class SpeckRewriter extends AbstractSpeckVisitor implements IRewriteResourceProvider {
  private final AstNodeCache nodeCache;
  private final SourceLookup lookup;

  private Speck speck;
  private Method method;
  private Block block;

  private VariableExpression thrownExceptionRef; // reference to speck.__thrown42
  private VariableExpression mockControllerRef; // reference to speck.__mockController42

  private boolean methodHasCondition;
  private boolean movedStatsBackToMethod;
  private int featureMethodCount = 0;
  private int oldValueCount = 0;

  public SpeckRewriter(AstNodeCache nodeCache, SourceLookup lookup) {
    this.nodeCache = nodeCache;
    this.lookup = lookup;
  }

  public void visitSpeck(Speck speck) {
    this.speck = speck;
    moveFieldInitializersIntoFixtureMethods();
    addMockController();
  }

  public void visitSpeckAgain(Speck speck) throws Exception {
    linkFixtureMethodsToParents();
  }

  private void linkFixtureMethodsToParents() {
    if (!isSpeckDerivedFromSpecificationBaseClass()) return;
    
    for (FixtureMethod method : speck.getFixtureMethods())
      method.getStatements().add(0,
          new ExpressionStatement(
              new MethodCallExpression(
                  VariableExpression.SUPER_EXPRESSION,
                  method.getAst().getName(),
                  ArgumentListExpression.EMPTY_ARGUMENTS)));
  }

  private boolean isSpeckDerivedFromSpecificationBaseClass() {
    return speck.getAst().isDerivedFrom(nodeCache.Specification);
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

  private void moveFieldInitializersIntoFixtureMethods() {
    List<FieldNode> fields = speck.getAst().getFields();
    // iterate backwards so that moved initializers in fixture methods are in original order
    ListIterator<FieldNode> iter = fields.listIterator(fields.size());
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
    transplantMethod(method);
    handleWhereBlock(method);
  }

  private void makeMethodPublic(Method method) {
    if (method instanceof HelperMethod) return;

    int modifiers = method.getAst().getModifiers();
    modifiers &= ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED);
    method.getAst().setModifiers(modifiers | Opcodes.ACC_PUBLIC);
  }

  private void transplantMethod(Method method) {
    if (!(method instanceof FeatureMethod)) return;

    MethodNode oldAst = method.getAst();
    MethodNode newAst = copyMethod(oldAst, "__feature" + featureMethodCount++);
    speck.getAst().addMethod(newAst);
    method.setAst(newAst);
    deactivateMethod(oldAst);
  }

  private MethodNode copyMethod(MethodNode method, String newName) {
    // can't hurt to set return type to void
    MethodNode newMethod = new MethodNode(newName, method.getModifiers(),
        ClassHelper.VOID_TYPE, method.getParameters(), method.getExceptions(), method.getCode());
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
  private void handleWhereBlock(Method method) {
    if (!(method instanceof FeatureMethod)) return;
    
    Block block = method.getLastBlock();
    if (!(block instanceof WhereBlock)) {
      Parameter[] params = method.getAst().getParameters();
      if (params.length > 0)
        throw new SyntaxException(params[0], "Feature methods without 'where' block may not declare parameters");
      return;
    }

    new DeepStatementRewriter(this).visitBlock(block);
    WhereBlockRewriter.rewrite((WhereBlock)block, nodeCache);
  }

  public void visitMethodAgain(Method method) {
    this.block = null;
    
    if (methodHasCondition)
      defineValueRecorder(AstUtil.getStatements(method.getAst()));

    if (!movedStatsBackToMethod)
      for (Block b : method.getBlocks())
        method.getStatements().addAll(b.getAst());

    if (method instanceof FeatureMethod)
      // for global required interactions
      method.getStatements().add(createMockControllerCall(MockController.LEAVE_SCOPE));
  }

  public void visitAnyBlock(Block block) {
    this.block = block;
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
      Statement newStat = deep.replace(stat);
      methodHasCondition |= deep.isConditionFound();

      if (stat instanceof AssertStatement)
        iter.set(newStat);
      else if (isExceptionCondition(newStat)) {
        throw new SyntaxException(newStat, "Exception conditions are only allowed in 'then' blocks");
      } else if (statHasInteraction(newStat, deep)) {
        throw new SyntaxException(newStat, "Interactions are only allowed in 'then' blocks");
      } else if (isImplicitCondition(newStat)) {
        iter.set(rewriteImplicitCondition(newStat));
        methodHasCondition = true;
      }
    }
  }

  public void visitThenBlock(ThenBlock block) {
    ListIterator<Statement> iter = block.getAst().listIterator();
    boolean blockHasExceptionCondition = false;
    List<Statement> interactions = new ArrayList<Statement>();

    while(iter.hasNext()) {
      Statement stat = iter.next();
      DeepStatementRewriter deep = new DeepStatementRewriter(this);
      Statement newStat = deep.replace(stat);
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
    return expr != null && AstUtil.isPredefDeclOrCall(expr, Constants.THROWN, 0, 1);
  }

  private void rewriteExceptionCondition(Statement stat, boolean hasExceptionCondition) {
    if (hasExceptionCondition)
      throw new SyntaxException(stat, "a 'then' block may only contain one exception condition");

    Expression expr = AstUtil.getExpression(stat, Expression.class);
    assert expr != null;   
    AstUtil.expandPredefDeclOrCall(expr, getThrownExceptionRef());
  }

  private boolean statHasInteraction(Statement stat, DeepStatementRewriter deep) {
    if (deep.isInteractionFound()) return true;

    Expression expr = AstUtil.getExpression(stat, Expression.class);
    return expr != null && AstUtil.isPredefCall(expr, Constants.INTERACTION, 0, 1);
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

  public Block getCurrentBlock() {
    return block;
  }

  public void defineValueRecorder(List<Statement> stats) {
    // recorder variable needs to be defined in outermost scope,
    // hence we insert it at the beginning of the block
    stats.add(0,
        new ExpressionStatement(
            new DeclarationExpression(
                new VariableExpression("__valueRecorder42"),
                Token.newSymbol(Types.ASSIGN, -1, -1),
                new ConstructorCallExpression(
                    nodeCache.ValueRecorder,
                    ArgumentListExpression.EMPTY_ARGUMENTS))));
  }

  public VariableExpression captureOldValue(Expression oldValue) {
    VariableExpression var = new OldValueExpression(oldValue, "__oldVal" + oldValueCount++);
    DeclarationExpression decl = new DeclarationExpression(
          var,
          Token.newSymbol(Types.ASSIGN, -1, -1),
          oldValue
      );
    decl.setSourcePosition(oldValue);

    // add declaration at end of block immediately preceding when-block
    // avoids any problems if when-block gets wrapped in try-statement
    block.getPrevious().getPrevious().getAst().add(new ExpressionStatement(decl));
    return var;
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
          ClassHelper.DYNAMIC_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());
      speck.getAst().addMethod(gMethod);
      FixtureMethod setup = new FixtureMethod(speck, gMethod);
      setup.addBlock(new AnonymousBlock(setup));
      speck.setSetup(setup);
    }

    return speck.getSetup();
  }

  private FixtureMethod getSetupSpeck() {
    if (speck.getSetupSpeck() == null) {
      MethodNode gMethod = new MethodNode(Constants.SETUP_SPECK_METHOD, Opcodes.ACC_PUBLIC,
          ClassHelper.DYNAMIC_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());
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
                Arrays.<Statement> asList(
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
