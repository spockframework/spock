package org.spockframework.runtime;

import org.spockframework.runtime.model.ExpressionInfo;
import org.spockframework.util.ObjectUtil;

public class FailedEqualityComparisonRenderer implements ExpressionComparisonRenderer {
  @Override
  public String render(ExpressionInfo expr) {
    if (!(Boolean.FALSE.equals(expr.getValue()))) return null;
    if (!expr.isEqualityComparison()) return null;

    ExpressionInfo expr1 = expr.getChildren().get(0);
    ExpressionInfo expr2 = expr.getChildren().get(1);
    if (expr1.getEffectiveRenderedValue().equals(expr2.getEffectiveRenderedValue())) {
      addTypeHint(expr1);
      addTypeHint(expr2);
    }

    return "false";
  }

  private void addTypeHint(ExpressionInfo expr) {
    if (expr.getRenderedValue() == null) return;

    Class<?> exprType = ObjectUtil.voidAwareGetClass(expr.getValue());
    expr.setRenderedValue(expr.getRenderedValue() + " (" + exprType.getName() + ")");
  }
}
