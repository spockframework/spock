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

package org.spockframework.runtime;

import groovy.transform.Internal;
import org.spockframework.util.*;

import java.beans.Introspector;
import java.lang.reflect.*;
import java.util.*;

import groovy.lang.*;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.runtime.*;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.codehaus.groovy.runtime.typehandling.*;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;

/**
 * Provides convenient access to Groovy language and runtime features.
 * By convention, all usages of Groovy's InvokerHelper and
 * ScriptBytecodeAdapter go through this class.
 *
 * @author Peter Niederwieser
 */
public abstract class GroovyRuntimeUtil {
  private static final String SET = "set";
  private static final String GET = "get";
  private static final String IS = "is";

  @Internal
  public static final int MAJOR_VERSION = parseInt(GroovySystem.getVersion().split("\\.", 2)[0]);
  public static Object[] EMPTY_ARGUMENTS = new Object[0];

  public static boolean isTruthy(Object obj) {
    return DefaultTypeTransformation.castToBoolean(obj);
  }

  @SuppressWarnings("unchecked")
  public static <T> T coerce(Object obj, Class<T> type) {
    // can't use `type.cast()` because it will fail if `type` is a primitive type
    return (T) DefaultTypeTransformation.castToType(obj, type);
  }

  @SafeVarargs
  public static <T> T coerce(Object obj, Class<? extends T>... types) {
    if (types.length == 0) {
      throw new IllegalArgumentException("caller must provide at least one target type");
    }

    GroovyCastException lastException = null;

    for (Class<? extends T> type : types) {
      try {
        return GroovyRuntimeUtil.coerce(obj, type);
      } catch (GroovyCastException e) {
        lastException = e;
      }
    }

    throw lastException;
  }

  public static boolean equals(Object obj, Object other) {
    return DefaultTypeTransformation.compareEqual(obj, other);
  }

  public static String toString(Object obj) {
    return DefaultGroovyMethods.toString(obj);
  }

  public static MetaClass getMetaClass(Object object) {
    return InvokerHelper.getMetaClass(object);
  }

  public static MetaClass getMetaClass(Class<?> clazz) {
    return InvokerHelper.getMetaClass(clazz);
  }

  public static void setMetaClass(Object object, MetaClass metaClass) {
    GroovyObject groovyObject = ObjectUtil.asInstance(object, GroovyObject.class);
    if (groovyObject != null) {
      groovyObject.setMetaClass(metaClass);
      return;
    }
    ((MetaClassRegistryImpl) GroovySystem.getMetaClassRegistry()).setMetaClass(object, metaClass);
  }

  public static void setMetaClass(Class<?> clazz, MetaClass metaClass) {
    GroovySystem.getMetaClassRegistry().setMetaClass(clazz, metaClass);
  }

  public static String propertyToMethodName(String prefix, String propertyName) {
    return prefix + MetaClassHelper.capitalize(propertyName);
  }

  public static String propertyToGetterMethodName(String propertyName) {
    return propertyToMethodName(GET, propertyName);
  }

  public static String propertyToSetterMethodName(String propertyName) {
    return propertyToMethodName(SET, propertyName);
  }

  public static String propertyToBooleanGetterMethodName(String propertyName) {
    return propertyToMethodName(IS, propertyName);
  }

  /**
   * Checks if the given method is a getter method according
   * to Groovy rules. If yes, the corresponding property name
   * is returned. Otherwise, null is returned.
   * This method differs from Groovy 1.6.8 in that the latter
   * doesn't support the "is" prefix for static boolean properties;
   * however, that seems more like a bug.
   * See https://jira.codehaus.org/browse/GROOVY-4206
   */
  @Nullable
  public static String getterMethodToPropertyName(String methodName, List<Class<?>> parameterTypes, Class<?> returnType) {
    if (!parameterTypes.isEmpty()) return null;
    if (returnType == void.class) return null; // Void.class is allowed

    if (methodName.startsWith(GET))
      return getPropertyName(methodName, 3);

    if (methodName.startsWith(IS) && (
      returnType == boolean.class // Boolean.class is not allowed for Groovy >= 4
        || (MAJOR_VERSION <= 3 && returnType == Boolean.class) // In Groovy <= 3 a isGetter can also be a boxed Boolean.
    ))
      return getPropertyName(methodName, 2);

    return null;
  }

  private static String getPropertyName(String methodName, int prefixLength) {
    String result = methodName.substring(prefixLength);
    if (result.length() == 0) return null;
    return Introspector.decapitalize(result);
  }

  /**
   * Note: This method may throw checked exceptions although it doesn't say so.
   */
  public static Object getProperty(Object target, String property) {
    try {
      return InvokerHelper.getProperty(target, property);
    } catch (InvokerInvocationException e) {
      return ExceptionUtil.sneakyThrow(e.getCause());
    }
  }

  /**
   * Note: This method may throw checked exceptions although it doesn't say so.
   */
  public static void setProperty(Object target, String property, Object value) {
    try {
      InvokerHelper.setProperty(target, property, value);
    } catch (InvokerInvocationException e) {
      ExceptionUtil.sneakyThrow(e.getCause());
    }
  }

  /**
   * Note: This method may throw checked exceptions although it doesn't say so.
   */
  public static Object invokeConstructor(Class<?> clazz, Object... args) {
    try {
      return InvokerHelper.invokeConstructorOf(clazz, args);
    } catch (InvokerInvocationException e) {
      return ExceptionUtil.sneakyThrow(e.getCause());
    }
  }

  /**
   * Note: This method may throw checked exceptions although it doesn't say so.
   */
  public static Object invokeMethod(Object target, String method, Object... args) {
    try {
      return InvokerHelper.invokeMethod(target, method, args);
    } catch (InvokerInvocationException e) {
      return ExceptionUtil.sneakyThrow(e.getCause());
    }
  }

  public static Object invokeMethodNullSafe(Object target, String method, Object... args) {
    try {
      return InvokerHelper.invokeMethodSafe(target, method, args);
    } catch (InvokerInvocationException e) {
      return ExceptionUtil.sneakyThrow(e.getCause());
    }
  }

  public static Object invokeMethodQuietly(Object target, String method, Object... args) {
    try {
      return InvokerHelper.invokeMethod(target, method, args);
    } catch (Throwable ignored) {
      return null;
    }
  }

  public static void closeQuietly(@Nullable Object... objects) {
    closeQuietly("close", objects);
  }

  public static void closeQuietly(String closeMethod, @Nullable Object... objects) {
    if (objects != null) {
      Arrays
        .stream(objects)
        .filter(Objects::nonNull)
        .forEach(object -> invokeMethodQuietly(object, closeMethod));
    }
  }

  /**
   * Note: This method may throw checked exceptions although it doesn't say so.
   */
  @SuppressWarnings("unchecked")
  public static <T> T invokeClosure(Closure<T> closure, Object... args) {
    try {
      return closure.call(args);
    } catch (InvokerInvocationException e) {
      return ExceptionUtil.sneakyThrow(e.getCause());
    }
  }

  /**
   * Note: This method may throw checked exceptions although it doesn't say so.
   */
  public static <T extends Closure<?>> T instantiateClosure(Class<T> closureType, Object owner, Object thisObject) {
    try {
      Constructor<T> constructor = closureType.getConstructor(Object.class, Object.class);
      return constructor.newInstance(owner, thisObject);
    } catch (Exception e) {
      return ExceptionUtil.sneakyThrow(e.getCause());
    }
  }

  /**
   * Note: This method may throw checked exceptions although it doesn't say so.
   */
  public static Iterator<Object> asIterator(Object object) {
    try {
      return InvokerHelper.asIterator(object);
    } catch (InvokerInvocationException e) {
      return ExceptionUtil.sneakyThrow(e.getCause());
    }
  }

  /**
   * If args is an array it is cloned and the clone is modified.
   * The original array is not changed.
   */
  public static Object[] asUnwrappedArgumentArray(Object args) {
    return InvokerHelper.asUnwrappedArray(InvokerHelper.asArray(args).clone());
  }

  /**
   * Need to be careful because this converts null to an empty array.
   * For single arguments, new Object[] {arg} should therefore be used.
   */
  public static Object[] asArgumentArray(Object args) {
    return InvokerHelper.asArray(args);
  }

  public static List<Object> asArgumentList(Object args) {
    // there may be a better impl than this (maybe on some Groovy class)
    return asList(GroovyRuntimeUtil.asArgumentArray(args));
  }

  public static Object[] despreadList(Object[] args, Object[] spreads, int[] positions) {
    return ScriptBytecodeAdapter.despreadList(args, spreads, positions);
  }

  // let's try to find the method that was invoked and see if it has return type void
  // since we now do another method dispatch (w/o actually invoking the method),
  // there is a small chance that we get an incorrect result because a MetaClass has
  // been changed since the first dispatch; to eliminate this chance we would have to
  // first find the MetaMethod and then invoke it, but the problem is that calling
  // MetaMethod.invoke doesn't have the exact same semantics as calling
  // InvokerHelper.invokeMethod, even if the same method is chosen (see Spec GroovyMopExploration)
  public static boolean isVoidMethod(@Nullable Object target, String method, Object... args) {
    if (target == null) return false; // no way to tell

    Class[] argTypes = ReflectionUtil.getTypes(args);

    // the way we choose metaClass, we won't find methods on java.lang.Class
    // but since java.lang.Class has no void methods other than the ones inherited
    // from java.lang.Object, and since we operate on a best effort basis, that's OK
    // also we will choose a static method like Foo.getName() over the equally
    // named method on java.lang.Class, but this is consistent with current Groovy semantics
    // (see https://jira.codehaus.org/browse/GROOVY-3548)
    // in the end it's probably best to rely on NullAwareInvokeMethodSpec to tell us if
    // everything is OK
    MetaClass metaClass = target instanceof Class ?
        InvokerHelper.getMetaClass((Class) target) : InvokerHelper.getMetaClass(target);

    // seems to find more methods than getMetaMethod()
    MetaMethod metaMethod = metaClass.pickMethod(method, argTypes);
    if (metaMethod == null) return false; // we were unable to figure out which method was called

    Class returnType = metaMethod.getReturnType();
    // although Void.class will occur rarely, it makes sense to handle
    // it in the same way as void.class
    return returnType == void.class || returnType == Void.class;
  }

  /**
   * Note: This method may throw checked exceptions although it doesn't say so.
   */
  public static Object getAttribute(Object target, String name) {
    try {
      return InvokerHelper.getAttribute(target, name);
    } catch (InvokerInvocationException e) {
      return ExceptionUtil.sneakyThrow(e.getCause());
    }
  }

  @Nullable
  public static Method toMethod(@Nullable MetaMethod metaMethod) {
    CachedMethod cachedMethod = ObjectUtil.asInstance(metaMethod, CachedMethod.class);
    return cachedMethod == null ? null : cachedMethod.getCachedMethod();
  }
}
