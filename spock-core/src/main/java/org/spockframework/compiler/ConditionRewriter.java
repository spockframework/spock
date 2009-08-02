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

import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.syntax.Types;
import org.spockframework.runtime.SpockRuntime;
import org.spockframework.runtime.ValueRecorder;
import org.spockframework.util.*;

import java.util.*;

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
public class ConditionRewriter extends AbstractExpressionConverter<Expression> {
  private final IRewriteResourceProvider resourceProvider;

  private int recordCount = 0;
  private boolean doNotRecordNextConstant = false;

  private ConditionRewriter(IRewriteResourceProvider resourceProvider) {
    this.resourceProvider = resourceProvider;
  }

  public static Statement rewriteExplicitCondition(AssertStatement stat, IRewriteResourceProvider resourceProvider) {
    if (AstUtil.hasAssertionMessage(stat))
      return rewriteExplicitConditionWithMessage(stat, resourceProvider);

    return new ConditionRewriter(resourceProvider).rewrite(stat, stat.getBooleanExpression(), true);
  }

  private static Statement rewriteExplicitConditionWithMessage(AssertStatement stat,
      IRewriteResourceProvider resourceProvider) {
    Expression condition = stat.getBooleanExpression();
    Expression message = stat.getMessageExpression();

    Statement result =
        new ExpressionStatement(
            new MethodCallExpression(
                new ClassExpression(resourceProvider.getAstNodeCache().SpockRuntime),
                SpockRuntime.VERIFY_CONDITION_WITH_MESSAGE,
                new ArgumentListExpression(
                    Arrays.asList(
                        message,
                        condition,
                        new ConstantExpression(resourceProvider.getSourceText(condition)),
                        new ConstantExpression(condition.getLineNumber()),
                        new ConstantExpression(condition.getColumnNumber())))));

    result.setSourcePosition(stat);
    return result;
  }

  public static Statement rewriteImplicitCondition(ExpressionStatement stat,
      IRewriteResourceProvider resourceProvider) {
    return new ConditionRewriter(resourceProvider).rewrite(stat, stat.getExpression(), false);
  }

  public void visitMethodCallExpression(MethodCallExpression expr) {
    MethodCallExpression conversion =
        new MethodCallExpression(
            expr.isImplicitThis() ?
                expr.getObjectExpression() :
                convert(expr.getObjectExpression()),
            convert(expr.getMethod()),
            convert(expr.getArguments()));

    conversion.setSafe(expr.isSafe());
    conversion.setSpreadSafe(expr.isSpreadSafe());
    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  // only used for statically imported methods called by their simple name
  public void visitStaticMethodCallExpression(StaticMethodCallExpression expr) {
    StaticMethodCallExpression conversion =
        new StaticMethodCallExpression(
            expr.getOwnerType(),
            recordNa(expr.getMethod()),
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
    result = recordNa(conversion);
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
    unsupported(); // unused AST node
  }

  public void visitMethodPointerExpression(MethodPointerExpression expr) {
    MethodPointerExpression conversion =
        new MethodPointerExpression(
            convert(expr.getExpression()),
            convert(expr.getMethodName()));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  public void visitVariableExpression(VariableExpression expr) {
    if (expr instanceof OldValueExpression) {
      Expression originalExpr = ((OldValueExpression)expr).getOrginalExpression();
      originalExpr.visit(this); // just to count up recordCount and produce the correct number of N/A values at runtime
      doNotRecordNextConstant = true; // we know Predef.old() has been rewritten to include a dummy as second argument
      result = expr;
      return;
    }

    result = record(expr);
  }

  public void visitDeclarationExpression(DeclarationExpression expr) {
    unsupported(); // not allowed to occur in conditions
  }

  public void visitRegexExpression(RegexExpression expr) {
    unsupported(); // unused AST node
  }

  public void visitBinaryExpression(BinaryExpression expr) {
    BinaryExpression conversion =
        new BinaryExpression(
            Types.ofType(expr.getOperation().getType(), Types.ASSIGNMENT_OPERATOR) ?
                // prevent lvalue from getting turned into record(lvalue), which can no longer be assigned to
                convertAndRecordNa(expr.getLeftExpression()) :
                convert(expr.getLeftExpression()),
            expr.getOperation(),
            convert(expr.getRightExpression()));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  public void visitConstantExpression(ConstantExpression expr) {
    if (doNotRecordNextConstant) {
      doNotRecordNextConstant = false;
      result = expr;
      return;
    }

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
    // NOTE: remove guessing (text == null) once http://jira.codehaus.org/browse/GROOVY-3552 is fixed
    recordCount += text == null ? 1 : Util.countOccurrences(text, '.') + 1;
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
    unsupported(); // cannot occur in assertion
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
            (List)convertAll(expr.getMapEntryExpressions()));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  public void visitMapEntryExpression(MapEntryExpression expr) {
    MapEntryExpression conversion =
        new MapEntryExpression(
            convert(expr.getKeyExpression()),
            convert(expr.getValueExpression()));

    conversion.setSourcePosition(expr);
    result = recordNa(conversion);
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
            convertAll(expr.getSizeExpression()));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  public void visitSpreadExpression(SpreadExpression expr) {
    SpreadExpression conversion =
        new SpreadExpression(
            convert(expr.getExpression()));

    conversion.setSourcePosition(expr);
    result = recordNa(conversion);
  }

  public void visitSpreadMapExpression(SpreadMapExpression expr) {
    // to not record the underlying MapExpression twice, we do nothing here
    // see http://jira.codehaus.org/browse/GROOVY-3421
    result = recordNa(expr);
  }

  public void visitTernaryExpression(TernaryExpression expr) {
    TernaryExpression conversion =
        new TernaryExpression(
            convertCompatibly(expr.getBooleanExpression()),
            convert(expr.getTrueExpression()),
            convert(expr.getFalseExpression()));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  public void visitShortTernaryExpression(ElvisOperatorExpression expr) {
    ElvisOperatorExpression conversion =
        new ElvisOperatorExpression(
            convert(expr.getTrueExpression()),
            convert(expr.getFalseExpression()));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  public void visitPrefixExpression(PrefixExpression expr) {
    PrefixExpression conversion =
        new PrefixExpression(
            expr.getOperation(),
            convertAndRecordNa(expr.getExpression()));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  public void visitPostfixExpression(PostfixExpression expr) {
    PostfixExpression conversion =
        new PostfixExpression(
            convertAndRecordNa(expr.getExpression()),
            expr.getOperation());

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  public void visitBooleanExpression(BooleanExpression expr) {
    BooleanExpression conversion =
        new BooleanExpression(
            convert(expr.getExpression())
        );

    conversion.setSourcePosition(expr);
    result = recordNa(conversion);
  }

  public void visitClosureExpression(ClosureExpression expr) {
    result = record(expr);
  }

  // only called for LHS of multi-assignment
  @SuppressWarnings("unchecked")
  public void visitTupleExpression(TupleExpression expr) {
    TupleExpression conversion =
        new TupleExpression(
            // prevent lvalue from getting turned into record(lvalue),
            // which can no longer be assigned to
            convertAllAndRecordNa(expr.getExpressions()));

    conversion.setSourcePosition(expr);
    result = record(conversion);
  }

  private Expression record(Expression expr) {
    return new MethodCallExpression(
        new VariableExpression("__valueRecorder42"),
        ValueRecorder.RECORD,
        new ArgumentListExpression(new ConstantExpression(recordCount++), expr));
  }

  private <T> T recordNa(T expr) {
    recordCount++;
    return expr;
  }

  private Expression convertAndRecordNa(Expression expr) {
    return unrecord(convert(expr));
  }

  private List<Expression> convertAllAndRecordNa(List<Expression> expressions) {
    List<Expression> conversions = new ArrayList<Expression>(expressions.size());
    for (Expression expr : expressions) conversions.add(convertAndRecordNa(expr));
    return conversions;
  }

  @SuppressWarnings("unchecked")
  private <T extends Expression> T convertCompatibly(T expr) {
    Expression conversion = convert(expr);
    Assert.that(expr.getClass().isInstance(conversion));
    return (T)conversion;
  }

  // unrecord(record(expr)) == expr
  // does not change recordCount, resulting in one N/A value at runtime
  private static Expression unrecord(Expression expr) {
    if (!(expr instanceof MethodCallExpression)) return expr;
    MethodCallExpression methodExpr = (MethodCallExpression)expr;
    Expression targetExpr = methodExpr.getObjectExpression();
    if (!(targetExpr instanceof VariableExpression)) return expr;
    VariableExpression var = (VariableExpression)targetExpr;
    if (!var.getName().equals("__valueRecorder42")) return expr;
    if(!methodExpr.getMethodAsString().equals(ValueRecorder.RECORD)) return expr;
    return ((ArgumentListExpression)methodExpr.getArguments()).getExpression(1);
  }

  private Statement rewrite(Statement stat, Expression expr, boolean explicitCondition) {
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
                        convertCondition(expr, explicitCondition),
                        new ConstantExpression(resourceProvider.getSourceText(expr)),
                        new ConstantExpression(expr.getLineNumber()),
                        new ConstantExpression(expr.getColumnNumber())))));

    result.setSourcePosition(stat);
    return result;
  }

  private Expression convertCondition(Expression expr, boolean explicitCondition) {
    if (explicitCondition || !AstUtil.isMethodInvocation(expr))
      return convert(expr);

    if (expr instanceof MethodCallExpression) {
      MethodCallExpression call = (MethodCallExpression)expr;
      if (call.isSpreadSafe()) return convert(call);
      return convertConditionNullAware(call);
    }

    return convertConditionNullAware((StaticMethodCallExpression)expr);
  }

  // foo.bar(arg1, arg2) -> SpockRuntime.nullAwareInvokeMethod(foo, bar, arg1, arg2)
  // (plus the usual condition rewriting, i.e. insertion of ValueRecorder.record(...))
  private Expression convertConditionNullAware(MethodCallExpression expr) {
    List<Expression> args = new ArrayList<Expression>();

    Expression result = new MethodCallExpression(
        new ClassExpression(resourceProvider.getAstNodeCache().SpockRuntime),
        new ConstantExpression(
            expr.isSafe() ? SpockRuntime.NULL_AWARE_INVOKE_METHOD_SAFE : SpockRuntime.NULL_AWARE_INVOKE_METHOD),
        new ArgumentListExpression(args));

    args.add(expr.isImplicitThis() ? expr.getObjectExpression() : convert(expr.getObjectExpression()));
    args.add(convert(expr.getMethod()));
    args.add(new ArrayExpression(ClassHelper.OBJECT_TYPE, recordNa(convertAll(AstUtil.getArguments(expr)))));

    result.setSourcePosition(expr);
    return record(result);
  }

  private Expression convertConditionNullAware(StaticMethodCallExpression expr) {
    List<Expression> args = new ArrayList<Expression>();

    Expression result = new MethodCallExpression(
        new ClassExpression(resourceProvider.getAstNodeCache().SpockRuntime),
        new ConstantExpression(SpockRuntime.NULL_AWARE_INVOKE_METHOD),
        new ArgumentListExpression(args));

    args.add(new ClassExpression(expr.getOwnerType()));
    args.add(recordNa(new ConstantExpression(expr.getMethod())));
    args.add(new ArrayExpression(ClassHelper.OBJECT_TYPE, recordNa(convertAll(AstUtil.getArguments(expr)))));

    result.setSourcePosition(expr);
    return record(result);
  }
}
