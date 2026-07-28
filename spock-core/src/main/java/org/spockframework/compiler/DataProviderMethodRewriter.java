/*
 * Copyright 2026 the original author or authors.
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

import groovy.lang.Tuple;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.SourceUnit;
import org.spockframework.compiler.WhereBlockRewriter.DataProviderArtifact;
import org.spockframework.compiler.condition.DefaultConditionErrorRecorders;
import org.spockframework.compiler.condition.IConditionErrorRecorders;
import org.spockframework.compiler.model.*;
import org.spockframework.runtime.model.StandaloneDataProviderMetadata;
import org.spockframework.util.Identifiers;
import org.spockframework.util.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Rewrites a single standalone {@code @DataProvider} method: parses its body as where-block
 * content via the shared {@link WhereBlockRewriter} builder, then replaces the body with a call
 * to {@code StandaloneDataProviders.dataIterator(...)} whose arguments are the parsed artifacts
 * emitted as inline closures.
 * <p>
 * Emitting inline (rather than as sibling methods like the feature path) is what makes the
 * method's parameters and {@code this} visible to the where-block variables, data providers,
 * data processor and filter, via ordinary closure capture.
 *
 * @since 2.5
 */
class DataProviderMethodRewriter {
  private static final String ITERATOR_NAME = Iterator.class.getName();
  private static final String GENERIC_TUPLE_NAME = Tuple.class.getName();
  private static final int HIGHEST_FIXED_TUPLE_ARITY = 16;

  private final MethodNode methodNode;
  private final AstNodeCache nodeCache;
  private final SourceUnit sourceUnit;
  private final ErrorReporter errorReporter;
  private final SourceLookup sourceLookup;

  private boolean inSpec;
  private IRewriteResources resources;
  private InstanceFieldAccessChecker instanceFieldAccessChecker;
  private ErrorRethrowerUsageDetector errorRethrowerUsageDetector;
  private boolean defineErrorRethrower;
  private WhereBlock whereBlock;
  @Nullable
  private FilterBlock filterBlock;

  DataProviderMethodRewriter(MethodNode methodNode, AstNodeCache nodeCache, SourceUnit sourceUnit,
                             ErrorReporter errorReporter, SourceLookup sourceLookup) {
    this.methodNode = methodNode;
    this.nodeCache = nodeCache;
    this.sourceUnit = sourceUnit;
    this.errorReporter = errorReporter;
    this.sourceLookup = sourceLookup;
  }

  void rewrite() {
    if (AstUtil.hasAnnotation(methodNode, groovy.transform.CompileStatic.class)
      || AstUtil.hasAnnotation(methodNode.getDeclaringClass(), groovy.transform.CompileStatic.class)) {
      errorReporter.error(methodNode,
        "@DataProvider methods do not support @CompileStatic, because the where-block grammar is inherently dynamic");
      return;
    }

    List<Statement> bodyStats = methodNode.getCode() == null ? null : AstUtil.getStatements(methodNode);
    if (bodyStats == null || bodyStats.isEmpty()) {
      errorReporter.error(methodNode, atLeastOneDataVariable());
      return;
    }

    inSpec = methodNode.getDeclaringClass().isDerivedFrom(nodeCache.Specification);

    if (!buildBlocks(bodyStats)) {
      return;
    }

    resources = new DataProviderMethodRewriteResources(whereBlock, nodeCache, sourceLookup, errorReporter,
      new DefaultConditionErrorRecorders(nodeCache));
    instanceFieldAccessChecker = new InstanceFieldAccessChecker(resources);
    errorRethrowerUsageDetector = new ErrorRethrowerUsageDetector();

    DeepBlockRewriter deep = new DeepBlockRewriter(resources);
    deep.visit(whereBlock);
    defineErrorRethrower = deep.isDeepNonGroupedConditionFound();

    WhereBlockRewriter parsed = WhereBlockRewriter.parse(whereBlock, resources, inSpec);

    validate(parsed);
    if (sourceUnit.getErrorCollector().hasErrors()) {
      return;
    }

    rewriteBody(parsed);
    addMetadataAnnotation(parsed);
    // the inline closures emitted above capture the method's parameters and 'this'; their
    // scopes must be rebuilt because any class-wide scope fixup (SpockTransform's, for a
    // spec) already ran on the raw body, and on a plain class none ran at all
    new VariableScopeVisitor(sourceUnit).visitClass(methodNode.getDeclaringClass());
  }

  // A @DataProvider body is where-block content. We open it with a `where:` label (unless the
  // author already labeled the first statement) and then reuse the shared BlockParser, so the
  // body is split into blocks and validated against the where-block grammar exactly like a feature
  // method's where-block. Only where and filter blocks are valid here, so a feature-method block
  // such as an `expect:` opener is rejected; the parser itself rejects unknown labels and illegal
  // transitions (a misplaced `where:`, a `combined:`/`filter:` opener, ...).
  private boolean buildBlocks(List<Statement> bodyStats) {
    Statement first = bodyStats.get(0);
    if ((first.getStatementLabels() == null) || first.getStatementLabels().isEmpty()) {
      first.addStatementLabel(Identifiers.WHERE);
    }

    HelperMethod method = new HelperMethod(new Spec(methodNode.getDeclaringClass()), methodNode);
    try {
      BlockParser.parseBlocks(method);
    } catch (InvalidSpecCompileException e) {
      errorReporter.error(e);
      return false;
    }

    for (Block block : method.getBlocks()) {
      if (block instanceof WhereBlock) {
        whereBlock = (WhereBlock) block;
      } else if (block instanceof FilterBlock) {
        filterBlock = (FilterBlock) block;
      } else if (!(block instanceof AnonymousBlock)) {
        errorReporter.error(blockPosition(block),
          "'%s:' blocks are not valid in a @DataProvider method; its body uses the where-block grammar, so it may open with an optional 'where:' label and otherwise only use 'and:', 'combined:' and 'filter:'",
          block.getParseInfo());
        return false;
      }
    }
    return whereBlock != null;
  }

  private ASTNode blockPosition(Block block) {
    List<Statement> stats = block.getAst();
    return stats.isEmpty() ? methodNode : stats.get(0);
  }

  private void validate(WhereBlockRewriter parsed) {
    List<VariableExpression> dataVariables = parsed.getDataProcessorVariables();
    if (dataVariables.isEmpty()) {
      // parsing errors (including where-block variables without a data variable) have
      // already been reported; only a silently empty body still needs one
      if (!sourceUnit.getErrorCollector().hasErrors()) {
        errorReporter.error(methodNode, atLeastOneDataVariable());
      }
      return;
    }

    validateParameterNames(parsed);
    handleReturnType(dataVariables);
  }

  // method parameters are body-wide inputs sharing the body's name namespace with
  // where-block variables and data variables, so collisions are rejected
  private void validateParameterNames(WhereBlockRewriter parsed) {
    List<String> dataVariableNames = dataVariableNames(parsed);
    for (Parameter parameter : methodNode.getParameters()) {
      String name = parameter.getName();
      if (dataVariableNames.contains(name)) {
        errorReporter.error(positionOf(parameter),
          "Data variable '%s' collides with a method parameter of the same name", name);
      } else if (parsed.getWhereBlockVariableNames().contains(name)) {
        errorReporter.error(positionOf(parameter),
          "where-block variable '%s' collides with a method parameter of the same name", name);
      }
    }
  }

  private ASTNode positionOf(Parameter parameter) {
    return parameter.getLineNumber() > 0 ? parameter : methodNode;
  }

  private void handleReturnType(List<VariableExpression> dataVariables) {
    int arity = dataVariables.size();
    if (methodNode.isDynamicReturnType() || ClassHelper.OBJECT_TYPE.equals(methodNode.getReturnType())) {
      methodNode.setReturnType(createIteratorOfTupleType(arity));
      return;
    }

    ClassNode returnType = methodNode.getReturnType();
    if (!ITERATOR_NAME.equals(returnType.getName())) {
      errorReporter.error(methodNode,
        "@DataProvider method '%s' must be declared 'def' or with a return type of java.util.Iterator, but is declared as '%s'",
        methodNode.getName(), returnType.getName());
      return;
    }

    GenericsType[] generics = returnType.getGenericsTypes();
    if (generics == null || generics.length != 1 || generics[0].isWildcard()) {
      // raw Iterator (or an unbounded wildcard) is acceptable
      return;
    }

    String elementTypeName = generics[0].getType().getName();
    if (GENERIC_TUPLE_NAME.equals(elementTypeName)) {
      return;
    }
    Integer declaredArity = tupleArity(elementTypeName);
    if (declaredArity == null) {
      errorReporter.error(methodNode,
        "@DataProvider method '%s' declares return type Iterator<%s>, but the element type must be groovy.lang.Tuple or an arity-specific Tuple class",
        methodNode.getName(), elementTypeName);
    } else if (declaredArity != arity) {
      errorReporter.error(methodNode,
        "@DataProvider method '%s' declares return type Iterator<Tuple%d>, but produces %d data variable(s) %s; declare Iterator<Tuple%d> or 'def'",
        methodNode.getName(), declaredArity, arity,
        dataVariables.stream().map(VariableExpression::getName).collect(toList()), arity);
    }
    // any element type arguments (e.g. Iterator<Tuple2<Integer, Integer>>) are accepted as
    // the user's authoritative declaration; cells are not always literals, so they are not
    // re-derived or cross-checked
  }

  @Nullable
  private static Integer tupleArity(String className) {
    String prefix = GENERIC_TUPLE_NAME;
    if (!className.startsWith(prefix)) {
      return null;
    }
    try {
      int arity = Integer.parseInt(className.substring(prefix.length()));
      return (arity >= 1 && arity <= HIGHEST_FIXED_TUPLE_ARITY) ? arity : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private ClassNode createIteratorOfTupleType(int arity) {
    ClassNode iteratorType = ClassHelper.makeWithoutCaching(Iterator.class).getPlainNodeReference();
    iteratorType.setGenericsTypes(new GenericsType[]{new GenericsType(resolveTupleType(arity))});
    return iteratorType;
  }

  // resolves the arity-specific tuple class dynamically: the available arities depend on
  // the Groovy version being compiled against (2.5 only ships Tuple1-Tuple9), so no
  // Tuple1..Tuple16 class may be referenced statically
  private static ClassNode resolveTupleType(int arity) {
    if (arity <= HIGHEST_FIXED_TUPLE_ARITY) {
      try {
        return ClassHelper.makeWithoutCaching(
          Class.forName(GENERIC_TUPLE_NAME + arity, false, Tuple.class.getClassLoader())).getPlainNodeReference();
      } catch (ClassNotFoundException ignored) {
        // fall through to the generic Tuple
      }
    }
    return ClassHelper.makeWithoutCaching(Tuple.class).getPlainNodeReference();
  }

  private void rewriteBody(WhereBlockRewriter parsed) {
    ClosureExpression whereVariablesClosure = createWhereVariablesClosure(parsed);
    List<Expression> dataProviderDescriptors = createDataProviderDescriptors(parsed);
    ClosureExpression dataProcessorClosure = createDataProcessorClosure(parsed);
    ClosureExpression filterClosure = createFilterClosure(parsed);
    ClosureExpression dataVariableMultiplicationsClosure = createDataVariableMultiplicationsClosure(parsed);

    ArgumentListExpression args = new ArgumentListExpression();
    args.addExpression(stringArray(dataVariableNames(parsed)));
    args.addExpression(orNull(whereVariablesClosure));
    args.addExpression(new ArrayExpression(nodeCache.StandaloneDataProviderDescriptor, dataProviderDescriptors));
    args.addExpression(dataProcessorClosure);
    args.addExpression(orNull(filterClosure));
    args.addExpression(orNull(dataVariableMultiplicationsClosure));

    MethodCallExpression call = AstUtil.createDirectMethodCall(
      new ClassExpression(nodeCache.StandaloneDataProviders),
      nodeCache.StandaloneDataProviders_DataIterator,
      args);

    BlockStatement newCode = new BlockStatement();
    newCode.addStatement(new ReturnStatement(call));
    methodNode.setCode(newCode);
  }

  @Nullable
  private ClosureExpression createWhereVariablesClosure(WhereBlockRewriter parsed) {
    if (parsed.getWhereBlockVariableStatements().isEmpty()) {
      return null;
    }

    if (inSpec) {
      instanceFieldAccessChecker.check(parsed.getWhereBlockVariableStatements());
    }

    // reuse the feature path's value-array construction so a where-block variable initializer that
    // throws still closes the AutoCloseable values created before it, instead of leaking them
    return createClosure(Parameter.EMPTY_ARRAY, parsed.createWhereVariableValueStatements());
  }

  private List<Expression> createDataProviderDescriptors(WhereBlockRewriter parsed) {
    List<Expression> descriptors = new ArrayList<>();
    for (DataProviderArtifact dataProvider : parsed.getDataProviders()) {
      Expression dataProviderExpr = dataProvider.getExpression();

      List<Statement> stats = new ArrayList<>();
      if (defineErrorRethrower && errorRethrowerUsageDetector.detectedErrorRethrowerUsage(dataProviderExpr)) {
        resources.getErrorRecorders().defineErrorRethrower(stats);
      }
      ReturnStatement returnStat = new ReturnStatement(dataProviderExpr);
      returnStat.setSourcePosition(dataProviderExpr);
      stats.add(returnStat);

      Parameter[] params = concatParameters(
        dataProvider.getPreviousDataTableVariables().stream()
          .map(previousDataTableVariable -> new Parameter(
            ClassHelper.LIST_TYPE.getPlainNodeReference(),
            WhereBlockRewriter.getDataTableParameterName(previousDataTableVariable)))
          .toArray(Parameter[]::new),
        createWhereVariableParameters(parsed));

      descriptors.add(new ConstructorCallExpression(nodeCache.StandaloneDataProviderDescriptor,
        new ArgumentListExpression(
          new ArrayList<>(java.util.Arrays.asList(
            createClosure(params, stats),
            stringArray(dataProvider.getDataVariables()),
            stringArray(dataProvider.getPreviousDataTableVariables()),
            new ConstantExpression(dataProviderExpr.getLineNumber()))))));
    }
    return descriptors;
  }

  @SuppressWarnings("unchecked")
  private ClosureExpression createDataProcessorClosure(WhereBlockRewriter parsed) {
    List<Statement> stats = new ArrayList<>(parsed.getDataProcessorStatements());

    if (inSpec) {
      instanceFieldAccessChecker.check(stats);
    }
    if (defineErrorRethrower && errorRethrowerUsageDetector.detectedErrorRethrowerUsage(stats)) {
      resources.getErrorRecorders().defineErrorRethrower(stats);
    }

    stats.add(
      new ReturnStatement(
        new ArrayExpression(
          ClassHelper.OBJECT_TYPE,
          (List) parsed.getDataProcessorVariables())));

    Parameter[] params = concatParameters(
      parsed.getDataProcessorParameters().toArray(Parameter.EMPTY_ARRAY),
      createWhereVariableParameters(parsed));

    return createClosure(params, stats);
  }

  @Nullable
  private ClosureExpression createFilterClosure(WhereBlockRewriter parsed) {
    if (filterBlock == null) {
      return null;
    }

    DeepBlockRewriter deep = new DeepBlockRewriter(resources);
    deep.visit(filterBlock);

    List<Statement> filterStats = new ArrayList<>(filterBlock.getAst());
    filterBlock.getAst().clear();

    if (inSpec) {
      instanceFieldAccessChecker.check(filterStats);
    }

    if (deep.isConditionFound()) {
      resources.getErrorRecorders().defineValueRecorder(filterStats);
    }
    if (deep.isDeepNonGroupedConditionFound()) {
      resources.getErrorRecorders().defineErrorRethrower(filterStats);
    }

    Parameter[] params = concatParameters(
      parsed.getDataProcessorVariables()
        .stream()
        .map(variable -> new Parameter(ClassHelper.OBJECT_TYPE, variable.getName()))
        .toArray(Parameter[]::new),
      createWhereVariableParameters(parsed));

    return createClosure(params, filterStats);
  }

  @Nullable
  private ClosureExpression createDataVariableMultiplicationsClosure(WhereBlockRewriter parsed) {
    if (parsed.getDataVariableMultiplications().isEmpty()) {
      return null;
    }

    List<Statement> stats = new ArrayList<>();
    stats.add(
      new ReturnStatement(
        new ArrayExpression(
          nodeCache.DataVariableMultiplication,
          parsed.getDataVariableMultiplications())));

    return createClosure(Parameter.EMPTY_ARRAY, stats);
  }

  private void addMetadataAnnotation(WhereBlockRewriter parsed) {
    AnnotationNode ann = new AnnotationNode(nodeCache.StandaloneDataProviderMetadata);
    ann.addMember(StandaloneDataProviderMetadata.DATA_VARIABLES,
      new ListExpression(dataVariableNames(parsed).stream()
        .map(ConstantExpression::new)
        .map(Expression.class::cast)
        .collect(toList())));
    ann.addMember(StandaloneDataProviderMetadata.LINE, new ConstantExpression(methodNode.getLineNumber()));
    methodNode.addAnnotation(ann);
  }

  private static List<String> dataVariableNames(WhereBlockRewriter parsed) {
    return parsed.getDataProcessorVariables().stream()
      .map(VariableExpression::getName)
      .collect(toList());
  }

  private static Parameter[] createWhereVariableParameters(WhereBlockRewriter parsed) {
    return parsed.getWhereBlockVariableNames().stream()
      .map(name -> new Parameter(ClassHelper.OBJECT_TYPE, name))
      .toArray(Parameter[]::new);
  }

  private static Parameter[] concatParameters(Parameter[] a, Parameter[] b) {
    if (b.length == 0) return a;
    Parameter[] result = java.util.Arrays.copyOf(a, a.length + b.length);
    System.arraycopy(b, 0, result, a.length, b.length);
    return result;
  }

  private ClosureExpression createClosure(Parameter[] params, List<Statement> stats) {
    ClosureExpression closure = new ClosureExpression(params, new BlockStatement(stats, null));
    closure.setSourcePosition(methodNode);
    return closure;
  }

  private static Expression stringArray(List<String> values) {
    return new ArrayExpression(ClassHelper.STRING_TYPE,
      values.stream().map(ConstantExpression::new).map(Expression.class::cast).collect(toList()));
  }

  private static Expression orNull(@Nullable Expression expression) {
    return expression == null ? ConstantExpression.NULL : expression;
  }

  private String atLeastOneDataVariable() {
    return String.format(java.util.Locale.ROOT,
      "@DataProvider method '%s' must declare at least one data variable (e.g. 'x << [1, 2]')",
      methodNode.getName());
  }

  private static class DataProviderMethodRewriteResources implements IRewriteResources {
    private final WhereBlock whereBlock;
    private final AstNodeCache nodeCache;
    private final SourceLookup lookup;
    private final ErrorReporter errorReporter;
    private final IConditionErrorRecorders errorRecorders;

    DataProviderMethodRewriteResources(WhereBlock whereBlock, AstNodeCache nodeCache, SourceLookup lookup,
                                       ErrorReporter errorReporter, IConditionErrorRecorders errorRecorders) {
      this.whereBlock = whereBlock;
      this.nodeCache = nodeCache;
      this.lookup = lookup;
      this.errorReporter = errorReporter;
      this.errorRecorders = errorRecorders;
    }

    @Override
    public Method getCurrentMethod() {
      return whereBlock.getParent();
    }

    @Override
    public Block getCurrentBlock() {
      // the shared BlockParser prepends an empty AnonymousBlock, so the where block is not
      // method.getFirstBlock(); deep rewriting operates on the where block itself
      return whereBlock;
    }

    @Override
    public VariableExpression captureOldValue(Expression oldValue) {
      throw new UnsupportedOperationException("This should only be called when processing a then-block");
    }

    @Override
    public MethodCallExpression getMockInvocationMatcher() {
      return null;
    }

    @Override
    public AstNodeCache getAstNodeCache() {
      return nodeCache;
    }

    @Override
    public String getSourceText(ASTNode node) {
      return lookup.lookup(node);
    }

    @Override
    public ErrorReporter getErrorReporter() {
      return errorReporter;
    }

    @Override
    public IConditionErrorRecorders getErrorRecorders() {
      return errorRecorders;
    }
  }
}
