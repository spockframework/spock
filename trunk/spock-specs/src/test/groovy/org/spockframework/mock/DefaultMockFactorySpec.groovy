/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.mock

import spock.lang.Specification

class DefaultMockFactorySpec extends Specification {
  IMockFactory factory = new DefaultMockFactory()
  IInvocationMatcher dummy = Mock() // useful if test fails and toString() is delegated to dispatcher

  def "can create mocks for interfaces"() {
    expect:
    factory.create("list", List, dummy) instanceof List
  }

  def "can create mocks for classes w/ parameterless constructor"() {
    expect:
    factory.create("list", ArrayList, dummy) instanceof ArrayList
  }

  def "can create mocks for classes wo/ parameterless constructor"() {
    expect:
    factory.create("node", Node, dummy) instanceof Node
  }

  def "can create mocks for interfaces defined in Groovy"() {
    expect:
    factory.create("mockMe", IMockMe, dummy) instanceof IMockMe
  }

  def "can create mocks for classes defined in Groovy"() {
    expect:
    factory.create("mockMe", MockMe, dummy) instanceof MockMe
  }
}

interface IMockMe {
  def foo(int i)
}

class MockMe implements IMockMe {
  def foo(int i) {}
}