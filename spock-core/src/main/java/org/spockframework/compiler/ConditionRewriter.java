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

import java.util.Arrays;

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.syntax.Types;

import org.spockframework.runtime.SpockRuntime;
import org.spockframework.runtime.ValueRecorder;
import org.spockframework.util.AbstractExpressionConverter;
import org.spockframework.util.Util;

// IDEA: refactor to allow more succinct expression of the pattern: construct conversion,
// set source position, return_(record())

// NOTE: currently some conversions reference old expression objects rather than copying them;
// this can potentially lead to aliasing problems (e.g. for Condition.originalExpression)
// background: it was found that transformExpression() cannot be used to copy all expressions
// (some implementations just return original expression); as a consequence, the design goal
// of copying everything was dropped for the time being

// IDEA: record type literals as class objects (since we also record other literals)
// but since a type literal cannot be evaluated, we would have to record class object
// before/after evaluating next/previous expression
/**
 * Rewrites explicit ("assert x > 3") and implicit ("x > 3") condition
 * statements. Replacing the original statement with the rewritten one is up
 * to clients.
 *
 * @author Peter Niederwieser
 */
// TODO: can't we reuse existing expression instances and just mutate them?!
public class ConditionRewriter extends AbstractExpressionConverter<Expression> {
  private final IRewriteResourceProvider resourceProvider;
  private final GroovyCodeVisitor closureRewriter;
  private int recordCount = 0;

  private ConditionRewriter(IRewriteResourceProvider resourceProvider, GroovyCodeVisitor closureRewriter) {
    this.resourceProvider = resourceProvider;
    this.closureRewriter = closureRewriter;
  }

  // TODO: make use of assertion message (if available)
  public static Statement rewriteExplicitCondition(AssertStatement stat,
      IRewriteResourceProvider resourceProvider, GroovyCodeVisitor closureRewriter) {
    return new ConditionRewriter(resourceProvider, closureRewriter).rewrite(stat, stat.getBooleanExpression());
  }

  public static Statement rewriteImplicitCondition(ExpressionStatement stat,
      IRewriteResourceProvider resourceProvider) {
    return new ConditionRewriter(resourceProvider, null).rewrite(stat, stat.getExpression());
  }

  public void visitMethodCallExpression(MethodCallExpression expr) {
    MethodCallExpression conversion =
        new MethodCallExpression(
            expr.isImplicitThis() ?
                expr.getObjectExpression() :
                convert(expr.getObjectExpression()),
            expr.getMethod(), // TODO: convert?
            convert(expr.getArguments()));

    conversion.setSafe(expr.isSafe());
    conversion.setSpreadSafe(expr.isSpreadSafe());
    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  // only used for static method calls with implicit target
  public void visitStaticMethodCallExpression(StaticMethodCallExpression expr) {
    StaticMethodCallExpression conversion =
        new StaticMethodCallExpression(
            expr.getOwnerType(),
            expr.getMethod(),
            convert(expr.getArguments()));

    conversion.setSourcePosition(expr);
    conversion.setMetaMethod(expr.getMetaMethod());
    result = record(conversion);
  }

  public void visitBytecodeExpression(BytecodeExpression expr) {
    unsupported();
  }

  @SuppressWarnings("unchecked")
  public void visitArgumentlistExpression(ArgumentListExpression expr) {
    ArgumentListExpression conversion =
        new ArgumentListExpression(
            convertAll(expr.getExpressions()));

    conversion.setSourcePosition(expr);
    result = conversion;
  }

  public void visitPropertyExpression(PropertyExpression expr) {
    PropertyExpression conversion =
        new PropertyExpression(
            expr.isImplicitThis() ?
                expr.getObjectExpression() :
                convert(expr.getObjectExpression()),
            expr.getProperty(),
            expr.isSafe());

    conversion.setSourcePosition(expr);
    conversion.setSpreadSafe(expr.isSpreadSafe());
    conversion.setStatic(expr.isStatic());
    conversion.setImplicitThis(expr.isImplicitThis());
    result = record(conversion);
  }

  public void visitAttributeExpression(AttributeExpression expr) {
    AttributeExpression conversion =
        new AttributeExpression(
            expr.isImplicitThis() ?
                expr.getObjectExpression() :
                convert(expr.getObjectExpression()),
            expr.getProperty(),
            expr.isSafe());

    conversion.setSourcePosition(expr);
    conversion.setSpreadSafe(expr.isSpreadSafe());
    conversion.setStatic(expr.isStatic());
    conversion.setImplicitThis(expr.isImplicitThis());
    result = record(conversion);
  }

  public void visitFieldExpression(FieldExpression expr) {
    result = record(expr);
  }

  public void visitMethodPointerExpression(MethodPointerExpression expr) {
    MethodPointerExpression conversion =
        new MethodPointerExpression(
            convert(expr.getExpression()),
            expr.getMethodName()); // TODO: convert?

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  public void visitVariableExpression(VariableExpression expr) {
    result = record(expr);
  }

  public void visitDeclarationExpression(DeclarationExpression expr) {
    unsupported(); // not allowed to occur in conditions
  }

  public void visitRegexExpression(RegexExpression expr) {
    result = expr;
  }

  public void visitBinaryExpression(BinaryExpression expr) {
    BinaryExpression conversion =
        new BinaryExpression(
            // only convert LHS if not an assignment expr
            Types.ofType(expr.getOperation().getType(), Types.ASSIGNMENT_OPERATOR) ?
                expr.getLeftExpression() :
                convert(expr.getLeftExpression()),
            expr.getOperation(),
            convert(expr.getRightExpression()));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  public void visitConstantExpression(ConstantExpression expr) {
    result = record(expr);
  }

  public void visitClassExpression(ClassExpression expr) {
    result = expr;
    // ensure this isn't the class part of a class member access where the member has been
    // imported statically and the class is therefore not present in the
    // code; instead of looking through imports, we try to infer this from
    // source position information
    if (!AstUtil.hasPlausibleSourcePosition(expr)) return;
    // because runtime condition parsing only proceeds up to compiler phase
    // CONVERSION, this ClassExpression will be seen as a VariableExpression
    // (e.g. "Type"), or a VariableExpression nested within one or more PropertyExpressions
    // (e.g. "org.Type", "Type.class", "org.Type.class");
    // therefore we have to provide one N/A value for every part of the class name
    String text = resourceProvider.getSourceText(expr);
    recordCount += Util.countOccurrences(text, '.') + 1;
  }

  public void visitUnaryMinusExpression(UnaryMinusExpression expr) {
    UnaryMinusExpression conversion =
        new UnaryMinusExpression(
            convert(expr.getExpression()));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  public void visitUnaryPlusExpression(UnaryPlusExpression expr) {
    UnaryPlusExpression conversion =
        new UnaryPlusExpression(
            convert(expr.getExpression()));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  public void visitBitwiseNegationExpression(BitwiseNegationExpression expr) {
    BitwiseNegationExpression conversion =
        new BitwiseNegationExpression(
            convert(expr.getExpression()));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  public void visitCastExpression(CastExpression expr) {
    CastExpression conversion =
        new CastExpression(
            expr.getType(),
            convert(expr.getExpression()),
            expr.isIgnoringAutoboxing());

    conversion.setSourcePosition(expr);
    conversion.setCoerce(expr.isCoerce());
    result = record(conversion);
  }

  public void visitClosureListExpression(ClosureListExpression expr) {
    result = expr;
  }

  public void visitNotExpression(NotExpression expr) {
    NotExpression conversion =
        new NotExpression(
            convert(expr.getExpression()));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  @SuppressWarnings("unchecked")
  public void visitListExpression(ListExpression expr) {
    ListExpression conversion =
        new ListExpression(
            convertAll(expr.getExpressions()));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  public void visitRangeExpression(RangeExpression expr) {
    RangeExpression conversion =
        new RangeExpression(
            convert(expr.getFrom()),
            convert(expr.getTo()),
            expr.isInclusive());

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  @SuppressWarnings("unchecked")
  public void visitMapExpression(MapExpression expr) {
    MapExpression conversion =
        new MapExpression(
            convertAll(expr.getMapEntryExpressions()));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  public void visitMapEntryExpression(MapEntryExpression expr) {
    MapEntryExpression conversion =
        new MapEntryExpression(
            convert(expr.getKeyExpression()),
            convert(expr.getValueExpression()));

    conversion.setSourcePosition(expr);
    result = conversion;
  }

  public void visitConstructorCallExpression(ConstructorCallExpression expr) {
    ConstructorCallExpression conversion =
        new ConstructorCallExpression(
            expr.getType(),
            convert(expr.getArguments()));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  @SuppressWarnings("unchecked")
  public void visitGStringExpression(GStringExpression expr) {
    GStringExpression conversion =
        new GStringExpression(
            expr.getText(),
            expr.getStrings(),
            convertAll(expr.getValues()));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  @SuppressWarnings("unchecked")
  public void visitArrayExpression(ArrayExpression expr) {
    ArrayExpression conversion =
        new ArrayExpression(
            expr.getElementType(),
            convertAll(expr.getExpressions()),
            expr.getSizeExpression()); // TODO: rewrite?

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  public void visitSpreadExpression(SpreadExpression expr) {
    SpreadExpression conversion =
        new SpreadExpression(
            convert(expr.getExpression()));

    conversion.setSourcePosition(expr);
    result = conversion;
  }

  public void visitSpreadMapExpression(SpreadMapExpression expr) {
    SpreadExpression conversion =
        new SpreadExpression(
            convert(expr.getExpression()));

    conversion.setSourcePosition(expr);
    result = conversion;
  }

  public void visitTernaryExpression(TernaryExpression expr) {
    TernaryExpression conversion =
        new TernaryExpression(
            new BooleanExpression(
                convert(expr.getBooleanExpression().getExpression())),
            convert(expr.getTrueExpression()),
            convert(expr.getFalseExpression()));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  public void visitShortTernaryExpression(ElvisOperatorExpression expr) {
    ElvisOperatorExpression conversion =
        new ElvisOperatorExpression(
            convert(expr.getBooleanExpression().getExpression()),
            convert(expr.getFalseExpression()));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  public void visitPrefixExpression(PrefixExpression expr) {
    PrefixExpression conversion =
        new PrefixExpression(
            expr.getOperation(),
            unrecord(convert(expr.getExpression())));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  public void visitPostfixExpression(PostfixExpression expr) {
    PostfixExpression conversion =
        new PostfixExpression(
            unrecord(convert(expr.getExpression())),
            expr.getOperation());

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  public void visitBooleanExpression(BooleanExpression expr) {
    // implicit conversion so don't record
    result = convert(expr.getExpression());
  }

  public void visitClosureExpression(ClosureExpression expr) {
    if (closureRewriter != null && expr.getCode() != null)
      expr.getCode().visit(closureRewriter);
    result = record(expr);
  }

  @SuppressWarnings("unchecked")
  public void visitTupleExpression(TupleExpression expr) {
    TupleExpression conversion =
        new TupleExpression(
            convertAll(expr.getExpressions()));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  private Expression record(Expression expr) {
    return new MethodCallExpression(
        new VariableExpression("__valueRecorder42"),
        ValueRecorder.RECORD,
        new ArgumentListExpression(new ConstantExpression(recordCount++), expr));
  }

  // does not change recordCount
  private Expression unrecord(Expression expr) {
    if (!(expr instanceof MethodCallExpression)) return expr;
    MethodCallExpression methodExpr = (MethodCallExpression)expr;
    Expression targetExpr = methodExpr.getObjectExpression();
    if (!(targetExpr instanceof VariableExpression)) return expr;
    VariableExpression var = (VariableExpression)targetExpr;
    if (!var.getName().equals("__valueRecorder42")) return expr;
    if(!methodExpr.getMethodAsString().equals(ValueRecorder.RECORD)) return expr;
    return ((ArgumentListExpression)methodExpr.getArguments()).getExpression(1);
  }

  private Statement rewrite(Statement stat, Expression expr) {
    Statement result =
        new ExpressionStatement(
            new MethodCallExpression(
                new ClassExpression(resourceProvider.getAstNodeCache().SpockRuntime),
                SpockRuntime.VERIFY_CONDITION,
                new ArgumentListExpression(
                    Arrays.asList(
                        new MethodCallExpression(
                            new VariableExpression("__valueRecorder42"),
                            ValueRecorder.RESET,
                            ArgumentListExpression.EMPTY_ARGUMENTS),
                        convert(expr),
                        new ConstantExpression(resourceProvider.getSourceText(expr)),
                        new ConstantExpression(expr.getLineNumber()),
                        new ConstantExpression(expr.getColumnNumber()))          )
        )
      );

    result.setSourcePosition(stat);
    return result;
  }
}
