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

import org.spockframework.EmbeddedSpecification

abstract class StubBasics extends EmbeddedSpecification {
  abstract List createListDouble()
  abstract ArrayList createArrayListDouble()
  abstract List createAndStubListDouble()

  def "can stub interface"() {
    def list = createListDouble()

    list.get(42) >> "foo"

    expect:
    list.get(42) == "foo"
  }

  def "can stub class"() {
    def list = createArrayListDouble()

    list.get(42) >> "foo"

    expect:
    list.get(42) == "foo"
  }

  def "can stub call at creation time"() {
    def list = createAndStubListDouble()

    expect:
    list.get(42) == "foo"
  }

  def "can stub call beforehand"() {
    def list = createListDouble()

    list.get(42) >> "foo"

    expect:
    list.get(42) == "foo"
  }

  def "can stub call in then-block"() {
    def list = createListDouble()

    when:
    def result = list.get(42)

    then:
    list.get(42) >> "foo"
    result == "foo"
  }
}

class JavaStubBasics extends StubBasics {
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
}

class GroovyStubBasics extends StubBasics {
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
}
