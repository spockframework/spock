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
package spock.mock

import spock.lang.Specification
import org.spockframework.mock.MockNature
import org.spockframework.mock.MockImplementation
import org.spockframework.mock.MockConfiguration

import org.spockframework.mock.IMockInvocationResponder
import org.spockframework.mock.ZeroOrNullResponder
import org.spockframework.mock.CallRealMethodResponder
import org.spockframework.mock.EmptyOrDummyResponder

class MockConfigurationSpec extends Specification {
  def "defaults for Mock nature"() {
    def options = new MockConfiguration("foo", List, MockNature.MOCK, MockImplementation.JAVA, [:])

    expect:
    with(options) {
      nature == MockNature.MOCK
      responder instanceof ZeroOrNullResponder
      global == false
      verified == true
      useObjenesis == true
    }
  }

  def "defaults for Stub nature"() {
    def options = new MockConfiguration("foo", List, MockNature.STUB, MockImplementation.JAVA, [:])

    expect:
    with(options) {
      nature == MockNature.STUB
      responder instanceof EmptyOrDummyResponder
      global == false
      verified == false
      useObjenesis == true
    }
  }

  def "defaults for Spy nature"() {
    def options = new MockConfiguration("foo", List, MockNature.SPY, MockImplementation.JAVA, [:])

    expect:
    with(options) {
      nature == MockNature.SPY
      responder instanceof CallRealMethodResponder
      global == false
      verified == true
      useObjenesis == false
    }
  }

  def "set options via map"() {
    def responder = [:] as IMockInvocationResponder
    def map = [name: "foo", type: List, nature: MockNature.SPY, implementation: MockImplementation.GROOVY,
      constructorArgs: ["foo", "bar"], responder: responder, global: true, verified: false, useObjenesis: true]

    def options = new MockConfiguration("bar", Map, MockNature.MOCK, MockImplementation.JAVA, map)

    expect:
    with(options) {
      name == "foo"
      type == List
      nature == MockNature.SPY
      implementation == MockImplementation.GROOVY
      constructorArgs == ["foo", "bar"]
      responder == responder
      global == true
      verified == false
      useObjenesis == true
    }
  }
}
