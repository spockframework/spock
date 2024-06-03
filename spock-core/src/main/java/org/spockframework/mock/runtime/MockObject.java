/*
 * Copyright 2010 the original author or authors.
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

import org.spockframework.util.GenericTypeReflectorUtil;
import org.spockframework.lang.Wildcard;
import org.spockframework.mock.*;
import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.util.Nullable;
import spock.lang.Specification;

import java.lang.reflect.Type;

public class MockObject implements IMockObject {
  private final SpecificationAttachable mockInterceptor;

  private Specification specification;
  private MockSettings mockSettings = new MockSettings(null, null, null, null, false, false);

  public MockObject(@Nullable String name, Type type, Object instance, boolean verified, boolean global,
      IDefaultResponse defaultResponse, Specification specification, SpecificationAttachable mockInterceptor) {
    this.mockSettings.setName(name);
    this.mockSettings.setType(type);
    this.mockSettings.setInstance(instance);
    this.mockSettings.setVerified(verified);
    this.mockSettings.setGlobal(global);
    this.mockSettings.setDefaultResponse(defaultResponse);
    this.specification = specification;
    this.mockInterceptor = mockInterceptor;
  }

  @Override
  @Nullable
  public String getName() {
    return mockSettings.getName();
  }

  @Override
  public Class<?> getType() {
    return GenericTypeReflectorUtil.erase(mockSettings.getType());
  }

  @Override
  public Type getExactType() {
    return mockSettings.getType();
  }

  @Override
  public Object getInstance() {
    return mockSettings.getInstance();
  }

  @Override
  public boolean isVerified() {
    return mockSettings.getVerified();
  }

  @Override
  public IDefaultResponse getDefaultResponse() {
    return mockSettings.getDefaultResponse();
  }

  @Override
  public Specification getSpecification() {
    return specification;
  }

  @Override
  public boolean matches(Object target, IMockInteraction interaction) {
    if (target instanceof Wildcard) return mockSettings.getVerified() || !interaction.isRequired();

    boolean match = mockSettings.getGlobal() ? matchGlobal(target) : mockSettings.getInstance() == target;
    if (match) {
      checkRequiredInteractionAllowed(interaction);
    }
    return match;
  }

  private boolean matchGlobal(Object target) {
    return (mockSettings.getInstance().getClass() == target.getClass()) && (!isMockOfClass() || (mockSettings.getInstance() == target));
  }

  private boolean isMockOfClass() {
    return mockSettings.getInstance() instanceof Class<?>;
  }

  private void checkRequiredInteractionAllowed(IMockInteraction interaction) {
    if (!mockSettings.getVerified() && interaction.isRequired()) {
      String mockName = mockSettings.getName() != null ? mockSettings.getName() : "unnamed";
      throw new InvalidSpecException("Stub '%s' matches the following required interaction:" +
          "\n\n%s\n\nRemove the cardinality (e.g. '1 *'), or turn the stub into a mock.\n").withArgs(mockName, interaction);
    }
  }

  @Override
  public void attach(Specification spec) {
    this.specification = spec;
    this.mockInterceptor.attach(spec);
  }

  @Override
  public void detach() {
    this.specification = null;
    this.mockInterceptor.detach();
  }
}
