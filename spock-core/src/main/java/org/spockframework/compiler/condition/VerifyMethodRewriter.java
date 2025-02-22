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
import org.spockframework.compiler.AstUtil;
import org.spockframework.compiler.ConditionRewriter;
import org.spockframework.compiler.IRewriteResources;
import org.spockframework.compiler.StatementReplacingVisitorSupport;

import static org.spockframework.compiler.condition.ImplicitConditionsUtils.checkIsValidImplicitCondition;
import static org.spockframework.compiler.condition.ImplicitConditionsUtils.isImplicitCondition;

public class VerifyMethodRewriter extends StatementReplacingVisitorSupport {

  private final IRewriteResources resources;
  private final MethodNode methodNode;
  private boolean conditionFound = false;

  public VerifyMethodRewriter(MethodNode methodNode, IRewriteResources resources) {
    this.resources = resources;
    this.methodNode = methodNode;
  }

  public static void rewrite(MethodNode node, IRewriteResources resources) {
    VerifyMethodRewriter rewriter = new VerifyMethodRewriter(node, resources);
    node.getCode().visit(rewriter);
    rewriter.defineRecorders();
  }

  @Override
  public void visitAssertStatement(AssertStatement stat) {
    super.visitAssertStatement(stat);
    conditionFound();
    replaceVisitedStatementWith(ConditionRewriter.rewriteExplicitCondition(stat, resources));
  }

  @Override
  public void visitExpressionStatement(ExpressionStatement statement) {
    super.visitExpressionStatement(statement);
    if (isImplicitCondition(statement)) {
      checkIsValidImplicitCondition(statement, resources.getErrorReporter());
      conditionFound();
      replaceVisitedStatementWith(ConditionRewriter.rewriteImplicitCondition(statement, resources));
    }
  }

  private void conditionFound() {
    conditionFound = true;
  }

  private void defineRecorders() {
    if (conditionFound) {
      IConditionErrorRecorders errorRecorders = resources.getErrorRecorders();
      errorRecorders.defineErrorRethrower(AstUtil.getStatements(methodNode));
      errorRecorders.defineValueRecorder(AstUtil.getStatements(methodNode));
    }
  }
}
