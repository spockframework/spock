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

import org.codehaus.groovy.runtime.InvokerHelper;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.MockitoFramework;
import org.mockito.creation.instance.InstantiationException;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.util.MockUtil;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.InvocationContainer;
import org.mockito.invocation.MockHandler;
import org.mockito.mock.MockCreationSettings;
import org.mockito.plugins.MockMaker;
import org.mockito.plugins.MockitoPlugins;
import org.spockframework.mock.*;
import org.spockframework.mock.runtime.IMockMaker;
import org.spockframework.mock.runtime.IProxyBasedMockInterceptor;
import org.spockframework.runtime.GroovyRuntimeUtil;
import org.spockframework.util.ExceptionUtil;
import org.spockframework.util.ReflectionUtil;
import org.spockframework.util.ThreadSafe;
import spock.mock.IMockMakerSettings;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static org.mockito.MockMakers.INLINE;
import static org.spockframework.util.ObjectUtil.uncheckedCast;

@ThreadSafe
class MockitoMockMakerImpl {
  private static final Class<?>[] CLASS_ARRAY = new Class[0];

  private final MockMaker inlineMockMaker;
  private final Method spockMockMethod;

  MockitoMockMakerImpl() {
    inlineMockMaker = resolveInlineMockMaker();
    spockMockMethod = ReflectionUtil.getMethodByName(ISpockMockObject.class, "$spock_get");
  }

  /**
   * Resolves the inline mock maker of Mockito, via different ways, depending on the Mockito version.
   *
   * @return The inline mock maker of Mockito
   */
  private MockMaker resolveInlineMockMaker() {
    MockitoFramework framework = Mockito.framework();
    MockitoPlugins plugins = framework.getPlugins();
    try {
      //First: Try to retrieve the inline mockMaker used by Mockito >= 5.6.0 public API, to share the same mocking state.
      MockMaker mockMaker = (MockMaker) InvokerHelper.invokeMethod(plugins, "getMockMaker", INLINE);
      return requireNonNull(mockMaker);

    } catch (Exception ignored) {
      try {
        //Second: Try to retrieve the inline mockMaker used by Mockito < 5.6.0 with private method, to share the same mocking state.
        MockMaker mockMaker = (MockMaker) InvokerHelper.invokeStaticMethod(MockUtil.class, "getMockMaker", INLINE);
        return requireNonNull(mockMaker);

      } catch (Exception ignored2) {
        //Fallback: Use our own InlineMockMaker instance from the Mockito public plugin API, but this will degrade the interoperability with Mockito static mocks.
        //The own InlineMockMaker created here, does not share its state with the Mockito mock maker.
        //It will work for all features, but if a class is mocked with both mock makers, it will yield strange behavior.
        return plugins.getInlineMockMaker();
      }
    }
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

  public boolean isStaticMock(Class<?> clazz) {
    requireNonNull(clazz);
    MockHandler<?> mockHandler = inlineMockMaker.getHandler(clazz);
    return mockHandler instanceof SpockMockHandler;
  }

  Object makeMock(IMockMaker.IMockCreationSettings settings) throws CannotCreateMockException {
    try {
      MockSettings mockitoSettings = Mockito.withSettings();
      mockitoSettings.mockMaker(INLINE);
      if (!settings.getAdditionalInterface().isEmpty()) {
        mockitoSettings.extraInterfaces(settings.getAdditionalInterface().toArray(CLASS_ARRAY));
      }
      if (settings.getConstructorArgs() != null) {
        mockitoSettings.useConstructor(settings.getConstructorArgs().toArray());
      } else if (settings.getMockNature() == MockNature.SPY) {
        //We need to say Mockito it shall use the constructor otherwise it will not initialize fields of the spy, see org.mockito.Mockito.spy(java.lang.Class<T>), which does the same
        mockitoSettings.useConstructor();
      }

      //We do not need the verification logic of Mockito
      mockitoSettings.stubOnly();

      applyMockMakerSettingsFromUser(settings, mockitoSettings);

      MockCreationSettings<?> mockitoCreationSettings = mockitoSettings.build(settings.getMockType());
      SpockMockHandler handler = new SpockMockHandler(settings.getMockInterceptor());

      return inlineMockMaker.createMock(mockitoCreationSettings, handler);
    } catch (MockitoException ex) {
      throw cannotCreateMockException(settings.getMockType(), ex);
    }
  }

  private static void applyMockMakerSettingsFromUser(IMockMaker.IMockCreationSettings settings, MockSettings mockitoSettings) {
    IMockMakerSettings mockMakerSettings = settings.getMockMakerSettings();
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

  public IMockMaker.IStaticMock makeStaticMock(IMockMaker.IMockCreationSettings settings) {
    try {
      MockSettings mockitoSettings = Mockito.withSettings();
      mockitoSettings.mockMaker(INLINE);
      //We do not need the verification logic of Mockito
      mockitoSettings.stubOnly();

      applyMockMakerSettingsFromUser(settings, mockitoSettings);
      Class<?> type = settings.getMockType();
      MockCreationSettings<Object> mockitoCreationSettings = mockitoSettings.build(uncheckedCast(type));
      SpockMockHandler handler = new SpockMockHandler(settings.getMockInterceptor());

      return new MockitoStaticMock(type, () -> inlineMockMaker.createStaticMock(uncheckedCast(type), mockitoCreationSettings, handler));
    } catch (MockitoException ex) {
      throw cannotCreateMockException(settings.getMockType(), ex);
    }
  }

  private static final class MockitoStaticMock implements IMockMaker.IStaticMock {
    private final Class<?> type;
    private final ThreadLocal<MockThreadData> threadData;

    MockitoStaticMock(Class<?> type, Supplier<MockMaker.StaticMockControl<Object>> staticMockControlCreation) {
      requireNonNull(staticMockControlCreation);
      this.type = requireNonNull(type);
      threadData = ThreadLocal.withInitial(() -> new MockThreadData(staticMockControlCreation));
      //Initialize the mock for the calling thread, to ensure that mockito exceptions are thrown during creation.
      threadLocalMockData();
    }

    private MockThreadData threadLocalMockData() {
      try {
        return threadData.get();
      } catch (MockitoException ex) {
        throw cannotCreateMockException(getType(), ex);
      }
    }

    @Override
    public Class<?> getType() {
      return type;
    }

    @Override
    public void enable() {
      threadLocalMockData().enable();
    }

    @Override
    public void disable() {
      if (threadLocalMockData().disable()) {
        threadData.remove();
      }
    }

    private static class MockThreadData {
      private final MockMaker.StaticMockControl<Object> mockControl;
      private int activations;

      MockThreadData(Supplier<MockMaker.StaticMockControl<Object>> staticMockControlCreation) {
        this.mockControl = staticMockControlCreation.get();
      }

      void enable() {
        if (activations == 0) {
          try {
            mockControl.enable();
          } catch (MockitoException ex) {
            throw cannotCreateMockException(mockControl.getType(), ex);
          }
        }
        activations++;
        if (activations < 0) {
          throw new IllegalStateException("Activations overflowed.");
        }
      }

      boolean disable() {
        activations--;
        if (activations < 0) {
          throw new IllegalStateException("disable() called, but there is no activation on the current thread.");
        } else if (activations == 0) {
          try {
            mockControl.disable();
            return true;
          } catch (MockitoException ex) {
            throw cannotCreateMockException(mockControl.getType(), ex);
          }
        }
        return false;
      }
    }
  }

  private static CannotCreateMockException cannotCreateMockException(Class<?> mockType, MockitoException ex) {
    final String mockitoMessage = extractMockitoExceptionMessage(ex);
    return new CannotCreateMockException(mockType, " with " + MockitoMockMaker.ID + ": " + mockitoMessage, ex);
  }

  private static String extractMockitoExceptionMessage(MockitoException ex) {
    Throwable exToUse = ex;
    if (ex.getCause() instanceof InstantiationException) {
      exToUse = ex.getCause();
    }
    return exToUse.getMessage().trim();
  }

  private static final class SpockMockHandler implements MockHandler<Object> {
    private final IProxyBasedMockInterceptor mockInterceptor;

    public SpockMockHandler(IProxyBasedMockInterceptor mockInterceptor) {
      this.mockInterceptor = mockInterceptor;
    }

    @Override
    public Object handle(Invocation invocation) {
      Object mock = invocation.getMock();
      return mockInterceptor.intercept(mock, invocation.getMethod(), invocation.getArguments(), new IResponseGenerator() {
        @Override
        public Supplier<Object> getResponseSupplier(IMockInvocation __) {
          return () -> {
            try {
              return invocation.callRealMethod();
            } catch (Throwable e) {
              return ExceptionUtil.sneakyThrow(e);
            }
          };
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
