/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.mock;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import groovy.lang.GString;
import groovy.lang.GroovyObject;

import org.spockframework.util.ReflectionUtil;
import org.spockframework.util.Beta;

import spock.lang.Specification;

/**
 * A response strategy that returns zero, an "empty" object, or a "dummy" object,
 * depending on the method's declared return type.
 */
@Beta
public class EmptyOrDummyResponse implements IDefaultResponse {
  public static final EmptyOrDummyResponse INSTANCE = new EmptyOrDummyResponse();
  private static final Class<?> OPTIONAL = ReflectionUtil.loadClassIfAvailable("java.util.Optional");
  private static final Method optionalEmptyMethod;

  static {
    optionalEmptyMethod = OPTIONAL == null ? null : ReflectionUtil.getDeclaredMethodByName(OPTIONAL, "empty");
  }

  private EmptyOrDummyResponse() {}

  public Object respond(IMockInvocation invocation) {
    IMockInteraction interaction = DefaultJavaLangObjectInteractions.INSTANCE.match(invocation);
    if (interaction != null) return interaction.accept(invocation);

    Class<?> returnType = invocation.getMethod().getReturnType();

    if (returnType == void.class || returnType == Void.class) {
      return null;
    }

    if (returnType.isPrimitive()) {
      return ReflectionUtil.getDefaultValue(returnType);
    }

    if (returnType.isInterface()) {
      if (returnType == Iterable.class) return new ArrayList();
      if (returnType == Collection.class) return new ArrayList();
      if (returnType == List.class) return new ArrayList();
      if (returnType == Set.class) return new HashSet();
      if (returnType == Map.class) return new HashMap();
      if (returnType == Queue.class) return new LinkedList();
      if (returnType == SortedSet.class) return new TreeSet();
      if (returnType == SortedMap.class) return new TreeMap();
      if (returnType == CharSequence.class) return "";
      return createDummy(invocation);
    }

    if (returnType.isArray()) {
      return Array.newInstance(returnType.getComponentType(), 0);
    }

    if (returnType.isEnum()) {
      Object[] enumConstants = returnType.getEnumConstants();
      return enumConstants.length > 0 ? enumConstants[0] : null; // null is only permissible value
    }

    if (CharSequence.class.isAssignableFrom(returnType)) {
      if (returnType == String.class) return "";
      if (returnType == StringBuilder.class) return new StringBuilder();
      if (returnType == StringBuffer.class) return new StringBuffer();
      if (returnType == GString.class) return GString.EMPTY;
      // continue on
    }

    if (returnType == OPTIONAL) return ReflectionUtil.invokeMethod(null, optionalEmptyMethod);

    Object emptyWrapper = createEmptyWrapper(returnType);
    if (emptyWrapper != null) return emptyWrapper;

    Object emptyObject = createEmptyObject(returnType);
    if (emptyObject != null) return emptyObject;

    return createDummy(invocation);
  }

  // also handles some numeric types which aren't primitive wrapper types
  private Object createEmptyWrapper(Class<?> type) {
    if (Number.class.isAssignableFrom(type)) {
      Method method = ReflectionUtil.getDeclaredMethodBySignature(type, "valueOf", String.class);
      if (method != null && method.getReturnType() == type) {
        return ReflectionUtil.invokeMethod(type, method, "0");
      }
      if (type == BigInteger.class) return BigInteger.ZERO;
      if (type == BigDecimal.class) return BigDecimal.ZERO;
      return null;
    }
    if (type == Boolean.class) return false;
    if (type == Character.class) return (char) 0; // better return something else?
    return null;
  }

  private Object createEmptyObject(Class<?> type) {
    try {
      return type.newInstance();
    } catch (Exception e) {
      return null;
    }
  }

  private Object createDummy(IMockInvocation invocation) {
    Class<?> type = invocation.getMethod().getReturnType();
    Type genericType = invocation.getMethod().getExactReturnType();
    Specification spec = invocation.getMockObject().getSpecification();
    return spec.createMock("dummy", genericType, MockNature.STUB, GroovyObject.class.isAssignableFrom(type) ?
        MockImplementation.GROOVY : MockImplementation.JAVA, Collections.<String, Object>emptyMap(), null);
  }
}
