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
      addTypeHint(expr1);
      expr1.setRelevant(true);
      addTypeHint(expr2);
      expr2.setRelevant(true);
    }

    if (Objects.equals(expr1.getRenderedValue(), expr2.getRenderedValue())) {
      addIdentityHashCode(expr1);
      addIdentityHashCode(expr2);
    }

    return "false";
  }

  private void addTypeHint(ExpressionInfo expr) {
    if (expr.getRenderedValue() == null) return;

    Class<?> exprType = ObjectUtil.voidAwareGetClass(expr.getValue());
    expr.setRenderedValue(expr.getRenderedValue() + " (" + exprType.getName() + ")");
  }

  private void renderIrrelevant(ExpressionInfo irrelevantExpr) {
    for (ExpressionInfo expr : irrelevantExpr.inPostfixOrder(false)) {
      if (!expr.isRelevant() && (expr.getRenderedValue() == null)) {
        expr.setRenderedValue(renderValue(expr));
      }
    }
  }

  private void addIdentityHashCode(ExpressionInfo expr) {
    if (expr.getRenderedValue() == null) return;

    expr.setRenderedValue(String.format("%s @%08x", expr.getRenderedValue(), System.identityHashCode(expr.getValue())));
  }
}
