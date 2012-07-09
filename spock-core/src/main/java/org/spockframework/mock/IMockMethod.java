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

import org.spockframework.util.Nullable;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Represents a method that is to be mocked. May or may not correspond to a physical method in byte code.
 */
public interface IMockMethod {
  /**
   * The name of the mocked method.
   *
   * @return name of the mocked method
   */
  String getName();

  /**
   * The parameter types of the mocked method. In cases where no static type information is available,
   * all arguments are assumed to have type {@code Object}.
   *
   * @return the parameter types of the mocked method
   */
  List<Class<?>> getParameterTypes();

  /**
   * The return type of the mocked method. In cases where no static type information is available,
   * the return type is assumed to be {@code Object}.
   *
   * @return the return type of the mocked method
   */
  Class<?> getReturnType();

  boolean isStatic();

  /**
   * Returns the physical method corresponding to the mocked method. Returns {@code null} if a dynamic
   * method is mocked, or if the target method isn't known because the invocation is intercepted before
   * a method has been selected.
   *
   * @return the physical method corresponding to the mocked method
   */
  @Nullable
  Method getTargetMethod();
}
