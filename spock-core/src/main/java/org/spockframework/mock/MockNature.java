/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.mock;

import spock.mock.MockingApi;

/**
 * A named set of defaults for a mock's configuration options. A mock nature is chosen at
 * mock creation time, typically by selecting the appropriate {@link MockingApi} factory method.
 */
public enum MockNature {
  /**
   * A mock object whose method calls are verified, which instantiates class-based mock objects with Objenesis,
   * and whose strategy for responding to unexpected method calls is {@link ZeroOrNullResponse}.
   */
  MOCK(true, true, ZeroOrNullResponse.INSTANCE, "Mock"),

  /**
   * A mock object whose method calls are not verified, which instantiates class-based mock objects with Objenesis,
   * and whose strategy for responding to unexpected method calls is {@link EmptyOrDummyResponse}.
   */
  STUB(false, true, EmptyOrDummyResponse.INSTANCE, "Stub"),

  /**
   * A mock object whose method calls are verified, which instantiates class-based mock objects by calling a
   * real constructor, and whose strategy for responding to unexpected method calls is {@link CallRealMethodResponse}.
   */
  SPY(true, false, CallRealMethodResponse.INSTANCE, "Spy");

  private final boolean verified;
  private final boolean useObjenesis;
  private final IDefaultResponse defaultResponse;
  private final String methodName;

  MockNature(boolean verified, boolean useObjenesis, IDefaultResponse defaultResponse, String methodName) {
    this.verified = verified;
    this.useObjenesis = useObjenesis;
    this.defaultResponse = defaultResponse;
    this.methodName = methodName;
  }

  /**
   * Tells whether method calls should be verified.
   *
   * @return whether method calls should be verified
   */
  public boolean isVerified() {
    return verified;
  }

  /**
   * Tells whether class-based mock objects should be instantiated with the Objenesis library (if available),
   * or by calling a real constructor.
   *
   * @return whether class-based mock objects should be instantiated with the Objenesis library (if available),
   * or by calling a real constructor
   */
  public boolean isUseObjenesis() {
    return useObjenesis;
  }

  /**
   * Returns the strategy for responding to unexpected method calls.
   *
   * @return the strategy for responding to unexpected method calls
   */
  public IDefaultResponse getDefaultResponse() {
    return defaultResponse;
  }

  @Override
  public String toString() {
    return methodName;
  }
}
