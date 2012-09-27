/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.mock;

import org.spockframework.util.Beta;

/**
 * The <em>nature</em> of a mock object determines its responsibilities and characteristics.
 * It is chosen at mock creation time, typically by selecting the appropriate {@link spock.lang.MockingApi} factory method.
 */
@Beta
public enum MockNature {
  /**
   * The most generic mock object nature. Can be used both for stubbing (i.e.
   * responding to method calls) and mocking (i.e. demanding certain method calls).
   * Returns {@code 0}, {@code false}, or {@code null} for unanticipated method calls,
   * depending on the method's return type.
   */
  MOCK(true, true, ZeroOrNullResponse.INSTANCE),

  /**
   * A mock object whose purpose is to respond to method calls, without ever demanding them.
   * Returns "empty" values (zero, empty string, empty collection, object created from default constructor, etc.)
   * for unanticipated method calls.
   */
  STUB(false, true, EmptyOrDummyResponse.INSTANCE),

  /**
   * A mock object that sits atop a real object of the same type. Method calls that aren't stubbed
   * or mocked explicitly are delegated to the underlying object.
   */
  SPY(true, false, CallRealMethodResponse.INSTANCE);

  private final boolean verified;
  private final boolean useObjenesis;
  private final IDefaultResponse defaultResponse;

  MockNature(boolean verified, boolean useObjenesis, IDefaultResponse defaultResponse) {
    this.verified = verified;
    this.useObjenesis = useObjenesis;
    this.defaultResponse = defaultResponse;
  }

  /**
   * Tells whether mock objects of this nature can verify method calls.
   *
   * @return whether mock objects of this nature can verify method calls
   */
  public boolean isVerified() {
    return verified;
  }

  /**
   * Tells whether the Objenesis library (if present) should be used to instantiate mock objects of this nature.
   *
   * @return whether the Objenesis library (if present) should be used to instantiate mock objects of this nature
   */
  public boolean isUseObjenesis() {
    return useObjenesis;
  }

  /**
   * Returns the default response strategy used by mocks of this nature.
   *
   * @return the default response strategy used by mocks of this nature
   */
  public IDefaultResponse getDefaultResponse() {
    return defaultResponse;
  }
}
