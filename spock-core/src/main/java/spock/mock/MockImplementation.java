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

package spock.mock;

import spock.lang.Experimental;

/**
 * The <em>implementation</em> of a mock object determines how method calls are processed
 * and matched against interactions. It is chosen at construction time, typically by choosing the
 * appropriate {@link spock.lang.MockingApi} factory method.
 */
@Experimental
public enum MockImplementation {
  /**
   * A generic implementation targeting any callers. Used by the {@link spock.lang.MockingApi#Mock},
   * {@link spock.lang.MockingApi#Stub}, and {@link spock.lang.MockingApi#Spy} factory methods.
   */
  JAVA,
  /**
   * An implementation specifically targeting Groovy callers. Supports mocking of dynamic methods,
   * constructors, static methods, and "magic" mocking of all objects of a particular type.
   * Used by the {@link spock.lang.MockingApi#GroovyMock}, {@link spock.lang.MockingApi#GroovyStub},
   * and {@link spock.lang.MockingApi#GroovySpy} factory methods.
   */
  GROOVY
}
