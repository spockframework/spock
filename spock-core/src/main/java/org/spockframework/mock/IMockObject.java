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

public interface IMockObject {
  /**
   * Returns the name of this mock object, or {@code null} if it has no name.
   *
   * @return the name of this mock object, or {@code null} if it has no name
   */
  @Nullable
  String getName();

  /**
   * Returns the declared type of this mock object.
   *
   * @return the declared type of this mock object
   */
  Class<?> getType();

  /**
   * Returns the instance of this mock object.
   *
   * @return the instance of this mock object
   */
  Object getInstance();

  /**
   * Tells whether this mock object supports verification of invocations.
   *
   * @return whether this mock object supports verification of invocations
   */
  boolean isVerified();

  /**
   * Returns the default responder for this mock object.
   *
   * @return the default responder for this mock object
   */
  IMockInvocationResponder getResponder();

  /**
   * Tells whether this mock object matches the target of the specified interaction.
   *
   * @param target the target of the interaction
   * @param interaction an interaction
   * @return whether this mock object matches the target of the specified interaction
   */
  boolean matches(Object target, IMockInteraction interaction);
}
