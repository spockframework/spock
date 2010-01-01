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
  private final AstNodeCache nodeCache;
  private final ErrorReporter errorReporter;

  private int dataProviderCount = 0;
  // parameters of the data processor method (one for each data provider)
  private final List<Parameter> dataProcessorParams = new ArrayList<Parameter>();
  // statements of the data processor method (one for each parameterization variable)
  private final List<Statement> dataProcessorStats = new ArrayList<Statement>();
  // parameterization variables of the data processor method
  private final List<VariableExpression> dataProcessorVars = new ArrayList<VariableExpression>();

  private WhereBlockRewriter(WhereBlock whereBlock, AstNodeCache nodeCache, ErrorReporter errorReporter) {
    this.whereBlock = whereBlock;
    this.nodeCache = nodeCache;
    this.errorReporter = errorReporter;
  }

  public static void rewrite(WhereBlock block, AstNodeCache nodeCache, ErrorReporter errorReporter) {
    new WhereBlockRewriter(block, nodeCache, errorReporter).rewrite();
  }

  private void rewrite() {
    for (Statement stat : whereBlock.getAst())
      try {
        rewriteWhereStat(stat);
      } catch (InvalidSpecCompileException e) {
        errorReporter.error(e);
      }

    whereBlock.getAst().clear();
    handleFeatureParameters();
    createDataProcessorMethod();
  }

  private void rewriteWhereStat(Statement stat) throws InvalidSpecCompileException {
    BinaryExpression binExpr = AstUtil.getExpression(stat, BinaryExpression.class);
    if (binExpr == null || binExpr.getClass() != BinaryExpression.class) // don't allow subclasses like DeclarationExpression
      notAParameterization(stat);

    @SuppressWarnings("ConstantConditions")
    int type = binExpr.getOperation().getType();

    if (type == Types.LEFT_SHIFT) {
      int nextDataVariableIndex = dataProcessorVars.size();

      Parameter parameter = createDataProcessorParameter();
      Expression leftExpr = binExpr.getLeftExpression();
      if (leftExpr instanceof VariableExpression)
        rewriteSimpleParameterization((VariableExpression)leftExpr, parameter, stat);
      else if (leftExpr instanceof ListExpression)
        rewriteMultiParameterization((ListExpression)leftExpr, parameter, stat);
      else notAParameterization(stat);

      createDataProviderMethod(binExpr, nextDataVariableIndex);
    } else if (type == Types.ASSIGN)
      rewriteDerivedParameterization(binExpr, stat);
    else notAParameterization(stat);
  }

  private void createDataProviderMethod(BinaryExpression binExpr, int nextDataVariableIndex) {
    Expression dataProviderExpr = binExpr.getRightExpression();

    MethodNode method =
        new MethodNode(
            BinaryNames.getDataProviderName(whereBlock.getParent().getAst().getName(), dataProviderCount++),
            Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
            ClassHelper.OBJECT_TYPE,
            Parameter.EMPTY_ARRAY,
            ClassNode.EMPTY_ARRAY,
            new BlockStatement(
                Arrays.asList(
                    new ReturnStatement(
                        new ExpressionStatement(dataProviderExpr))),
                new VariableScope()));

    method.addAnnotation(createDataProviderAnnotation(dataProviderExpr, nextDataVariableIndex));
    whereBlock.getParent().getParent().getAst().addMethod(method);
  }

  private AnnotationNode createDataProviderAnnotation(Expression dataProviderExpr, int nextDataVariableIndex) {
    AnnotationNode ann = new AnnotationNode(nodeCache.DataProviderMetadata);
    ann.addMember(DataProviderMetadata.LINE, new ConstantExpression(dataProviderExpr.getLineNumber()));
    ann.addMember(DataProviderMetadata.COLUMN, new ConstantExpression(dataProviderExpr.getColumnNumber()));
    List<Expression> dataVariableNames = new ArrayList<Expression>();
    for (int i = nextDataVariableIndex; i < dataProcessorVars.size(); i++)
      dataVariableNames.add(new ConstantExpression(dataProcessorVars.get(i).getName()));
    ann.addMember(DataProviderMetadata.DATA_VARIABLES, new ListExpression(dataVariableNames));
    return ann;
  }

  private Parameter createDataProcessorParameter() {
    Parameter p = new Parameter(ClassHelper.DYNAMIC_TYPE, "p" + dataProcessorParams.size());
    dataProcessorParams.add(p);
    return p;
  }

  // generates: arg = argMethodParam
  private void rewriteSimpleParameterization(VariableExpression arg, Parameter dataProcessorParameter,
      Statement enclosingStat) throws InvalidSpecCompileException {
    VariableExpression dataVar = createDataProcessorVariable(arg, enclosingStat);
    ExpressionStatement exprStat = new ExpressionStatement(
        new DeclarationExpression(
            dataVar,
            Token.newSymbol(Types.ASSIGN, -1, -1),
            new VariableExpression(dataProcessorParameter)));
    exprStat.setSourcePosition(enclosingStat);
    dataProcessorStats.add(exprStat);
  }

  // generates:
  // arg0 = argMethodParam.getAt(0)
  // arg1 = argMethodParam.getAt(1)
  private void rewriteMultiParameterization(ListExpression list, Parameter dataProcessorParameter,
      Statement enclosingStat) throws InvalidSpecCompileException {
    @SuppressWarnings("unchecked")
    List<Expression> listElems = list.getExpressions();
    for (int i = 0; i < listElems.size(); i++) {
      Expression listElem = listElems.get(i);
      if (AstUtil.isPlaceholderVariableRef(listElem)) continue;
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

  private VariableExpression createDataProcessorVariable(Expression varExpr, Statement enclosingStat)
      throws InvalidSpecCompileException {
    if (!(varExpr instanceof VariableExpression))
      notAParameterization(enclosingStat);

    VariableExpression typedVarExpr = (VariableExpression)varExpr;
    verifyDataProcessorVariable(typedVarExpr);

    VariableExpression result = new VariableExpression(typedVarExpr.getName(), typedVarExpr.getType());
    dataProcessorVars.add(result);
    return result;
  }

  private void verifyDataProcessorVariable(VariableExpression varExpr) {
    Variable accessedVar = varExpr.getAccessedVariable();
    if (!(accessedVar instanceof DynamicVariable || accessedVar instanceof Parameter)) {
      errorReporter.error(varExpr, "A variable named '%s' already exists in this scope", varExpr.getName());
      return;
    }

    if (whereBlock.getParent().getAst().getParameters().length == 0) {
      assert accessedVar instanceof DynamicVariable;
      if (getDataProcessorVariable(varExpr.getName()) != null)
        errorReporter.error(varExpr, "Duplicate declaration of data variable '%s'", varExpr.getName());
    } else {
      if (!(accessedVar instanceof Parameter))
        errorReporter.error(varExpr,
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
        errorReporter.error(param, "Parameter '%s' does not refer to a data variable", param.getName());
  }

  private void addFeatureParameters() {
    Parameter[] parameters = new Parameter[dataProcessorVars.size()];
    for (int i = 0; i < dataProcessorVars.size(); i++)
      parameters[i] = new Parameter(ClassHelper.DYNAMIC_TYPE, dataProcessorVars.get(i).getName());
    whereBlock.getParent().getAst().setParameters(parameters);
  }

  private void createDataProcessorMethod() {
    if (dataProcessorVars.isEmpty()) return;
    
    dataProcessorStats.add(
        new ReturnStatement(
            new ArrayExpression(
                ClassHelper.OBJECT_TYPE,
                dataProcessorVars)));

    whereBlock.getParent().getParent().getAst().addMethod(
      new MethodNode(
          BinaryNames.getDataProcessorName(whereBlock.getParent().getAst().getName()),
          Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
          ClassHelper.OBJECT_TYPE,
          dataProcessorParams.toArray(new Parameter[dataProcessorParams.size()]),
          ClassNode.EMPTY_ARRAY,
          new BlockStatement(
              dataProcessorStats,
              new VariableScope())));
  }

  private static void notAParameterization(Statement stat) throws InvalidSpecCompileException {
    throw new InvalidSpecCompileException(stat,
"where-blocks may only contain parameterizations (e.g. 'salary << [1000, 5000, 9000]; salaryk = salary / 1000')");
  }
}
