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

import groovy.lang.GroovyObject;
import org.spockframework.lang.ISpecificationContext;
import org.spockframework.mock.*;
import org.spockframework.runtime.GroovyRuntimeUtil;
import org.spockframework.runtime.RunContext;
import org.spockframework.util.ReflectionUtil;
import org.spockframework.util.SpockDocLinks;
import spock.lang.Specification;

import java.util.List;

import groovy.lang.MetaClass;

public class JavaMockFactory implements IMockFactory {
  public static final JavaMockFactory INSTANCE = new JavaMockFactory();

  @Override
  public boolean canCreate(IMockConfiguration configuration) {
    return configuration.getImplementation() == MockImplementation.JAVA;
  }

  @Override
  public Object create(IMockConfiguration configuration, Specification specification) {
    return createInternal(configuration, specification, specification.getClass().getClassLoader());
  }

	@Override
  public Object createDetached(IMockConfiguration configuration, ClassLoader classLoader) {
		 return createInternal(configuration, null, classLoader);
	}

  private Object createInternal(IMockConfiguration configuration, Specification specification, ClassLoader classLoader) {
    Class<?> type = configuration.getType();
    checkNotGlobal(configuration);

    MetaClass mockMetaClass = GroovyRuntimeUtil.getMetaClass(type);
    JavaMockInterceptor interceptor = new JavaMockInterceptor(configuration, specification, mockMetaClass);
    Object proxy = getMockMakerRegistry().makeMock(MockCreationSettings.settingsFromMockConfiguration(configuration, interceptor, classLoader));
    List<Class<?>> additionalInterfaces = configuration.getAdditionalInterfaces();
    if (!additionalInterfaces.isEmpty() && GroovyObject.class.isAssignableFrom(type)) {
      //Issue #1405: We need to update the mockMetaClass to reflect the methods of the additional interfaces
      //             The MetaClass of the mock is a bit too much, but we do not have a class representing the hierarchy without the internal Spock interfaces like ISpockMockObject
      interceptor.setMetaClass(GroovyRuntimeUtil.getMetaClass(proxy.getClass()));
    }

    if ((configuration.getNature() == MockNature.SPY) && (configuration.getInstance() != null)) {
      try {
        ReflectionUtil.deepCopyFields(configuration.getInstance(), proxy);
      } catch (Exception e) {
        throw new CannotCreateMockException(type,
          ". Cannot copy fields.\n" + SpockDocLinks.SPY_ON_JAVA_17.getLink(),
          e);
      }
    }
    return proxy;
  }

  private MockMakerRegistry getMockMakerRegistry() {
    return RunContext.get().getMockMakerRegistry();
  }

  private static void checkNotGlobal(IMockConfiguration configuration) {
    if (configuration.isGlobal()) {
      throw new CannotCreateMockException(configuration.getType(),
        " because Java mocks cannot mock globally. If the code under test is written in Groovy, use a Groovy mock.");
    }
  }

  public void createStaticMock(MockConfiguration configuration, Specification specification) {
    checkNotGlobal(configuration);
    MetaClass mockMetaClass = GroovyRuntimeUtil.getMetaClass(configuration.getType());
    IProxyBasedMockInterceptor interceptor = new JavaMockInterceptor(configuration, specification, mockMetaClass);

    MockCreationSettings creationSettings = MockCreationSettings.settingsFromMockConfigurationForStaticMock(configuration, interceptor, specification.getClass().getClassLoader());
    IMockMaker.IStaticMock mockMakerStaticMock = getMockMakerRegistry().makeStaticMock(creationSettings);
    mockMakerStaticMock.enable();
    ISpecificationContext specificationContext = specification.getSpecificationContext();
    specificationContext.getCurrentIteration().addCleanup(mockMakerStaticMock::disable);
    specificationContext.getThreadAwareMockController().registerStaticMock(mockMakerStaticMock);
  }
}
