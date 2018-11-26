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

import org.spockframework.runtime.model.ExpressionInfo;
import org.spockframework.util.*;

import java.util.*;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExpressionInfoValueRenderer {
  private static final List<ExpressionComparisonRenderer> RENDERERS = Arrays.asList(
    new FailedStringComparisonRenderer(),
    new FailedSetEqualityComparisonRenderer(),
    new FailedEqualityComparisonRenderer(),
    new FailedInstanceOfComparisonRenderer()
  );

  private static final StackTraceFilter genericStackTraceFilter = new StackTraceFilter(new IMethodNameMapper() {
    @Override
    public boolean isInitializerOrFixtureMethod(String className, String methodName) {
      return false;
    }

    @Override
    public String toFeatureName(String methodName) {
      return methodName;
    }
  });

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
  static String renderValue(ExpressionInfo expr) {
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

    if (value instanceof Throwable) {
      Throwable throwable = (Throwable) value;
      genericStackTraceFilter.filter(throwable);
      StringWriter stackTrace = new StringWriter();
      throwable.printStackTrace(new PrintWriter(stackTrace));
      return stackTrace.toString();
    }

    if (str == null || "".equals(str)) {
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

  private static String javaLangObjectToString(Object value) {
    String hash = Integer.toHexString(System.identityHashCode(value));
    Class<?> type = value.getClass();
    String typeName = type.getCanonicalName();
    return (typeName == null ? type.getName() : typeName) + "@" + hash;
  }

  private static String doRenderValue(ExpressionInfo expr) {
    for (ExpressionComparisonRenderer renderer : RENDERERS) {
      String result = renderer.render(expr);
      if (result != null) return result;
    }
    return renderToStringOrDump(expr);
  }

  private static String renderToStringOrDump(ExpressionInfo expr) {
    return RenderUtil.toStringOrDump(expr.getValue());
  }
}
