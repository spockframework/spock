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

import org.spockframework.compiler.model.WhereBlock;
import org.spockframework.runtime.model.DataProviderMetadata;
import org.spockframework.util.*;

import java.util.*;
import java.util.function.Function;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.*;
import org.objectweb.asm.Opcodes;

import static org.spockframework.compiler.AstUtil.createGetAtMethod;
import static org.spockframework.compiler.AstUtil.isDataTableSeparator;
import static org.spockframework.compiler.AstUtil.getExpression;

/**
 *
 * @author Peter Niederwieser
 */
public class WhereBlockRewriter {
  private final WhereBlock whereBlock;
  private final IRewriteResources resources;
  private final InstanceFieldAccessChecker instanceFieldAccessChecker;

  private int dataProviderCount = 0;
  // parameters of the data processor method (one for each data provider)
  private final List<Parameter> dataProcessorParams = new ArrayList<>();
  // statements of the data processor method (one for each parameterization variable)
  private final List<Statement> dataProcessorStats = new ArrayList<>();
  // parameterization variables of the data processor method
  private final List<VariableExpression> dataProcessorVars = new ArrayList<>();

  private WhereBlockRewriter(WhereBlock whereBlock, IRewriteResources resources) {
    this.whereBlock = whereBlock;
    this.resources = resources;
    instanceFieldAccessChecker = new InstanceFieldAccessChecker(resources);
  }

  public static void rewrite(WhereBlock block, IRewriteResources resources) {
    new WhereBlockRewriter(block, resources).rewrite();
  }

  private void rewrite() {
    ListIterator<Statement> stats = whereBlock.getAst().listIterator();
    while (stats.hasNext())
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

    // binary expressions are potentially parameterizations
    BinaryExpression binExpr = AstUtil.getExpression(stat, BinaryExpression.class);
    if (binExpr != null) {
      // don't allow subclasses like DeclarationExpression
      if (binExpr.getClass() != BinaryExpression.class) {
        throw notAParameterization(stat);
      }

      stats.previous();
      rewriteBinaryWhereStat(stats);
      return;
    }

    // reposition before current statement
    stats.previous();
    // search for semicolon separated data table header row
    List<Expression> potentialHeaderRow = getExpressionChain(stats);
    // rewind
    potentialHeaderRow
      .stream()
      .skip(1)
      .forEach(expression -> stats.previous());

    if (potentialHeaderRow.size() > 1) {
      if (!potentialHeaderRow.stream().allMatch(VariableExpression.class::isInstance)) {
        throw dataTableHeaderMayOnlyContainVariableNames(stat);
      }
      stats.previous();
      rewriteExpressionTableLikeParameterization(stats);
      return;
    }

    if (!isDataTableSeparator(stat)) {
      // if statement is a data table separator (two or more underscores)
      // just ignore it, it is mainly meant to separate two consecutive
      // data tables, but is allowed anywhere in the where block, for
      // example to use it as top border for a data table like:
      // __________
      // x | y || z
      // 1 | 2 || 3
      // 4 | 5 || 6
      //
      // otherwise => not a parameterization
      throw notAParameterization(stat);
    }
  }

  private void rewriteBinaryWhereStat(ListIterator<Statement> stats) throws InvalidSpecCompileException {
    Statement stat = stats.next();
    BinaryExpression binExpr = AstUtil.getExpression(stat, BinaryExpression.class);
    int type = binExpr.getOperation().getType();

    if (type == Types.LEFT_SHIFT) {
      // potentially a data pipe like:
      // x << [1, 2, 3]
      Expression leftExpr = binExpr.getLeftExpression();
      if (leftExpr instanceof VariableExpression) {
        // x << [1, 2, 3]
        rewriteSimpleParameterization(binExpr, stat);
      } else if (leftExpr instanceof ListExpression) {
        // [x, y, z] << [[1, 2, 3]]
        rewriteMultiParameterization(binExpr, stat);
      } else {
        // neither of the other two
        throw notAParameterization(stat);
      }
    } else if (type == Types.ASSIGN) {
      // derived data variable like:
      // y = 2 * x
      rewriteDerivedParameterization(binExpr, stat);
    } else if (getOrExpression(binExpr) != null) {
      // header line of data table like:
      // x | y || z
      // 1 | 2 || 3
      // 4 | 5 || 6
      // push back header line
      stats.previous();
      // rewrite data table
      rewriteBinaryTableLikeParameterization(stats);
    } else {
      // binary expression is neither of type left-shift, nor assign and not a data table
      throw notAParameterization(stat);
    }
  }

  private List<Expression> getExpressionChain(ListIterator<Statement> stats) {
    List<Expression> result = new ArrayList<>();

    if (!stats.hasNext()) {
      return result;
    }

    Statement stat = stats.next();
    while (true) {
      Expression expr = getExpression(stat, Expression.class);
      if (expr == null) {
        stats.previous();
        break;
      }

      result.add(expr);

      if (!stats.hasNext()) {
        break;
      }

      Statement nextStat = stats.next();
      if (nextStat.getLineNumber() != stat.getLastLineNumber()) {
        // new data table row
        stats.previous();
        break;
      }
      stat = nextStat;
    }

    return result;
  }

  private void createDataProviderMethod(Expression dataProviderExpr, int nextDataVariableIndex) {
    instanceFieldAccessChecker.check(dataProviderExpr);

    dataProviderExpr = dataProviderExpr.transformExpression(new DataTablePreviousVariableTransformer());

    ExpressionStatement exprStat = new ExpressionStatement(dataProviderExpr);
    exprStat.setSourcePosition(dataProviderExpr);

    ReturnStatement returnStat = new ReturnStatement(exprStat);
    returnStat.setSourcePosition(dataProviderExpr);

    MethodNode method =
      new MethodNode(
        InternalIdentifiers.getDataProviderName(whereBlock.getParent().getAst().getName(), dataProviderCount++),
        Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
        ClassHelper.OBJECT_TYPE,
        getPreviousParameters(nextDataVariableIndex),
        ClassNode.EMPTY_ARRAY,
        new BlockStatement(
          Arrays.<Statement>asList(returnStat),
          new VariableScope()));

    method.addAnnotation(createDataProviderAnnotation(dataProviderExpr, nextDataVariableIndex));
    whereBlock.getParent().getParent().getAst().addMethod(method);
  }

  private Parameter[] getPreviousParameters(int nextDataVariableIndex) {
    Parameter[] results = new Parameter[nextDataVariableIndex];
    for (int i = 0; i < nextDataVariableIndex; i++)
      results[i] = new Parameter(ClassHelper.DYNAMIC_TYPE,
                                 dataProcessorVars.get(i).getName());
    return results;
  }

  private AnnotationNode createDataProviderAnnotation(Expression dataProviderExpr, int nextDataVariableIndex) {
    AnnotationNode ann = new AnnotationNode(resources.getAstNodeCache().DataProviderMetadata);
    ann.addMember(DataProviderMetadata.LINE, new ConstantExpression(dataProviderExpr.getLineNumber()));
    List<Expression> dataVariableNames = new ArrayList<>();
    for (int i = nextDataVariableIndex; i < dataProcessorVars.size(); i++)
      dataVariableNames.add(new ConstantExpression(dataProcessorVars.get(i).getName()));
    ann.addMember(DataProviderMetadata.DATA_VARIABLES, new ListExpression(dataVariableNames));
    return ann;
  }

  private Parameter createDataProcessorParameter() {
    Parameter p = new Parameter(ClassHelper.DYNAMIC_TYPE, "$spock_p" + dataProcessorParams.size());
    dataProcessorParams.add(p);
    return p;
  }

  // generates: arg = argMethodParam
  private void rewriteSimpleParameterization(BinaryExpression binExpr, ASTNode sourcePos)
      throws InvalidSpecCompileException {
    int nextDataVariableIndex = dataProcessorVars.size();
    Parameter dataProcessorParameter = createDataProcessorParameter();
    VariableExpression arg = (VariableExpression) binExpr.getLeftExpression();

    VariableExpression dataVar = createDataProcessorVariable(arg, sourcePos);
    ExpressionStatement exprStat = new ExpressionStatement(
        new DeclarationExpression(
            dataVar,
            Token.newSymbol(Types.ASSIGN, -1, -1),
            new VariableExpression(dataProcessorParameter)));
    exprStat.setSourcePosition(sourcePos);
    dataProcessorStats.add(exprStat);

    createDataProviderMethod(binExpr.getRightExpression(), nextDataVariableIndex);
  }

  // generates:
  // arg0 = argMethodParam.getAt(0)
  // arg1 = argMethodParam.getAt(1)
  private void rewriteMultiParameterization(BinaryExpression binExpr, Statement enclosingStat)
      throws InvalidSpecCompileException {
    int nextDataVariableIndex = dataProcessorVars.size();
    Parameter dataProcessorParameter = createDataProcessorParameter();
    ListExpression list = (ListExpression) binExpr.getLeftExpression();

    @SuppressWarnings("unchecked")
    List<Expression> listElems = list.getExpressions();
    for (int i = 0; i < listElems.size(); i++) {
      Expression listElem = listElems.get(i);
      if (AstUtil.isWildcardRef(listElem)) continue;
      VariableExpression dataVar = createDataProcessorVariable(listElem, enclosingStat);
      ExpressionStatement exprStat =
          new ExpressionStatement(
              new DeclarationExpression(
                  dataVar,
                  Token.newSymbol(Types.ASSIGN, -1, -1),
                  createGetAtMethod(new VariableExpression(dataProcessorParameter), i)));
      exprStat.setSourcePosition(enclosingStat);
      dataProcessorStats.add(exprStat);
    }

    createDataProviderMethod(binExpr.getRightExpression(), nextDataVariableIndex);
  }

  private void rewriteDerivedParameterization(BinaryExpression parameterization, Statement enclosingStat)
      throws InvalidSpecCompileException {
    VariableExpression dataVar = createDataProcessorVariable(parameterization.getLeftExpression(), enclosingStat);

    ExpressionStatement exprStat =
        new ExpressionStatement(
            new DeclarationExpression(
                dataVar,
                Token.newSymbol(Types.ASSIGN, -1, -1),
                parameterization.getRightExpression()));

    exprStat.setSourcePosition(enclosingStat);
    dataProcessorStats.add(exprStat);
  }

  private void rewriteBinaryTableLikeParameterization(ListIterator<Statement> stats) throws InvalidSpecCompileException {
    rewriteTableLikeParameterization(stats, result -> {
      Statement stat = stats.next();
      BinaryExpression orExpr = getOrExpression(stat);
      if (orExpr == null) {
        stats.previous();
        return true;
      }
      splitRow(orExpr, result);
      return false;
    });
  }

  private void rewriteExpressionTableLikeParameterization(ListIterator<Statement> stats) throws InvalidSpecCompileException {
    rewriteTableLikeParameterization(stats, result -> {
      List<Expression> row = getExpressionChain(stats);
      if (row.size() <= 1) {
        // rewind
        row.forEach(expression -> stats.previous());
        return true;
      }
      result.addAll(row);
      return false;
    });
  }

  private void rewriteTableLikeParameterization(ListIterator<Statement> stats, Function<List<Expression>, Boolean> rowExtractor) throws InvalidSpecCompileException {
    LinkedList<List<Expression>> rows = new LinkedList<>();

    while (stats.hasNext()) {
      List<Expression> row = new ArrayList<>();
      if (rowExtractor.apply(row)) {
        break;
      }
      if (rows.size() > 0 && rows.getLast().size() != row.size())
        throw new InvalidSpecCompileException(row.get(0), String.format("Row in data table has wrong number of elements (%s instead of %s)", row.size(), rows.getLast().size()));
      rows.add(row);
    }

    for (List<Expression> column : transposeTable(rows))
      turnIntoSimpleParameterization(column);
  }

  List<List<Expression>> transposeTable(List<List<Expression>> rows) {
    List<List<Expression>> columns = new ArrayList<>();
    if (rows.isEmpty()) return columns;

    for (int i = 0; i < rows.get(0).size(); i++)
      columns.add(new ArrayList<>());

    for (List<Expression> row : rows)
      for (int i = 0; i < row.size(); i++)
        columns.get(i).add(row.get(i));

    return columns;
  }

  private void turnIntoSimpleParameterization(List<Expression> column) throws InvalidSpecCompileException {
    VariableExpression varExpr = ObjectUtil.asInstance(column.get(0), VariableExpression.class);
    if (varExpr == null)
      throw dataTableHeaderMayOnlyContainVariableNames(column.get(0));
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
    rewriteSimpleParameterization(binExpr, varExpr);
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

  private BinaryExpression getOrExpression(Expression expr) {
    BinaryExpression binExpr = ObjectUtil.asInstance(expr, BinaryExpression.class);
    if (binExpr == null) return null;

    int binExprType = binExpr.getOperation().getType();
    if (binExprType == Types.BITWISE_OR || binExprType == Types.LOGICAL_OR) return binExpr;

    return null;
  }

  private VariableExpression createDataProcessorVariable(Expression varExpr, ASTNode sourcePos)
      throws InvalidSpecCompileException {
    if (!(varExpr instanceof VariableExpression))
      throw notAParameterization(sourcePos);

    VariableExpression typedVarExpr = (VariableExpression)varExpr;
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
      if (var.getName().equals(name))
        return true;
    return false;
  }

  private void handleFeatureParameters() {
    Parameter[] parameters = whereBlock.getParent().getAst().getParameters();
    if (parameters.length == 0)
      addFeatureParameters();
    else
      checkAllParametersAreDataVariables(parameters);
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

    dataProcessorStats.add(
        new ReturnStatement(
            new ArrayExpression(
                ClassHelper.OBJECT_TYPE,
                (List) dataProcessorVars)));

    BlockStatement blockStat = new BlockStatement(dataProcessorStats, new VariableScope());
    new DataProcessorVariableRewriter().visitBlockStatement(blockStat);

    whereBlock.getParent().getParent().getAst().addMethod(
      new MethodNode(
          InternalIdentifiers.getDataProcessorName(whereBlock.getParent().getAst().getName()),
          Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
          ClassHelper.OBJECT_TYPE,
          dataProcessorParams.toArray(new Parameter[dataProcessorParams.size()]),
          ClassNode.EMPTY_ARRAY,
          blockStat));
  }

  private static InvalidSpecCompileException notAParameterization(ASTNode stat) {
    return new InvalidSpecCompileException(stat,
"where-blocks may only contain parameterizations (e.g. 'salary << [1000, 5000, 9000]; salaryk = salary / 1000')");
  }

  private static InvalidSpecCompileException dataTableHeaderMayOnlyContainVariableNames(ASTNode stat) {
    return new InvalidSpecCompileException(stat, "Header of data table may only contain variable names");
  }

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

  private class DataTablePreviousVariableTransformer extends ClassCodeExpressionTransformer {
    private int depth = 0;
    private int rowIndex = -1;

    @Override
    protected SourceUnit getSourceUnit() { return null; }

    @Override
    public Expression transform(Expression expression) {
      if (depth == 0)
        rowIndex++;

      if ((expression instanceof VariableExpression) && isDataProcessorVariable(expression.getText())) {
        return AstUtil.createGetAtMethod(expression, rowIndex);
      }

      depth++;
      Expression transform = super.transform(expression);
      depth--;

      return transform;
    }
  }
}
