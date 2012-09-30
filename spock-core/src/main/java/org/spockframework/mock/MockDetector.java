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

import org.spockframework.util.Beta;

/**
 * Detects mock objects and provides information about them.
 */
@Beta
public class MockDetector {
  /**
   * Tells whether the given object is a (Spock) mock object.
   *
   * @param object an arbitrary object
   *
   * @return whether the given object is a (Spock) mock object
   */
  public boolean isMock(Object object) {
    return object instanceof ISpockMockObject;
  }

  /**
   * Returns information about a mock object.
   *
   * @param object a mock object
   *
   * @return information about the mock object
   *
   * @throws IllegalArgumentException if the given object is not a mock object
   */
  public IMockObject asMock(Object object) {
    if (!isMock(object)) {
      throw new IllegalArgumentException("Not a mock object: " + object.toString());
    }

    ISpockMockObject handle = (ISpockMockObject) object;
    return handle.$spock_get();
  }
}
