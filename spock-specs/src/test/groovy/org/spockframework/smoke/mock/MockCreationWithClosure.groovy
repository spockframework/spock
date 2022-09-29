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

package org.spockframework.smoke.mock

import org.spockframework.mock.TooFewInvocationsError

import spock.lang.Specification
import spock.lang.FailsWith

class MockCreationWithClosure extends Specification {
  def "stub method"() {
    def list = Mock(List) {
      size() >> 42
    }

    expect:
    list.size() == 42
  }

  @FailsWith(TooFewInvocationsError)
  def "mock method"() {
    List list = Mock {
      2 * size() >> 42
    }

    expect:
    list.size() == 42
  }

  def "stub property"() {
    List list = Mock(name: "myList") {
      _.empty >> false
    }

    expect:
    !list.empty
  }

  @FailsWith(TooFewInvocationsError)
  def "mock property"() {
    def list = Mock(List) {
      2 * _.empty >> false
    }

    expect:
    !list.empty
  }

  def "mock is declared in interaction"() {
    def list = Mock(List)
    list.get(_) >> { int index ->
      Mock(Map) {
        containsKey(_) >> { index % 2 == 0 }
      }
    }

    expect:
    list.get(42).containsKey("foo")
    !list.get(21).containsKey("foo")
  }

  def "nested mock declaration"() {
    def list = Mock(List) {
      get(_) >> { int index ->
        Mock(Map) {
          size() >> index + 1
        }
      }
    }

    expect:
    list.get(21).size() == 22
    list.get(88).size() == 89
    list.get(21).size() == 22
    list.get(88).size() == 89
  }

  def "wildcard is scoped to current mock object"() {
    def list = Mock(List) {
      2 * _ >>> [42, true]
    }
    def map = Mock(Map)

    expect:
    list.size() == 42
    list.add("elem")
    !map.isEmpty() // not matched by wildcard, hence no TooManyInvocationsError
  }
}
