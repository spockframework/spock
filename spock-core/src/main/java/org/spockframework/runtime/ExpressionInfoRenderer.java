/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime;

import org.spockframework.runtime.model.*;

import java.util.*;
import java.util.regex.Pattern;

import static java.util.Comparator.comparingInt;

/**
 * Creates a string representation of an assertion and its recorded values.
 *
 * @author Peter Niederwieser
 */
public class ExpressionInfoRenderer {
  private static final Pattern NEWLINE_PATTERN = Pattern.compile("\r\n|\r|\n");
  private final ExpressionInfo expr;

  private final List<StringBuilder> lines = new ArrayList<>();

  // startColumns.get(i) is the first non-empty column of lines.get(i)
  private final List<Integer> startColumns = new ArrayList<>();

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
    placeText();
    placeValues();
    return linesToString();
  }

  private void placeText() {
    lines.add(new StringBuilder(expr.getText()));
    startColumns.add(0);
  }

  private void placeValues() {
    Comparator<ExpressionInfo> comparator =
      comparingInt((ExpressionInfo exprInfo) -> exprInfo.getAnchor().getColumn()).reversed();

    for (ExpressionInfo exprInfo : this.expr.inCustomOrder(true, comparator))
      placeValue(exprInfo);
  }

  private void placeValue(ExpressionInfo expr) {
    String str = expr.getRenderedValue();
    if (str == null) return;

    int startColumn = expr.getAnchor().getColumn();
    if (startColumn < 1) return; // node with invalid source position

    String[] strs = NEWLINE_PATTERN.split(str);
    int endColumn = strs.length == 1 ?
        expr.getAnchor().getColumn() + str.length() : // exclusive
        Integer.MAX_VALUE; // multi-line strings are always placed on new lines

    if (lines.size() == 1) { // we have never placed a value yet
      // let's add an empty line between text and values
      lines.add(new StringBuilder());
      startColumns.add(0);
    }

    for (int i = 1; i < lines.size(); i++)
      if (endColumn < startColumns.get(i)) {
        placeString(lines.get(i), str, startColumn);
        startColumns.set(i, startColumn);
        return;
      } else {
        placeString(lines.get(i), "|", startColumn);
        if (i > 1) // i == 1 is the empty line between text and values, which should stay empty
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
    StringBuilder result = new StringBuilder();
    for (StringBuilder line : lines)
      result.append(line).append('\n');
    return result.toString();
  }

  private static void placeString(StringBuilder line, String str, int column) {
    while (line.length() < column)
      line.append(' ');
    line.replace(column - 1, column - 1 + str.length(), str);
  }
}
