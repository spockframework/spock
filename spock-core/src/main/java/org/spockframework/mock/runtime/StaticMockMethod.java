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

package org.spockframework.mock.runtime;

import org.spockframework.gentyref.GenericTypeReflector;
import org.spockframework.mock.IMockMethod;
import org.spockframework.util.ReflectionUtil;

import java.lang.reflect.*;
import java.util.*;

public class StaticMockMethod implements IMockMethod {
  private final Method method;
  private final Type mockType;

  public StaticMockMethod(Method method, Type mockType) {
    this.method = method;
    this.mockType = mockType;
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
    return Arrays.asList(GenericTypeReflector.getExactParameterTypes(method, mockType));
  }

  @Override
  public Class<?> getReturnType() {
    return GenericTypeReflector.erase(getExactReturnType());
  }

  @Override
  public Type getExactReturnType() {
    return GenericTypeReflector.getExactReturnType(method, mockType);
  }

  @Override
  public boolean isStatic() {
    return Modifier.isStatic(method.getModifiers());
  }
}
