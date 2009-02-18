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

package org.spockframework.runtime.model;

import java.util.*;

/**
 * A ...
 *
 * @author Peter Niederwieser
 */
// IDEA: Add some information about an expression's operation (e.g. method/property/operator name).
// This would pave the way for extensions that give the user additional information
// about why a condition failed. Example: Given the condition "abc" == "axc",
// the difference between the string literals could be highlighted.
public class ExpressionInfo implements Iterable<ExpressionInfo> {
  public static final String TEXT_NOT_AVAILABLE = "(n/a)";
  /**
   * Indicates that an expression's value is not available, either because the
   * expression has no value (e.g. def foo = 42), or because it wasn't evaluated
   * (due to shortcut evaluation of boolean expressions).
   */
  public static final Object VALUE_NOT_AVAILABLE = new Object() {
    @Override
    public String toString() {
      return "(n/a)";
    }
  };

  private TextRegion region;
  private TextPosition anchor;
  private final List<ExpressionInfo> children;
  private String text;
  private Object value;
  private boolean relevant = true;

  public ExpressionInfo(TextRegion region, TextPosition anchor, List<ExpressionInfo> children) {
    this.region = region;
    this.anchor = anchor;
    this.children = children;
  }

  public ExpressionInfo(TextRegion region, TextPosition anchor, ExpressionInfo... children) {
    this(region, anchor, Arrays.asList(children));
  }

  public TextRegion getRegion() {
    return region;
  }

  public TextPosition getAnchor() {
    return anchor;
  }

  public List<ExpressionInfo> getChildren() {
    return children;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Object getValue() {
    return value;
  }

  public ExpressionInfo setValue(Object value) {
    this.value = value;
    return this;
  }

  public boolean isRelevant() {
    return relevant && value != VALUE_NOT_AVAILABLE;
  }

  public ExpressionInfo setRelevant(boolean relevant) {
    this.relevant = relevant;
    return this;
  }

  public void shiftVertically(int numLines) {
    region = region.shiftVertically(numLines);
    anchor = anchor.shiftVertically(numLines);
  }

  public Iterator<ExpressionInfo> iterator() {
    List<ExpressionInfo> list = new ArrayList<ExpressionInfo>();
    collectPrefix(list, false);
    return list.iterator();
  }

  public Iterable<ExpressionInfo> inPrefixOrder(final boolean skipIrrelevant) {
    return new Iterable<ExpressionInfo>() {
      public Iterator<ExpressionInfo> iterator() {
        List<ExpressionInfo> list = new ArrayList<ExpressionInfo>();
        collectPrefix(list, skipIrrelevant);
        return list.iterator();
      }
    };
  }

  public Iterable<ExpressionInfo> inPostfixOrder(final boolean skipIrrelevant) {
    return new Iterable<ExpressionInfo>() {
      public Iterator<ExpressionInfo> iterator() {
        List<ExpressionInfo> list = new ArrayList<ExpressionInfo>();
        collectPostfix(list, skipIrrelevant);
        return list.iterator();
      }
    };
  }

  public Iterable<ExpressionInfo> inCustomOrder(boolean skipIrrelevant, Comparator<ExpressionInfo> comparator) {
    List<ExpressionInfo> list = new ArrayList<ExpressionInfo>();
    collectPrefix(list, skipIrrelevant);
    Collections.sort(list, comparator);
    return list;
  }

  private void collectPrefix(List<ExpressionInfo> collector, boolean skipIrrelevant) {
    if (!skipIrrelevant || isRelevant()) collector.add(this);
    for (ExpressionInfo expr : children) expr.collectPrefix(collector,skipIrrelevant);
  }

  private void collectPostfix(List<ExpressionInfo> collector, boolean skipIrrelevant) {
    for (ExpressionInfo expr : children) expr.collectPostfix(collector, skipIrrelevant);
    if (!skipIrrelevant || isRelevant()) collector.add(this);
  }
}
