/*
 * Copyright 2010 the original author or authors.
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
import spock.mock.IMockInvocationResponder;

public interface IMockObject {
  /**
   * Returns the name of the mock object, or {@code null} if it has no name.
   *
   * @return the name of the mock object, or {@code null} if it has no name
   */
  @Nullable
  String getName();

  /**
   * Returns the declared type of the mock object.
   *
   * @return the declared type of the mock object
   */
  Class<?> getType();

  /**
   * Returns the instance of the mock object.
   *
   * @return the instance of the mock object
   */
  Object getInstance();

  /**
   * Tells whether interactions with this mock object should be verified.
   *
   * @return whether interactions with this mock object should be verified
   */
  boolean isVerified();

  /**
   * Tells whether this mock object represents all instances of the mocked type.
   *
   * @return whether this mock object represents all instances of the mocked type
   */
  boolean isGlobal();

  IMockInvocationResponder getDefaultResponse();
}
