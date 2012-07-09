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

public class MockObject implements IMockObject {
  @Nullable
  private final String name;
  private final Class<?> type;
  private final Object instance;
  private final boolean verified;
  private final boolean global;
  private final IMockInvocationResponder defaultResponse;

  public MockObject(@Nullable String name, Class<?> type, Object instance,
      boolean verified, boolean global, IMockInvocationResponder defaultResponse) {
    this.name = name;
    this.type = type;
    this.instance = instance;
    this.verified = verified;
    this.global = global;
    this.defaultResponse = defaultResponse;
  }

  public String getName() {
    return name;
  }

  @Nullable
  public Class<?> getType() {
    return type;
  }

  public Object getInstance() {
    return instance;
  }

  public boolean isVerified() {
    return verified;
  }

  public boolean isGlobal() {
    return global;
  }

  public IMockInvocationResponder getDefaultResponse() {
    return defaultResponse;
  }
}
