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
 * Determines how method calls are processed and matched against interactions. A mock implementation
 * is chosen at mock creation time, typically by selecting the appropriate {@link MockingApi} factory method.
 */
public enum MockImplementation {
  /**
   * A generic implementation targeting any callers. Used by the {@link MockingApi#Mock},
   * {@link MockingApi#Stub}, and {@link MockingApi#Spy} factory methods.
   */
  JAVA,
  /**
   * An implementation specifically targeting Groovy callers. Supports mocking of dynamic methods,
   * constructors, static methods, and "magic" mocking of all objects of a particular type.
   * Used by the {@link MockingApi#GroovyMock}, {@link MockingApi#GroovyStub},
   * and {@link MockingApi#GroovySpy} factory methods.
   */
  GROOVY
}
