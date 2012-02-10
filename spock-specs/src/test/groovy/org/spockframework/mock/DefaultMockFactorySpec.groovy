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
import spock.lang.Issue

class DefaultMockFactorySpec extends Specification {
  IMockFactory factory = new DefaultMockFactory()
  IInvocationDispatcher dummy = Mock() // needed if test fails and toString() is delegated to dispatcher

  def "can create mocks for interfaces"() {
    expect:
    factory.create("foo", List, dummy) instanceof List
  }

  @Issue("http://issues.spockframework.org/detail?id=227")
  def "can create mocks for interfaces containing nested classes"() {
    expect:
    factory.create("foo", InterfaceWithNestedClass, dummy) instanceof InterfaceWithNestedClass
  }

  def "can create mocks for classes w/ parameterless constructor"() {
    expect:
    factory.create("foo", ArrayList, dummy) instanceof ArrayList
  }

  def "can create mocks for classes wo/ parameterless constructor"() {
    expect:
    factory.create("foo", Node, dummy) instanceof Node
  }

  def "can create mocks for interfaces defined in Groovy"() {
    expect:
    factory.create("foo", IMockMe, dummy) instanceof IMockMe
  }

  def "can create mocks for classes defined in Groovy"() {
    expect:
    factory.create("foo", MockMe, dummy) instanceof MockMe
  }
}

interface IMockMe {
  def foo(int i)
}

class MockMe implements IMockMe {
  def foo(int i) {}
}