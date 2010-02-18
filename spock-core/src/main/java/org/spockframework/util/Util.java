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
import java.lang.reflect.Method;
import java.util.*;

/**
 * General collection of utility methods.
 * 
 * @author Peter Niederwieser
 */
public class Util {
  /**
   * Null-aware equals() implementation.
   */
  public static boolean equals(Object obj1, Object obj2) {
    if (obj1 == null) return obj2 == null;
    return obj1.equals(obj2);
  }

  /**
   * Null-aware toString() implementation.
   */
  public static String toString(Object obj) {
    return obj == null ? "null" : obj.toString();
  }

  public static void closeQuietly(Closeable closeable) {
    if (closeable == null) return;
    try {
      closeable.close();
    } catch (IOException ignored) {}
  }

  public static <E, F> ArrayList<F> filterMap(Collection<E> collection, IFunction<E, F> function) {
    ArrayList<F> result = new ArrayList<F>(collection.size());

    for (E elem : collection) {
      F resultElem = function.apply(elem);
      if (resultElem != null)
        result.add(resultElem);
    }

    return result;
  }

  public static boolean isClassAvailable(String className) {
    try {
      Util.class.getClassLoader().loadClass(className);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  public static int countOccurrences(String text, char symbol) {
    int result = 0;
    for (char ch : text.toCharArray())
      if (ch == symbol) result++;
    return result;
  }

  public static Object getDefaultValue(Class<?> type) {
    if (type.isPrimitive()) {
      if (type == boolean.class) return false;
      if (type == int.class) return 0;
      if (type == long.class) return 0l;
      if (type == float.class) return 0f;
      if (type == double.class) return 0d;
      if (type == char.class) return (char)0;
      if (type == short.class) return (short)0;
      if (type == byte.class) return (byte)0;
      return null; // type == void.class
    }

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

  /**
   * (Partial) replacement for Arrays.copyOfRange, which is only available in JDK6.
   */
  public static Object[] copyArray(Object[] array, int from, int to) {
    Object[] result = new Object[to - from];
    System.arraycopy(array, from, result, 0, to - from);
    return result;
  }

  /**
   * Finds a public method with the given name declared in the given
   * class/interface or one of its super classes/interfaces. If multiple such
   * methods exists, it is undefined which one is returned.
   */
  public static @Nullable Method getMethod(Class<?> clazz, String name) {
    for (Method method : clazz.getMethods())
      if (method.getName().equals(name))
        return method;

    return null;
  }
}
