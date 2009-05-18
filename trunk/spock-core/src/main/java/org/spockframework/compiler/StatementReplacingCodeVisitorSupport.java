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
import org.codehaus.groovy.control.SourceUnit;

/**
 * Adds the ability to replace statements.
 *
 * @author Peter Niederwieser
 */
// Implementation note: It is only necessary to override visit methods
// for AST nodes that reference statements. For ClosureExpression we rely on
// the assumption that it always references a BlockStatement and hence our
// visitBlockStatement() method gets called.
public abstract class StatementReplacingCodeVisitorSupport extends CodeVisitorSupport {
  private Statement replacement;

  /*
   * Visits the specified statement. If the statement's visit method calls
   * replaceVisitedMethodWith(), the statement will be replaced.
   */
  public Statement replace(Statement stat) {
    replacement = null;
    stat.visit(this);
    Statement result = replacement == null ? stat : replacement;
    replacement = null;
    return result;
  }

  /*
   * Visits the statements in the specified mutable list. If a statement's
   * visit method calls replaceVisitedMethodWith(), the statement will be
   * replaced.
   */
  protected void replaceAll(List<Statement> stats) {
    ListIterator<Statement> iter = stats.listIterator();
    while (iter.hasNext())
      iter.set(replace(iter.next()));
  }

  /*
   * Allows to visit an expression in-place (e.g. when constructing a new statement).
   */
  protected <T extends Expression> T visit(T expr) {
    expr.visit(this);
    return expr;
  }

  /*
   * Replaces the currently visited statement with the specified statement.
   */
  protected void replaceVisitedStatementWith(Statement other) {
    replacement = other;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void visitBlockStatement(BlockStatement stat) {
    replaceAll(stat.getStatements());
  }

  @Override
  public void visitForLoop(ForStatement stat) {
    visit(stat.getCollectionExpression());
    stat.setLoopBlock(replace(stat.getLoopBlock()));
  }

  @Override
  public void visitWhileLoop(WhileStatement stat) {
   visit(stat.getBooleanExpression());
   stat.setLoopBlock(replace(stat.getLoopBlock()));
  }

  @Override
  public void visitDoWhileLoop(DoWhileStatement stat) {
    visit(stat.getBooleanExpression());
    stat.setLoopBlock(replace(stat.getLoopBlock()));
  }

  @Override
  public void visitIfElse(IfStatement stat) {
    visit(stat.getBooleanExpression());
    stat.setIfBlock(replace(stat.getIfBlock()));
    stat.setElseBlock(replace(stat.getElseBlock()));
  }

  @SuppressWarnings("unchecked")
  @Override
  public void visitTryCatchFinally(TryCatchStatement stat) {
    stat.setTryStatement(replace(stat.getTryStatement()));
    replaceAll(stat.getCatchStatements());
    stat.setFinallyStatement(replace(stat.getFinallyStatement()));
  }

  @SuppressWarnings("unchecked")  
  @Override
  public void visitSwitch(SwitchStatement stat) {
    visit(stat.getExpression());
    replaceAll(stat.getCaseStatements());
    stat.setDefaultStatement(replace(stat.getDefaultStatement()));
  }

  @Override
  public void visitCaseStatement(CaseStatement stat) {
    visit(stat.getExpression());
    stat.setCode(replace(stat.getCode()));
  }

  @Override
  public void visitSynchronizedStatement(SynchronizedStatement stat) {
    visit(stat.getExpression());
    stat.setCode(replace(stat.getCode()));
  }

  @Override
  public void visitCatchStatement(CatchStatement stat) {
    stat.setCode(replace(stat.getCode()));
  }

  protected SourceUnit getSourceUnit() {
    throw new UnsupportedOperationException("getSourceUnit");
  }
}
