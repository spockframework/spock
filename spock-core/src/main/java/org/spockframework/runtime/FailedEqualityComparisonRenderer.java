package org.spockframework.runtime;

import org.spockframework.runtime.model.ExpressionInfo;
import org.spockframework.util.ObjectUtil;

import java.util.Objects;

import static org.spockframework.runtime.ExpressionInfoValueRenderer.renderValue;

public class FailedEqualityComparisonRenderer implements ExpressionComparisonRenderer {
  @Override
  public String render(ExpressionInfo expr) {
    if (!(Boolean.FALSE.equals(expr.getValue()))) return null;
    if (!expr.isEqualityComparison()) return null;

    ExpressionInfo expr1 = expr.getChildren().get(0);
    ExpressionInfo expr2 = expr.getChildren().get(1);
    if (expr1.getRenderedValue() == null) renderIrrelevant(expr1);
    if (expr2.getRenderedValue() == null) renderIrrelevant(expr2);
    if (Objects.equals(expr1.getRenderedValue(), expr2.getRenderedValue())) {
      addTypeHints(expr1, expr2);
      expr1.setRelevant(true);
      expr2.setRelevant(true);
    }

    return "false";
  }

  private void addTypeHints(ExpressionInfo expr1, ExpressionInfo expr2) {
    String expr1RenderedValue = expr1.getRenderedValue();
    if (expr1RenderedValue == null) return;
    String expr1TypeHint = getTypeHint(expr1);
    String expr2TypeHint = getTypeHint(expr2);
    if (expr1TypeHint.equals(expr2TypeHint)) {
      expr1TypeHint += "@" + Integer.toHexString(System.identityHashCode(expr1.getValue()));
      expr2TypeHint += "@" + Integer.toHexString(System.identityHashCode(expr2.getValue()));
    }
    expr1.setRenderedValue(expr1RenderedValue + " (" + expr1TypeHint + ")");
    expr2.setRenderedValue(expr2.getRenderedValue() + " (" + expr2TypeHint + ")");
  }

  private String getTypeHint(ExpressionInfo expr) {
    Class<?> exprType = ObjectUtil.voidAwareGetClass(expr.getValue());
    String typeName = exprType.getCanonicalName();
    return typeName == null ? exprType.getName() : typeName;
  }

  private void renderIrrelevant(ExpressionInfo irrelevantExpr) {
    for (ExpressionInfo expr : irrelevantExpr.inPostfixOrder(false)) {
      if (!expr.isRelevant() && (expr.getRenderedValue() == null)) {
        expr.setRenderedValue(renderValue(expr));
      }
    }
  }
}
