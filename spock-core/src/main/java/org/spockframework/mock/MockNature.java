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
 * The <em>nature</em> of a mock object determines its primary responsibility and characteristics.
 * It is chosen at construction time, typically by choosing the appropriate {@link spock.lang.MockingApi} factory method.
 */
@Beta
public enum MockNature {
  /**
   * A mock object whose specific and only purpose is stubbing (i.e. reacting to method calls).
   * Returns "empty" values (zero, empty string, empty collection, object created from default constructor, etc.)
   * for unanticipated method calls, or {@code null} if an empty value cannot be constructed.
   */
  STUB(false, true, EmptyOrDummyResponse.INSTANCE),

  /**
   * The most generic kind of mock object. Can be used both for stubbing (i.e.
   * reacting to method calls) and mocking (i.e. verifying that certain method calls occur).
   * Returns {@code 0}, {@code false}, or {@code null} for unanticipated method calls,
   * depending on the method's return type.
   */
  MOCK(true, true, ZeroOrNullResponse.INSTANCE),

  /**
   * A mock object that selectively stubs and/or mocks a real object. Can act as a partial mock.
   * Calls through to the real object for unanticipated method calls.
   */
  SPY(true, false, CallRealMethodResponse.INSTANCE);

  private final boolean verified;
  private final boolean useObjenesis;

  private final IMockResponse responder;

  MockNature(boolean verified, boolean useObjenesis, IMockResponse responder) {
    this.verified = verified;
    this.useObjenesis = useObjenesis;
    this.responder = responder;
  }

  boolean isVerified() {
    return verified;
  }

  boolean isUseObjenesis() {
    return useObjenesis;
  }

  IMockResponse getResponder() {
    return responder;
  }
}
