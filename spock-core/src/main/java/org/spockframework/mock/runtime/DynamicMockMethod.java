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

import org.spockframework.gentyref.GenericTypeReflector;
import org.spockframework.mock.IMockMethod;
import org.spockframework.util.ReflectionUtil;

import java.lang.reflect.Type;
import java.util.*;

public class DynamicMockMethod implements IMockMethod {
  private final String methodName;
  private final List<Type> parameterTypes;
  Type returnType;
  private final boolean isStatic;

  public DynamicMockMethod(String methodName, int argumentCount, boolean isStatic) {
    this(methodName, Collections.nCopies(argumentCount , Object.class), Object.class, isStatic);
  }

  public DynamicMockMethod(String methodName, List<Type> parameterTypes, Type returnType, boolean isStatic) {
    this.methodName = methodName;
    this.parameterTypes = parameterTypes;
    this.returnType = returnType;
    this.isStatic = isStatic;
  }

  @Override
  public String getName() {
    return methodName;
  }

  @Override
  public List<Class<?>> getParameterTypes() {
    return ReflectionUtil.eraseTypes(getExactParameterTypes());
  }

  @Override
  public List<Type> getExactParameterTypes() {
    return parameterTypes;
  }

  @Override
  public Class<?> getReturnType() {
    return GenericTypeReflector.erase(returnType);
  }

  @Override
  public Type getExactReturnType() {
    return returnType;
  }

  @Override
  public boolean isStatic() {
    return isStatic;
  }
}
