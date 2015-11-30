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

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.*;
import org.objectweb.asm.Opcodes;
import org.spockframework.compiler.model.WhereBlock;
import org.spockframework.util.*;

import java.util.*;

import static org.spockframework.compiler.AstUtil.createGetAtMethod;
import static org.spockframework.runtime.model.DataProviderMetadata.*;
import static org.spockframework.util.CollectionUtil.toArray;

/**
 * @author Peter Niederwieser
 */
public class WhereBlockRewriter {
  private final WhereBlock whereBlock;
  private final IRewriteResources resources;
  private final InstanceFieldAccessChecker instanceFieldAccessChecker;

  private int dataProviderCount = 0;

  private final List<Parameter> dataProcessorParams = new ArrayList<Parameter>(); // parameters of the data processor method (one for each data provider)
  private final List<Statement> dataProcessorStats = new ArrayList<Statement>(); // statements of the data processor method (one for each parameterization variable)
  private final List<VariableExpression> dataProcessorVars = new ArrayList<VariableExpression>(); // parameterization variables of the data processor method // TODO unify with dataProcessorStats

  private WhereBlockRewriter(WhereBlock whereBlock, IRewriteResources resources) {
    this.whereBlock = whereBlock;
    this.resources = resources;
    instanceFieldAccessChecker = new InstanceFieldAccessChecker(resources);
  }

  public static void rewrite(WhereBlock block, IRewriteResources resources) {
    new WhereBlockRewriter(block, resources).rewrite();
  }

  private void rewrite() {
    for (ListIterator<Statement> stats = whereBlock.getAst().listIterator(); stats.hasNext(); )
      try {
        rewriteWhereStat(stats);
      } catch (InvalidSpecCompileException e) {
        resources.getErrorReporter().error(e);
      }

    whereBlock.getAst().clear();
    handleFeatureParameters();
    createDataProcessorMethod();
  }

  private void rewriteWhereStat(ListIterator<Statement> stats) throws InvalidSpecCompileException {
    Statement stat = stats.next();
    BinaryExpression binExpr = AstUtil.getExpression(stat, BinaryExpression.class);
    if (binExpr == null || binExpr.getClass() != BinaryExpression.class) // don't allow subclasses like DeclarationExpression
      notAParameterization(stat);

    int type = binExpr.getOperation().getType();

    if (type == Types.LEFT_SHIFT)
      rewriteDataPipeParameterization(stat, binExpr); // e.g. `a << [1]`
    else if (type == Types.ASSIGN)
      rewriteDerivedParameterization(stat, binExpr);  // e.g. `a = 1`
    else if (isOrExpression(type)) {
      stats.previous();
      rewriteTableLikeParameterization(stats);        // e.g. a | b \n 0 | 1
    } else notAParameterization(stat);
  }

  private void rewriteDataPipeParameterization(ASTNode parent, BinaryExpression binExpr) throws InvalidSpecCompileException {
    Expression leftExpr = binExpr.getLeftExpression();
    if (leftExpr instanceof VariableExpression)
      rewriteSimpleParameterization(parent, binExpr); // e.g. a << [1,2]
    else if (leftExpr instanceof ListExpression)
      rewriteMultiParameterization(parent, binExpr);  // e.g. [a,b] << [1,2]
    else notAParameterization(parent);
  }

  private void createDataProviderMethod(Expression dataProviderExpr, int nextDataVariableIndex, boolean isCollection) {
    instanceFieldAccessChecker.check(dataProviderExpr);

    dataProviderExpr = dataProviderExpr.transformExpression(new DataTablePreviousVariableTransformer());

    MethodNode method = new MethodNode(
      InternalIdentifiers.getDataProviderName(whereBlock.getParent().getAst().getName(), dataProviderCount++),
      Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
      ClassHelper.DYNAMIC_TYPE,
      getParameters(nextDataVariableIndex),
      ClassNode.EMPTY_ARRAY,
      new BlockStatement(Arrays.<Statement>asList(new ExpressionStatement(dataProviderExpr)), new VariableScope())
    );

    method.addAnnotation(createDataProviderAnnotation(dataProviderExpr, nextDataVariableIndex, isCollection));
    whereBlock.getParent().getParent().getAst().addMethod(method);
  }

  /**
   * every data provider depends on the previous data providers
   */
  private Parameter[] getParameters(int nextDataVariableIndex) {
    List<Parameter> results = new ArrayList<Parameter>();

    for (int i = 0; i < nextDataVariableIndex; i++)
      results.add(new Parameter(ClassHelper.DYNAMIC_TYPE,
                                dataProcessorVars.get(i).getName()));

    return toArray(results, Parameter.class);
  }

  private AnnotationNode createDataProviderAnnotation(Expression dataProviderExpr, int nextDataVariableIndex, boolean isCollection) {
    AnnotationNode ann = new AnnotationNode(resources.getAstNodeCache().DataProviderMetadata);

    ann.addMember(LINE, new ConstantExpression(dataProviderExpr.getLineNumber()));
    ann.addMember(IS_COLLECTION, new ConstantExpression(isCollection));

    List<Expression> dataVariableNames = new ArrayList<Expression>();
    for (int i = nextDataVariableIndex; i < dataProcessorVars.size(); i++)
      dataVariableNames.add(new ConstantExpression(dataProcessorVars.get(i).getName()));

    ann.addMember(DATA_VARIABLES, new ListExpression(dataVariableNames));

    return ann;
  }

  private VariableExpression createDataProcessorVariable() {
    Parameter p = new Parameter(ClassHelper.DYNAMIC_TYPE, "$spock_p" + dataProcessorParams.size());
    dataProcessorParams.add(p);
    return new VariableExpression(p);
  }

  // generates: arg = argMethodParam
  private void rewriteSimpleParameterization(ASTNode parent, BinaryExpression binExpr) throws InvalidSpecCompileException {
    int nextDataVariableIndex = dataProcessorVars.size();

    addDataProcessorStat(
      parent,
      binExpr.getLeftExpression(),
      createDataProcessorVariable()
    );

    createDataProviderMethod(binExpr.getRightExpression(), nextDataVariableIndex, true);
  }

  // generates:
  // arg0 = argMethodParam.getAt(0)
  // arg1 = argMethodParam.getAt(1)
  private void rewriteMultiParameterization(ASTNode parent, BinaryExpression binExpr) throws InvalidSpecCompileException {
    int nextDataVariableIndex = dataProcessorVars.size();

    VariableExpression dataProcessorVariable = createDataProcessorVariable();
    List<Expression> listElems = ((ListExpression) binExpr.getLeftExpression()).getExpressions();

    for (int i = 0; i < listElems.size(); i++) {
      addDataProcessorStat(
        parent,
        listElems.get(i),
        createGetAtMethod(dataProcessorVariable, new ConstantExpression(i))
      );
    }

    createDataProviderMethod(binExpr.getRightExpression(), nextDataVariableIndex, true);
  }

  /* TODO make this a data provider also (see tests in DataTables)
   * TODO IS_COLLECTION -> false
   * TODO in case there are no collection data providers, it should iterate only once
   * TODO should be recomputed for every iteration (can depend on previous data provider values)
   * TODO inner closures should be able to reference previous data values also
   * TODO other data providers should be able to reference this value
   */
  private void rewriteDerivedParameterization(ASTNode parent, BinaryExpression binExpr) throws InvalidSpecCompileException {
    addDataProcessorStat(
      parent,
      binExpr.getLeftExpression(),
      binExpr.getRightExpression()
    );
  }

  private void addDataProcessorStat(ASTNode parent, Expression left, Expression right) throws InvalidSpecCompileException {
    if (AstUtil.isWildcardRef(left)) return;

    ExpressionStatement exprStat = new ExpressionStatement(new DeclarationExpression(
      createDataProcessorVariable(left, parent),
      Token.newSymbol(Types.ASSIGN, -1, -1),
      right)
    );
    exprStat.setSourcePosition(parent);
    dataProcessorStats.add(exprStat);
  }

  private void rewriteTableLikeParameterization(ListIterator<Statement> stats) throws InvalidSpecCompileException {
    LinkedList<List<Expression>> rows = new LinkedList<List<Expression>>();

    while (stats.hasNext()) {
      Statement stat = stats.next();
      BinaryExpression orExpr = getOrExpression(stat);
      if (orExpr == null) {
        stats.previous();
        break;
      }

      List<Expression> row = new ArrayList<Expression>();
      splitRow(orExpr, row);
      if (rows.size() > 0 && rows.getLast().size() != row.size())
        throw new InvalidSpecCompileException(stat, String.format("Row in data table has wrong number of elements (%s instead of %s)", row.size(), rows.getLast().size()));
      rows.add(row);
    }

    for (List<Expression> column : transposeTable(rows))
      turnIntoSimpleParameterization(column);
  }

  List<List<Expression>> transposeTable(List<List<Expression>> rows) {
    List<List<Expression>> columns = new ArrayList<List<Expression>>();
    if (rows.isEmpty()) return columns;

    for (int i = 0; i < rows.get(0).size(); i++)
      columns.add(new ArrayList<Expression>());

    for (List<Expression> row : rows)
      for (int i = 0; i < row.size(); i++)
        columns.get(i).add(row.get(i));

    return columns;
  }

  private void turnIntoSimpleParameterization(List<Expression> column) throws InvalidSpecCompileException {
    VariableExpression varExpr = ObjectUtil.asInstance(column.get(0), VariableExpression.class);
    if (varExpr == null)
      throw new InvalidSpecCompileException(column.get(0),
                                            "Header of data table may only contain variable names");
    if (AstUtil.isWildcardRef(varExpr)) {
      // assertion: column has a wildcard header, but the method's
      // explicit parameter list does not have a wildcard parameter
      return; // ignore column (see https://github.com/spockframework/spock/pull/48/)
    }

    ListExpression listExpr = new ListExpression(column.subList(1, column.size()));
    BinaryExpression binExpr = new BinaryExpression(varExpr, Token.newSymbol(Types.LEFT_SHIFT, -1, -1), listExpr);
    // NOTE: varExpr may not be the "perfect" source position here, but as long as we rewrite data tables
    // into simple parameterizations, it seems like the best approximation; also this source position is
    // unlikely to make it into a compile error, because header variable has already been checked, and the
    // assignment itself is unlikely to cause a compile error. (It's more likely that the rval causes a
    // compile error, but the rval's source position is retained.)
    rewriteSimpleParameterization(varExpr, binExpr);
  }

  private void splitRow(Expression row, List<Expression> parts) {
    BinaryExpression orExpr = getOrExpression(row);
    if (orExpr == null)
      parts.add(row);
    else {
      splitRow(orExpr.getLeftExpression(), parts);
      splitRow(orExpr.getRightExpression(), parts);
    }
  }

  private BinaryExpression getOrExpression(Statement stat) {
    Expression expr = AstUtil.getExpression(stat, Expression.class);
    return getOrExpression(expr);
  }

  private @Nullable BinaryExpression getOrExpression(Expression expr) {
    BinaryExpression binExpr = ObjectUtil.asInstance(expr, BinaryExpression.class);
    if (binExpr == null) return null;

    if (isOrExpression(binExpr.getOperation().getType())) return binExpr;
    else return null;
  }

  private boolean isOrExpression(int binExprType) {
    return (binExprType == Types.BITWISE_OR)
           || (binExprType == Types.LOGICAL_OR);
  }

  private VariableExpression createDataProcessorVariable(Expression varExpr, ASTNode sourcePos) throws InvalidSpecCompileException {
    if (!(varExpr instanceof VariableExpression))
      notAParameterization(sourcePos);

    VariableExpression typedVarExpr = (VariableExpression) varExpr;
    verifyDataProcessorVariable(typedVarExpr);

    VariableExpression result = new VariableExpression(typedVarExpr.getName(), typedVarExpr.getType());
    dataProcessorVars.add(result);
    return result;
  }

  private void verifyDataProcessorVariable(VariableExpression varExpr) {
    Variable accessedVar = varExpr.getAccessedVariable();

    if (accessedVar instanceof VariableExpression) { // local variable
      resources.getErrorReporter().error(varExpr, "A variable named '%s' already exists in this scope", varExpr.getName());
      return;
    }

    if (isDataProcessorVariable(varExpr.getName())) {
      resources.getErrorReporter().error(varExpr, "Duplicate declaration of data variable '%s'", varExpr.getName());
      return;
    }

    if (whereBlock.getParent().getAst().getParameters().length > 0 && !(accessedVar instanceof Parameter)) {
      resources.getErrorReporter().error(varExpr,
                                         "Data variable '%s' needs to be declared as method parameter",
                                         varExpr.getName());
    }
  }

  private boolean isDataProcessorVariable(String name) {
    for (VariableExpression var : dataProcessorVars)
      if (name.equals(var.getName()))
        return true;
    return false;
  }

  private void handleFeatureParameters() {
    Parameter[] parameters = whereBlock.getParent().getAst().getParameters();
    if (parameters.length == 0)
      addFeatureParameters();
    else checkAllParametersAreDataVariables(parameters);
  }

  private void checkAllParametersAreDataVariables(Parameter[] parameters) {
    for (Parameter param : parameters)
      if (!isDataProcessorVariable(param.getName()))
        resources.getErrorReporter().error(param, "Parameter '%s' does not refer to a data variable", param.getName());
  }

  private void addFeatureParameters() {
    Parameter[] parameters = new Parameter[dataProcessorVars.size()];

    for (int i = 0; i < dataProcessorVars.size(); i++)
      parameters[i] = new Parameter(ClassHelper.DYNAMIC_TYPE, dataProcessorVars.get(i).getName());

    whereBlock.getParent().getAst().setParameters(parameters);
  }

  @SuppressWarnings("unchecked")
  private void createDataProcessorMethod() {
    if (dataProcessorVars.isEmpty()) return;

    dataProcessorStats.add(new ReturnStatement(new ArrayExpression(ClassHelper.DYNAMIC_TYPE, (List) dataProcessorVars)));

    BlockStatement blockStat = new BlockStatement(dataProcessorStats, new VariableScope());
    new DataProcessorVariableRewriter().visitBlockStatement(blockStat);

    whereBlock.getParent().getParent().getAst().addMethod(new MethodNode(
      InternalIdentifiers.getDataProcessorName(whereBlock.getParent().getAst().getName()),
      Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
      ClassHelper.OBJECT_TYPE,
      toArray(dataProcessorParams, Parameter.class),
      ClassNode.EMPTY_ARRAY,
      blockStat)
    );
  }

  private static void notAParameterization(ASTNode stat) throws InvalidSpecCompileException {
    throw new InvalidSpecCompileException(stat,
                                          "where-blocks may only contain parameterizations" +
                                          " (e.g. 'salary << [1000, 5000, 9000];" +
                                          " salaryk = salary / 1000')");
  }

  /**
   * TODO should be generalized for all data providers (see: DataTables#'in closure scope of data provider')
   */
  private class DataProcessorVariableRewriter extends ClassCodeVisitorSupport {
    @Override
    protected SourceUnit getSourceUnit() {
      throw new UnsupportedOperationException("getSourceUnit");
    }

    @Override
    public void visitClosureExpression(ClosureExpression expr) {
      super.visitClosureExpression(expr);
      AstUtil.fixUpLocalVariables(dataProcessorVars, expr.getVariableScope(), true);
    }

    @Override
    public void visitBlockStatement(BlockStatement stat) {
      super.visitBlockStatement(stat);
      AstUtil.fixUpLocalVariables(dataProcessorVars, stat.getVariableScope(), false);
    }
  }

  /**
   * Transformer, used to access previous cell references from Data Tables
   * Only works for random access collections (e.g. lists), i.e. those that have a getAt method.
   * Iterables wouldn't work, since the same data provider can be accessed multiple times
   * and Iterables don't have a 'current' method, just a 'next'.
   */
  private class DataTablePreviousVariableTransformer extends ClassCodeExpressionTransformer {
    private int depth = 0, rowIndex = 0;

    @Override
    protected SourceUnit getSourceUnit() { return null; }

    @Override
    public Expression transform(Expression expression) {
      if ((expression instanceof VariableExpression) && isDataProcessorVariable(expression.getText()))
        return createGetAtMethod(expression, new ConstantExpression(rowIndex));

      depth++;
      Expression transform = super.transform(expression);
      depth--;

      if (depth == 0)
        rowIndex++;

      return transform;
    }
  }
}
