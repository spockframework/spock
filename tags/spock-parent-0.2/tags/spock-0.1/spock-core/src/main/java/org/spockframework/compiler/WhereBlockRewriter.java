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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

import org.spockframework.compiler.model.WhereBlock;
import org.spockframework.util.BinaryNames;
import org.spockframework.util.SyntaxException;

/**
 *
 * @author Peter Niederwieser
 */
public class WhereBlockRewriter {
  private final WhereBlock block;

  private int dataProviderCount = 0;
  // parameters of the data processor method (one for each data provider)
  private final List<Parameter> dataProcessorParams = new ArrayList<Parameter>();
  // statements of the data processor method (one for each parameterization variable)
  private final List<Statement> dataProcessorStats = new ArrayList<Statement>();
  // parameterization variables of the data processor method
  private final List<VariableExpression> dataProcessorVars = new ArrayList<VariableExpression>();

  private WhereBlockRewriter(WhereBlock block) {
    this.block = block;
  }

  public static void rewrite(WhereBlock block) {
    new WhereBlockRewriter(block).rewrite();
  }

  private void rewrite() {
    List<Statement> whereStats = block.getAst();

    for (Statement stat : whereStats) {
      BinaryExpression binExpr = AstUtil.getExpression(stat, BinaryExpression.class);
      if (binExpr == null) invalidParameterization(stat);

      int type = binExpr.getOperation().getType();
      if (type == Types.LEFT_SHIFT) {
        createDataProviderMethod(binExpr);
        Parameter parameter = createDataProcessorParameter();
        Expression leftExpr = binExpr.getLeftExpression();
        if (leftExpr instanceof VariableExpression)
          rewriteSimpleParameterization((VariableExpression)leftExpr, parameter);
        else if (leftExpr instanceof ListExpression)
          rewriteMultiParameterization((ListExpression)leftExpr, parameter, stat);
        else invalidParameterization(stat);
      } else if (type == Types.ASSIGN)
        rewriteDerivedParameterization(binExpr, stat);
      else invalidParameterization(stat);
    }

    whereStats.clear();
    addFeatureParameters();
    createDataProcessorMethod();
  }

  private void createDataProviderMethod(BinaryExpression binExpr) {
    block.getParent().getParent().getAst().addMethod(
        new MethodNode(
            BinaryNames.getDataProviderName(block.getParent().getAst().getName(), dataProviderCount++),
            Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
            ClassHelper.OBJECT_TYPE,
            Parameter.EMPTY_ARRAY,
            ClassNode.EMPTY_ARRAY,
            new BlockStatement(
                Arrays.asList(
                    new ReturnStatement(
                    new ExpressionStatement(
                        binExpr.getRightExpression()))),
                new VariableScope())));
  }

  private Parameter createDataProcessorParameter() {
    Parameter p = new Parameter(ClassHelper.DYNAMIC_TYPE, "p" + dataProcessorParams.size());
    dataProcessorParams.add(p);
    return p;
  }

  // generates: arg = argMethodParam
  private void rewriteSimpleParameterization(VariableExpression arg, Parameter argComputerParameter) {
    dataProcessorVars.add(arg);
    dataProcessorStats.add(
        new ExpressionStatement(
            new DeclarationExpression(
                arg,
                Token.newSymbol(Types.ASSIGN, -1, -1),
                new VariableExpression(argComputerParameter))));
  }

  // generates:
  // arg0 = argMethodParam.getAt(0)
  // arg1 = argMethodParam.getAt(1)
  private void rewriteMultiParameterization(ListExpression list, Parameter argComputerParameter, Statement enclosingStat) {
    @SuppressWarnings("unchecked")
    List<Expression> listElems = list.getExpressions();
    for (int i = 0; i < listElems.size(); i++) {
      Expression listElem = listElems.get(i);
      if (AstUtil.isPlaceholderVariableRef(listElem)) continue;
      if (!(listElem instanceof VariableExpression)) invalidParameterization(enclosingStat);
      VariableExpression arg = (VariableExpression)listElem;
      dataProcessorVars.add(arg);
      dataProcessorStats.add(
          new ExpressionStatement(
              new DeclarationExpression(
                  arg,
                  Token.newSymbol(Types.ASSIGN, -1, -1),
                  new MethodCallExpression(
                      new VariableExpression(argComputerParameter),
                      "getAt",
                      new ConstantExpression(i)))));
    }
  }

  private void rewriteDerivedParameterization(BinaryExpression parameterization, Statement enclosingStat) {
    Expression leftExpr = parameterization.getLeftExpression();
    if (!(leftExpr instanceof VariableExpression)) invalidParameterization(enclosingStat);
    VariableExpression arg = (VariableExpression)leftExpr;
    dataProcessorVars.add(arg);
    dataProcessorStats.add(
        new ExpressionStatement(
            new DeclarationExpression(
                arg,
                Token.newSymbol(Types.ASSIGN, -1, -1),
                parameterization.getRightExpression())));
  }

  private void addFeatureParameters() {
    if (block.getParent().getAst().getParameters().length > 0)
      return;

    Parameter[] parameters = new Parameter[dataProcessorVars.size()];
    for (int i = 0; i < dataProcessorVars.size(); i++)
      parameters[i] = new Parameter(ClassHelper.DYNAMIC_TYPE, dataProcessorVars.get(i).getName());
    block.getParent().getAst().setParameters(parameters);
  }

  private void createDataProcessorMethod() {
    if (dataProcessorVars.isEmpty()) return;
    
    dataProcessorStats.add(
        new ReturnStatement(
            new ArrayExpression(
                ClassHelper.OBJECT_TYPE,
                dataProcessorVars)));
    
    block.getParent().getParent().getAst().addMethod(
      new MethodNode(
          BinaryNames.getDataProcessorName(block.getParent().getAst().getName()),
          Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
          ClassHelper.OBJECT_TYPE,
          dataProcessorParams.toArray(new Parameter[dataProcessorParams.size()]),
          ClassNode.EMPTY_ARRAY,
          new BlockStatement(
              dataProcessorStats,
              new VariableScope())));
  }

  private static void invalidParameterization(Statement stat) {
    throw new SyntaxException(stat,
"where-blocks may only contain parameterizations (e.g. 'salary << [1000, 3000, 5000]; salaryk = salary / 1000')");
  }
}
