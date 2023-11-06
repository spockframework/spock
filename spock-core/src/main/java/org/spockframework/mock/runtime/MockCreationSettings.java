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

import org.spockframework.mock.IMockConfiguration;
import org.spockframework.mock.MockNature;
import org.spockframework.util.Beta;
import org.spockframework.util.Nullable;
import spock.mock.IMockMakerSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.spockframework.util.ObjectUtil.uncheckedCast;

@Beta
public class MockCreationSettings implements IMockMaker.IMockCreationSettings {

  private final IMockMakerSettings mockMakerSettings;
  private final Class<?> mockType;
  private final ClassLoader classLoader;
  private final boolean useObjenesis;
  private final List<Class<?>> additionalInterfaces;
  @Nullable
  private final List<Object> constructorArgs;
  private final IProxyBasedMockInterceptor mockInterceptor;
  private final MockNature mockNature;
  private final boolean staticMock;

  public static MockCreationSettings settingsFromMockConfiguration(IMockConfiguration mockConfig, IProxyBasedMockInterceptor interceptor, ClassLoader classLoader) {
    return new MockCreationSettings(mockConfig.getMockMaker(), mockConfig.getType(), mockConfig.getNature(), new ArrayList<>(mockConfig.getAdditionalInterfaces()), mockConfig.getConstructorArgs(), interceptor, classLoader, mockConfig.isUseObjenesis(), false);
  }

  public static MockCreationSettings settings(Class<?> mockType, List<Class<?>> additionalInterfaces, IProxyBasedMockInterceptor interceptor, ClassLoader classLoader, boolean useObjenesis) {
    return new MockCreationSettings(null, mockType, MockNature.MOCK, additionalInterfaces, null, interceptor, classLoader, useObjenesis, false);
  }

  private MockCreationSettings(@Nullable IMockMakerSettings mockMakerSettings,
                       Class<?> mockType,
                       MockNature mockNature,
                       List<Class<?>> additionalInterfaces,
                       @Nullable List<Object> constructorArgs,
                       IProxyBasedMockInterceptor mockInterceptor,
                       ClassLoader classLoader,
                       boolean useObjenesis,
                       boolean staticMock) {
    this.mockMakerSettings = mockMakerSettings;
    this.mockType = requireNonNull(mockType);
    this.mockNature = requireNonNull(mockNature);
    this.classLoader = classLoader;
    this.useObjenesis = useObjenesis;
    this.additionalInterfaces = additionalInterfaces;
    this.constructorArgs = constructorArgs;
    this.mockInterceptor = mockInterceptor;
    this.staticMock = staticMock;
  }

  @Override
  public Class<?> getMockType() {
    return mockType;
  }

  @Override
  public MockNature getMockNature() {
    return mockNature;
  }

  @Override
  public List<Class<?>> getAdditionalInterface() {
    return additionalInterfaces;
  }

  @Override
  public List<Object> getConstructorArgs() {
    return constructorArgs;
  }

  @Override
  public IProxyBasedMockInterceptor getMockInterceptor() {
    return mockInterceptor;
  }

  @Override
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  @Override
  public boolean isUseObjenesis() {
    return useObjenesis;
  }

  @Override
  public boolean isStaticMock() {
    return staticMock;
  }

  @Override
  public <T extends IMockMakerSettings> T getMockMakerSettings() {
    return uncheckedCast(mockMakerSettings);
  }
}
