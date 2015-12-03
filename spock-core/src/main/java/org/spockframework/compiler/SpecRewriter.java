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
import org.spockframework.mock.runtime.MockController;
import org.spockframework.runtime.SpecificationContext;
import org.spockframework.util.*;

/**
 * A Spec visitor responsible for most of the rewriting of a Spec's AST.
 *
 * @author Peter Niederwieser
 */
// IDEA: mock controller / leaveScope calls should only be inserted when necessary (increases robustness)
public class SpecRewriter extends AbstractSpecVisitor implements IRewriteResources {
  private final AstNodeCache nodeCache;
  private final SourceLookup lookup;
  private final ErrorReporter errorReporter;

  private Spec spec;
  private int specDepth;
  private Method method;
  private Block block;

  private boolean methodHasCondition;
  private boolean movedStatsBackToMethod;
  private boolean thenBlockChainHasExceptionCondition; // reset once per chain of then-blocks

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
  }

  private int computeDepth(ClassNode node) {
    if (node.equals(ClassHelper.OBJECT_TYPE) || node.equals(nodeCache.Specification)) return -1;
    return computeDepth(node.getSuperClass()) + 1;
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
    getter = new MethodNode(getterName, determineVisibilityForSharedFieldAccessor(field) | Opcodes.ACC_SYNTHETIC,
        ClassHelper.DYNAMIC_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, getterBlock);

    getterBlock.addStatement(
        new ReturnStatement(
            new ExpressionStatement(
                new AttributeExpression(
                    getSharedInstance(),
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
    setter = new MethodNode(setterName, determineVisibilityForSharedFieldAccessor(field) | Opcodes.ACC_SYNTHETIC,
        ClassHelper.VOID_TYPE, params, ClassNode.EMPTY_ARRAY, setterBlock);

    setterBlock.addStatement(
        new ExpressionStatement(
            new BinaryExpression(
                new AttributeExpression(
                    getSharedInstance(),
                    // use internal name
                    new ConstantExpression(field.getAst().getName())),
                Token.newSymbol(Types.ASSIGN, -1, -1),
                new VariableExpression("$spock_value"))));

    setter.setSourcePosition(field.getAst());
    spec.getAst().addMethod(setter);
  }

  // we try to use the visibility of the original field, but change
  // private to protected to solve http://issues.spockframework.org/detail?id=151
  private int determineVisibilityForSharedFieldAccessor(Field field) {
    if (field.getOwner() == null) { // true field
      int visibility = AstUtil.getVisibility(field.getAst());
      if (visibility == Opcodes.ACC_PRIVATE) visibility = Opcodes.ACC_PROTECTED;
      return visibility;
    } else { // property
      return Opcodes.ACC_PUBLIC;
    }
  }

  private void moveSharedFieldInitializer(Field field) {
    if (field.getAst().getInitialValueExpression() != null)
      moveInitializer(field, getSharedInitializerMethod(), sharedFieldInitializerCount++);
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
      moveInitializer(field, getInitializerMethod(), fieldInitializerCount++);
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

    if (method instanceof FixtureMethod) {
      checkFieldAccessInFixtureMethod(method);
      // de-virtualize method s.t. multiple fixture methods along hierarchy can be called independently
      AstUtil.setVisibility(method.getAst(), Opcodes.ACC_PRIVATE);
    } else if (method instanceof FeatureMethod) {
      transplantMethod(method);
      handleWhereBlock(method);
    }
  }

  private void checkFieldAccessInFixtureMethod(Method method) {
    if (method != spec.getSetupSpecMethod() && method != spec.getCleanupSpecMethod()) return;

    new InstanceFieldAccessChecker(this).check(method);
  }

  private void transplantMethod(Method method) {
    FeatureMethod feature = (FeatureMethod) method;
    MethodNode oldAst = feature.getAst();
    MethodNode newAst = copyMethod(oldAst, createInternalName(feature));
    spec.getAst().addMethod(newAst);
    feature.setAst(newAst);
    AstUtil.deleteMethod(spec.getAst(), oldAst);
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

  // where block must be rewritten before all other blocks
  // s.t. missing method parameters are added; these parameters
  // will then be used by DeepBlockRewriter
  private void handleWhereBlock(Method method) {
    Block block = method.getLastBlock();
    if (!(block instanceof WhereBlock)) return;

    new DeepBlockRewriter(this).visit(block);
    WhereBlockRewriter.rewrite((WhereBlock) block, this);
  }

  public void visitMethodAgain(Method method) {
    this.block = null;

    if (!movedStatsBackToMethod)
      for (Block b : method.getBlocks())
        method.getStatements().addAll(b.getAst());

    // for global required interactions
    if (method instanceof FeatureMethod)
      method.getStatements().add(createMockControllerCall(MockController.LEAVE_SCOPE));

    if (methodHasCondition)
      defineRecorders(method.getStatements(), false);
  }

  public void visitAnyBlock(Block block) {
    this.block = block;
    if (block instanceof ThenBlock) return;

    DeepBlockRewriter deep = new DeepBlockRewriter(this);
    deep.visit(block);
    methodHasCondition |= deep.isConditionFound() || deep.isGroupConditionFound();
  }

  public void visitThenBlock(ThenBlock block) {
    if (block.isFirstInChain()) thenBlockChainHasExceptionCondition = false;

    DeepBlockRewriter deep = new DeepBlockRewriter(this);
    deep.visit(block);
    methodHasCondition |= deep.isConditionFound() || deep.isGroupConditionFound();

    if (deep.isExceptionConditionFound()) {
      if (thenBlockChainHasExceptionCondition) {
        errorReporter.error(deep.getFoundExceptionCondition(), "A chain of 'then' blocks may only have a single exception condition");
      }
      rewriteWhenBlockForExceptionCondition(block.getPrevious(WhenBlock.class));
      thenBlockChainHasExceptionCondition = true;
    }

    moveInteractions(deep.getThenBlockInteractionStats(), block);
  }

  private void moveInteractions(List<Statement> interactions, ThenBlock block) {
    if (interactions.isEmpty()) return;

    ListIterator<Statement> listIterator = block.getAst().listIterator();
    while (listIterator.hasNext()) {
      Statement next = listIterator.next();
      if (interactions.contains(next)) listIterator.remove();
    }

    List<Statement> statsBeforeWhenBlock = block.getPrevious(WhenBlock.class).getPrevious().getAst();

    statsBeforeWhenBlock.add(createMockControllerCall(
        block.isFirstInChain() ? MockController.ENTER_SCOPE : MockController.ADD_BARRIER));

    statsBeforeWhenBlock.addAll(interactions);

    if (block.isFirstInChain())
      // insert at beginning of then-block rather than end of when-block
      // s.t. it's outside of try-block inserted for exception conditions
      block.getAst().add(0, createMockControllerCall(MockController.LEAVE_SCOPE));
  }

  private Statement createMockControllerCall(String methodName) {
    return new ExpressionStatement(
        new MethodCallExpression(
            getMockInvocationMatcher(),
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

  // IRewriteResources members

  public Spec getCurrentSpec() {
    return spec;
  }

  public Method getCurrentMethod() {
    return method;
  }

  public Block getCurrentBlock() {
    return block;
  }

  public void defineRecorders(List<Statement> stats, boolean enableErrorCollector) {
    // recorder variable needs to be defined in outermost scope,
    // hence we insert it at the beginning of the block
    List<Statement> allStats = new ArrayList<Statement>(stats);

    stats.clear();

    stats.add(0,
        new ExpressionStatement(
            new DeclarationExpression(
                new VariableExpression("$spock_valueRecorder", nodeCache.ValueRecorder),
                Token.newSymbol(Types.ASSIGN, -1, -1),
                new ConstructorCallExpression(
                    nodeCache.ValueRecorder,
                    ArgumentListExpression.EMPTY_ARGUMENTS))));
    stats.add(0,
        new ExpressionStatement(
            new DeclarationExpression(
                new VariableExpression("$spock_errorCollector", nodeCache.ErrorCollector),
                Token.newSymbol(Types.ASSIGN, -1, -1),
                new ConstructorCallExpression(
                    nodeCache.ErrorCollector,
                    new ArgumentListExpression(Collections.<Expression>singletonList(new ConstantExpression(enableErrorCollector, true)))))));

    stats.add(
      new TryCatchStatement(
        new BlockStatement(allStats, new VariableScope()),
        new ExpressionStatement(
          AstUtil.createDirectMethodCall(
            new VariableExpression("$spock_errorCollector"),
            nodeCache.ErrorCollector_Validate,
            ArgumentListExpression.EMPTY_ARGUMENTS
          ))));
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

  public MethodCallExpression getSpecificationContext() {
    return AstUtil.createDirectMethodCall(VariableExpression.THIS_EXPRESSION,
        nodeCache.SpecInternals_GetSpecificationContext, ArgumentListExpression.EMPTY_ARGUMENTS);
  }

  public MethodCallExpression getMockInvocationMatcher() {
    return new MethodCallExpression(getSpecificationContext(),
        SpecificationContext.GET_MOCK_CONTROLLER, ArgumentListExpression.EMPTY_ARGUMENTS);
  }

  public MethodCallExpression setThrownException(Expression value) {
    return new MethodCallExpression(getSpecificationContext(),
        SpecificationContext.SET_THROWN_EXCEPTION, new ArgumentListExpression(value));
  }

  public MethodCallExpression getSharedInstance() {
    return new MethodCallExpression(getSpecificationContext(),
        SpecificationContext.GET_SHARED_INSTANCE, ArgumentListExpression.EMPTY_ARGUMENTS);
  }

  public AstNodeCache getAstNodeCache() {
    return nodeCache;
  }

  private FixtureMethod getInitializerMethod() {
    if (spec.getInitializerMethod() == null) {
      // method is private s.t. multiple initializer methods along hierarchy can be called independently
      MethodNode gMethod = new MethodNode(InternalIdentifiers.INITIALIZER_METHOD, Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC,
          ClassHelper.DYNAMIC_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());
      spec.getAst().addMethod(gMethod);
      FixtureMethod method = new FixtureMethod(spec, gMethod);
      method.addBlock(new AnonymousBlock(method));
      spec.setInitializerMethod(method);
    }

    return spec.getInitializerMethod();
  }

  public String getSourceText(ASTNode node) {
    return lookup.lookup(node);
  }

  public ErrorReporter getErrorReporter() {
    return errorReporter;
  }

  private FixtureMethod getSharedInitializerMethod() {
    if (spec.getSharedInitializerMethod() == null) {
      // method is private s.t. multiple initializer methods along hierarchy can be called independently
      MethodNode gMethod = new MethodNode(InternalIdentifiers.SHARED_INITIALIZER_METHOD, Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC,
          ClassHelper.DYNAMIC_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());
      spec.getAst().addMethod(gMethod);
      FixtureMethod method = new FixtureMethod(spec, gMethod);
      method.addBlock(new AnonymousBlock(method));
      spec.setSharedInitializerMethod(method);
    }

    return spec.getSharedInitializerMethod();
  }

  private void rewriteWhenBlockForExceptionCondition(WhenBlock block) {
    List<Statement> tryStats = block.getAst();
    List<Statement> blockStats = new ArrayList<Statement>();
    block.setAst(blockStats);

    blockStats.add(
        new ExpressionStatement(
            setThrownException(ConstantExpression.NULL)));

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
                Arrays.<Statement> asList(
                    new ExpressionStatement(
                      setThrownException(
                          new VariableExpression("$spock_ex")))),
                new VariableScope())));
  }

  /*
   * Moves variable declarations from one statement list to another. Initializer
   * expressions are kept at their former place.
   */
  private void moveVariableDeclarations(List<Statement> from, List<Statement> to) {
    for (Statement stat : from) {
      DeclarationExpression declExpr = AstUtil.getExpression(stat, DeclarationExpression.class);
      if (declExpr == null) continue;

      ((ExpressionStatement) stat).setExpression(
          new BinaryExpression(
              copyLhsVariableExpressions(declExpr),
              Token.newSymbol(Types.ASSIGN, -1, -1),
              declExpr.getRightExpression()));

      declExpr.setRightExpression(createDefaultValueInitializer(declExpr));
      to.add(new ExpressionStatement(declExpr));
    }
  }

  private Expression createDefaultValueInitializer(DeclarationExpression expr) {
    TupleExpression tupleExpr = ObjectUtil.asInstance(expr.getLeftExpression(), TupleExpression.class);
    if (tupleExpr == null) {
      return EmptyExpression.INSTANCE;
    }

    assert expr.isMultipleAssignmentDeclaration();

    // initializing to empty list only works when none of the element types is primitive,
    // so create a proper list expression
    ListExpression listExpr = new ListExpression();
    for (Expression elementExpr : tupleExpr.getExpressions()) {
      Variable variable = (Variable) elementExpr;
      listExpr.addExpression(new ConstantExpression(
          ReflectionUtil.getDefaultValue(variable.getOriginType().getTypeClass())));
    }

    return listExpr;
  }

  private Expression copyLhsVariableExpressions(DeclarationExpression declExpr) {
    if (declExpr.isMultipleAssignmentDeclaration()) {
      ArgumentListExpression result = new ArgumentListExpression();
      for (Expression expr : declExpr.getTupleExpression().getExpressions()) {
        VariableExpression varExpr = (VariableExpression) expr;
        result.addExpression(new VariableExpression(varExpr.getName(), varExpr.getOriginType()));
      }
      return result;
    }

    VariableExpression varExpr = declExpr.getVariableExpression();
    return new VariableExpression(varExpr.getName(), varExpr.getOriginType());
  }
}
