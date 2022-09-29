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

package org.spockframework.mock;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Represents a method that can be mocked. Typically but not necessarily corresponds to a
 * physically declared method in an interface or class.
 */
public interface IMockMethod {
  /**
   * Returns the name of the method.
   *
   * @return the name of the method
   */
  String getName();

  /**
   * Returns the exact parameter types of the method.
   * If no static type information is available for the method, parameters are assumed to have type {@link Object}.
   *
   * @return the parameter types of the method
   */
  List<Class<?>> getParameterTypes();

  /**
   * Returns the exact parameter types of the method. Each returned {@link Type} is either a {@link Class} or
   * a {@link java.lang.reflect.ParameterizedType}. Unbound type parameters are substituted with {@link Object}.
   * If no static type information is available for the method, parameters are assumed to have type {@link Object}.
   *
   * @return the exact parameter types of the method
   */
  List<Type> getExactParameterTypes();

  /**
   * Returns the return type of the method. If no static type information is available for the method,
   * the return type is assumed to be {@code Object}.
   *
   * @return the return type of the method
   */
  Class<?> getReturnType();

  /**
   * Returns the exact return type of the method. The returned {@link Type} is either a {@link Class} or
   * a {@link java.lang.reflect.ParameterizedType}. In cases where no static type information is available,
   * the return type is assumed to be {@link Object}.
   *
   * @return the exact return type of the method
   */
  Type getExactReturnType();

  /**
   * Tells whether the method is static or an instance method.
   *
   * @return whether the method is static or an instance method
   */
  boolean isStatic();
}
