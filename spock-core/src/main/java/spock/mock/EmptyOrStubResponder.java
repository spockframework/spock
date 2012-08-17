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

package spock.mock;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import groovy.lang.GString;

import org.spockframework.mock.EqualsHashCodeToStringInteractions;
import org.spockframework.mock.IMockInteraction;
import org.spockframework.mock.IMockInvocation;
import org.spockframework.util.ReflectionUtil;
import spock.lang.Experimental;

@Experimental
public class EmptyOrStubResponder implements IMockInvocationResponder {
  public static final EmptyOrStubResponder INSTANCE = new EmptyOrStubResponder();

  private EmptyOrStubResponder() {}

  public Object respond(IMockInvocation invocation) {
    IMockInteraction interaction = EqualsHashCodeToStringInteractions.INSTANCE.match(invocation);
    if (interaction != null) return interaction.accept(invocation);

    Class<?> returnType = invocation.getMethod().getReturnType();

    if (returnType == void.class || returnType == Void.class) {
      return null;
    }

    if (returnType.isPrimitive()) {
      return returnType == boolean.class ? false : 0;
    }

    if (returnType.isInterface()) {
      if (returnType == List.class) return new ArrayList();
      if (returnType == Set.class) return new HashSet();
      if (returnType == Map.class) return new HashMap();
      if (returnType == Queue.class) return new LinkedList();
      if (returnType == SortedSet.class) return new TreeSet();
      if (returnType == SortedMap.class) return new TreeMap();
      return createStub(returnType);
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

    Object emptyWrapper = createEmptyWrapper(returnType);
    if (emptyWrapper != null) return emptyWrapper;

    Object emptyObject = createEmptyObject(returnType);
    if (emptyObject != null) return emptyObject;

    return createStub(returnType);
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
      if (type == AtomicInteger.class) return new AtomicInteger(0);
      if (type == AtomicLong.class) return new AtomicLong(0);
      return null;
    }
    if (type == Boolean.class) return Boolean.FALSE;
    if (type == Character.class) return ' '; // better return something else?
    return null;
  }

  private Object createEmptyObject(Class<?> type) {
    try {
      return type.newInstance();
    } catch (Exception e) {
      return null;
    }
  }

  // TODO: returning a stub needs some more thought
  // where to get class loader/specification instance from?
  // should calls on this stub get dispatched to mock controller at all? (probably)
  // should this stub share some properties with its "owner" (groovy, global, etc.)?
  private Object createStub(Class<?> type) {
    return null;
  }
}
