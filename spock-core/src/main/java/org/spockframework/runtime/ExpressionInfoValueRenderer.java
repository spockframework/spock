/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime;

import groovy.lang.GString;
import org.spockframework.runtime.condition.EditDistance;
import org.spockframework.runtime.condition.EditPathRenderer;
import org.spockframework.runtime.model.ExpressionInfo;
import org.spockframework.runtime.GroovyRuntimeUtil;
import org.spockframework.util.ObjectUtil;
import org.spockframework.util.Nullable;

public class ExpressionInfoValueRenderer {
  public static final long MAX_EDIT_DISTANCE_MEMORY = 1024*1024;
  private final ExpressionInfo expr;

  private ExpressionInfoValueRenderer(ExpressionInfo expr) {
    this.expr = expr;
  }

  public static void render(ExpressionInfo expr) {
    new ExpressionInfoValueRenderer(expr).render();
  }

  private void render() {
    for (ExpressionInfo expr : this.expr.inPostfixOrder(true)) {
      expr.setRenderedValue(renderValue(expr));
    }
  }

  /**
   * Returns a string representation of a value, or <tt>null</tt> if
   * the value should not be shown (because it does not add any valuable
   * information). Note that the method may also change rendered values
   * of child expression.
   *
   * @param expr the expression whose value is to be rendered
   * @return a string representation of the value
   */
  @Nullable
  private String renderValue(ExpressionInfo expr) {
    Object value = expr.getValue();

    if (value == null) return "null";
    if ("".equals(value)) return "\"\""; // value.equals() might throw exception, so we use "".equals() instead

    String str;

    try {
      str = doRenderValue(expr);
    } catch (Exception e) {
      return String.format("%s (renderer threw %s)",
          javaLangObjectToString(value), e.getClass().getSimpleName());
    }

    if (str == null || str.equals("")) {
      return javaLangObjectToString(value);
    }

    // only print enum values that add valuable information
    if (value instanceof Enum) {
      String text = expr.getText().trim();
      int index = text.lastIndexOf('.');
      String potentialEnumConstantNameInText = text.substring(index + 1);
      if (str.equals(potentialEnumConstantNameInText)) return null;
    }

    return str;
  }

  private String javaLangObjectToString(Object value) {
    String hash = Integer.toHexString(System.identityHashCode(value));
    return value.getClass().getName() + "@" + hash;
  }

  private String doRenderValue(ExpressionInfo expr) {
    String result = renderAsFailedStringComparison(expr);
    if (result != null) return result;
    
    result = renderAsFailedEqualityComparison(expr);
    if (result != null) return result;
    
    return GroovyRuntimeUtil.toString(expr.getValue());
  }
  
  private String renderAsFailedStringComparison(ExpressionInfo expr) {
    if (!(Boolean.FALSE.equals(expr.getValue()))) return null;
    if (!expr.isEqualityComparison(String.class, GString.class)) return null;

    // values can't be null here
    String str1 = expr.getChildren().get(0).getValue().toString();
    String str2 = expr.getChildren().get(1).getValue().toString();

    if (str1.length() * str2.length() > MAX_EDIT_DISTANCE_MEMORY) {
      return tryReduceStringSizes(str1, str2);
    } else {
      return createAndRenderEditDistance(str1, str2);
    }
  }

  private String tryReduceStringSizes(String str1, String str2) {
    int minLength = Math.min(str1.length(), str2.length());
    int commonStart = minLength;
    for (int i = 0; i < minLength; i++) {
      if (str1.charAt(i) != str2.charAt(i)) {
        commonStart = i-1;
        break;
      }
    }
    commonStart = Math.max(0, commonStart);
    int end1 = str1.length()-1;
    int end2 = str2.length()-1;
    while (end1 >= 0 && end2 >= 0 && str1.charAt(end1) == str2.charAt(end2)){
      end1--; end2--;
    }
    end1++;
    end2++;

    if ((end1-commonStart) * (end2-commonStart) > MAX_EDIT_DISTANCE_MEMORY) {
      return "false\nStrings too large to calculate edit distance.";
    } else {
      // Check if we can add some context
      if ((end1 - commonStart + 20) * (end2 - commonStart + 20) < MAX_EDIT_DISTANCE_MEMORY){
        commonStart = Math.max(0, commonStart - 10);
        end1 = Math.min(str1.length(), end1 + 10);
        end2 = Math.min(str2.length(), end2 + 10);
      }
      return createAndRenderEditDistance(str1.substring(commonStart, end1), str2.substring(commonStart, end2));
    }
  }

  private String createAndRenderEditDistance(String str1, String str2) {
    EditDistance dist = new EditDistance(str1, str2);
    return String.format("false\n%d difference%s (%d%% similarity)\n%s",
      dist.getDistance(), dist.getDistance() == 1 ? "" : "s", dist.getSimilarityInPercent(),
      new EditPathRenderer().render(str1, str2, dist.calculatePath()));
  }

  private String renderAsFailedEqualityComparison(ExpressionInfo expr) {
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
