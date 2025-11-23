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

import groovy.lang.Closure;
import org.spockframework.mock.CannotCreateMockException;
import org.spockframework.mock.IMockObject;
import org.spockframework.mock.ISpockMockObject;
import org.spockframework.mock.runtime.IMockMaker.IStaticMock;
import org.spockframework.mock.runtime.IMockMaker.MockMakerCapability;
import org.spockframework.runtime.GroovyRuntimeUtil;
import org.spockframework.util.InternalSpockError;
import org.spockframework.util.Nullable;
import org.spockframework.util.ThreadSafe;
import spock.mock.IMockMakerSettings;
import spock.mock.MockMakerId;

import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

import static org.spockframework.mock.runtime.IMockMaker.IMockCreationSettings;
import static org.spockframework.mock.runtime.IMockMaker.IMockabilityResult;

@ThreadSafe
public final class MockMakerRegistry {
  private static final String NL = "\n";
  private static final int PREFERRED_MOCK_MAKER_PRIORITY = 0;

  private final Map<MockMakerId, IMockMaker> makerMap = new HashMap<>();

  private final List<IMockMaker> makerList = new ArrayList<>();

  public static MockMakerRegistry createFromServiceLoader(MockMakerConfiguration configuration) {
    ServiceLoader<IMockMaker> mockMakers = ServiceLoader.load(IMockMaker.class);
    return new MockMakerRegistry(mockMakers, configuration.preferredMockMaker);
  }

  MockMakerRegistry(Iterable<IMockMaker> mockMakers, @Nullable IMockMakerSettings preferredMockMakerParam) {
    mockMakers.forEach(m -> {
      validateMockMakerId(m);
      if (m.getCapabilities() == null || m.getCapabilities().isEmpty()) {
        throw new IllegalStateException("The IMockMaker '" + m + "' does not define any capability.");
      }
      IMockMaker old = makerMap.put(m.getId(), m);
      if (old != null) {
        throw new IllegalStateException("Duplicated IMockMaker instances with the same ID found: " + m.getId());
      }
      makerList.add(m);
    });
    if (makerList.isEmpty()) {
      throw new InternalSpockError("No IMockMaker implementations found.");
    }
    MockMakerId preferredMockMakerId;
    if (preferredMockMakerParam != null) {
      preferredMockMakerId = preferredMockMakerParam.getMockMakerId();
      if (makerMap.get(preferredMockMakerId) == null) {
        throw new IllegalStateException("No IMockMaker with ID '" + preferredMockMakerId + "' exists, but was request via mockMaker.preferredMockMaker configuration. Is a runtime dependency missing?");
      }
    } else {
      preferredMockMakerId = null;
    }
    //Sort by priority and if that is the same by ID
    makerList.sort(Comparator.comparing((IMockMaker maker) -> {
        if (Objects.equals(maker.getId(), preferredMockMakerId)) {
          return PREFERRED_MOCK_MAKER_PRIORITY;
        }
        return maker.getPriority();
      })
      .thenComparing(IMockMaker::getId));
  }

  private static void validateMockMakerId(IMockMaker mockMaker) {
    MockMakerId id = mockMaker.getId();
    if (id == null) {
      throw new IllegalStateException("The IMockMaker '" + mockMaker.getClass().getName() + "' has an invalid ID. The ID must not be null.");
    }
    int priority = mockMaker.getPriority();
    if (priority < 0) {
      throw new IllegalStateException("The IMockMaker '" + id + "' has an invalid priority " + priority + ". Priorities < 0 are invalid.");
    }
    if (priority == PREFERRED_MOCK_MAKER_PRIORITY) {
      throw new IllegalStateException("The IMockMaker '" + id + "' has an invalid priority " + priority + ". The priority " + priority + " is reserved.");
    }
  }

  public List<IMockMaker> getMakerList() {
    return Collections.unmodifiableList(makerList);
  }

  /**
   * Mocks the type provided by the {@code settings}.
   *
   * @param settings the mock settings
   * @return the new instance of the mocked type defined by the settings
   * @throws CannotCreateMockException if a mock with the provided settings can't be created.
   */
  public Object makeMock(IMockCreationSettings settings) throws CannotCreateMockException {
    if (settings.isStaticMock()) {
      throw new IllegalArgumentException("Can't mock static mocks with makeMock().");
    }
    return makeMockInternal(settings, IMockMaker::makeMock);
  }

  /**
   * Mocks the static methods of the type provided by the {@code settings}.
   *
   * @param settings the mock settings
   * @return the {@link IStaticMock} object representing the static mocked class
   * @throws CannotCreateMockException if a mock with the provided settings can't be created.
   */
  public IStaticMock makeStaticMock(IMockCreationSettings settings) throws CannotCreateMockException {
    if (!settings.isStaticMock()) {
      throw new IllegalArgumentException("Can't mock instance mocks with makeStaticMock().");
    }
    return makeMockInternal(settings, IMockMaker::makeStaticMock);
  }

  public <T> T makeMockInternal(IMockCreationSettings settings, BiFunction<IMockMaker, IMockCreationSettings, T> code) throws CannotCreateMockException {
    IMockMakerSettings mockMakerSettings = settings.getMockMakerSettings();
    if (mockMakerSettings != null) {
      MockMakerId mockMakerId = getMockMakerId(settings, mockMakerSettings);
      IMockMaker mockMaker = makerMap.get(mockMakerId);
      if (mockMaker == null) {
        checkForStaticMockUsageWithClosure(settings, mockMakerSettings, mockMakerId, null);
        throw new CannotCreateMockException(settings.getMockType(), " because MockMaker with ID '" + mockMakerId + "' does not exist.");
      }
      verifyIsMockable(mockMaker, settings);
      return code.apply(mockMaker, settings);
    }
    return createWithAppropriateMockMaker(settings, code);
  }

  private static MockMakerId getMockMakerId(IMockCreationSettings settings, IMockMakerSettings mockMakerSettings) {
    try {
      return mockMakerSettings.getMockMakerId();
    } catch (ClassCastException ex) {
      checkForStaticMockUsageWithClosure(settings, mockMakerSettings, null, ex);
      throw ex;
    }
  }

  private static void checkForStaticMockUsageWithClosure(IMockCreationSettings settings, IMockMakerSettings mockMakerSettings, @Nullable MockMakerId mockMakerId, @Nullable Throwable cause) {
    if (settings.isStaticMock() && (mockMakerSettings instanceof Proxy || mockMakerSettings instanceof Closure)) {
      String nature = settings.getMockNature().toString();
      throw new CannotCreateMockException(settings.getMockType(), " because the MockMakerSettings returned the invalid ID '" + mockMakerId + "'."
        + "\nThe syntax " + nature + "Static(" + settings.getMockType().getSimpleName() + "){} is not supported, please use " + nature + "Static(" + settings.getMockType().getSimpleName() + ") without a Closure instead."
        , cause
      );
    }
  }

  /**
   * Returns information about a mock object, or {@code null} if the object is no mock.
   *
   * @param object a mock object
   * @return information about the mock object
   */
  public IMockObject asMockOrNull(Object object) {
    if (object instanceof ISpockMockObject) {
      return ((ISpockMockObject) object).$spock_get();
    }
    for (IMockMaker mockMaker : makerList) {
      IMockObject mock = mockMaker.asMockOrNull(object);
      if (mock != null) {
        return mock;
      }
    }
    return null;
  }

  /**
   * Returns {@code true} if the passed class is a Spock static mock currently active on the current {@link Thread}.
   *
   * @param clazz the class to check
   * @return {@code true} if this class is a Spock static mock currently active on the current {@code Thread}
   */
  public boolean isStaticMock(Class<?> clazz) {
    requireNonNull(clazz);
    for (IMockMaker mockMaker : makerList) {
      if (mockMaker.isStaticMock(clazz)) {
        return true;
      }
    }
    return false;
  }

  private <T> T createWithAppropriateMockMaker(IMockCreationSettings settings, BiFunction<IMockMaker, IMockCreationSettings, T> code) {
    for (IMockMaker mockMaker : makerList) {
      if (isMockable(mockMaker, settings).isMockable()) {
        return code.apply(mockMaker, settings);
      }
    }
    IMockabilityResult typeMockable = collectNotMockableReasons(settings);
    throw new CannotCreateMockException(settings.getMockType(), "." + NL + typeMockable.getNotMockableReason());
  }

  private void verifyIsMockable(IMockMaker mockMaker, IMockCreationSettings settings) {
    IMockabilityResult result = isMockable(mockMaker, settings);
    if (!result.isMockable()) {
      throw new CannotCreateMockException(settings.getMockType(), ". " + mockMaker.getId() + ": " + result.getNotMockableReason());
    }
  }

  private IMockabilityResult collectNotMockableReasons(IMockCreationSettings settings) {
    return () -> makerList.stream()
      .map(m -> {
        IMockabilityResult result = isMockable(m, settings);
        if (!result.isMockable()) {
          return m.getId() + ": " + result.getNotMockableReason();
        }
        return null;
      })
      .filter(Objects::nonNull)
      .collect(Collectors.joining(NL));
  }

  private IMockabilityResult isMockable(IMockMaker mockMaker, IMockCreationSettings settings) {
    Set<MockMakerCapability> capabilities = mockMaker.getCapabilities();
    Class<?> mockType = settings.getMockType();
    if (mockType.isInterface() && !capabilities.contains(MockMakerCapability.INTERFACE)) {
      return MockMakerCapability.INTERFACE.notMockable();
    }
    if (!mockType.isInterface() && !capabilities.contains(MockMakerCapability.CLASS)) {
      return MockMakerCapability.CLASS.notMockable();
    }
    if (!settings.getAdditionalInterface().isEmpty() && !capabilities.contains(MockMakerCapability.ADDITIONAL_INTERFACES)) {
      return MockMakerCapability.ADDITIONAL_INTERFACES.notMockable();
    }
    if (settings.getConstructorArgs() != null && !capabilities.contains(MockMakerCapability.EXPLICIT_CONSTRUCTOR_ARGUMENTS)) {
      return MockMakerCapability.EXPLICIT_CONSTRUCTOR_ARGUMENTS.notMockable();
    }
    if (Modifier.isFinal(mockType.getModifiers()) && !capabilities.contains(MockMakerCapability.FINAL_CLASS)) {
      return MockMakerCapability.FINAL_CLASS.notMockable();
    }
    //Note MockMakerCapability.FINAL_METHOD is not testable here.
    if (settings.isStaticMock() && !capabilities.contains(MockMakerCapability.STATIC_METHOD)) {
      return MockMakerCapability.STATIC_METHOD.notMockable();
    }
    return mockMaker.getMockability(settings);
  }
}
