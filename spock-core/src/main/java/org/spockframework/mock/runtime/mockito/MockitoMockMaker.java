/*
 * Copyright 2023 the original author or authors.
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

package org.spockframework.mock.runtime.mockito;

import org.spockframework.mock.CannotCreateMockException;
import org.spockframework.mock.IMockObject;
import org.spockframework.mock.runtime.IMockMaker;
import org.spockframework.util.ReflectionUtil;
import org.spockframework.util.ThreadSafe;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

@ThreadSafe
public class MockitoMockMaker implements IMockMaker {
  public static final MockMakerId ID = new MockMakerId("mockito");
  private static final boolean mockitoAvailable = ReflectionUtil.isClassAvailable("org.mockito.Mockito");
  private static final Set<MockMakerCapability> CAPABILITIES = Collections.unmodifiableSet(EnumSet.of(
    MockMakerCapability.INTERFACE,
    MockMakerCapability.CLASS,
    MockMakerCapability.ADDITIONAL_INTERFACES,
    MockMakerCapability.EXPLICIT_CONSTRUCTOR_ARGUMENTS,
    MockMakerCapability.FINAL_CLASS,
    MockMakerCapability.FINAL_METHOD
  ));

  private final MockitoMockMakerImpl impl;

  public MockitoMockMaker() {
    if (mockitoAvailable) {
      this.impl = new MockitoMockMakerImpl();
    } else {
      this.impl = null;
    }
  }

  @Override
  public MockMakerId getId() {
    return ID;
  }

  @Override
  public Set<MockMakerCapability> getCapabilities() {
    return CAPABILITIES;
  }

  @Override
  public int getPriority() {
    return 300;
  }

  @Override
  public IMockObject asMockOrNull(Object object) {
    if (impl == null) {
      return null;
    }
    return impl.asMockOrNull(object);
  }

  @Override
  public Object makeMock(IMockCreationSettings settings) throws CannotCreateMockException {
    return Objects.requireNonNull(impl).makeMock(settings);
  }

  @Override
  public IMockabilityResult getMockability(IMockCreationSettings settings) {
    Class<?> mockType = settings.getMockType();
    if (Modifier.isFinal(mockType.getModifiers()) && !settings.getAdditionalInterface().isEmpty()) {
      return () -> "Cannot mock final classes with additional interfaces.";
    }
    if (!mockitoAvailable) {
      return () -> "The mockito-core library >= 4.11 is missing on the class path.";
    }
    return Objects.requireNonNull(impl).isMockable(settings.getMockType());
  }
}
