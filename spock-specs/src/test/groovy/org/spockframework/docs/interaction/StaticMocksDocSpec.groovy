/*
 * Copyright 2024 the original author or authors.
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

package org.spockframework.docs.interaction


import spock.lang.Specification

import java.util.concurrent.Executors

class StaticMocksDocSpec extends Specification {

  def "StaticMock"() {
    // tag::mock-static1[]
    given:
    MockStatic(StaticClass)

    expect: "By default MockStatic() will return null"
    StaticClass.staticMethod() == null

    when:
    def result = StaticClass.staticMethod()

    then:
    1 * StaticClass.staticMethod() >> "MockValue"
    result == "MockValue"
    // end::mock-static1[]
  }

  def "StaticMock with answers"() {
    // tag::mock-static-answers[]
    given:
    MockStatic(StaticClass) {
      staticMethod() >> "MockValue"
    }

    expect:
    StaticClass.staticMethod() == "MockValue"
    // end::mock-static-answers[]
  }

  def "StaticMock with interactions"() {
    // tag::mock-static-interactions[]
    given:
    MockStatic(StaticClass)
    StaticClass.staticMethod() >> "MockValue"

    expect:
    StaticClass.staticMethod() == "MockValue"
    // end::mock-static-interactions[]
  }

  def "SpyStatic"() {
    // tag::spy-static[]
    given:
    SpyStatic(StaticClass)

    expect:
    StaticClass.staticMethod() == "RealValue"

    when:
    def result = StaticClass.staticMethod()

    then:
    1 * StaticClass.staticMethod() >> "MockValue"
    result == "MockValue"
    // end::spy-static[]
  }

  def "StaticMock in a different thread"() {
    // tag::mock-static-different-thread[]
    given:
    MockStatic(StaticClass) {
      staticMethod() >> "MockValue"
    }

    when:
    def executor = Executors.newSingleThreadExecutor()
    def wasCalled = false
    def future = executor.submit {
      assert StaticClass.staticMethod() == "RealValue"

      withActiveThreadAwareMocks {
        //Now the static methods of StaticClass are mocked:
        assert StaticClass.staticMethod() == "MockValue"
        wasCalled = true
      }

      assert StaticClass.staticMethod() == "RealValue"
    }
    future.get()

    then:
    StaticClass.staticMethod() == "MockValue"
    wasCalled

    cleanup:
    executor.shutdown()
    // end::mock-static-different-thread[]
  }

// tag::mock-static-class[]
  private static class StaticClass {
    static String staticMethod() {
      return "RealValue"
    }
  }
// end::mock-static-class[]
}
