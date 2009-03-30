/*
 * Copyright 2008 the original author or authors.
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

package org.spockframework.runtime;

import org.spockframework.runtime.model.ExpressionInfo;
import org.spockframework.runtime.model.TextPosition;

import java.util.*;

/**
 * Creates a string representation of an assertion and its recorded values.
 *
 * @author Peter Niederwieser
 */
public class ExpressionInfoRenderer {
  private final ExpressionInfo expr;

  private final List<StringBuilder> lines = new ArrayList<StringBuilder>();

  // startColumns.get(i) is the first non-empty column of lines.get(i)
  private final List<Integer> startColumns = new ArrayList<Integer>();

  private ExpressionInfoRenderer(ExpressionInfo expr) {
    TextPosition start = expr.getRegion().getStart();
    if (start.getLine() != 1 || start.getColumn() != 1)
      throw new IllegalArgumentException("can only print expressions starting at 1,1");
    if (expr.getRegion().getEnd().getLine() != 1)
      throw new IllegalArgumentException("can only print expressions ending on line 1");
    this.expr = expr;
  }

  public static String render(ExpressionInfo expr) {
    return new ExpressionInfoRenderer(expr).render();
  }

  private String render() {
    renderText();
    renderValues();
    return linesToString();
  }

  private void renderText() {
    lines.add(new StringBuilder(expr.getText()));
    startColumns.add(0);
    lines.add(new StringBuilder()); // empty line
    startColumns.add(0);
  }

  private void renderValues() {
    Comparator<ExpressionInfo> comparator = new Comparator<ExpressionInfo>() {
      public int compare(ExpressionInfo expr1, ExpressionInfo expr2) {
        return expr2.getAnchor().getColumn() - expr1.getAnchor().getColumn();
      }
    };

    for (ExpressionInfo expr : this.expr.inCustomOrder(true, comparator))
      renderValue(expr);
  }

  private void renderValue(ExpressionInfo expr) {
    String str = valueToString(expr);
    if (str == null) return;

    int startColumn = expr.getAnchor().getColumn();
    if (startColumn < 1) return; // node with invalid source position

    String[] strs = str.split("\n");
    int endColumn = strs.length == 1 ?
        expr.getAnchor().getColumn() + str.length() : // exclusive
        Integer.MAX_VALUE; // multi-line strings are always placed on new lines

    for (int i = 1; i < lines.size(); i++)
      if (endColumn < startColumns.get(i)) {
        placeString(lines.get(i), str, startColumn);
        startColumns.set(i, startColumn);
        return;
      } else {
        placeString(lines.get(i), "|", startColumn);
        if (i > 1) // make sure that no values are ever placed on empty line
          startColumns.set(i, startColumn + 1); // + 1: no whitespace required between end of value and "|"
      }

    // value could not be placed on existing lines, so place it on new line(s)
    for (String s : strs) {
      StringBuilder newLine = new StringBuilder();
      lines.add(newLine);
      placeString(newLine, s, startColumn);
      startColumns.add(startColumn);
    }
  }

  private String linesToString() {
    StringBuilder firstLine = lines.get(0);
    for (int i = 1; i < lines.size(); i++)
      firstLine.append('\n').append(lines.get(i).toString());
    return firstLine.toString();
  }

  private static void placeString(StringBuilder line, String str, int column) {
    while (line.length() < column)
      line.append(' ');
    line.replace(column - 1, column - 1 + str.length(), str);
  }

  /**
   * Returns a string representation of a value, or <tt>null</tt> if
   * the value should not be rendered (because it does not add any valuable
   * information).
   *
   * @param expr the expression whose value is to be rendered
   * @return a string representation of the value
   */
  private static String valueToString(ExpressionInfo expr) {
    Object value = expr.getValue();

    if (value == null) return "null";

    String str;
    String errorMsg = "";
    try {
      str = value.getClass().isArray() ? arrayToString(value) : value.toString();
    } catch (Exception e) {
      str = null;
      errorMsg = " (toString() threw " + e.getClass().getName() + ")";
    }

    // if the value has no string representation, produce something like Object.toString()
    if (str == null || str.equals("")) {
      String hash = Integer.toHexString(System.identityHashCode(value));
      return value.getClass().getName() + "@" + hash + errorMsg;
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

  private static String arrayToString(Object array) {
    Class<?> clazz = array.getClass();

    if (clazz == byte[].class) return Arrays.toString((byte[]) array);
    if (clazz == short[].class) return Arrays.toString((short[]) array);
    if (clazz == int[].class) return Arrays.toString((int[]) array);
    if (clazz == long[].class) return Arrays.toString((long[]) array);
    if (clazz == char[].class) return Arrays.toString((char[]) array);
    if (clazz == float[].class) return Arrays.toString((float[]) array);
    if (clazz == double[].class) return Arrays.toString((double[]) array);
    if (clazz == boolean[].class) return Arrays.toString((boolean[]) array);
    return Arrays.deepToString((Object[]) array);
  }
}
