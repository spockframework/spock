/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.runtime.model;

import org.codehaus.groovy.ast.ASTNode;

/**
 * A region of text spanning all characters between a start position (inclusive)
 * and an end position (exclusive). Positions are given as line/column pairs
 * (starting at 1). TextRegion instances are immutable.
 *
 * @author Peter Niederwieser
 */
public class TextRegion {
  // IDEA: override methods to throw UnsupportedOperationException
  public static final TextRegion NOT_AVAILABLE =
    new TextRegion(TextPosition.NOT_AVAILABLE, TextPosition.NOT_AVAILABLE);

  private final TextPosition start; // inclusive
  private final TextPosition end;   // exclusive

  private TextRegion(TextPosition start, TextPosition end) {
    this.start = start;
    this.end = end;
  }

  public TextPosition getStart() {
    return start;
  }

  public TextPosition getEnd() {
    return end;
  }

  public boolean contains(TextPosition position) {
    return position.compareTo(start) >= 0 && position.compareTo(end) <= 0;
  }

  public TextRegion shiftVertically(int numLines) {
    return create(start.shiftVertically(numLines), end.shiftVertically(numLines));
  }

  public boolean equals(Object obj) {
    if (obj == null || getClass() != obj.getClass()) return false;
    TextRegion other = (TextRegion)obj;
    return start.equals(other.start) && end.equals(other.end);
  }

  public int hashCode() {
    return start.hashCode() * 31 + end.hashCode();
  }

  public String toString() {
    return String.format("%s-%s", start, end);
  }

  public static TextRegion of(ASTNode node) {
    return create(TextPosition.startOf(node), TextPosition.endOf(node));
  }

  public static TextRegion create(TextPosition start, TextPosition end) {
    if (start == TextPosition.NOT_AVAILABLE || end == TextPosition.NOT_AVAILABLE)
      return TextRegion.NOT_AVAILABLE;
    return new TextRegion(start, end);
  }
}
