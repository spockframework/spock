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

import org.spockframework.compiler.model.WhereBlock;
import org.spockframework.runtime.model.DataProviderMetadata;
import org.spockframework.util.*;

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
  private final List<Parameter> dataProcessorParams = new ArrayList<Parameter>();
  // statements of the data processor method (one for each parameterization variable)
  private final List<Statement> dataProcessorStats = new ArrayList<Statement>();
  // parameterization variables of the data processor method
  private final List<VariableExpression> dataProcessorVars = new ArrayList<VariableExpression>();

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
    BinaryExpression binExpr = AstUtil.getExpression(stat, BinaryExpression.class);
    if (binExpr == null || binExpr.getClass() != BinaryExpression.class) // don't allow subclasses like DeclarationExpression
      notAParameterization(stat);

    @SuppressWarnings("ConstantConditions")
    int type = binExpr.getOperation().getType();

    if (type == Types.LEFT_SHIFT) {
      Expression leftExpr = binExpr.getLeftExpression();
      if (leftExpr instanceof VariableExpression)
        rewriteSimpleParameterization(binExpr, stat);
      else if (leftExpr instanceof ListExpression)
        rewriteMultiParameterization(binExpr, stat);
      else notAParameterization(stat);
    } else if (type == Types.ASSIGN)
      rewriteDerivedParameterization(binExpr, stat);
    else if (type == Types.BITWISE_OR) {
      stats.previous();
      rewriteTableLikeParameterization(stats);
    }
    else notAParameterization(stat);
  }

  private void createDataProviderMethod(Expression dataProviderExpr, int nextDataVariableIndex) {
    instanceFieldAccessChecker.check(dataProviderExpr);

    MethodNode method =
        new MethodNode(
            InternalIdentifiers.getDataProviderName(whereBlock.getParent().getAst().getName(), dataProviderCount++),
            Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
            ClassHelper.OBJECT_TYPE,
            Parameter.EMPTY_ARRAY,
            ClassNode.EMPTY_ARRAY,
            new BlockStatement(
                Arrays.<Statement> asList(
                    new ReturnStatement(
                        new ExpressionStatement(dataProviderExpr))),
                new VariableScope()));

    method.addAnnotation(createDataProviderAnnotation(dataProviderExpr, nextDataVariableIndex));
    whereBlock.getParent().getParent().getAst().addMethod(method);
  }

  private AnnotationNode createDataProviderAnnotation(Expression dataProviderExpr, int nextDataVariableIndex) {
    AnnotationNode ann = new AnnotationNode(resources.getAstNodeCache().DataProviderMetadata);
    ann.addMember(DataProviderMetadata.LINE, new ConstantExpression(dataProviderExpr.getLineNumber()));
    ann.addMember(DataProviderMetadata.COLUMN, new ConstantExpression(dataProviderExpr.getColumnNumber()));
    List<Expression> dataVariableNames = new ArrayList<Expression>();
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
                  new MethodCallExpression(
                      new VariableExpression(dataProcessorParameter),
                      "getAt",
                      new ConstantExpression(i))));
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

  private void rewriteTableLikeParameterization(ListIterator<Statement> stats) throws InvalidSpecCompileException {
    LinkedList<List<Expression>> rows = new LinkedList<List<Expression>>();

    while (stats.hasNext()) {
      Statement stat = stats.next();
      BinaryExpression binExpr = AstUtil.getExpression(stat, BinaryExpression.class);
      if (binExpr == null || binExpr.getOperation().getType() != Types.BITWISE_OR) {
        stats.previous();
        break;
      }
      List<Expression> row = new ArrayList<Expression>();
      splitRow(binExpr, row);
      if (rows.size() > 0 && rows.getLast().size() != row.size())
        throw new InvalidSpecCompileException(stat, "Row in data table has wrong number of elements");
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
    VariableExpression varExpr = AstUtil.asInstance(column.get(0), VariableExpression.class);
    if (varExpr == null)
      throw new InvalidSpecCompileException(column.get(0),
          "Header of data table may only contain variable names");

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
    BinaryExpression binExpr = AstUtil.asInstance(row, BinaryExpression.class);
    if (binExpr == null || binExpr.getOperation().getType() != Types.BITWISE_OR)
      parts.add(row);
    else {
      splitRow(binExpr.getLeftExpression(), parts);
      parts.add(binExpr.getRightExpression());
    }
  }

  private VariableExpression createDataProcessorVariable(Expression varExpr, ASTNode sourcePos)
      throws InvalidSpecCompileException {
    if (!(varExpr instanceof VariableExpression))
      notAParameterization(sourcePos);

    VariableExpression typedVarExpr = (VariableExpression)varExpr;
    verifyDataProcessorVariable(typedVarExpr);

    VariableExpression result = new VariableExpression(typedVarExpr.getName(), typedVarExpr.getType());
    dataProcessorVars.add(result);
    return result;
  }

  private void verifyDataProcessorVariable(VariableExpression varExpr) {
    Variable accessedVar = varExpr.getAccessedVariable();
    if (!(accessedVar instanceof DynamicVariable || accessedVar instanceof Parameter)) {
      resources.getErrorReporter().error(varExpr, "A variable named '%s' already exists in this scope", varExpr.getName());
      return;
    }

    if (whereBlock.getParent().getAst().getParameters().length == 0) {
      assert accessedVar instanceof DynamicVariable;
      if (getDataProcessorVariable(varExpr.getName()) != null)
        resources.getErrorReporter().error(varExpr, "Duplicate declaration of data variable '%s'", varExpr.getName());
    } else {
      if (!(accessedVar instanceof Parameter))
        resources.getErrorReporter().error(varExpr,
            "Data variable '%s' needs to be declared as method parameter",
            varExpr.getName());
    }
  }

  private VariableExpression getDataProcessorVariable(String name) {
    for (VariableExpression var : dataProcessorVars)
      if (var.getName().equals(name)) return var;

    return null;
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
      if (getDataProcessorVariable(param.getName()) == null)
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
                (List)dataProcessorVars)));

    whereBlock.getParent().getParent().getAst().addMethod(
      new MethodNode(
          InternalIdentifiers.getDataProcessorName(whereBlock.getParent().getAst().getName()),
          Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
          ClassHelper.OBJECT_TYPE,
          dataProcessorParams.toArray(new Parameter[dataProcessorParams.size()]),
          ClassNode.EMPTY_ARRAY,
          new BlockStatement(
              dataProcessorStats,
              new VariableScope())));
  }

  private static void notAParameterization(ASTNode stat) throws InvalidSpecCompileException {
    throw new InvalidSpecCompileException(stat,
"where-blocks may only contain parameterizations (e.g. 'salary << [1000, 5000, 9000]; salaryk = salary / 1000')");
  }
}
