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

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spockframework.compiler.model.*;
import org.spockframework.runtime.SpockException;
import org.spockframework.util.InternalIdentifiers;
import org.spockframework.util.ObjectUtil;
import org.spockframework.util.ReflectionUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.spockframework.compiler.AstUtil.createDirectMethodCall;

/**
 * A Spec visitor responsible for most of the rewriting of a Spec's AST.
 *
 * @author Peter Niederwieser
 */
// IDEA: mock controller / leaveScope calls should only be inserted when necessary (increases robustness)
public class SpecRewriter extends AbstractSpecVisitor implements IRewriteResources {
  // https://issues.apache.org/jira/browse/GROOVY-10403
  // needed for groovy-4 compatibility and only available since groovy-4
  private static final java.lang.reflect.Method GET_PLAIN_NODE_REFERENCE = ReflectionUtil.getMethodBySignature(ClassNode.class, "getPlainNodeReference", boolean.class);
  public static final String SPOCK_VALUE = "$spock_value";
  public static final String SPOCK_FEATURE_THROWABLE = "$spock_feature_throwable";
  public static final String SPOCK_TMP_THROWABLE = "$spock_tmp_throwable";

  private final AstNodeCache nodeCache;
  private final SourceLookup lookup;
  private final ErrorReporter errorReporter;

  private Spec spec;
  private int specDepth;
  private Method method;
  private Block block;

  private boolean methodHasCondition;
  private boolean methodHasDeepNonGroupedCondition;
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

  @Override
  public void visitSpec(Spec spec) {
    this.spec = spec;
    specDepth = computeDepth(spec.getAst());
  }

  private int computeDepth(ClassNode node) {
    if (node.equals(ClassHelper.OBJECT_TYPE) || node.equals(nodeCache.Specification)) return -1;
    return computeDepth(node.getSuperClass()) + 1;
  }

  @Override
  public void visitField(Field field) {
    if (field.isShared())
      handleSharedField(field);
    else
      handleNonSharedField(field);
  }

  private void handleSharedField(Field field) {
    changeSharedFieldInternalName(field);
    createSharedFieldGetter(field);
    if (!field.getAst().isFinal()) {
      createSharedFieldSetter(field);
    }
    moveSharedFieldInitializer(field);
    makeSharedFieldProtectedAndVolatile(field);
    AstUtil.setFinal(field.getAst(), false);
  }

  // field.getAst().getName() changes, field.getName() remains the same
  private void changeSharedFieldInternalName(Field field) {
    field.getAst().rename(InternalIdentifiers.getSharedFieldName(field.getName()));
  }

  private void changeFinalFieldInternalName(Field field) {
    field.getAst().rename(InternalIdentifiers.getFinalFieldName(field.getName()));
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
        field.getAst().getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, getterBlock);

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

  private void createFinalFieldGetter(Field field) {
    String getterName = "get" + MetaClassHelper.capitalize(field.getName());
    MethodNode getter = spec.getAst().getMethod(getterName, Parameter.EMPTY_ARRAY);
    if (getter != null) {
      errorReporter.error(field.getAst(),
          "Final field '%s' conflicts with method '%s'; please rename either of them",
          field.getName(), getter.getName());
      return;
    }

    BlockStatement getterBlock = new BlockStatement();
    getter = new MethodNode(getterName, determineVisibilityForFinalFieldAccessor(field) | Opcodes.ACC_SYNTHETIC,
        field.getAst().getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, getterBlock);

    getterBlock.addStatement(
        new ReturnStatement(
            new ExpressionStatement(
                new VariableExpression(field.getAst().getName(), field.getAst().getType()))));

    getter.setSourcePosition(field.getAst());
    spec.getAst().addMethod(getter);
  }

  private void createSharedFieldSetter(Field field) {
    String setterName = "set" + MetaClassHelper.capitalize(field.getName());
    Parameter[] params = new Parameter[] { new Parameter(field.getAst().getType(), SPOCK_VALUE) };
    MethodNode setter = spec.getAst().getMethod(setterName, params);
    if (setter != null) {
      errorReporter.error(field.getAst(),
                "@Shared field '%s' conflicts with method '%s'; please rename either of them",
                field.getName(), setter.getName());
      return;
    }

    BlockStatement setterBlock = new BlockStatement();
    setter = new MethodNode(setterName, determineVisibilityForSharedFieldAccessor(field) | Opcodes.ACC_SYNTHETIC,
        getPlainReference(ClassHelper.VOID_TYPE), params, ClassNode.EMPTY_ARRAY, setterBlock);

    setterBlock.addStatement(
        new ExpressionStatement(
            new BinaryExpression(
                new AttributeExpression(
                    getSharedInstance(),
                    // use internal name
                    new ConstantExpression(field.getAst().getName())),
                Token.newSymbol(Types.ASSIGN, -1, -1),
                new VariableExpression(SPOCK_VALUE))));

    setter.setSourcePosition(field.getAst());
    spec.getAst().addMethod(setter);
  }

  // we try to use the visibility of the original field, but change
  // private to protected to solve https://github.com/spockframework/spock/issues/273
  private int determineVisibilityForSharedFieldAccessor(Field field) {
    if (field.getOwner() == null) { // true field
      int visibility = AstUtil.getVisibility(field.getAst());
      if (visibility == Opcodes.ACC_PRIVATE) visibility = Opcodes.ACC_PROTECTED;
      return visibility;
    } else { // property
      return Opcodes.ACC_PUBLIC;
    }
  }

  private int determineVisibilityForFinalFieldAccessor(Field field) {
    if (field.getOwner() == null) { // true field
      return AstUtil.getVisibility(field.getAst());
    } else { // property
      return Opcodes.ACC_PUBLIC;
    }
  }

  private void moveSharedFieldInitializer(Field field) {
    if (field.getAst().getInitialValueExpression() != null) {
      moveInitializer(field, getSharedInitializerMethod(), sharedFieldInitializerCount++);
    } else if (field.getAst().isFinal()) {
      errorReporter.error(field.getAst(),
              "@Shared final field '%s' is not initialized.",
              field.getName());
    }
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
    boolean wasFinal = field.getAst().isFinal();
    if (wasFinal) {
      changeFinalFieldInternalName(field);
      createFinalFieldGetter(field);
      AstUtil.setFinal(field.getAst(), false);
    }
    if (field.getAst().getInitialValueExpression() != null) {
      moveInitializer(field, getInitializerMethod(), fieldInitializerCount++);
    } else if (wasFinal) {
      errorReporter.error(field.getAst(),
              "Final field '%s' is not initialized.",
              field.getName());
    }
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

  @Override
  public void visitMethod(Method method) {
    this.method = method;
    methodHasCondition = false;
    methodHasDeepNonGroupedCondition = false;
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

    Iterator<InnerClassNode> innerClassesOfDeclaringClass = oldAst.getDeclaringClass().getInnerClasses();
    while (innerClassesOfDeclaringClass.hasNext()) {
      InnerClassNode innerClass = innerClassesOfDeclaringClass.next();
      if (oldAst.equals(innerClass.getEnclosingMethod())) {
        innerClass.setEnclosingMethod(newAst);
      }
    }
  }

  private String createInternalName(FeatureMethod feature) {
    return String.format("$spock_feature_%d_%d", specDepth, feature.getOrdinal());
  }

  private MethodNode copyMethod(MethodNode method, String newName) {
    // can't hurt to set return type to void
    MethodNode newMethod = new MethodNode(newName, method.getModifiers(),
      getPlainReference(ClassHelper.VOID_TYPE), method.getParameters(), method.getExceptions(), method.getCode());

    newMethod.addAnnotations(method.getAnnotations());
    newMethod.setSynthetic(method.isSynthetic());
    newMethod.setDeclaringClass(method.getDeclaringClass());
    newMethod.setSourcePosition(method);
    newMethod.setVariableScope(method.getVariableScope());
    newMethod.setGenericsTypes(method.getGenericsTypes());
    newMethod.setAnnotationDefault(method.hasAnnotationDefault());

    return newMethod;
  }

  private ClassNode getPlainReference(ClassNode type) {
    // https://issues.apache.org/jira/browse/GROOVY-10403
    // needed for groovy-4 compatibility
    if (GET_PLAIN_NODE_REFERENCE != null) {
      try {
        return (ClassNode) GET_PLAIN_NODE_REFERENCE.invoke(type, false);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new SpockException("Could not create plain reference");
      }
    }
    return type;
  }

  // where block must be rewritten before all other blocks
  // s.t. missing method parameters are added; these parameters
  // will then be used by DeepBlockRewriter
  private void handleWhereBlock(Method method) {
    Block lastblock = method.getLastBlock();
    FilterBlock filterBlock;
    WhereBlock whereBlock;
    if (lastblock instanceof FilterBlock) {
      filterBlock = (FilterBlock) lastblock;
      whereBlock = (WhereBlock) lastblock.getPrevious();
    } else if (lastblock instanceof WhereBlock) {
      filterBlock = null;
      whereBlock = (WhereBlock) lastblock;
    } else {
      return;
    }

    DeepBlockRewriter deep = new DeepBlockRewriter(this);
    deep.visit(whereBlock);
    WhereBlockRewriter.rewrite(whereBlock, filterBlock, this, deep.isDeepNonGroupedConditionFound());
  }

  @Override
  public void visitMethodAgain(Method method) {
    this.block = null;

    if (!movedStatsBackToMethod) {
      for (Block b : method.getBlocks()) {
        // This will only run if there was no 'cleanup' block in the method.
        // Otherwise, the blocks have already been copied to try block by visitCleanupBlock.
        // We need to run as late as possible, so we'll have to do the handling here and in visitCleanupBlock.
        addBlockListeners(b);
        method.getStatements().addAll(b.getAst());
      }
    }

    // for global required interactions
    if (method instanceof FeatureMethod) {
      method.getStatements().add(createMockControllerCall(nodeCache.MockController_LeaveScope));
    }

    if (methodHasCondition) {
      defineValueRecorder(method.getStatements(), "");
    }
    if (methodHasDeepNonGroupedCondition) {
      defineErrorRethrower(method.getStatements());
    }
  }


  private void addBlockListeners(Block block) {
    BlockParseInfo blockType = block.getParseInfo();
    if (blockType == BlockParseInfo.WHERE
      || blockType == BlockParseInfo.METHOD_END
      || blockType == BlockParseInfo.COMBINED
      || blockType == BlockParseInfo.ANONYMOUS) return;

    // SpockRuntime.enterBlock(getSpecificationContext(), new BlockInfo(blockKind, blockMetaDataIndex))
    MethodCallExpression enterBlockCall = createBlockListenerCall(block, blockType, nodeCache.SpockRuntime_CallEnterBlock);
    // SpockRuntime.exitedBlock(getSpecificationContext(), new BlockInfo(blockKind, blockMetaDataIndex))
    MethodCallExpression exitBlockCall = createBlockListenerCall(block, blockType, nodeCache.SpockRuntime_CallExitBlock);

    if (blockType == BlockParseInfo.CLEANUP) {
      // In case of a cleanup block we need store a reference of the previously `currentBlock` in case that an exception occurred
      // and restore it at the end of the cleanup block, so that the correct `BlockInfo` is available for the `IErrorContext`.
      // The restoration happens in the `finally` statement created by `createCleanupTryCatch`.
      VariableExpression failedBlock = new VariableExpression(SpockNames.FAILED_BLOCK, nodeCache.BlockInfo);
      block.getAst().addAll(0, asList(
        ifThrowableIsNotNull(storeFailedBlock(failedBlock)),
        new ExpressionStatement(enterBlockCall)
      ));
      block.getAst().add(new ExpressionStatement(exitBlockCall));
    } else {
      block.getAst().add(0, new ExpressionStatement(enterBlockCall));
      block.getAst().add(new ExpressionStatement(exitBlockCall));
    }
  }

  private @NotNull Statement storeFailedBlock(VariableExpression failedBlock) {
    MethodCallExpression getCurrentBlock = createDirectMethodCall(getSpecificationContext(), nodeCache.SpecificationContext_GetBlockCurrentBlock, ArgumentListExpression.EMPTY_ARGUMENTS);
    return new ExpressionStatement(new BinaryExpression(failedBlock, Token.newSymbol(Types.ASSIGN, -1, -1), getCurrentBlock));
  }

  private @NotNull Statement restoreFailedBlock(VariableExpression failedBlock) {
    return new ExpressionStatement(createDirectMethodCall(new CastExpression(nodeCache.SpecificationContext, getSpecificationContext()), nodeCache.SpecificationContext_SetBlockCurrentBlock, new ArgumentListExpression(failedBlock)));
  }

  private IfStatement ifThrowableIsNotNull(Statement statement) {
    return new IfStatement(
      // if ($spock_feature_throwable != null)
      new BooleanExpression(AstUtil.createVariableIsNotNullExpression(new VariableExpression(SpecRewriter.SPOCK_FEATURE_THROWABLE, nodeCache.Throwable))),
      statement,
      EmptyStatement.INSTANCE
    );
  }

  private MethodCallExpression createBlockListenerCall(Block block, BlockParseInfo blockType, MethodNode blockListenerMethod) {
    if (block.getBlockMetaDataIndex() < 0) throw new SpockException("Block metadata index not set: " + block);
    return createDirectMethodCall(
      new ClassExpression(nodeCache.SpockRuntime),
      blockListenerMethod,
      new ArgumentListExpression(
        getSpecificationContext(),
        new ConstantExpression(block.getBlockMetaDataIndex(), true)
      ));
  }

  @Override
  public void visitAnyBlock(Block block) {
    this.block = block;
    if (block instanceof ThenBlock) return;

    DeepBlockRewriter deep = new DeepBlockRewriter(this);
    deep.visit(block);
    methodHasCondition |= deep.isConditionFound();
    methodHasDeepNonGroupedCondition |= deep.isDeepNonGroupedConditionFound();
  }

  @Override
  public void visitThenBlock(ThenBlock block) {
    if (block.isFirstInChain()) thenBlockChainHasExceptionCondition = false;

    DeepBlockRewriter deep = new DeepBlockRewriter(this);
    deep.visit(block);
    methodHasCondition |= deep.isConditionFound();
    methodHasDeepNonGroupedCondition |= deep.isDeepNonGroupedConditionFound();

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

    block.getAst().removeIf(interactions::contains);

    List<Statement> statsBeforeWhenBlock = block.getPrevious(WhenBlock.class).getPrevious().getAst();

    statsBeforeWhenBlock.add(createMockControllerCall(
        block.isFirstInChain() ? nodeCache.MockController_EnterScope : nodeCache.MockController_AddBarrier));

    statsBeforeWhenBlock.addAll(interactions);

    if (block.isFirstInChain())
      // insert at beginning of then-block rather than end of when-block
      // s.t. it's outside of try-block inserted for exception conditions
      block.getAst().add(0, createMockControllerCall(nodeCache.MockController_LeaveScope));
  }

  private Statement createMockControllerCall(MethodNode method) {
    return new ExpressionStatement(createDirectMethodCall(getMockInvocationMatcher(), method, ArgumentListExpression.EMPTY_ARGUMENTS));
  }

  /*
    This emulates Java 7 try-with-resources exception handling

    Throwable $spock_feature_throwable = null;

    try {
      // feature statements (setup, when/then, expect, etc. blocks)
    } catch (Throwable $spock_tmp_throwable) {
      $spock_feature_throwable = $spock_tmp_throwable;
      throw $spock_tmp_throwable;
    } finally {
      try {
        // feature cleanup statements (cleanup block)
      } catch (Throwable $spock_tmp_throwable) {
        if($spock_feature_throwable != null) {
            $spock_feature_throwable.addSuppressed($spock_tmp_throwable);
        } else {
            throw $spock_tmp_throwable;
        }
      }
    }
  */
  @Override
  public void visitCleanupBlock(CleanupBlock block) {
    for (Block b : method.getBlocks()) {
      // call addBlockListeners() here, as this method will already copy the contents of the blocks,
      // so we need to transform the block listeners here as they won't be copied in visitMethodAgain where we normally add them
      addBlockListeners(b);
      if (b == block) break;
      moveVariableDeclarations(b.getAst(), method.getStatements());
    }

    VariableExpression featureThrowableVar =
        new VariableExpression(SPOCK_FEATURE_THROWABLE, nodeCache.Throwable);
    method.getStatements().add(createVariableDeclarationStatement(featureThrowableVar));

    List<Statement> featureStats = new ArrayList<>();
    for (Block b : method.getBlocks()) {
      if (b == block) break;
      featureStats.addAll(b.getAst());
    }

    CatchStatement featureCatchStat = createThrowableAssignmentAndRethrowCatchStatement(featureThrowableVar);
    VariableExpression failedBlock = new VariableExpression(SpockNames.FAILED_BLOCK, nodeCache.BlockInfo);
    List<Statement> cleanupStats = asList(
        new ExpressionStatement(new DeclarationExpression(failedBlock, Token.newSymbol(Types.ASSIGN, -1, -1), ConstantExpression.NULL)),
        createCleanupTryCatch(block, featureThrowableVar, failedBlock));

    TryCatchStatement tryFinally =
        new TryCatchStatement(
            new BlockStatement(featureStats, null),
            new BlockStatement(cleanupStats, null));
    tryFinally.addCatch(featureCatchStat);

    method.getStatements().add(tryFinally);

    // a cleanup-block may only be followed by a where-block and filter-block, whose
    // statements are copied to newly generated methods rather than
    // the original method
    movedStatsBackToMethod = true;
  }

  private Statement createVariableDeclarationStatement(VariableExpression var) {
    DeclarationExpression throwableDecl =
        new DeclarationExpression(
            var,
            Token.newSymbol(Types.ASSIGN, -1, -1),
            EmptyExpression.INSTANCE);

    return new ExpressionStatement(throwableDecl);
  }

  private TryCatchStatement createCleanupTryCatch(CleanupBlock block, VariableExpression featureThrowableVar, VariableExpression failedBlock) {
    List<Statement> cleanupStats = new ArrayList<>(block.getAst());
    TryCatchStatement tryCatchStat =
        new TryCatchStatement(
            new BlockStatement(cleanupStats, null),
            ifThrowableIsNotNull(restoreFailedBlock(failedBlock))
        );

    tryCatchStat.addCatch(createHandleSuppressedThrowableStatement(featureThrowableVar));

    return tryCatchStat;
  }

  private CatchStatement createThrowableAssignmentAndRethrowCatchStatement(VariableExpression assignmentVar) {
    Parameter catchParameter = new Parameter(nodeCache.Throwable, SPOCK_TMP_THROWABLE);

    BinaryExpression assignThrowableExpr =
        new BinaryExpression(
            new VariableExpression(assignmentVar),
            Token.newSymbol(Types.ASSIGN, -1, -1),
            new VariableExpression(catchParameter));

    return new CatchStatement(catchParameter,
        new BlockStatement(
            asList(
              new ExpressionStatement(assignThrowableExpr),
              new ThrowStatement(new VariableExpression(catchParameter))),
          null));
  }

  private CatchStatement createHandleSuppressedThrowableStatement(VariableExpression featureThrowableVar) {
    Parameter catchParameter = new Parameter(nodeCache.Throwable, SPOCK_TMP_THROWABLE);

    BinaryExpression featureThrowableNotNullExpr = AstUtil.createVariableIsNotNullExpression(featureThrowableVar);

    List<Statement> addSuppressedStats =
      singletonList(new ExpressionStatement(
        createDirectMethodCall(
          featureThrowableVar,
          nodeCache.Throwable_AddSuppressed,
          new ArgumentListExpression(new VariableExpression(catchParameter)))));
    List<Statement> throwFeatureStats =
      singletonList(new ThrowStatement(new VariableExpression(catchParameter)));

    IfStatement ifFeatureNotNullStat = new IfStatement(new BooleanExpression(featureThrowableNotNullExpr),
      new BlockStatement(addSuppressedStats, null),
      new BlockStatement(throwFeatureStats, null));

    return new CatchStatement(catchParameter,
      new BlockStatement(
        singletonList(ifFeatureNotNullStat),
        null));
  }

  // IRewriteResources members

  @Override
  public Spec getCurrentSpec() {
    return spec;
  }

  @Override
  public Method getCurrentMethod() {
    return method;
  }

  @Override
  public Block getCurrentBlock() {
    return block;
  }

  @Override
  public void defineValueRecorder(List<Statement> stats, String variableNameSuffix) {
    // recorder variable needs to be defined in outermost scope,
    // hence we insert it at the beginning of the block
    stats.add(0,
        new ExpressionStatement(
            new DeclarationExpression(
                new VariableExpression(SpockNames.VALUE_RECORDER + variableNameSuffix, nodeCache.ValueRecorder),
                Token.newSymbol(Types.ASSIGN, -1, -1),
                new ConstructorCallExpression(
                    nodeCache.ValueRecorder,
                    ArgumentListExpression.EMPTY_ARGUMENTS))));
  }

  // This is necessary as otherwise within `with(someMap) { ... }` the variable is resolved to `null`,
  // but having this local variable beats the resolving against the closure delegate.
  @Override
  public void defineErrorRethrower(List<Statement> stats) {
    stats.add(0,
      new ExpressionStatement(
        new DeclarationExpression(
          new VariableExpression(SpockNames.ERROR_COLLECTOR, nodeCache.ErrorCollector),
          Token.newSymbol(Types.ASSIGN, -1, -1),
          new PropertyExpression(new ClassExpression(nodeCache.ErrorRethrower), "INSTANCE"))));
  }

  @Override
  public void defineErrorCollector(List<Statement> stats, String variableNameSuffix) {
    // collector variable needs to be defined in outermost scope,
    // hence we insert it at the beginning of the block
    List<Statement> allStats = new ArrayList<>(stats);

    stats.clear();

    stats.add(
      new ExpressionStatement(
        new DeclarationExpression(
          new VariableExpression(SpockNames.ERROR_COLLECTOR + variableNameSuffix, nodeCache.ErrorCollector),
          Token.newSymbol(Types.ASSIGN, -1, -1),
          new ConstructorCallExpression(
            nodeCache.ErrorCollector,
            ArgumentListExpression.EMPTY_ARGUMENTS))));

    stats.add(
      new TryCatchStatement(
        new BlockStatement(allStats, null),
        new ExpressionStatement(
          createDirectMethodCall(
            new VariableExpression(SpockNames.ERROR_COLLECTOR + variableNameSuffix),
            nodeCache.ErrorCollector_Validate,
            ArgumentListExpression.EMPTY_ARGUMENTS
          ))));
  }

  @Override
  public VariableExpression captureOldValue(Expression oldValue) {
    VariableExpression var = new OldValueExpression(oldValue, SpockNames.OLD_VALUE + oldValueCount++);
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
    return createDirectMethodCall(VariableExpression.THIS_EXPRESSION,
        nodeCache.SpecInternals_GetSpecificationContext, ArgumentListExpression.EMPTY_ARGUMENTS);
  }

  @Override
  public MethodCallExpression getMockInvocationMatcher() {
    return createDirectMethodCall(
      getSpecificationContext(),
      nodeCache.SpecificationContext_GetMockController,
      ArgumentListExpression.EMPTY_ARGUMENTS);
  }

  public MethodCallExpression setThrownException(Expression value) {
    return createDirectMethodCall(
      getSpecificationContext(),
      nodeCache.SpecificationContext_SetThrownException,
      new ArgumentListExpression(value));
  }

  public MethodCallExpression getSharedInstance() {
    return createDirectMethodCall(
      getSpecificationContext(),
      nodeCache.SpecificationContext_GetSharedInstance,
      ArgumentListExpression.EMPTY_ARGUMENTS);
  }

  @Override
  public AstNodeCache getAstNodeCache() {
    return nodeCache;
  }

  private FixtureMethod getInitializerMethod() {
    if (spec.getInitializerMethod() == null) {
      // method is private s.t. multiple initializer methods along hierarchy can be called independently
      MethodNode gMethod = new MethodNode(InternalIdentifiers.INITIALIZER_METHOD, Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC,
          getPlainReference(ClassHelper.OBJECT_TYPE), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());
      spec.getAst().addMethod(gMethod);
      FixtureMethod method = new FixtureMethod(spec, gMethod);
      method.addBlock(new AnonymousBlock(method));
      spec.setInitializerMethod(method);
    }

    return spec.getInitializerMethod();
  }

  @Override
  public String getSourceText(ASTNode node) {
    return lookup.lookup(node);
  }

  @Override
  public ErrorReporter getErrorReporter() {
    return errorReporter;
  }

  private FixtureMethod getSharedInitializerMethod() {
    if (spec.getSharedInitializerMethod() == null) {
      // method is private s.t. multiple initializer methods along hierarchy can be called independently
      MethodNode gMethod = new MethodNode(InternalIdentifiers.SHARED_INITIALIZER_METHOD, Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC,
        getPlainReference(ClassHelper.OBJECT_TYPE), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());
      spec.getAst().addMethod(gMethod);
      FixtureMethod method = new FixtureMethod(spec, gMethod);
      method.addBlock(new AnonymousBlock(method));
      spec.setSharedInitializerMethod(method);
    }

    return spec.getSharedInitializerMethod();
  }

  private void rewriteWhenBlockForExceptionCondition(WhenBlock block) {
    List<Statement> tryStats = block.getAst();
    List<Statement> blockStats = new ArrayList<>();
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
            new BlockStatement(tryStats, null),
            new BlockStatement());

    blockStats.add(tryCatchStat);

    tryCatchStat.addCatch(
        new CatchStatement(
            new Parameter(nodeCache.Throwable, SpockNames.SPOCK_EX),
            new BlockStatement(
              singletonList(
                new ExpressionStatement(
                  setThrownException(
                    new VariableExpression(SpockNames.SPOCK_EX)))),
              null)));
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
              transformRhsExpressionIfNecessary(declExpr)));

      declExpr.setRightExpression(createDefaultValueInitializer(declExpr));
      to.add(new ExpressionStatement(declExpr));
    }
  }

  private Expression transformRhsExpressionIfNecessary(DeclarationExpression declExpr) {
    Expression rightExpression = declExpr.getRightExpression();
    // The case of `def foo = foo()` will break if we split it up without changing the target,
    // as groovy would now interpret it as a `foo.call()` invocation on the local variable.
    if (rightExpression instanceof MethodCallExpression) {
      MethodCallExpression methodCallExpression = (MethodCallExpression)rightExpression;
      if (methodCallExpression.isImplicitThis())
        if (declExpr.isMultipleAssignmentDeclaration()) {
          ArgumentListExpression argumentListExpression = (ArgumentListExpression)declExpr.getLeftExpression();
          argumentListExpression.getExpressions().stream()
            .filter(VariableExpression.class::isInstance)
            .map(VariableExpression.class::cast)
            .map(VariableExpression::getName)
            .filter(methodCallExpression.getMethod().getText()::equals)
            .findAny()
            .ifPresent(ignore-> methodCallExpression.setImplicitThis(false));
        } else if (declExpr.getVariableExpression().getName().equals(methodCallExpression.getMethod().getText())) {
          // change to explicit `this` to turn the expression to `foo = this.foo()`
          methodCallExpression.setImplicitThis(false);
        }
    }
    return rightExpression;
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
        result.addExpression(copyVarExpr((VariableExpression) expr));
      }
      return result;
    }

    return copyVarExpr(declExpr.getVariableExpression());
  }

  private Expression copyVarExpr(VariableExpression varExpr) {
    VariableExpression newVarExpr = new VariableExpression(varExpr.getName(), varExpr.getOriginType());
    newVarExpr.setAccessedVariable(varExpr.getAccessedVariable());
    newVarExpr.setSourcePosition(varExpr);
    return newVarExpr;
  }
}
