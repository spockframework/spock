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

import java.io.*;
import java.util.*;
import java.util.regex.*;

import static java.util.Arrays.asList;

/**
 * Utility methods for text processing.
 *
 * @author Peter Niederwieser
 */
public abstract class TextUtil {
  private static final Pattern LOWER_UPPER = Pattern.compile("([^\\p{Upper}]*)(\\p{Upper}*)");

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

  public static String changeSubsequentIndent(String block, int delta, String lineSeparator) {
    List<String> lines = asList(block.split(lineSeparator));
    if (lines.size() == 1) {
      return block;
    }
    changeIndent(lines.subList(1, lines.size()), delta);
    return join(lineSeparator, lines);
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

  public static String join(String separator, List<?> objects) {
    StringBuilder builder = new StringBuilder();
    int size = objects.size();
    for (int i = 0; i < size; i++) {
      builder.append(objects.get(i));
      if (i != size - 1)
        builder.append(separator);
    }
    return builder.toString();
  }

  public static String join(String separator, Object... objects) {
    return join(separator, asList(objects));
  }

  public static int countOccurrences(String text, char symbol) {
    int result = 0;
    for (char ch : text.toCharArray())
      if (ch == symbol) result++;
    return result;
  }

  public static String escape(char ch) {
    if (ch == '\\') return "\\\\";
    if (ch == '\t') return "\\t";
    if (ch == '\n') return "\\n";
    if (ch == '\b') return "\\b";
    if (ch == '\r') return "\\r";
    if (ch == '\f') return "\\f";
    return String.valueOf(ch);
  }

  public static String escape(CharSequence seq) {
    StringBuilder builder = new StringBuilder(seq.length() * 3 / 2);
    for (int i = 0; i < seq.length(); i++)
      builder.append(escape(seq.charAt(i)));
    return builder.toString();
  }

  public static String printStackTrace(Throwable throwable) {
    StringWriter writer = new StringWriter();
    throwable.printStackTrace(new PrintWriter(writer));
    return writer.toString();
  }

  // taken from:
  // http://stackoverflow.com/questions/1660501/what-is-a-good-64bit-hash-function-in-java-for-textual-strings
  public static long longHashCode(String string) {
    long h = 1125899906842597L;
    int len = string.length();

    for (int i = 0; i < len; i++) {
      h = 31 * h + string.charAt(i);
    }
    return h;
  }

  public static String camelCaseToConstantCase(String value) {
    if (value == null || "".equals(value)) return value;

    StringBuilder result = new StringBuilder();
    Matcher matcher = LOWER_UPPER.matcher(value);

    while (matcher.find()) {
      String lowers = matcher.group(1);
      String uppers = matcher.group(2);

      if (uppers.length() == 0) {
        result.append(lowers.toUpperCase(Locale.ROOT));
      } else {
        if (lowers.length() > 0) {
          result.append(lowers.toUpperCase(Locale.ROOT));
          result.append('_');
        }
        if (uppers.length() > 1 && !matcher.hitEnd()) {
          result.append(uppers.substring(0, uppers.length() - 1));
          result.append('_');
          result.append(uppers.charAt(uppers.length() - 1));
        } else {
          result.append(uppers);
        }
      }
    }

    return result.toString();
  }

  public static String capitalize(String str) {
    if (str == null || str.length() == 0) return str;

    StringBuilder builder = new StringBuilder();
    builder.append(Character.toUpperCase(str.charAt(0)));
    builder.append(str.substring(1));
    return builder.toString();
  }
}
