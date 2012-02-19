/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.util;

/**
 * Utility methods applicable to (almost) any object. Includes null-safe variants of methods on class Object.
 */
public abstract class ObjectUtil {
  public static boolean equals(Object obj1, Object obj2) {
    if (obj1 == null) return obj2 == null;
    return obj1.equals(obj2);
  }

  public static int hashCode(Object obj) {
    return obj == null ? 0 : obj.hashCode();
  }

  public static String toString(Object obj) {
    return obj == null ? "null" : obj.toString();
  }

  public static Class<?> getClass(Object obj) {
    return obj == null ? null : obj.getClass();
  }

  public static Class<?> voidAwareGetClass(Object obj) {
    return obj == null ? void.class : obj.getClass();
  }
  
  public static boolean eitherNull(Object... objs) {
    for (Object obj: objs) {
      if (obj == null) return true;
    }
    return false;
  }

  public static <T extends Comparable<T>> int compare(T c1, T c2) {
    if (c1 == null && c2 == null) return 0;
    if (c1 == null) return -1;
    if (c2 == null) return 1;
    return c1.compareTo(c2);
  }
}