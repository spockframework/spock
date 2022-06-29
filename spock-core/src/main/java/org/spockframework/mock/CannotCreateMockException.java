/*
 * Copyright 2009 the original author or authors.
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

/**
 * Thrown to indicate a problem when creating a mock object.
 *
 * @author Peter Niederwieser
 */
// IDEA: filter stack trace so that the top entry is the point of mock declaration
public class CannotCreateMockException extends RuntimeException {
  public CannotCreateMockException(Class<?> mockType, String message) {
    this(mockType, message, null);
  }

  public CannotCreateMockException(Class<?> mockType, Throwable cause) {
    this(mockType, "", cause);
  }

  public CannotCreateMockException(Class<?> mockType, String message, Throwable cause) {
    super(String.format("Cannot create mock for %s %s", mockType, message), cause);
  }
}
