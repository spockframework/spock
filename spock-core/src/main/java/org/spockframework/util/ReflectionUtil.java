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

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;

public abstract class ReflectionUtil {
  /**
   * Determines the package name without calling {@code Class.getPackage()}
   * (which may return null).
   */
  public static String getPackageName(Class<?> clazz) {
    int lengthDiff = clazz.getName().length() - clazz.getSimpleName().length();
    return lengthDiff == 0 ? clazz.getName() : clazz.getName().substring(0, lengthDiff - 1);
  }

  public static Class<?> loadClassIfAvailable(String className) {
    try {
      return ReflectionUtil.class.getClassLoader().loadClass(className);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  public static boolean isClassAvailable(String className) {
    return loadClassIfAvailable(className) != null;
  }

  public static boolean isMethodAvailable(String className, String methodName) {
    try {
      Class clazz = ReflectionUtil.class.getClassLoader().loadClass(className);
      return getMethodByName(clazz, methodName) != null;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  public static boolean isAnnotationPresent(AnnotatedElement element, String className) {
    for (Annotation ann : element.getAnnotations())
      if (ann.annotationType().getName().equals(className))
        return true;

    return false;
  }

  public static boolean isFinalMethod(Method method) {
    return Modifier.isFinal(method.getDeclaringClass().getModifiers() | method.getModifiers());
  }

  /**
   * Finds a public method with the given name declared in the given
   * class/interface or one of its super classes/interfaces. If multiple such
   * methods exists, it is undefined which one is returned.
   */
  @Nullable
  public static Method getMethodByName(Class<?> clazz, String name) {
    for (Method method : clazz.getMethods())
      if (method.getName().equals(name))
        return method;

    return null;
  }

  @Nullable
  public static Method getDeclaredMethodByName(Class<?> clazz, String name) {
    for (Method method : clazz.getDeclaredMethods())
      if (method.getName().equals(name))
        return method;

    return null;
  }

  @Nullable
  public static Method getMethodBySignature(Class<?> clazz, String name, Class<?>... parameterTypes) {
    try {
      return clazz.getMethod(name, parameterTypes);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  @Nullable
  public static Method getDeclaredMethodBySignature(Class<?> clazz, String name, Class<?>... parameterTypes) {
    try {
      return clazz.getDeclaredMethod(name, parameterTypes);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  /**
   * Returns the class file for the given class (which has been verified to exist in the returned location),
   * or null if the class file could not be found (e.g. because it is contained in a Jar).
   */
  public static File getClassFile(Class<?> clazz) {
    File dir = new File(clazz.getProtectionDomain().getCodeSource().getLocation().getPath());
    if (!dir.isDirectory()) return null; // class file might be contained in Jar
    File clazzFile = new File(dir, clazz.getName().replace('.', File.separatorChar) + ".class");
    return clazzFile.isFile() ? clazzFile : null;
  }

  public static Object getDefaultValue(Class<?> type) {
    if (!type.isPrimitive()) return null;

    if (type == boolean.class) return false;
    if (type == int.class) return 0;
    if (type == long.class) return 0l;
    if (type == float.class) return 0f;
    if (type == double.class) return 0d;
    if (type == char.class) return (char) 0;
    if (type == short.class) return (short) 0;
    if (type == byte.class) return (byte) 0;

    assert type == void.class;
    return null;
  }

  public static boolean hasAnyOfTypes(Object value, Class<?>... types) {
    for (Class<?> type : types) 
      if (type.isInstance(value)) return true;

    return false;
  }

  public static Class[] getTypes(Object... objects) {
    Class[] classes = new Class[objects.length];
    for (int i = 0; i < objects.length; i++)
      classes[i] = ObjectUtil.getClass(objects[i]);
    return classes;
  }

  @Nullable
  public static Object invokeMethod(@Nullable Object target, Method method, @Nullable Object... args) {
    try {
      return method.invoke(target, args);
    } catch (IllegalAccessException e) {
      ExceptionUtil.sneakyThrow(e);
      return null; // never reached
    } catch (InvocationTargetException e) {
      ExceptionUtil.sneakyThrow(e.getCause());
      return null; // never reached
    }
  }
}