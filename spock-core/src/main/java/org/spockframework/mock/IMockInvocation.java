/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.mock;

import java.util.List;

/**
 * A method invocation on a mock object.
 *
 * @author Peter Niederwieser
 */
public interface IMockInvocation {
  /**
   * The mock object that received the invocation.
   */
  IMockObject getMockObject();

  /**
   * The invoked method.
   */
  IMockMethod getMethod();

  /**
   * The arguments for the invocation.
   */
  List<Object> getArguments();

  /**
   * Delegates this method invocation to the real object underlying this mock object,
   * including any method arguments.
   * If this mock object has no underlying real (non-interface) object or interface
   * default method implementation, a {@link CannotInvokeRealMethodException} is thrown.
   *
   * @return the return value of the method to which this invocation was delegated
   */
  Object callRealMethod();

  /**
   * Delegates this method invocation to the real object underlying this mock object,
   * replacing the original method arguments with the specified arguments.
   * If this mock object has no underlying real (non-interface) object or interface
   * default method implementation, a {@link CannotInvokeRealMethodException} is thrown.
   *
   * @return the return value of the method to which this invocation was delegated
   */
  Object callRealMethodWithArgs(Object... arguments);
}
