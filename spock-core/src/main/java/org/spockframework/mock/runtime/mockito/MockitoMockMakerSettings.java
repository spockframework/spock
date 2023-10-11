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

import groovy.lang.Closure;
import org.mockito.MockSettings;
import org.spockframework.mock.runtime.IMockMaker;
import org.spockframework.runtime.GroovyRuntimeUtil;
import spock.mock.MockMakers;

import static java.util.Objects.requireNonNull;
import static org.spockframework.mock.runtime.IMockMaker.MockMakerId;

public final class MockitoMockMakerSettings implements IMockMaker.IMockMakerSettings {
  private final Closure<?> mockitoCode;

  public static MockitoMockMakerSettings createSettings(Closure<?> mockitoCode) {
    return new MockitoMockMakerSettings(mockitoCode);
  }

  private MockitoMockMakerSettings(Closure<?> mockitoCode) {
    this.mockitoCode = requireNonNull(mockitoCode);
  }

  @Override
  public MockMakerId getMockMakerId() {
    return MockMakers.mockito.getMockMakerId();
  }

  void applySettings(MockSettings mockitoSettings) {
    requireNonNull(mockitoSettings);
    mockitoCode.setResolveStrategy(Closure.DELEGATE_FIRST);
    mockitoCode.setDelegate(mockitoSettings);
    GroovyRuntimeUtil.invokeClosure(mockitoCode, mockitoSettings);
  }

  @Override
  public String toString() {
    return getMockMakerId() + " mock maker settings";
  }
}
