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

package org.spockframework.runtime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.spockframework.runtime.model.ExpressionInfo;
import org.spockframework.runtime.model.TextPosition;
import org.spockframework.util.TextUtil;

/**
 * A pretty printer for <tt>ExpressionInfo</tt>s. Only accepts expressions that
 * start at position (1,1) and end on line 1 (i.e. don't extend over multiple
 * lines).
 *
 * @author Peter Niederwieser
 */
// port good ideas from AssertionRenderer, esp. toString() for values
public class ExpressionInfoPrinter {
  // the expression to print
  private final ExpressionInfo exprInfo;

  // lines of output
  private final List<StringBuilder> builders = new ArrayList<StringBuilder>();
  private final List<Integer> columnIndices = new ArrayList<Integer>();

  public ExpressionInfoPrinter(ExpressionInfo exprInfo) {
    TextPosition start = exprInfo.getRegion().getStart();
    if (start.getLine() != 1 || start.getColumn() != 1)
      throw new IllegalArgumentException("can only print expressions starting at 1,1");
    if (exprInfo.getRegion().getEnd().getLine() != 1)
      throw new IllegalArgumentException("can only print expressions ending on line 1");
    this.exprInfo = exprInfo;
  }

  public String print() {
    String line0 = exprInfo.getText();
    // empty line; filling it up with whitespace will later simplify rendering of anchor bar
    String line1 = TextUtil.repeatChar(' ', line0.length());

    builders.add(new StringBuilder(line0));
    builders.add(new StringBuilder(line1));
    
    // add dummy indices for first two lines
    columnIndices.add(0);
    columnIndices.add(0);

    Comparator<ExpressionInfo> comparator = new Comparator<ExpressionInfo>() {
      public int compare(ExpressionInfo o1, ExpressionInfo o2) {
        return o2.getAnchor().getColumn() - o1.getAnchor().getColumn();
      }
    };

    for (ExpressionInfo e : exprInfo.inCustomOrder(true, comparator))
      renderValue(e);

    return buildersToString();
  }

  private void renderValue(ExpressionInfo exprInfo) {
    int startColumnIndex = exprInfo.getAnchor().getColumnIndex();

    Object rawValue = exprInfo.getValue();
    String strValue;
    if (rawValue == null) strValue = "null";
    else if (rawValue.equals("")) strValue = "\"\"";
    else {
      strValue = rawValue.toString();
      if (strValue == null) strValue = rawValue.getClass().getName() + "@" + Integer.toHexString(rawValue.hashCode());
    }
    int endColumnIndex = startColumnIndex + strValue.length();

    // filter out enum constants whose toString() method just prints out the constant's name
    if (rawValue instanceof Enum && exprInfo.getText().equals(strValue)) return;
    
    // find top-most line with sufficient space left
    int lineIndex = builders.size();
    for (int i = 2; i < builders.size(); i++) {
      int colIndex = columnIndices.get(i);
      if (colIndex > endColumnIndex) {
        lineIndex = i;
        break;
      }
    }

    // place value
    if (lineIndex < builders.size()) {
      builders.get(lineIndex).replace(startColumnIndex, endColumnIndex, strValue);
      columnIndices.set(lineIndex, startColumnIndex);
    } else {
      StringBuilder builder = new StringBuilder(endColumnIndex);
      builder.append(TextUtil.repeatChar(' ', startColumnIndex));
      builder.append(strValue);
      builders.add(builder);
      columnIndices.add(startColumnIndex);
    }

    // place anchor bar
    for (int i = 1; i < lineIndex; i++) {
      builders.get(i).setCharAt(startColumnIndex, '|');
      columnIndices.set(i, startColumnIndex + 1); // + 1 because no whitespace required between end of a value and a |
    }
  }

  private String buildersToString() {
    StringBuilder result = new StringBuilder();
    for (StringBuilder builder : builders)
      result.append(builder).append('\n');
    return result.toString();
  }
}