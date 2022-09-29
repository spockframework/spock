/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.smoke.mock

import spock.lang.Issue

abstract class MockBasics extends StubBasics {
  abstract List createAndMockListDouble()
  abstract List createStubAndMockListDouble()

  def "can mock interface"() {
    def list = createListDouble()

    when:
    list.add(1)

    then:
    1 * list.add(1)
  }

  def "can mock class"() {
    def list = createArrayListDouble()

    when:
    list.add(1)

    then:
    1 * list.add(1)
  }

  def "can mock call at creation time"() {
    def list = createAndMockListDouble()

    setup:
    list.get(42)
  }

  def "can stub and mock call at creation time"() {
    def list = createStubAndMockListDouble()

    expect:
    list.get(42) == "foo"
  }

  def "can mock call beforehand"() {
    def list = createListDouble()

    1 * list.get(42)

    setup:
    list.get(42)
  }

  def "can stub and mock call beforehand"() {
    def list = createListDouble()

    1 * list.get(42) >> "foo"

    expect:
    list.get(42) == "foo"
  }

  def "can mock call in then-block"() {
    def list = createListDouble()

    when:
    list.get(42)

    then:
    1 * list.get(42)
  }

  def "can stub and mock call in then-block"() {
    def list = createListDouble()

    when:
    def result = list.get(42)

    then:
    1 * list.get(42) >> "foo"
    result == "foo"
  }

  @Issue("https://github.com/spockframework/spock/issues/1035")
  def "using >>_ returns stubbed value"() {
    def list = createListDouble()

    when:
    def result = list.get(42)
    def resultDefault = list.get(0)

    then:
    1 * list.get(42) >> _
    1 * list.get(0)
    result != null
    resultDefault == null

  }
}

class JavaMockBasics extends MockBasics {
  List createListDouble() {
    Mock(List)
  }

  ArrayList createArrayListDouble() {
    Mock(ArrayList)
  }

  List createAndStubListDouble() {
    Mock(List) {
      get(42) >> "foo"
    }
  }

  List createAndMockListDouble() {
    Mock(List) {
      1 * get(42)
    }
  }

  List createStubAndMockListDouble() {
    Mock(List) {
      1 * get(42) >> "foo"
    }
  }
}

class GroovyMockBasics extends MockBasics {
  List createListDouble() {
    GroovyMock(List)
  }

  ArrayList createArrayListDouble() {
    GroovyMock(ArrayList)
  }

  List createAndStubListDouble() {
    GroovyMock(List) {
      get(42) >> "foo"
    }
  }

  List createAndMockListDouble() {
    GroovyMock(List) {
      1 * get(42)
    }
  }

  List createStubAndMockListDouble() {
    GroovyMock(List) {
      1 * get(42) >> "foo"
    }
  }
}
