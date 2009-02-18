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

package org.spockframework.mock;

import java.lang.reflect.*;

import org.spockframework.util.Util;

/**
 * A ...
 *
 * @author Peter Niederwieser
 */
// IDEA: use this for ">> dummy"
// could also create new type of test double, e.g. @IntelliMock
public class DummyResultGenerator implements IResultGenerator {
  public static final DummyResultGenerator INSTANCE = new DummyResultGenerator();

  private DummyResultGenerator() {}

  public Object generate(IMockInvocation invocation) {
    return createDefaultValue(invocation.getMethod().getReturnType());
  }

  private Object createDefaultValue(Class<?> type) {
    if (type.isPrimitive())
      return Util.getDefaultValue(type);
    if (type.isEnum())
      return type.getEnumConstants()[0];
    if (type.isArray())
      return createEmptyArray(type);
    if (type.isInterface())
      // IDEA: cache proxies (one per type)
      // IDEA: use cglib proxy for classes
      return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
        new Class<?>[] {type}, handler);
    try {
      return type.getConstructor().newInstance();
    } catch (Exception ignored) {}
    return null;
    // IDEA: try factory methods(new*, create*), try constructors/factory methods with parameters
  }

  private Object createEmptyArray(Class<?> type) {
    int arity = 1;
    while ((type = type.getComponentType()).isArray()) arity++;
    return Array.newInstance(type, new int[arity]);
  }

  private final InvocationHandler handler = new InvocationHandler() {
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      return createDefaultValue(method.getReturnType());
    }
  };
}