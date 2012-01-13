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
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;

import org.codehaus.groovy.runtime.*;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides convenient access to Groovy language and runtime features.
 * Only contains methods that can be fully abstracted from Groovy types.
 * Where this isn't possible (e.g. for metaclass handling), use the Groovy APIs directly.
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

  /**
   * Note: This method may throw checked exceptions although it doesn't say so.
   */
  public static Object getProperty(Object object, String property) {
    try {
      return InvokerHelper.getProperty(object, property);
    } catch (InvokerInvocationException e) {
      ExceptionUtil.sneakyThrow(e.getCause());
      return null; // unreachable
    }
  }
  
  public static void setProperty(Object object, String property, Object value) {
    try {
      InvokerHelper.setProperty(object, property, value);
    } catch (InvokerInvocationException e) {
      ExceptionUtil.sneakyThrow(e.getCause());
      return; // unreachable
    }
  }

  /**
   * Note: This method may throw checked exceptions although it doesn't say so.
   */
  @SuppressWarnings("unchecked")
  public static <T> T invokeConstructor(Class<T> type, Object... args) {
    try {
      return (T) InvokerHelper.invokeConstructorOf(type, args);
    } catch (InvokerInvocationException e) {
      ExceptionUtil.sneakyThrow(e.getCause());
      return null; // unreachable
    }
  }

  /**
   * Note: This method may throw checked exceptions although it doesn't say so.
   */
  @SuppressWarnings("unchecked")
  public static Object invokeMethod(Object target, String method, Object... args) {
    try {
      return InvokerHelper.invokeMethod(target, method, args);
    } catch (InvokerInvocationException e) {
      ExceptionUtil.sneakyThrow(e.getCause());
      return null; // unreachable
    }
  }

  public static Object invokeMethodSafe(Object target, String method, Object... args) {
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
  public static Object callClosure(Closure closure, Object... args) {
    try {
      return closure.call(args);
    } catch (InvokerInvocationException e) {
      ExceptionUtil.sneakyThrow(e.getCause());
      return null; // never reached
    }
  }

  // let's try to find the method that was invoked and see if it has return type void
  // since we now do another method dispatch (w/o actually invoking the method),
  // there is a small chance that we get an incorrect result because a MetaClass has
  // been changed since the first dispatch; to eliminate this chance we would have to
  // first find the MetaMethod and then invoke it, but the problem is that calling
  // MetaMethod.invoke doesn't have the exact same semantics as calling
  // InvokerHelper.invokeMethod, even if the same method is chosen (see Spec GroovyMopExploration)
  public static boolean isVoidMethod(Object target, String method, Object... args) {
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

  public static Object readField(Object target, String name) {
    return InvokerHelper.getAttribute(target, name);
  }
}
