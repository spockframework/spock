/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.compiler.condition;

import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.spockframework.compiler.AstNodeCache;
import org.spockframework.compiler.SpockNames;

import java.util.ArrayList;
import java.util.List;

import static org.spockframework.compiler.AstUtil.createDirectMethodCall;
import static org.spockframework.util.Assert.notNull;

public class DefaultConditionErrorRecorders implements IConditionErrorRecorders {

  private final AstNodeCache nodeCache;

  public DefaultConditionErrorRecorders(AstNodeCache nodeCache) {
    this.nodeCache = notNull(nodeCache);
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

}
