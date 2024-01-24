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

package org.spockframework.mock.runtime;

import org.spockframework.mock.CannotCreateMockException;
import org.spockframework.mock.ISpockMockObject;
import org.spockframework.util.ThreadSafe;
import spock.mock.MockMakerId;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@ThreadSafe
public class JavaProxyMockMaker implements IMockMaker {
  public static final MockMakerId ID = new MockMakerId("java-proxy");
  private static final Class<?>[] CLASSES = new Class<?>[0];
  private static final Set<MockMakerCapability> CAPABILITIES = Collections.unmodifiableSet(EnumSet.of(
    MockMakerCapability.INTERFACE,
    MockMakerCapability.ADDITIONAL_INTERFACES
  ));

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
    return 100;
  }

  @Override
  public Object makeMock(IMockCreationSettings settings) throws CannotCreateMockException {
    return createMock(settings);
  }

  private Object createMock(IMockMaker.IMockCreationSettings settings) {
    List<Class<?>> interfaces = new ArrayList<>();
    interfaces.add(settings.getMockType());
    interfaces.addAll(settings.getAdditionalInterface());
    interfaces.add(ISpockMockObject.class);
    return Proxy.newProxyInstance(
      settings.getClassLoader(),
      interfaces.toArray(CLASSES),
      new JavaProxyMockInterceptorAdapter(settings.getMockInterceptor())
    );
  }

  @Override
  public IMockabilityResult getMockability(IMockCreationSettings settings) {
    return IMockabilityResult.MOCKABLE;
  }
}
