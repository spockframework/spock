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

import static java.util.Objects.*;

public class MockObject implements IMockObject {
  private final IMockConfiguration configuration;
  private final Object instance;
  private final SpecificationAttachable mockInterceptor;
  @Nullable
  private Specification specification;

  public MockObject(IMockConfiguration configuration, Object instance, Specification specification, SpecificationAttachable mockInterceptor) {
    this.configuration = requireNonNull(configuration);
    this.instance = instance;
    this.specification = specification;
    this.mockInterceptor = mockInterceptor;
  }

  @Override
  @Nullable
  public String getName() {
    return configuration.getName();
  }

  @Override
  public String getMockName() {
    return getName() != null ? getName() : "unnamed";
  }

  @Override
  public Class<?> getType() {
    return GenericTypeReflectorUtil.erase(getExactType());
  }

  @Override
  public Type getExactType() {
    return configuration.getExactType();
  }

  @Override
  public Object getInstance() {
    return instance;
  }

  @Override
  public boolean isVerified() {
    return configuration.isVerified();
  }

  private boolean isGlobal() {
    return configuration.isGlobal();
  }

  @Override
  public IDefaultResponse getDefaultResponse() {
    return configuration.getDefaultResponse();
  }

  @Override
  public Specification getSpecification() {
    return specification;
  }

  @Override
  public boolean matches(Object target, IMockInteraction interaction) {
    if (target instanceof Wildcard) return isVerified() || !interaction.isRequired();

    boolean match = isGlobal() ? matchGlobal(target) : instance == target;
    if (match) {
      checkRequiredInteractionAllowed(interaction);
    }
    return match;
  }

  private boolean matchGlobal(Object target) {
    return (instance.getClass() == target.getClass()) && (!isMockOfClass() || (instance == target));
  }

  private boolean isMockOfClass() {
    return instance instanceof Class<?>;
  }

  private void checkRequiredInteractionAllowed(IMockInteraction interaction) {
    if (!isVerified() && interaction.isRequired()) {
      throw new InvalidSpecException("Stub '%s' matches the following required interaction:" +
        "\n\n%s\n\nRemove the cardinality (e.g. '1 *'), or turn the stub into a mock.\n").withArgs(getMockName(), interaction);
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

  @Override
  public IMockConfiguration getConfiguration() {
    return configuration;
  }
}
