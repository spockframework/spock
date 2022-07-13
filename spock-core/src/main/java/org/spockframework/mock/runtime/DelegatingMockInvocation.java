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

package org.spockframework.mock.runtime;

import org.spockframework.mock.*;

import java.util.List;

public class DelegatingMockInvocation implements IMockInvocation {
  private final IMockInvocation delegate;

  public DelegatingMockInvocation(IMockInvocation delegate) {
    this.delegate = delegate;
  }

  @Override
  public IMockObject getMockObject() {
    return delegate.getMockObject();
  }

  @Override
  public IMockMethod getMethod() {
    return delegate.getMethod();
  }

  @Override
  public List<Object> getArguments() {
    return delegate.getArguments();
  }

  @Override
  public Object callRealMethod() {
    return delegate.callRealMethod();
  }

  @Override
  public Object callRealMethodWithArgs(Object... arguments) {
    return delegate.callRealMethodWithArgs(arguments);
  }
}
