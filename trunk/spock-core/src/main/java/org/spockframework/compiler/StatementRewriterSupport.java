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

import java.util.List;
import java.util.ListIterator;

import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.*;

/**
 * Walks the statement and expression tree and allows for statements (but not
 * expressions) to be replaced.
 *
 * @author Peter Niederwieser
 */
public class StatementRewriterSupport extends CodeVisitorSupport {
  protected Statement result;

  public Statement visit(Statement stat) {
    // EmptyStatement has no visit method, hence result won't be set
    if (stat instanceof EmptyStatement) return stat;

    stat.visit(this);
    return result;
  }

  public <T extends Expression> T visit(T expr) {
    expr.visit(this);
    return expr;
  }

  @Override
  public void visitBlockStatement(BlockStatement block) {
    @SuppressWarnings("unchecked")
    ListIterator<Statement> iter = block.getStatements().listIterator();
    while (iter.hasNext())
      iter.set(visit(iter.next()));
  }

  // missing: setLoopBlock
  @Override
  public void visitForLoop(ForStatement stat) {
    ForStatement newStat = new ForStatement(
        stat.getVariable(),
        visit(stat.getCollectionExpression()),
        visit(stat.getLoopBlock()));

    newStat.setVariableScope(stat.getVariableScope());

    result = newStat;
    result.setSourcePosition(stat);
    result.setStatementLabel(stat.getStatementLabel());
  }

  // missing: setLoopBlock()
  @Override
  public void visitWhileLoop(WhileStatement stat) {
    result = new WhileStatement(
        visit(stat.getBooleanExpression()),
        visit(stat.getLoopBlock()));

    result.setSourcePosition(stat);
    result.setStatementLabel(stat.getStatementLabel());
  }

  // missing: setLoopBlock()
  @Override
  public void visitDoWhileLoop(DoWhileStatement stat) {
    result = new DoWhileStatement(
        visit(stat.getBooleanExpression()),
        visit(stat.getLoopBlock()));

    result.setSourcePosition(stat);
    result.setStatementLabel(stat.getStatementLabel());
  }

  @Override
  public void visitIfElse(IfStatement stat) {
    visit(stat.getBooleanExpression());
    stat.setIfBlock(visit(stat.getIfBlock()));
    stat.setElseBlock(visit(stat.getElseBlock()));
    result = stat;
  }

  @Override
  public void visitExpressionStatement(ExpressionStatement stat) {
    visit(stat.getExpression());
    result = stat;
  }

  @Override
  public void visitReturnStatement(ReturnStatement stat) {
    visit(stat.getExpression());
    result = stat;
  }

  @Override
  public void visitAssertStatement(AssertStatement stat) {
    visit(stat.getBooleanExpression());
    visit(stat.getMessageExpression());
    result = stat;
  }

  // missing: setFinallyStatement()
  @Override
  public void visitTryCatchFinally(TryCatchStatement stat) {
    TryCatchStatement newStat = new TryCatchStatement(
        visit(stat.getTryStatement()),
        visit(stat.getFinallyStatement()));

    @SuppressWarnings("unchecked")
    List<CatchStatement> catchStats = stat.getCatchStatements();
    for (CatchStatement catchStat : catchStats)
      newStat.addCatch((CatchStatement)visit(catchStat));

    result = newStat;
    result.setSourcePosition(stat);
    result.setStatementLabel(stat.getStatementLabel());
  }

  @Override
  public void visitSwitch(SwitchStatement stat) {
    visit(stat.getExpression());

    @SuppressWarnings("unchecked")
    ListIterator<CaseStatement> iter = stat.getCaseStatements().listIterator();
    while (iter.hasNext())
      iter.set((CaseStatement)visit(iter.next()));

    stat.setDefaultStatement(visit(stat.getDefaultStatement()));

    result = stat;
  }

  // missing: setCode()
  @Override
  public void visitCaseStatement(CaseStatement stat) {
    result = new CaseStatement(
        visit(stat.getExpression()),
        visit(stat.getCode()));

    result.setSourcePosition(stat);
    result.setStatementLabel(stat.getStatementLabel());
  }

  @Override
  public void visitBreakStatement(BreakStatement stat) {
    result = stat;
  }

  @Override
  public void visitContinueStatement(ContinueStatement stat) {
    result = stat;
  }

  @Override
  public void visitSynchronizedStatement(SynchronizedStatement stat) {
    visit(stat.getExpression());
    stat.setCode(visit(stat.getCode()));
    result = stat;
  }

  @Override
  public void visitThrowStatement(ThrowStatement stat) {
    visit(stat.getExpression());
    result = stat;
  }

  @Override
  public void visitCatchStatement(CatchStatement stat) {
    stat.setCode(visit(stat.getCode()));
    result = stat;
  }
}
