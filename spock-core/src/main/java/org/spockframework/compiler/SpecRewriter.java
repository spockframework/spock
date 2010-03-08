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
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

import org.spockframework.compiler.model.*;
import org.spockframework.mock.MockController;
import org.spockframework.util.InternalIdentifiers;
import org.spockframework.util.Identifiers;
import org.spockframework.runtime.SpockRuntime;

/**
 * A Spec visitor responsible for most of the rewriting of a Spec's AST.
 * 
 * @author Peter Niederwieser
 */
// IDEA: mock controller / leaveScope calls should only be inserted when necessary (increases robustness)
public class SpecRewriter extends AbstractSpecVisitor implements IRewriteResourceProvider {
  private final AstNodeCache nodeCache;
  private final SourceLookup lookup;
  private final ErrorReporter errorReporter;

  private Spec spec;
  private int specDepth;
  private Method method;
  private Block block;

  private VariableExpression thrownExceptionRef; // reference to field $spock_thrown; always accessed through getter
  private VariableExpression mockControllerRef;  // reference to field $spock_mockController; always accessed through getter
  private VariableExpression sharedInstanceRef;  // reference to field $spock_sharedInstance

  private boolean methodHasCondition;
  private boolean movedStatsBackToMethod;
  private int fieldInitializerCount = 0;
  private int sharedFieldInitializerCount = 0;
  private int oldValueCount = 0;

  public SpecRewriter(AstNodeCache nodeCache, SourceLookup lookup, ErrorReporter errorReporter) {
    this.nodeCache = nodeCache;
    this.lookup = lookup;
    this.errorReporter = errorReporter;
  }

  public void visitSpec(Spec spec) {
    this.spec = spec;
    specDepth = computeDepth(spec.getAst());
    createThrownExceptionFieldAndRef();
    createMockControllerFieldAndRef();
    createSharedInstanceFieldAndRef();
  }

  private int computeDepth(ClassNode node) {
    if (node.equals(ClassHelper.OBJECT_TYPE) || node.equals(nodeCache.Specification)) return -1;
    return computeDepth(node.getSuperClass()) + 1;
  }

  public void visitSpecAgain(Spec spec) throws Exception {
    addMockControllerFieldInitialization();
  }

  private void createThrownExceptionFieldAndRef() {
    Variable var;

    if (isLowestSpecificationInInheritanceChain())
      var = spec.getAst().addField(
          "$spock_thrown",
          Opcodes.ACC_PROTECTED | Opcodes.ACC_SYNTHETIC,
          ClassHelper.DYNAMIC_TYPE,
          null);
    else
      var = new DynamicVariable("$spock_thrown", false);

    thrownExceptionRef = new VariableExpression(var);
  }

  private void createMockControllerFieldAndRef() {
    Variable var;

    if (isLowestSpecificationInInheritanceChain())
      var = spec.getAst().addField(
          "$spock_mockController",
          Opcodes.ACC_PROTECTED | Opcodes.ACC_SYNTHETIC,
          ClassHelper.DYNAMIC_TYPE,
          null);
    else
      var = new DynamicVariable("$spock_mockController", false);

    mockControllerRef = new VariableExpression(var);
  }

  private void createSharedInstanceFieldAndRef() {
    Variable var;

    if (isLowestSpecificationInInheritanceChain())
      var = spec.getAst().addField(
          InternalIdentifiers.SHARED_INSTANCE_NAME,
          Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, // public s.t. runner can easily set it
          ClassHelper.DYNAMIC_TYPE,
          null);
      else
        var = new DynamicVariable(InternalIdentifiers.SHARED_INSTANCE_NAME, false);

    sharedInstanceRef = new VariableExpression(var);
  }

  private void addMockControllerFieldInitialization() {
    if (!isLowestSpecificationInInheritanceChain()) return;
    
    // mock controller needs to be initialized before all other fields
    getSetup().getStatements().add(0,
        new ExpressionStatement(
            new BinaryExpression(
                mockControllerRef,
                Token.newSymbol(Types.ASSIGN, -1, -1),
                new ConstructorCallExpression(
                    nodeCache.MockController,
                    new FieldExpression(nodeCache.DefaultMockFactory_INSTANCE)))));
  }

  private boolean isLowestSpecificationInInheritanceChain() {
    ClassNode superClass = spec.getAst().getSuperClass();
    return superClass.equals(ClassHelper.OBJECT_TYPE)
        || superClass.equals(nodeCache.Specification);
  }

  public void visitField(Field field) {
    if (field.isShared())
      handleSharedField(field);
    else
      handleNonSharedField(field);
  }

  private void handleSharedField(Field field) {
    changeSharedFieldInternalName(field);
    createSharedFieldGetter(field);
    createSharedFieldSetter(field);
    moveSharedFieldInitializer(field);
    makeSharedFieldProtectedAndVolatile(field);
  }

  // field.getAst().getName() changes, field.getName() remains the same
  private void changeSharedFieldInternalName(Field field) {
    field.getAst().rename(InternalIdentifiers.getSharedFieldName(field.getName()));
  }

  private void createSharedFieldGetter(Field field) {
    String getterName = "get" + MetaClassHelper.capitalize(field.getName());
    MethodNode getter = spec.getAst().getMethod(getterName, Parameter.EMPTY_ARRAY);
    if (getter != null) {
      errorReporter.error(field.getAst(),
          "@Shared field '%s' conflicts with method '%s'; please rename either of them",
          field.getName(), getter.getName());
      return;
    }

    BlockStatement getterBlock = new BlockStatement();
    int visibility = field.getOwner() == null ? AstUtil.getVisibility(field.getAst()) : Opcodes.ACC_PUBLIC;
    getter = new MethodNode(getterName, visibility | Opcodes.ACC_SYNTHETIC,
        ClassHelper.DYNAMIC_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, getterBlock);

    getterBlock.addStatement(
        new ReturnStatement(
            new ExpressionStatement(
                new AttributeExpression(
                    sharedInstanceRef,
                    // use internal name
                    new ConstantExpression(field.getAst().getName())))));

    getter.setSourcePosition(field.getAst());
    spec.getAst().addMethod(getter);
  }

  private void createSharedFieldSetter(Field field) {
    String setterName = "set" + MetaClassHelper.capitalize(field.getName());
    Parameter[] params = new Parameter[] { new Parameter(field.getAst().getType(), "$spock_value") };
    MethodNode setter = spec.getAst().getMethod(setterName, params);
    if (setter != null) {
      errorReporter.error(field.getAst(),
                "@Shared field '%s' conflicts with method '%s'; please rename either of them",
                field.getName(), setter.getName());
      return;
    }

    BlockStatement setterBlock = new BlockStatement();
    int visibility = field.getOwner() == null ? AstUtil.getVisibility(field.getAst()) : Opcodes.ACC_PUBLIC;
    setter = new MethodNode(setterName, visibility | Opcodes.ACC_SYNTHETIC,
        ClassHelper.VOID_TYPE, params, ClassNode.EMPTY_ARRAY, setterBlock);

    setterBlock.addStatement(
        new ExpressionStatement(
            new BinaryExpression(
                new AttributeExpression(
                    sharedInstanceRef,
                    // use internal name
                    new ConstantExpression(field.getAst().getName())),
                Token.newSymbol(Types.ASSIGN, -1, -1),
                new VariableExpression("$spock_value"))));

    setter.setSourcePosition(field.getAst());
    spec.getAst().addMethod(setter);
  }

  private void moveSharedFieldInitializer(Field field) {
    if (field.getAst().getInitialValueExpression() != null)
      moveInitializer(field, getSetupSpec(), sharedFieldInitializerCount++);
  }

  /*
     We would prefer to make shared fields private, but this doesn't work in the following case:

     class BaseSpec {
       public sharedInstance
       private sharedField = 5

       def sharedFieldGetter() { sharedInstance.sharedField }
     }

     class DerivedSpec extends BaseSpec {}

     // when DerivedSpec is run:
     
     def sharedInstance = new DerivedSpec()

     def spec = new DerivedSpec()
     spec.sharedInstance = sharedInstance

     spec.sharedFieldGetter() // would normally occur within a specification

     groovy.lang.MissingPropertyException: No such property: sharedField for class: DerivedSpec

     Solutions:
     1. Make sharedField protected
     2. Route all accesses to sharedField through sharedInstance's (!) sharedFieldGetter()
        (from there, sharedField can be accessed w/o qualification)

     Reasons why we chose 1:
     - A bit easier to implement
     - We might at some point decide that we need to reroute some
       VariableExpression/AttributeExpression/PropertyExpression that accesses
       sharedField directly (but on the wrong instance). However, these kinds of
       expressions cannot be replaced with a MethodCallExpression (calling
       sharedFieldGetter/Setter). Instead, we would have to replace them with
       an AttributeExpression/PropertyExpression of the form sharedInstance.sharedField,
       which would again require as to make sharedField protected.

     If we find that making shared fields protected can potentially cause name clashes within
     an inheritance chain, we should make sure that shared field names are unique within
     a chain. For example, this could be done by mixing in a number that measures
     the distance between the declaring class and class Specification. (That's how it's done
     in Groovy.)
   */
  private static void makeSharedFieldProtectedAndVolatile(Field field) {
    AstUtil.setVisibility(field.getAst(), Opcodes.ACC_PROTECTED);
    field.getAst().setModifiers(field.getAst().getModifiers() | Opcodes.ACC_VOLATILE);
  }

  private void handleNonSharedField(Field field) {
    if (field.getAst().getInitialValueExpression() != null)
      moveInitializer(field, getSetup(), fieldInitializerCount++);
  }

  /*
   * Moves initialization of the given field to the given position of the first block of the given method.
   */
  private void moveInitializer(Field field, Method method, int position) {
    method.getFirstBlock().getAst().add(position,
        new ExpressionStatement(
            new FieldInitializationExpression(field.getAst())));
    field.getAst().setInitialValueExpression(null);
  }

  public void visitMethod(Method method) {
    this.method = method;
    methodHasCondition = false;
    movedStatsBackToMethod = false;

    if (method instanceof FixtureMethod)
      // de-virtualize method s.t. multiple fixture methods along hierarchy can be called independently
      AstUtil.setVisibility(method.getAst(), Opcodes.ACC_PRIVATE);
    else if (method instanceof FeatureMethod) {
      transplantMethod(method);
      handleWhereBlock(method);
    }
  }

  private void transplantMethod(Method method) {
    FeatureMethod feature = (FeatureMethod)method;
    MethodNode oldAst = feature.getAst();
    MethodNode newAst = copyMethod(oldAst, createInternalName(feature));
    spec.getAst().addMethod(newAst);
    feature.setAst(newAst);
    deactivateMethod(oldAst);
  }

  private String createInternalName(FeatureMethod feature) {
    return String.format("$spock_feature_%d_%d", specDepth, feature.getOrdinal());
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

  // NOTE: alternatively, we could remove method with:
  // spec.getAst().getMethods().remove(method)
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
    Block block = method.getLastBlock();
    if (!(block instanceof WhereBlock)) {
      Parameter[] params = method.getAst().getParameters();
      if (params.length > 0)
        errorReporter.error(params[0], "Feature methods without 'where' block may not declare parameters");
      return;
    }

    new DeepStatementRewriter(this).visitBlock(block);
    WhereBlockRewriter.rewrite((WhereBlock)block, nodeCache, errorReporter);
  }

  public void visitMethodAgain(Method method) {
    this.block = null;
    
    if (methodHasCondition)
      defineValueRecorder(method.getStatements());

    if (!movedStatsBackToMethod)
      for (Block b : method.getBlocks())
        method.getStatements().addAll(b.getAst());

    // for global required interactions
    if (method instanceof FeatureMethod)
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
        errorReporter.error(newStat, "Exception conditions are only allowed in 'then' blocks");
      } else if (statHasInteraction(newStat, deep))
        errorReporter.error(newStat, "Interactions are only allowed in 'then' blocks");
      else if (isImplicitCondition(newStat)) {
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
    return expr != null && AstUtil.isBuiltinMemberDeclOrCall(expr, Identifiers.THROWN, 0, 1);
  }

  private void rewriteExceptionCondition(Statement stat, boolean hasExceptionCondition) {
    if (hasExceptionCondition) {
      errorReporter.error(stat, "a 'then' block may only contain one exception condition");
      return;
    }

    Expression expr = AstUtil.getExpression(stat, Expression.class);
    assert expr != null;
    try {
      AstUtil.expandBuiltinMemberDeclOrCall(expr, thrownExceptionRef);
    } catch (InvalidSpecCompileException e) {
      errorReporter.error(e);
    }
  }

  private boolean statHasInteraction(Statement stat, DeepStatementRewriter deep) {
    if (deep.isInteractionFound()) return true;

    Expression expr = AstUtil.getExpression(stat, Expression.class);
    return expr != null && AstUtil.isBuiltinMemberCall(expr, Identifiers.INTERACTION, 0, 1);
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
            mockControllerRef,
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
                new VariableExpression("$spock_valueRecorder"),
                Token.newSymbol(Types.ASSIGN, -1, -1),
                new ConstructorCallExpression(
                    nodeCache.ValueRecorder,
                    ArgumentListExpression.EMPTY_ARGUMENTS))));
  }

  public VariableExpression captureOldValue(Expression oldValue) {
    VariableExpression var = new OldValueExpression(oldValue, "$spock_oldValue" + oldValueCount++);
    DeclarationExpression decl = new DeclarationExpression(
        var,
        Token.newSymbol(Types.ASSIGN, -1, -1),
        oldValue);
    decl.setSourcePosition(oldValue);

    // add declaration at end of block immediately preceding when-block
    // avoids any problems if when-block gets wrapped in try-statement
    block.getPrevious().getPrevious().getAst().add(new ExpressionStatement(decl));
    return var;
  }

  public VariableExpression getMockControllerRef() {
    return mockControllerRef;
  }

  public AstNodeCache getAstNodeCache() {
    return nodeCache;
  }

  private FixtureMethod getSetup() {
    if (spec.getSetup() == null) {
      // method is private s.t. multiple setup methods along hierarchy can be called independently
      MethodNode gMethod = new MethodNode(Identifiers.SETUP, Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC,
          ClassHelper.DYNAMIC_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());
      spec.getAst().addMethod(gMethod);
      FixtureMethod setup = new FixtureMethod(spec, gMethod);
      setup.addBlock(new AnonymousBlock(setup));
      spec.setSetup(setup);
    }

    return spec.getSetup();
  }

  public String getSourceText(ASTNode node) {
    return lookup.lookup(node);
  }

  public ErrorReporter getErrorReporter() {
    return errorReporter;
  }

  private FixtureMethod getSetupSpec() {
    if (spec.getSetupSpec() == null) {
      // method is private s.t. multiple setupSpec methods along hierarchy can be called independently
      MethodNode gMethod = new MethodNode(Identifiers.SETUP_SPEC_METHOD, Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC,
          ClassHelper.DYNAMIC_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());
      spec.getAst().addMethod(gMethod);
      FixtureMethod setupSpec = new FixtureMethod(spec, gMethod);
      setupSpec.addBlock(new AnonymousBlock(setupSpec));
      spec.setSetupSpec(setupSpec);
    }

    return spec.getSetupSpec();
  }

  private void rewriteWhenBlockForExceptionCondition(WhenBlock block) {
    List<Statement> tryStats = block.getAst();
    List<Statement> blockStats = new ArrayList<Statement>();
    block.setAst(blockStats);

    blockStats.add(
        new ExpressionStatement(
            new BinaryExpression(
                thrownExceptionRef,
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
            new Parameter(nodeCache.Throwable, "$spock_ex"),
            new BlockStatement(
                Arrays.asList(
                    new ExpressionStatement(
                    new BinaryExpression(
                        thrownExceptionRef,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        new VariableExpression("$spock_ex")))),
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
