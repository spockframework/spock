package org.spockframework.runtime;

import org.spockframework.runtime.model.ExpressionInfo;
import org.spockframework.util.ObjectUtil;

public class FailedInstanceOfComparisonRenderer implements ExpressionComparisonRenderer {
  @Override
  public String render(ExpressionInfo expr) {
    if (!(Boolean.FALSE.equals(expr.getValue()))) return null;
    if (!expr.isInstanceOfComparison()) return null;

    addTypeHint(expr.getChildren().get(0));
    return "false";
  }

  private void addTypeHint(ExpressionInfo expr) {
    if (expr.getRenderedValue() == null) return;

    Class<?> exprType = ObjectUtil.voidAwareGetClass(expr.getValue());
    expr.setRenderedValue(expr.getRenderedValue() + " (" + exprType.getName() + ")");
  }
}
