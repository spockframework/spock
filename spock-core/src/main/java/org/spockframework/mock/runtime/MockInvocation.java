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

package org.spockframework.mock.runtime;

import org.spockframework.mock.*;
import org.spockframework.util.*;

import java.util.*;

import static java.util.Arrays.asList;

/**
 *
 * @author Peter Niederwieser
 */
public class MockInvocation implements IMockInvocation {
  private final IMockObject mockObject;
  private final IMockMethod method;
  private final List<Object> arguments;
  private final IResponseGenerator realMethodInvoker;

  public MockInvocation(IMockObject mockObject, IMockMethod method, List<Object> arguments,
      IResponseGenerator realMethodInvoker) {
    this.mockObject = mockObject;
    this.method = method;
    this.arguments = arguments;
    this.realMethodInvoker = realMethodInvoker;
  }

  @Override
  public IMockObject getMockObject() {
    return mockObject;
  }

  @Override
  public IMockMethod getMethod() {
    return method;
  }

  @Override
  public List<Object> getArguments() {
    return arguments;
  }

  @Override
  public Object callRealMethod() {
    return realMethodInvoker.respond(this).get();
  }

  @Override
  public Object callRealMethodWithArgs(final Object... arguments) {
      return realMethodInvoker.respond(new DelegatingMockInvocation(this) {
        @Override
        public List<Object> getArguments() {
          return asList(arguments);
        }
      }).get();
  }

  @Override
  public String toString() {
    String mockName = mockObject.getName();
    if (mockName == null) mockName = String.format("<%s>", mockObject.getType().getSimpleName());
    return String.format("%s.%s(%s)", mockName, method.getName(), render(arguments));
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof IMockInvocation)) return false;

    IMockInvocation otherInv = (IMockInvocation) other;
    return mockObject.getInstance() == otherInv.getMockObject().getInstance()
        && method.getName().equals(otherInv.getMethod().getName())
        && arguments.equals(otherInv.getArguments());
  }

  public int hashCode() {
    int result = mockObject.getInstance().hashCode();
    result = result * 31 + method.getName().hashCode();
    result = result * 31 + arguments.hashCode();
    return result;
  }

  private String render(List<Object> arguments) {
    List<String> strings = new ArrayList<>();
    for (Object arg : arguments) strings.add(RenderUtil.inspectOrDump(arg));
    return TextUtil.join(", ", strings);
  }
}
