/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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

import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.classgen.BytecodeExpression;

/**
 * Groovy AST visitor that allows to replace both statements and expressions.
 *
 * @author Peter Niederwieser
 */
public class ExpressionReplacingVisitorSupport extends StatementReplacingVisitorSupport {
  private Expression replacementExpr;

  public Expression replaceExpr(Expression expr) {
    replacementExpr = null;
    expr.visit(this);
    Expression result = replacementExpr == null ? expr : replacementExpr;
    replacementExpr = null;
    return result;
  }

  @SuppressWarnings("unchecked")
  protected <T extends Expression> void replaceAllExprs(List<T> exprs) {
    ListIterator<T> iter = exprs.listIterator();
    while (iter.hasNext())
      iter.set((T) replaceExpr(iter.next()));
  }

  /*
   * Replaces the currently visited expression with the specified expression.
   */
  protected void replaceVisitedExpressionWith(Expression other) {
    replacementExpr = other;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void visitBlockStatement(BlockStatement stat) {
    replaceAll(stat.getStatements());
  }

  @Override
  public void visitForLoop(ForStatement stat) {
    stat.setCollectionExpression(replaceExpr(stat.getCollectionExpression()));
    stat.setLoopBlock(replace(stat.getLoopBlock()));
  }

  @Override
  public void visitWhileLoop(WhileStatement stat) {
    stat.setBooleanExpression((BooleanExpression)replaceExpr(stat.getBooleanExpression()));
    stat.setLoopBlock(replace(stat.getLoopBlock()));
  }

  @Override
  public void visitDoWhileLoop(DoWhileStatement stat) {
    stat.setBooleanExpression((BooleanExpression)replaceExpr(stat.getBooleanExpression()));
    stat.setLoopBlock(replace(stat.getLoopBlock()));
  }

  @Override
  public void visitIfElse(IfStatement stat) {
    stat.setBooleanExpression((BooleanExpression)replaceExpr(stat.getBooleanExpression()));
    stat.setIfBlock(replace(stat.getIfBlock()));
    stat.setElseBlock(replace(stat.getElseBlock()));
  }

  @Override
  @SuppressWarnings("unchecked")
  public void visitTryCatchFinally(TryCatchStatement stat) {
    stat.setTryStatement(replace(stat.getTryStatement()));
    replaceAll(stat.getCatchStatements());
    stat.setFinallyStatement(replace(stat.getFinallyStatement()));
  }

  @Override
  @SuppressWarnings("unchecked")
  public void visitSwitch(SwitchStatement stat) {
    stat.setExpression(replaceExpr(stat.getExpression()));
    replaceAll(stat.getCaseStatements());
    stat.setDefaultStatement(replace(stat.getDefaultStatement()));
  }

  @Override
  public void visitCaseStatement(CaseStatement stat) {
    stat.setExpression(replaceExpr(stat.getExpression()));
    stat.setCode(replace(stat.getCode()));
  }

  @Override
  public void visitSynchronizedStatement(SynchronizedStatement stat) {
    stat.setExpression(replaceExpr(stat.getExpression()));
    stat.setCode(replace(stat.getCode()));
  }

  @Override
  public void visitCatchStatement(CatchStatement stat) {
    stat.setCode(replace(stat.getCode()));
  }

  @Override
  public void visitMethodCallExpression(MethodCallExpression expr) {
    expr.setObjectExpression(replaceExpr(expr.getObjectExpression()));
    expr.setMethod(replaceExpr(expr.getMethod()));
    expr.setArguments(replaceExpr(expr.getArguments()));
  }

  @Override
  public void visitStaticMethodCallExpression(StaticMethodCallExpression expr) {
    StaticMethodCallExpression result = new StaticMethodCallExpression(
        expr.getOwnerType(),
        expr.getMethod(),
        replaceExpr(expr.getArguments()));
    result.setType(expr.getType());
    result.setSourcePosition(expr);
    replaceVisitedExpressionWith(result);
  }

  @Override
  public void visitConstructorCallExpression(ConstructorCallExpression expr) {
    ConstructorCallExpression result = new ConstructorCallExpression(
        expr.getType(),
        replaceExpr(expr.getArguments()));
    result.setSourcePosition(expr);
    result.setUsingAnonymousInnerClass(expr.isUsingAnonymousInnerClass());
    replaceVisitedExpressionWith(result);
  }

  @Override
  public void visitBinaryExpression(BinaryExpression expr) {
    expr.setLeftExpression(replaceExpr(expr.getLeftExpression()));
    expr.setRightExpression(replaceExpr(expr.getRightExpression()));
  }

  @Override
  public void visitTernaryExpression(TernaryExpression expr) {
    TernaryExpression result = new TernaryExpression(
        (BooleanExpression)replaceExpr(expr.getBooleanExpression()),
        replaceExpr(expr.getTrueExpression()),
        replaceExpr(expr.getFalseExpression()));
    result.setType(expr.getType());
    result.setSourcePosition(expr);
    replaceVisitedExpressionWith(result);
  }

  @Override
  public void visitShortTernaryExpression(ElvisOperatorExpression expr) {
    ElvisOperatorExpression result = new ElvisOperatorExpression(
        replaceExpr(expr.getTrueExpression()),
        replaceExpr(expr.getFalseExpression()));
    result.setType(expr.getType());
    result.setSourcePosition(expr);
    replaceVisitedExpressionWith(result);
  }

  @Override
  public void visitPostfixExpression(PostfixExpression expr) {
    expr.setExpression(replaceExpr(expr.getExpression()));
  }

  @Override
  public void visitPrefixExpression(PrefixExpression expr) {
    expr.setExpression(replaceExpr(expr.getExpression()));
  }

  @Override
  public void visitBooleanExpression(BooleanExpression expr) {
    BooleanExpression result = new BooleanExpression(
        replaceExpr(expr.getExpression()));
    result.setType(expr.getType());
    result.setSourcePosition(expr);
    replaceVisitedExpressionWith(result);
  }

  @Override
  public void visitNotExpression(NotExpression expr) {
    NotExpression result = new NotExpression(
        replaceExpr(expr.getExpression()));
    result.setType(expr.getType());
    result.setSourcePosition(expr);
    replaceVisitedExpressionWith(result);
  }

  @Override
  public void visitClosureExpression(ClosureExpression expr) {
    super.visitClosureExpression(expr);
    replaceVisitedExpressionWith(expr);
  }

  @Override
  public void visitTupleExpression(TupleExpression expr) {
    replaceAllExprs(expr.getExpressions());
  }

  @Override
  public void visitListExpression(ListExpression expr) {
    replaceAllExprs(expr.getExpressions());
  }

  @Override
  public void visitArrayExpression(ArrayExpression expr) {
    replaceAllExprs(expr.getExpressions());
    if (expr.getSizeExpression() != null) {
      replaceAllExprs(expr.getSizeExpression());
    }
  }

  @Override
  public void visitMapExpression(MapExpression expr) {
    replaceAllExprs(expr.getMapEntryExpressions());

  }

  @Override
  public void visitMapEntryExpression(MapEntryExpression expr) {
    MapEntryExpression result = new MapEntryExpression(
      replaceExpr(expr.getKeyExpression()),
      replaceExpr(expr.getValueExpression())
    );
    result.setType(expr.getType());
    result.setSourcePosition(expr);
    replaceVisitedExpressionWith(result);
  }

  @Override
  public void visitRangeExpression(RangeExpression expr) {
    RangeExpression result = new RangeExpression(
        replaceExpr(expr.getFrom()),
        replaceExpr(expr.getTo()),
        expr.isInclusive()
    );
    result.setType(expr.getType());
    result.setSourcePosition(expr);
    replaceVisitedExpressionWith(result);
  }

  @Override
  public void visitSpreadExpression(SpreadExpression expr) {
    SpreadExpression result = new SpreadExpression(
        replaceExpr(expr.getExpression())
    );
    result.setType(expr.getType());
    result.setSourcePosition(expr);
    replaceVisitedExpressionWith(result);
  }

  @Override
  public void visitSpreadMapExpression(SpreadMapExpression expr) {
    SpreadMapExpression result = new SpreadMapExpression(
        replaceExpr(expr.getExpression())
    );
    result.setType(expr.getType());
    result.setSourcePosition(expr);
    replaceVisitedExpressionWith(result);
  }

  @Override
  public void visitMethodPointerExpression(MethodPointerExpression expr) {
    MethodPointerExpression result = new MethodPointerExpression(
        replaceExpr(expr.getExpression()),
        expr.getMethodName()
    );
    result.setType(expr.getType());
    result.setSourcePosition(expr);
    replaceVisitedExpressionWith(result);
  }

  @Override
  public void visitUnaryMinusExpression(UnaryMinusExpression expr) {
    UnaryMinusExpression result = new UnaryMinusExpression(
        replaceExpr(expr.getExpression())
    );
    result.setType(expr.getType());
    result.setSourcePosition(expr);
    replaceVisitedExpressionWith(result);
  }

  @Override
  public void visitUnaryPlusExpression(UnaryPlusExpression expr) {
    UnaryPlusExpression result = new UnaryPlusExpression(
        replaceExpr(expr.getExpression())
    );
    result.setType(expr.getType());
    result.setSourcePosition(expr);
    replaceVisitedExpressionWith(result);
  }

  @Override
  public void visitBitwiseNegationExpression(BitwiseNegationExpression expr) {
    BitwiseNegationExpression result = new BitwiseNegationExpression(
        replaceExpr(expr.getExpression())
    );
    result.setType(expr.getType());
    result.setSourcePosition(expr);
    replaceVisitedExpressionWith(result);
  }

  @Override
  public void visitCastExpression(CastExpression expr) {
    CastExpression result = new CastExpression(
        expr.getType(),
        replaceExpr(expr.getExpression()),
        expr.isIgnoringAutoboxing()
    );
    result.setCoerce(expr.isCoerce());
    result.setType(expr.getType());
    result.setSourcePosition(expr);
    replaceVisitedExpressionWith(result);
  }

  @Override
  public void visitDeclarationExpression(DeclarationExpression expr) {
    visitBinaryExpression(expr);
    replaceVisitedExpressionWith(expr);
  }

  @Override
  public void visitPropertyExpression(PropertyExpression expr) {
    PropertyExpression result = new PropertyExpression(
        replaceExpr(expr.getObjectExpression()),
        replaceExpr(expr.getProperty()),
        expr.isSafe()
    );
    result.setSpreadSafe(expr.isSpreadSafe());
    result.setStatic(expr.isStatic());
    result.setImplicitThis(expr.isImplicitThis());
    result.setType(expr.getType());
    result.setSourcePosition(expr);
    replaceVisitedExpressionWith(result);
  }

  @Override
  public void visitAttributeExpression(AttributeExpression expr) {
    visitPropertyExpression(expr);
    replaceVisitedExpressionWith(expr);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void visitGStringExpression(GStringExpression expr) {
    replaceAllExprs(expr.getStrings());
    replaceAllExprs(expr.getValues());
  }

  @Override
  public void visitArgumentlistExpression(ArgumentListExpression expr) {
    visitTupleExpression(expr);
    replaceVisitedExpressionWith(expr);
  }

  @Override
  public void visitClosureListExpression(ClosureListExpression expr) {
    visitListExpression(expr);
    replaceVisitedExpressionWith(expr);
  }

  @Override
  public void visitAssertStatement(AssertStatement stat) {
    replaceExpr(stat.getBooleanExpression());
    replaceExpr(stat.getMessageExpression());
  }

  @Override
  public void visitExpressionStatement(ExpressionStatement stat) {
    replaceExpr(stat.getExpression());
  }

  @Override
  public void visitReturnStatement(ReturnStatement stat) {
    replaceExpr(stat.getExpression());
  }

  @Override
  public void visitThrowStatement(ThrowStatement stat) {
    replaceExpr(stat.getExpression());
  }

  @Override
  public void visitListOfExpressions(List exprs) {
    throw new UnsupportedOperationException("visitListOfExpressions");
  }

  // remaining methods are here to make sure we didn't forget anything

  @Override
  public void visitBreakStatement(BreakStatement stat) {}
  @Override
  public void visitContinueStatement(ContinueStatement stat) {}
  @Override
  public void visitConstantExpression(ConstantExpression expr) {}
  @Override
  public void visitClassExpression(ClassExpression expr) {}
  @Override
  public void visitVariableExpression(VariableExpression expr) {}
  @Override
  public void visitFieldExpression(FieldExpression expr) {}
  @Override
  public void visitBytecodeExpression(BytecodeExpression expr) {}
}
