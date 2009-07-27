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

import java.io.Closeable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;

/**
 * @author Peter Niederwieser
 */
public class Util {
 public static boolean equals(Object obj1, Object obj2) {
    if (obj1 == null) return obj2 == null;
    return obj1.equals(obj2);
  }

  public static void closeSilently(Closeable closeable) {
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

  // TODO:
  // 1. behavior with closures (called from Groovy or Java)
  // 2. behavior with (overridden) operators
  // 2. what if MOP throws exception (e.g. method not found)? probably should only delete lines once target method call has been found
  @Deprecated // not suitable for Groovy 1.6
  public static void filterStackTrace(Throwable throwable) {
    StackTraceElement[] oldTrace = throwable.getStackTrace();
    List<StackTraceElement> newTrace = new ArrayList<StackTraceElement>();
    ListIterator<StackTraceElement> iter = Arrays.asList(oldTrace).listIterator(oldTrace.length);

    while (iter.hasPrevious()) {
      findGroovyRuntimeCall(iter, newTrace);
      findTargetMethodCall(iter);
      skipTargetMethodCallInternals(iter);
    }

    Collections.reverse(newTrace);
    throwable.setStackTrace((newTrace.toArray(new StackTraceElement[newTrace.size()])));
  }

  private static void findGroovyRuntimeCall(ListIterator<StackTraceElement> iterator, List<StackTraceElement> newTrace) {
    while (iterator.hasPrevious()) {
      StackTraceElement prev = iterator.previous();
      if (prev.getClassName().equals("org.codehaus.groovy.runtime.ScriptBytecodeAdapter") &&
        prev.getMethodName().startsWith("invoke"))
          break;
      else newTrace.add(prev);
    }
  }

  private static void findTargetMethodCall(ListIterator<StackTraceElement> iterator) {
    while (iterator.hasPrevious() && !iterator.previous().getClassName().startsWith("java.lang.reflect")) ;
  }

  private static void skipTargetMethodCallInternals(ListIterator<StackTraceElement> iterator) {
    while (iterator.hasPrevious()) {
      StackTraceElement prev = iterator.previous();
      if (!prev.getClassName().startsWith("sun.reflect") && !prev.getMethodName().contains("$")) {
        iterator.next(); // unconsume element
        break;
      }
    }
  }

  public static <T extends Annotation> T getDeclaredAnnotation(AnnotatedElement elem, Class<T> annotationType) {
    for (Annotation a : elem.getDeclaredAnnotations())
      if (a.annotationType() == annotationType) return annotationType.cast(a);
    return null;
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
}
