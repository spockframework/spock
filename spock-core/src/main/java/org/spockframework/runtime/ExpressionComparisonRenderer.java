package org.spockframework.runtime;

import org.spockframework.runtime.model.ExpressionInfo;
import org.spockframework.util.Nullable;

public interface ExpressionComparisonRenderer {
  /**
   * Renders a comparison expression.
   *
   * @param expr the expression to be rendered
   * @return the rendering or {@code null} if it can't be rendered by this renderer
   */
  @Nullable
  String render(ExpressionInfo expr);
}
