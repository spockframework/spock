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

import groovy.lang.Closure;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;

import org.codehaus.groovy.runtime.*;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.beans.Introspector;
import java.util.Iterator;
import java.util.List;

/**
 * Provides convenient access to Groovy language and runtime features.
 * By convention, all usages of Groovy's InvokerHelper and
 * ScriptBytecodeAdapter go through this class.
 *
 * @author Peter Niederwieser
 */
public abstract class GroovyRuntimeUtil {
  public static boolean isTruthy(Object obj) {
    return DefaultTypeTransformation.castToBoolean(obj);
  }

  // can't generify return type because DefaultTypeTransformation
  // returns a wrapper if 'type' refers to a primitive type
  public static Object coerce(Object obj, Class<?> type) {
    return DefaultTypeTransformation.castToType(obj, type);
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
    return GroovySystem.getMetaClassRegistry().getMetaClass(clazz);
  }
  
  public static String propertyToMethodName(String prefix, String propertyName) {
    return prefix + MetaClassHelper.capitalize(propertyName);
  }

  /**
   * Checks if the given method is a getter method according
   * to Groovy rules. If yes, the corresponding property name
   * is returned. Otherwise, null is returned.
   * This method differs from Groovy 1.6.8 in that the latter
   * doesn't support the "is" prefix for static boolean properties;
   * however, that seems more like a bug.
   * See http://jira.codehaus.org/browse/GROOVY-4206
   */
  @Nullable
  public static String getterMethodToPropertyName(String methodName, List<Class<?>> parameterTypes, Class<?> returnType) {
    if (!parameterTypes.isEmpty()) return null;
    if (returnType == void.class) return null; // Void.class is allowed

    if (methodName.startsWith("get"))
      return getPropertyName(methodName, 3);

    if (methodName.startsWith("is")
        && returnType == boolean.class) // Boolean.class is not allowed
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
      ExceptionUtil.sneakyThrow(e.getCause());
      return null; // never reached
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
      ExceptionUtil.sneakyThrow(e.getCause());
      return null; // never reached
    }
  }

  /**
   * Note: This method may throw checked exceptions although it doesn't say so.
   */
  public static Object invokeMethod(Object target, String method, Object... args) {
    try {
      return InvokerHelper.invokeMethod(target, method, args);
    } catch (InvokerInvocationException e) {
      ExceptionUtil.sneakyThrow(e.getCause());
      return null; // never reached
    }
  }

  public static Object invokeMethodNullSafe(Object target, String method, Object... args) {
    try {
      return InvokerHelper.invokeMethodSafe(target, method, args);
    } catch (InvokerInvocationException e) {
      ExceptionUtil.sneakyThrow(e.getCause());
      return null; // never reached
    }
  }

  public static Object invokeMethodQuietly(Object target, String method, Object... args) {
    try {
      return InvokerHelper.invokeMethod(target, method, args);
    } catch (Throwable ignored) {
      return null;
    }
  }

  /**
   * Note: This method may throw checked exceptions although it doesn't say so.
   */
  @SuppressWarnings("unchecked")
  public static Object invokeClosure(Closure closure, Object... args) {
    try {
      return closure.call(args);
    } catch (InvokerInvocationException e) {
      ExceptionUtil.sneakyThrow(e.getCause());
      return null; // never reached
    }
  }

  /**
   * Note: This method may throw checked exceptions although it doesn't say so.
   */
  public static Iterator<Object> asIterator(Object object) {
    try {
      return InvokerHelper.asIterator(object);
    } catch (InvokerInvocationException e) {
      ExceptionUtil.sneakyThrow(e.getCause());
      return null; // never reached
    }
  }
  
  public static Object[] asArray(Object args) {
    return InvokerHelper.asArray(args);
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
    // (see http://jira.codehaus.org/browse/GROOVY-3548)
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
      ExceptionUtil.sneakyThrow(e.getCause());
      return null; // never reached
    }
  }
}
