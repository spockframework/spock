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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.spockframework.mock.IMockMethod;

public class StaticMockMethod implements IMockMethod {
  private final Method method;

  public StaticMockMethod(Method method) {
    this.method = method;
  }

  public String getName() {
    return method.getName();
  }

  public List<Class<?>> getParameterTypes() {
    return Arrays.asList(method.getParameterTypes());
  }

  public Class<?> getReturnType() {
    return method.getReturnType();
  }

  public boolean isStatic() {
    return Modifier.isStatic(method.getModifiers());
  }
}
