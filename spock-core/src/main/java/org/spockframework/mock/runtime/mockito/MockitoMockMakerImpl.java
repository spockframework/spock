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

import org.mockito.MockMakers;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.MockitoFramework;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.InvocationContainer;
import org.mockito.invocation.MockHandler;
import org.mockito.mock.MockCreationSettings;
import org.mockito.plugins.MockMaker;
import org.spockframework.mock.CannotCreateMockException;
import org.spockframework.mock.IMockObject;
import org.spockframework.mock.ISpockMockObject;
import org.spockframework.mock.MockNature;
import org.spockframework.mock.runtime.IMockMaker;
import org.spockframework.mock.runtime.IProxyBasedMockInterceptor;
import org.spockframework.runtime.GroovyRuntimeUtil;
import org.spockframework.util.ExceptionUtil;
import org.spockframework.util.ObjectUtil;
import org.spockframework.util.ReflectionUtil;
import org.spockframework.util.ThreadSafe;

import java.lang.reflect.Method;

@ThreadSafe
class MockitoMockMakerImpl {
  private static final Class<?>[] CLASS_ARRAY = new Class[0];

  private final MockMaker inlineMockMaker;
  private final Method spockMockMethod;

  MockitoMockMakerImpl() {
    MockitoFramework framework = Mockito.framework();
    this.inlineMockMaker = framework.getPlugins().getInlineMockMaker();
    this.spockMockMethod = ReflectionUtil.getMethodByName(ISpockMockObject.class, "$spock_get");
  }

  IMockObject asMockOrNull(Object object) {
    MockHandler<?> mockHandler = inlineMockMaker.getHandler(object);
    if (mockHandler instanceof SpockMockHandler) {
      SpockMockHandler spockHandler = (SpockMockHandler) mockHandler;
      //This should be changed to a better API to retrieve the IMockObject, which is currently implemented in two places JavaMockInterceptor and GroovyMockInterceptor
      return (IMockObject) spockHandler.mockInterceptor.intercept(object, spockMockMethod, null, null);
    }
    return null;
  }

  Object makeMock(IMockMaker.IMockCreationSettings settings) throws CannotCreateMockException {
    try {
      MockSettings mockitoSettings = Mockito.withSettings();
      mockitoSettings.mockMaker(MockMakers.INLINE);
      if (!settings.getAdditionalInterface().isEmpty()) {
        mockitoSettings.extraInterfaces(settings.getAdditionalInterface().toArray(CLASS_ARRAY));
      }
      if (settings.getConstructorArgs() != null) {
        mockitoSettings.useConstructor(settings.getConstructorArgs().toArray(GroovyRuntimeUtil.EMPTY_ARGUMENTS));
      } else if (settings.getMockNature() == MockNature.SPY) {
        //We need to say Mockito it shall use the constructor otherwise it will not initialize fields of the spy, see org.mockito.Mockito.spy(java.lang.Class<T>), which does the same
        mockitoSettings.useConstructor();
      }

      //We do not need the verification logic of Mockito
      mockitoSettings.stubOnly();

      applyMockMakerSettingsFromUser(settings, mockitoSettings);

      MockCreationSettings<Object> mockitoCreationSettings = ObjectUtil.uncheckedCast(mockitoSettings.build(settings.getMockType()));
      SpockMockHandler handler = new SpockMockHandler(settings.getMockInterceptor());

      return inlineMockMaker.createMock(mockitoCreationSettings, handler);
    } catch (MockitoException ex) {
      throw new CannotCreateMockException(settings.getMockType(), " with " + MockitoMockMaker.ID + ": " + ex.getMessage().trim(), ex);
    }
  }

  private static void applyMockMakerSettingsFromUser(IMockMaker.IMockCreationSettings settings, MockSettings mockitoSettings) {
    IMockMaker.IMockMakerSettings mockMakerSettings = settings.getMockMakerSettings();
    if (mockMakerSettings instanceof MockitoMockMakerSettings) {
      MockitoMockMakerSettings mockitoMakerSettings = (MockitoMockMakerSettings) mockMakerSettings;
      mockitoMakerSettings.applySettings(mockitoSettings);
    }
  }

  public IMockMaker.IMockabilityResult isMockable(Class<?> mockType) {
    MockMaker.TypeMockability result = inlineMockMaker.isTypeMockable(mockType);
    if (result.mockable()) {
      return IMockMaker.IMockabilityResult.MOCKABLE;
    }
    return result::nonMockableReason;
  }

  static class SpockMockHandler implements MockHandler<Object> {
    private final IProxyBasedMockInterceptor mockInterceptor;

    public SpockMockHandler(IProxyBasedMockInterceptor mockInterceptor) {
      this.mockInterceptor = mockInterceptor;
    }

    @Override
    public Object handle(Invocation invocation) {
      Object mock = invocation.getMock();
      return mockInterceptor.intercept(mock, invocation.getMethod(), invocation.getArguments(), spockInvocation -> {
        try {
          return invocation.callRealMethod();
        } catch (Throwable e) {
          return ExceptionUtil.sneakyThrow(e);
        }
      });
    }

    @Override
    public MockCreationSettings<Object> getMockSettings() {
      throw new UnsupportedOperationException();
    }

    @Override
    public InvocationContainer getInvocationContainer() {
      throw new UnsupportedOperationException();
    }
  }
}
