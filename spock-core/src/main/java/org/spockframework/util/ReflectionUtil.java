/*
 * Copyright 2023 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.util;

import static java.util.Arrays.asList;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.security.CodeSource;
import java.util.*;

import org.codehaus.groovy.runtime.MetaClassHelper;

public abstract class ReflectionUtil {
  /**
   * Determines the package name without calling {@code Class.getPackage()}
   * (which may return null).
   */
  public static String getPackageName(Class<?> clazz) {
    int lengthDiff = clazz.getName().length() - clazz.getSimpleName().length();
    return lengthDiff == 0 ? clazz.getName() : clazz.getName().substring(0, lengthDiff - 1);
  }

  public static Class<?> loadFirstAvailableClass(String... classNames) {
    for (String className : classNames) {
      Class<?> clazz = loadClassIfAvailable(className);
      if (clazz != null) {return clazz;}
    }
    return null;
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
  public static boolean isArray(Object obj) {
    return (obj != null && obj.getClass().isArray());
  }

  public static boolean isAnnotationPresentRecursive(Class<?> cls, Class<? extends Annotation> annotationClass) {
    return cls.isAnnotationPresent(annotationClass) ||
      (Object.class.equals(cls) ? false : isAnnotationPresentRecursive(cls.getSuperclass(), annotationClass));
  }

  public static <T extends Annotation> T getAnnotationRecursive(Class<?> cls, Class<T> annotationClass) {
    T annotation = cls.getAnnotation(annotationClass);
    if (annotation != null) return annotation;
    if (Object.class.equals(cls)) return null;
    return getAnnotationRecursive(cls.getSuperclass(), annotationClass);
  }

  public static <T extends Annotation> List<T> collectAnnotationRecursive(Class<?> cls, Class<T> annotationClass) {
    return collectAnnotationRecursive(cls, annotationClass, new ArrayList<>());
  }

  private static <T extends Annotation> List<T> collectAnnotationRecursive(Class<?> cls, Class<T> annotationClass,
                                                                           List<T> result) {
    T annotation = cls.getAnnotation(annotationClass);
    if (annotation != null) {
      result.add(annotation);
    }
    if (Object.class.equals(cls)) return result;
    return collectAnnotationRecursive(cls.getSuperclass(), annotationClass, result);
  }

  public static boolean isFinalMethod(Method method) {
    return Modifier.isFinal(method.getDeclaringClass().getModifiers() | method.getModifiers());
  }

  /**
   * Returns {@code true} if the argument {@code m} is a public method of java.lang.Object.
   */
  public static boolean isObjectMethod(Method m) {
    return asList(Object.class.getMethods()).contains(m);
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
  @Nullable
  public static File getClassFile(Class<?> clazz) {
    CodeSource source = clazz.getProtectionDomain().getCodeSource();
    if (source == null) {
      return null;
    }
    File dir = new File(source.getLocation().getPath());
    if (!dir.isDirectory()) return null; // class file might be contained in Jar
    File clazzFile = new File(dir, clazz.getName().replace('.', File.separatorChar) + ".class");
    return clazzFile.isFile() ? clazzFile : null;
  }

  @Nullable
  public static Object getDefaultValue(Class<?> type) {
    if (!type.isPrimitive()) return null;

    else if (type == boolean.class) return false;
    else if (type == int.class) return 0;
    else if (type == long.class) return 0L;
    else if (type == float.class) return 0F;
    else if (type == double.class) return 0D;
    else if (type == char.class) return (char) 0;
    else if (type == short.class) return (short) 0;
    else if (type == byte.class) return (byte) 0;

    assert type == void.class;
    return null;
  }

  public static boolean hasAnyOfTypes(Object value, Class<?>... types) {
    for (Class<?> type : types)
      if (type.isInstance(value)) return true;

    return false;
  }

  public static Class[] getTypes(Object... objects) {
    return MetaClassHelper.convertToTypeArray(objects);
  }

  @Nullable
  public static Object invokeMethod(@Nullable Object target, Method method, @Nullable Object... args) {
    try {
      validateArguments(method, args);
      return method.invoke(target, args);
    } catch (IllegalAccessException e) {
      return ExceptionUtil.sneakyThrow(e);
    } catch (InvocationTargetException e) {
      return ExceptionUtil.sneakyThrow(e.getCause());
    }
  }

  public static void validateArguments(Method method, Object[] args) {
    if (!hasValidArguments(args, method.getParameterTypes()))
      throw new IllegalArgumentException(
        String.format("Method '%s(%s)' can't be called with parameters '%s'!",
                      method.getName(), Arrays.toString(method.getParameterTypes()), Arrays.toString(args)));
  }

  public static boolean hasValidArguments(Object[] args, Class<?>[] parameterTypes) {
    if (parameterTypes.length != args.length)
      return false;

    for (int i = 0; i < parameterTypes.length; i++) {
      if (!isAssignable(parameterTypes[i], args[i]))
        return false;
    }

    return true;
  }

  public static boolean isAssignable(Class<?> type, @Nullable Object arg) {
    if (arg == null)
      return !type.isPrimitive();

    type = getWrapperType(type);
    Class<?> argType = getWrapperType(arg.getClass());
    return type.isAssignableFrom(argType);
  }

  @SuppressWarnings("ConstantConditions")
  private static Class<?> getWrapperType(Class<?> type) {
    return type.isPrimitive() ? getDefaultValue(type).getClass()
                              : type;
  }

  public static List<Class<?>> eraseTypes(List<Type> types) {
    List<Class<?>> result = new ArrayList<>();
    for (Type type : types) {
      result.add(GenericTypeReflectorUtil.erase(type));
    }
    return result;
  }

  public static boolean isToStringOverridden(Class<?> valueClass) {
    try {
      return !Object.class.equals(valueClass.getMethod("toString").getDeclaringClass());
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  public static void deepCopyFields(Object source, Object target) {
    if (!source.getClass().isAssignableFrom(target.getClass())) {
      throw new IllegalArgumentException("source and target are not compatible.");
    }
    Class<?> clazz = source.getClass();
    while (!clazz.equals(Object.class)) {
      Field[] fields = clazz.getDeclaredFields();
      Arrays.stream(fields)
        .filter(field -> !Modifier.isStatic(field.getModifiers()))
        .forEach(field -> copyField(field, source, target));
      clazz = clazz.getSuperclass();
    }
  }

  private static void copyField(Field field, Object source, Object target) {
      boolean accessible = field.isAccessible();
      field.setAccessible(true);
      try {
        field.set(target, field.get(source));
      } catch (IllegalAccessException e) {
        // ignore
      }
      field.setAccessible(accessible);
  }

  public static <T> T newInstance(Class<T> clazz, Object... args) {
    Checks.notNull(clazz, () -> "Class must not be null");
    Checks.notNull(args, () -> "Argument array must not be null");
    Checks.containsNoNullElements(args, () -> "Individual arguments must not be null");

    try {
      Class<?>[] parameterTypes = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
      return newInstance(clazz.getDeclaredConstructor(parameterTypes), args);
    } catch (Throwable t) {
      return ExceptionUtil.sneakyThrow(ExceptionUtil.getUnderlyingCause(t));
    }
  }

  public static <T> T newInstance(Constructor<T> constructor, Object... args) {
    Checks.notNull(constructor, () -> "Constructor must not be null");

    try {
      return makeAccessible(constructor).newInstance(args);
    } catch (Throwable t) {
      return ExceptionUtil.sneakyThrow(ExceptionUtil.getUnderlyingCause(t));
    }
  }

  @SuppressWarnings("deprecation") // "AccessibleObject.isAccessible()" is deprecated in Java 9
  public static <T extends AccessibleObject> T makeAccessible(T object) {
    if (!object.isAccessible()) {
      object.setAccessible(true);
    }
    return object;
  }

  /**
   * Checks if the {@code classToCheck} is visible by the passed {@code classLoader}
   *
   * @param classToCheck the class to check
   * @param classLoader  the classLoader
   * @return {@code true} if the class is visible
   */
  public static boolean isClassVisibleInClassloader(Class<?> classToCheck, ClassLoader classLoader) {
    try {
      if (classToCheck.getClassLoader() == null) {
        //Classes from the bootstrap classloader are always visible
        return true;
      }
      if (classToCheck.getClassLoader() == classLoader) {
        return true;
      }
      Class<?> loadedClass = classLoader.loadClass(classToCheck.getName());
      if (loadedClass == classToCheck) {
        return true;
      }
    } catch (ClassNotFoundException ignored) {
      //Ignored
    }
    return false;
  }
}
