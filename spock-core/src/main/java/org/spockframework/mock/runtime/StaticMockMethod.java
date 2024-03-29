/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.mock.runtime;

import org.spockframework.util.GenericTypeReflectorUtil;
import org.spockframework.mock.IMockMethod;
import org.spockframework.util.ReflectionUtil;

import java.lang.reflect.*;
import java.util.*;

import static java.util.Arrays.asList;

public class StaticMockMethod implements IMockMethod {
  private final Method method;
  private final Type mockType;

  public StaticMockMethod(Method method, Type mockType) {
    this.method = method;
    if (isMethodFromAdditionalInterfaces(mockType, method)) {
      //We need to switch the mockType to the additional interface type, because otherwise the type resolution will fail with NPE
      this.mockType = method.getDeclaringClass();
    } else {
      this.mockType = mockType;
    }
  }

  @Override
  public String getName() {
    return method.getName();
  }

  @Override
  public List<Class<?>> getParameterTypes() {
    return ReflectionUtil.eraseTypes(getExactParameterTypes());
  }

  @Override
  public List<Type> getExactParameterTypes() {
    return asList(GenericTypeReflectorUtil.getParameterTypes(method, mockType));
  }

  @Override
  public Class<?> getReturnType() {
    return GenericTypeReflectorUtil.erase(getExactReturnType());
  }

  @Override
  public Type getExactReturnType() {
    return GenericTypeReflectorUtil.getReturnType(method, mockType);
  }

  @Override
  public boolean isStatic() {
    return Modifier.isStatic(method.getModifiers());
  }

  /**
   * @param mockType the mocked type
   * @param method   the intercepted method
   * @return <code>true</code>, if the method is not part of the hierarchy of the mocked type.
   */
  private static boolean isMethodFromAdditionalInterfaces(Type mockType, Method method) {
    //We have a method from additional interfaces of the mock, because there was no common type found.
    return GenericTypeReflectorUtil.getExactSuperType(mockType, method.getDeclaringClass()) == null;
  }
}
