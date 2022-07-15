/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.mock.runtime

import org.spockframework.mock.MockNature
import org.spockframework.mock.MockImplementation
import org.spockframework.mock.InterfaceWithNestedClass

import spock.lang.Specification
import spock.lang.Issue

class JavaMockFactorySpec extends Specification {
  JavaMockFactory factory = new JavaMockFactory()

  def "can create mocks for interfaces"() {
    expect:
    factory.create(new MockConfiguration("foo", List, MockNature.MOCK, MockImplementation.JAVA, [:]), this) instanceof List
  }

  @Issue("https://github.com/spockframework/spock/issues/349")
  def "can create mocks for interfaces containing nested classes"() {
    expect:
    factory.create(new MockConfiguration("foo", InterfaceWithNestedClass, MockNature.MOCK, MockImplementation.JAVA, [:]), this) instanceof InterfaceWithNestedClass
  }

  def "can create mocks for classes w/ parameterless constructor"() {
    expect:
    factory.create(new MockConfiguration("foo", ArrayList, MockNature.MOCK, MockImplementation.JAVA, [:]), this) instanceof ArrayList
  }

  def "can create mocks for classes wo/ parameterless constructor"() {
    expect:
    factory.create(new MockConfiguration("foo", Node, MockNature.MOCK, MockImplementation.JAVA, [:]), this) instanceof Node
  }

  def "can create mocks for interfaces defined in Groovy"() {
    expect:
    factory.create(new MockConfiguration("foo", IMockMe, MockNature.MOCK, MockImplementation.JAVA, [:]), this) instanceof IMockMe
  }

  def "can create mocks for classes defined in Groovy"() {
    expect:
    factory.create(new MockConfiguration("foo", MockMe, MockNature.MOCK, MockImplementation.JAVA, [:]), this) instanceof MockMe
  }
}

interface IMockMe {
  def foo(int i)
}

class MockMe implements IMockMe {
  def foo(int i) {}
}
