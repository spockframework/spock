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

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.spockframework.compiler.AstUtil;
import org.spockframework.compiler.ConditionRewriter;
import org.spockframework.compiler.IRewriteResources;
import org.spockframework.compiler.StatementReplacingVisitorSupport;

import java.util.List;
import java.util.ListIterator;

import static org.spockframework.compiler.condition.ImplicitConditionsUtils.checkIsValidImplicitCondition;
import static org.spockframework.compiler.condition.ImplicitConditionsUtils.isImplicitCondition;

abstract class BaseVerifyMethodRewriter extends StatementReplacingVisitorSupport {

  final IRewriteResources resources;
  private final MethodNode methodNode;
  private Statement currentTopLevelStatement;
  private boolean conditionFound = false;

  BaseVerifyMethodRewriter(MethodNode methodNode, IRewriteResources resources) {
    this.resources = resources;
    this.methodNode = methodNode;
  }

  abstract void defineErrorCollector(List<Statement> statements);

  public void rewrite() {
    ListIterator<Statement> statements = AstUtil.getStatements(methodNode).listIterator();
    while (statements.hasNext()) {
      Statement next = statements.next();
      currentTopLevelStatement = next;
      statements.set(replace(next));
    }
    defineRecorders();
  }

  @Override
  public void visitAssertStatement(AssertStatement stat) {
    super.visitAssertStatement(stat);
    if (stat == currentTopLevelStatement) {
      conditionFound();
      replaceVisitedStatementWith(ConditionRewriter.rewriteExplicitCondition(stat, resources));
    }
  }

  @Override
  public void visitExpressionStatement(ExpressionStatement stat) {
    super.visitExpressionStatement(stat);
    if (stat == currentTopLevelStatement && isImplicitCondition(stat)) {
      checkIsValidImplicitCondition(stat, resources.getErrorReporter());
      conditionFound();
      replaceVisitedStatementWith(ConditionRewriter.rewriteImplicitCondition(stat, resources));
    }
  }

  private void conditionFound() {
    conditionFound = true;
  }

  private void defineRecorders() {
    if (conditionFound) {
      defineErrorCollector(AstUtil.getStatements(methodNode));
      resources.getErrorRecorders().defineValueRecorder(AstUtil.getStatements(methodNode));
    }
  }
}
