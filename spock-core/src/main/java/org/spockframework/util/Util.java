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

/**
 * General collection of utility methods.
 * 
 * @author Peter Niederwieser
 */
public class Util {
  public static void closeQuietly(Closeable closeable) {
    if (closeable == null) return;
    try {
      closeable.close();
    } catch (IOException ignored) {}
  }

  public static Object getDefaultValue(Class<?> type) {
    if (!type.isPrimitive()) return null;

    if (type == boolean.class) return false;
    if (type == int.class) return 0;
    if (type == long.class) return 0l;
    if (type == float.class) return 0f;
    if (type == double.class) return 0d;
    if (type == char.class) return (char)0;
    if (type == short.class) return (short)0;
    if (type == byte.class) return (byte)0;

    assert type == void.class;
    return null;
  }

  public static String getText(File path) throws IOException {
    StringBuilder source = new StringBuilder();
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(path));
      String line = reader.readLine();
      while (line != null) {
        source.append(line);
        source.append('\n');
        line = reader.readLine();
      }
    } finally {
      Util.closeQuietly(reader);
    }

    return source.toString();
  }

}
