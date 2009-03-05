/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.mock;

import java.lang.reflect.Method;
import java.util.List;

/**
 *
 * @author Peter Niederwieser
 */
// @Immutable
public class MockInvocation implements IMockInvocation {
  private final Object mockObject;
  private final String mockObjectName;
  private final Method method;
  private final List<Object> arguments;

  public MockInvocation(Object mockObject, String mockObjectName, Method method, List<Object> arguments) {
    this.mockObject = mockObject;
    this.mockObjectName = mockObjectName;
    this.method = method;
    this.arguments = arguments;
  }

  public Object getMockObject() {
    return mockObject;
  }

  public String getMockObjectName() {
    return mockObjectName;
  }

  public Method getMethod() {
    return method;
  }

  public List<Object> getArguments() {
    return arguments;
  }

  // TODO: also print arguments (or their types)?
  @Override
  public String toString() {
    return String.format("%s.%s(...)", mockObjectName, method.getName());
  }
}
