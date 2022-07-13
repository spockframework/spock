/*
 * Copyright 2013 the original author or authors.
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

package org.spockframework.util;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Note: The {@link #writeString} and {@link #hex} methods
 * are based on code from class {@code StringEscapeUtils} in Commons Lang 2.6.
 */
public class JsonWriter {
  private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);

  private final Writer out;

  private boolean prettyPrint = false;
  private String indent = "    ";
  private int indentLevel = 0;

  static {
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  public JsonWriter(Writer out) {
    this.out = out;
  }

  public boolean isPrettyPrint() {
    return prettyPrint;
  }

  public void setPrettyPrint(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
  }

  public String getIndent() {
    return indent;
  }

  public void setIndent(String indent) {
    this.indent = indent;
  }

  public void write(Object object) throws IOException {
    if (object == null) {
      out.write("null");
    } else if (object instanceof Number) {
      writeNumber((Number) object);
    } else if (object.getClass().isArray()) {
      writeIterable(CollectionUtil.arrayToList(object));
    } else if (object instanceof Iterable) {
      writeIterable((Iterable) object);
    } else if (object instanceof Map) {
      writeMap((Map<?, ?>) object);
    } else if (object instanceof Date) {
      writeDate((Date) object);
    } else {
      writeString(object);
    }
  }

  private void writeNumber(Number number) throws IOException {
    String str = number.toString();
    if ("NaN".equals(str) || "Infinity".equals(str) || "-Infinity".equals(str)) {
      throw new IllegalArgumentException(number + " cannot be represented in JSON");
    }
    out.write(str);
  }

  private void writeIterable(Iterable iterable) throws IOException {
    out.write("[");
    if (prettyPrint) {
      ++indentLevel;
      writeNewline();
    }
    Iterator iterator = iterable.iterator();
    if (iterator.hasNext()) {
      while(true) {
        write(iterator.next());
        if (!iterator.hasNext()) break;
        out.write(",");
        if (prettyPrint) {
          writeNewline();
        }
      }
    }
    if (prettyPrint) {
      --indentLevel;
      writeNewline();
    }
    out.write("]");
  }

  private <K, V> void writeMap(Map<K, V> map) throws IOException {
    out.write("{");
    if (prettyPrint) {
      ++indentLevel;
      writeNewline();
    }
    Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();
    if (iterator.hasNext()) {
      while(true) {
        Map.Entry<K, V> entry = iterator.next();
        writeString(entry.getKey());
        out.write(":");
        if (prettyPrint) {
          out.write(" ");
        }
        write(entry.getValue());
        if (!iterator.hasNext()) break;
        out.write(",");
        if (prettyPrint) {
          writeNewline();
        }
      }
    }
    if (prettyPrint) {
      --indentLevel;
      writeNewline();
    }
    out.write("}");
  }

  private void writeDate(Date date) throws IOException {
    writeString(dateFormat.format(date));
  }

  private void writeString(Object object) throws IOException {
    out.write("\"");

    String str = object.toString();
    if (str == null) {
      throw new IllegalArgumentException("Non-null object of type '"
          + object.getClass() + "' returned 'null' toString() representation");
    }

    int sz = str.length();

    for (int i = 0; i < sz; i++) {
      char ch = str.charAt(i);

      // handle unicode
      if (ch > 0xfff) {
        out.write("\\u" + hex(ch));
      } else if (ch > 0xff) {
        out.write("\\u0" + hex(ch));
      } else if (ch > 0x7f) {
        out.write("\\u00" + hex(ch));
      } else if (ch < 32) {
        switch (ch) {
          case '\b':
            out.write('\\');
            out.write('b');
            break;
          case '\n':
            out.write('\\');
            out.write('n');
            break;
          case '\t':
            out.write('\\');
            out.write('t');
            break;
          case '\f':
            out.write('\\');
            out.write('f');
            break;
          case '\r':
            out.write('\\');
            out.write('r');
            break;
          default:
            if (ch > 0xf) {
              out.write("\\u00" + hex(ch));
            } else {
              out.write("\\u000" + hex(ch));
            }
            break;
        }
      } else {
        switch (ch) {
          case '"':
            out.write('\\');
            out.write('"');
            break;
          case '\\':
            out.write('\\');
            out.write('\\');
            break;
          case '/':
            out.write('\\');
            out.write('/');
            break;
          default:
            out.write(ch);
            break;
        }
      }
    }

    out.write("\"");
  }

  private void writeNewline() throws IOException {
    out.write("\n");
    for (int i = 0; i < indentLevel; i++) {
      out.write(indent);
    }
  }

  private String hex(char ch) {
    return Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
  }
}
