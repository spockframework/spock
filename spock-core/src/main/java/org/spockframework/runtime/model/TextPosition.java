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

import java.io.Serializable;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.syntax.Token;

/**
 * A position in a text, given as a line/column pair. The first character in the
 * text has position (1,1). TextPosition instances are immutable.
 *
 * @author Peter Niederwieser
 */
public class TextPosition implements Comparable<TextPosition>, Serializable {
  // IDEA: override methods to throw UnsupportedOperationException
  public static final TextPosition NOT_AVAILABLE = new TextPosition(-1, -1);

  private static final long serialVersionUID = 1L;

  private final int line;
  private final int column;

  /**
   * Creates a new TextPosition instance.
   *
   * @param line the position's line number
   * @param column the position's column number
   * @throws IllegalArgumentException if line or column is less than 1
   */
  private TextPosition(int line, int column) {
    this.line = line;
    this.column = column;
  }

  public int getLine() {
    return line;
  }

  public int getColumn() {
    return column;
  }

  public int getLineIndex() {
    return line - 1;
  }

  public int getColumnIndex() {
    return column - 1;
  }

  public TextPosition shiftVertically(int numLines) {
    return create(line + numLines, column);
  }

  public boolean equals(Object obj) {
    if (obj == null || getClass() != obj.getClass()) return false;
    TextPosition other = (TextPosition)obj;
    return line == other.line && column == other.column;
  }

  public int hashCode() {
    return line * 31 + column;
  }

  public String toString() {
    return String.format("(%d,%d)", line, column);
  }

  @Override
  public int compareTo(TextPosition other) {
    if (line != other.line) return line - other.line;
    return column - other.column;
  }

  // inclusive
  public static TextPosition startOf(Token token) {
    return create(token.getStartLine(), token.getStartColumn());
  }

  // inclusive
  public static TextPosition startOf(ASTNode node) {
    return create(node.getLineNumber(), node.getColumnNumber());
  }

  // exclusive
  public static TextPosition endOf(ASTNode node) {
    return create(node.getLastLineNumber(), node.getLastColumnNumber());
  }

  public static TextPosition create(int line, int column) {
    if (line < 1 || column < 1)
      return TextPosition.NOT_AVAILABLE;
    return new TextPosition(line, column);
  }
}
