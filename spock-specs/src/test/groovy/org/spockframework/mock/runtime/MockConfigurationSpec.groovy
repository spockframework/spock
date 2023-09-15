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
package org.spockframework.mock.runtime

import org.spockframework.mock.ZeroOrNullResponse
import org.spockframework.mock.EmptyOrDummyResponse
import org.spockframework.mock.CallRealMethodResponse
import org.spockframework.mock.MockNature
import org.spockframework.mock.MockImplementation
import org.spockframework.mock.IDefaultResponse

import spock.lang.Specification
import spock.mock.MockMakers

class MockConfigurationSpec extends Specification {
  def "defaults for Mock nature"() {
    def options = new MockConfiguration("foo", List, MockNature.MOCK, MockImplementation.JAVA, [:])

    expect:
    with(options) {
      nature == MockNature.MOCK
      defaultResponse instanceof ZeroOrNullResponse
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
      defaultResponse instanceof EmptyOrDummyResponse
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
      defaultResponse instanceof CallRealMethodResponse
      global == false
      verified == true
      useObjenesis == false
    }
  }

  def "set options via map"() {
    def defaultResponse = [:] as IDefaultResponse
    def map = [
      name           : "foo",
      type           : List,
      nature         : MockNature.SPY,
      implementation : MockImplementation.GROOVY,
      constructorArgs: ["foo", "bar"],
      defaultResponse: defaultResponse,
      global         : true,
      verified       : false,
      useObjenesis   : true,
      mockMaker      : MockMakers.javaProxy
    ]

    def options = new MockConfiguration("bar", Map, MockNature.MOCK, MockImplementation.JAVA, map)

    expect:
    with(options) {
      name == "foo"
      type == List
      nature == MockNature.SPY
      implementation == MockImplementation.GROOVY
      constructorArgs == ["foo", "bar"]
      defaultResponse == defaultResponse
      global == true
      verified == false
      useObjenesis == true
      mockMaker == MockMakers.javaProxy
    }
  }
}
