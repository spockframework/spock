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

import org.spockframework.mock.runtime.*;
import org.spockframework.util.*;
import spock.lang.Specification;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Detects mock objects and provides information about them.
 */
@Beta
public class MockUtil {
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

  /**
   * Attaches mock to a Specification.
   *
   * @param object a mock object
   * @param specification the Specification to attach to
   */
  public void attachMock(Object object, Specification specification) {
    asMock(object).attach(specification);
  }

  /**
   * Detaches mock from its current Specification.
   *
   * @param object a mock object
   */
  public void detachMock(Object object) {
    asMock(object).detach();
  }

  /**
   * Creates a detached mock.
   *
   * @param name the name
   * @param type the type of the mock
   * @param nature the nature
   * @param implementation the implementation
   * @param options the options
   * @param classloader the classloader to use
   * @return the mock
   */
  @Beta
  public Object createDetachedMock(@Nullable String name, Type type, MockNature nature,
      MockImplementation implementation, Map<String, Object> options,  ClassLoader classloader) {

    Object mock = CompositeMockFactory.INSTANCE.createDetached(
        new MockConfiguration(name, type, nature, implementation, options), classloader);
    return mock;
  }

  /**
   * Creates a detached mock.
   *
   * @param name the name
   * @param obj the existing object to wrap
   * @param nature the nature
   * @param implementation the implementation
   * @param options the options
   * @param classloader the classloader to use
   * @return the mock
   */
  @Beta
  public Object createDetachedMock(@Nullable String name, Object obj, MockNature nature,
      MockImplementation implementation, Map<String, Object> options,  ClassLoader classloader) {

    Object mock = CompositeMockFactory.INSTANCE.createDetached(
        new MockConfiguration(name, obj.getClass(), obj, nature, implementation, options), classloader);
    return mock;
  }
}
