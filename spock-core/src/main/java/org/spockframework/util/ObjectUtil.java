/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
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
  public static boolean equals(@Nullable Object obj1, @Nullable Object obj2) {
    if (obj1 == null) return obj2 == null;
    return obj1.equals(obj2);
  }

  public static int hashCode(@Nullable Object obj) {
    return obj == null ? 0 : obj.hashCode();
  }

  public static String toString(@Nullable Object obj) {
    return obj == null ? "null" : obj.toString();
  }

  public static Class<?> getClass(@Nullable Object obj) {
    return obj == null ? null : obj.getClass();
  }

  public static Class<?> voidAwareGetClass(@Nullable Object obj) {
    return obj == null ? void.class : obj.getClass();
  }

  public static boolean eitherNull(Object... objs) {
    for (Object obj: objs) {
      if (obj == null) return true;
    }
    return false;
  }

  @Nullable
  @SuppressWarnings("unchecked")
  public static <T> T asInstance(Object obj, Class<T> type) {
    return type.isInstance(obj) ? (T) obj : null;
  }

  public static <T extends Comparable<T>> int compare(@Nullable T comparable1, @Nullable T comparable2) {
    if (comparable1 == null && comparable2 == null) return 0;
    if (comparable1 == null) return -1;
    if (comparable2 == null) return 1;
    return comparable1.compareTo(comparable2);
  }
}
