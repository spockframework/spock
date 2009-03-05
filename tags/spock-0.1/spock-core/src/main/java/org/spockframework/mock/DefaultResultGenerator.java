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

import java.lang.reflect.Method;
import java.util.Arrays;

import groovy.lang.GroovyObject;

import org.spockframework.util.InternalSpockError;
import org.spockframework.util.Util;

/**
 *
 * @author Peter Niederwieser
 */
public class DefaultResultGenerator implements IResultGenerator {
  public static final DefaultResultGenerator INSTANCE = new DefaultResultGenerator();

  private final Method OBJECT_EQUALS;
  private final Method OBJECT_HASH_CODE;
  private final Method OBJECT_TO_STRING;
  private final Method GROOVY_OBJECT_GET_PROPERTY;
  //private final Method GROOVY_OBJECT_SET_PROPERTY;
  //private final Method GROOVY_OBJECT_INVOKE_METHOD;

  private DefaultResultGenerator() {
    try {
      OBJECT_EQUALS = Object.class.getMethod("equals", Object.class);
      OBJECT_HASH_CODE = Object.class.getMethod("hashCode");
      OBJECT_TO_STRING = Object.class.getMethod("toString");
      GROOVY_OBJECT_GET_PROPERTY = GroovyObject.class.getMethod("getProperty", String.class);
    } catch (NoSuchMethodException e) {
      throw new InternalSpockError("Fascinating...", e);
    }
  }

  public Object generate(IMockInvocation invocation) {
    if (overrides(invocation.getMethod(), OBJECT_EQUALS))
      return invocation.getMockObject() == invocation.getArguments().get(0);
    if (overrides(invocation.getMethod(), OBJECT_HASH_CODE))
      return 42;
    if (overrides(invocation.getMethod(), OBJECT_TO_STRING))
      // once IMockInvocation contains more information, we should come up with
      // something better than that; also, it might be a good idea to move
      // default value generation into objects representing different
      // kinds of mocks which are accessible through IMockInvocation
      return invocation.getMockObject().getClass().getName() + "@unknown";
    //if (overrides(invocation.getMethod(), GROOVY_OBJECT_GET_PROPERTY))

    return Util.getDefaultValue(invocation.getMethod().getReturnType());
  }

  // TODO: need to check accessibility?
  private boolean overrides(Method m1, Method m2) {
    return m2.getDeclaringClass().isAssignableFrom(m1.getDeclaringClass())
      && m1.getName().equals(m2.getName())
      && Arrays.equals(m1.getParameterTypes(), m2.getParameterTypes());
  }
}
