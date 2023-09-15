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

package org.spockframework.docs.extension;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.spockframework.mock.runtime.IMockMaker;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

// tag::fancy-mock-maker[]
public final class FancyMockMaker implements IMockMaker {
  static final MockMakerId ID = new MockMakerId("fancy");

  private static final Set<MockMakerCapability> CAPABILITIES = EnumSet.of(
    MockMakerCapability.CLASS,
    MockMakerCapability.EXPLICIT_CONSTRUCTOR_ARGUMENTS);

  public FancyMockMaker() {
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
    return 5000;
  }

  @Override
  public Object makeMock(IMockCreationSettings settings) {
    FancyMockMakerSettings fancySettings = settings.getMockMakerSettings();
    return makeMockInternal(fancySettings);
  }

  @Override
  public IMockabilityResult getMockability(IMockCreationSettings settings) {
    FancyMockMakerSettings fancySettings = settings.getMockMakerSettings();
    if (fancySettings != null &&
      fancySettings.isSerialization() &&
      !fancySettings.getFancyTypes().isEmpty()) {
      return () -> "Mock with serialization and fancy types is not supported.";
    }
    return IMockabilityResult.MOCKABLE;
  }

  Object makeMockInternal(FancyMockMakerSettings settings) {
    //Implementation omitted ...
    return null;
  }
}

final class FancyMockMakerSettings implements IMockMaker.IMockMakerSettings {
  private boolean serialization;
  private final List<Type> specialTypes = new ArrayList<>();

  FancyMockMakerSettings() {
  }

  boolean isSerialization() {
    return serialization;
  }

  List<Type> getFancyTypes() {
    return specialTypes;
  }

  @Override
  public IMockMaker.MockMakerId getMockMakerId() {
    return FancyMockMaker.ID;
  }

  public void withSerialization() {
    serialization = true;
  }

  public void fancyTypes(Type... types) {
    this.specialTypes.addAll(Arrays.asList(types));
  }
}

final class FancyMockMakers {
  /**
   * Public static entry point for the User.
   */
  public static IMockMaker.IMockMakerSettings fancyMock(
    @DelegatesTo(FancyMockMakerSettings.class)
    Closure<?> code) {
    FancyMockMakerSettings settings = new FancyMockMakerSettings();
    code.setDelegate(settings);
    code.call();
    return settings;
  }
}
// end::fancy-mock-maker[]
