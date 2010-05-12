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

package org.spockframework.util;

import java.util.Arrays;
import java.util.List;

/**
 * Utility methods for text processing.
 *
 * @author Peter Niederwieser
 */
public abstract class TextUtil {
  public static String repeatChar(char ch, int times) {
    char[] chars = new char[times];
    Arrays.fill(chars, ch);
    return new String(chars);
  }

  public static int getIndent(String line) {
    int i = 0;
    while (i < line.length() && Character.isWhitespace(line.charAt(i))) i++;
    return i;
  }

  public static int getIndent(List<String> lines) {
    int result = Integer.MAX_VALUE;
    for (String line : lines) {
      int indent = getIndent(line);
      if (indent < result) result = indent;
    }
    return result;
  }

  public static String changeIndent(String line, int delta) {
    return delta > 0 ? repeatChar(' ', delta) + line : line.substring(-delta);
  }

  public static void changeIndent(List<String> lines, int delta) {
    if (delta == 0) return;

    for (int i = 0; i < lines.size(); i++)
      lines.set(i, changeIndent(lines.get(i), delta));
  }

  public static String erase(String line, int from, int to) {
    return line.substring(0, from) + repeatChar(' ', to - from) + line.substring(to);
  }

  public static String erase(String line, int from) {
    return erase(line, from, line.length());
  }

  /**
   * Returns the number of whitespace characters at the end of the given line.
   *
   * @param line
   * @return
   */
  public static int getTrailingWhitespace(String line) {
    return line.length() - line.trim().length() - getIndent(line);
  }

  public static String join(List<?> objects, String separator) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < objects.size(); i++) {
      builder.append(objects.get(i));
      if (i != objects.size() - 1)
        builder.append(separator);
    }
    return builder.toString();
  }

  public static int countOccurrences(String text, char symbol) {
    int result = 0;
    for (char ch : text.toCharArray())
      if (ch == symbol) result++;
    return result;
  }
}
