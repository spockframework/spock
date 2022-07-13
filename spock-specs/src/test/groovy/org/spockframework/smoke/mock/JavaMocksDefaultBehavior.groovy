/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.smoke.mock

import spock.lang.*

class JavaMocksDefaultBehavior extends Specification {
  def "by default, a mock is only equal to itself"() {
    def mock = Mock(Foo)
    def anotherMockOfSameType = Mock(Foo)
    def mockOfDifferentType = Mock(Bar)

    expect:
    mock == mock
    mock != anotherMockOfSameType
    mock != mockOfDifferentType

    and:
    mock.equals(mock)
    !mock.equals(anotherMockOfSameType)
    !mock.equals(mockOfDifferentType)
  }

  def "by default, a mock returns System.identityHashCode() for its hash code"() {
    def mock = Mock(Foo)

    expect:
    mock.hashCode() == System.identityHashCode(mock)
  }

  def "default toString() output for named mock"() {
    def myMock = Mock(Foo)

    expect:
    myMock.toString() == "Mock for type 'Foo' named 'myMock'"
  }

  def "default toString() output for unnamed mock"() {
    expect:
    Mock(Foo).toString() == "Mock for type 'Foo'"
  }

  def "default equals() behavior can be overridden"() {
    def foo = Mock(Foo)
    def bar = Mock(Bar)

    foo.equals(_) >> { it[0].is(bar) }
    bar.equals(_) >> false

    expect:
    !foo.equals(foo)
    foo.equals(bar)
    !bar.equals(bar)
  }

  def "default hashCode() behavior can be overridden"() {
    def foo = Mock(Foo)
    def bar = Mock(Bar)

    _.hashCode() >> 42

    expect:
    foo.hashCode() == 42
    bar.hashCode() == 42
  }

  def "default toString() behavior can be overridden"() {
    def foo = Mock(Foo)

    foo.toString() >> "mock around the clock"

    expect:
    foo.toString() == "mock around the clock"
  }

  interface Foo {}

  interface Bar {}
}


