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

package org.spockframework.mock.runtime.mockito

import org.mockito.Answers
import org.mockito.Mockito
import org.mockito.exceptions.base.MockitoException
import org.spockframework.mock.CannotCreateMockException
import org.spockframework.mock.IDefaultResponse
import org.spockframework.mock.IMockInvocation
import org.spockframework.mock.MockUtil
import org.spockframework.runtime.InvalidSpecException
import spock.lang.Shared
import spock.lang.Specification
import spock.mock.MockMakers

import java.util.concurrent.Callable
import java.util.concurrent.Executors

@SuppressWarnings(["GroovyAssignabilityCheck", "UnnecessaryQualifiedReference"])
class MockitoStaticMocksSpec extends Specification {
  private static final String REAL_VALUE = "RealValue"
  private static final String MOCK_VALUE = "MockValue"
  private static final String MOCK_VALUE2 = "MockValue2"

  @Shared
  def mockUtil = new MockUtil()

  def cleanup() {
    //Ensure that all static mocks where cleaned up
    assert !mockUtil.isStaticMock(StaticClass)
    assert !mockUtil.isStaticMock(StaticClass2)
  }

  def "Mockito API Test static mock with response"() {
    given:
    def mock = Mockito.mockStatic(StaticClass)
    mock.when {
      StaticClass.staticMethod()
    }.thenReturn(MOCK_VALUE)

    when:
    def result = StaticClass.staticMethod()

    then:
    result == MOCK_VALUE
    mock.verify {
      StaticClass.staticMethod()
    }

    then: "Spock shall not recognize a Mockito static mock as Spock static mock"
    !mockUtil.isStaticMock(StaticClass)

    cleanup:
    mock.close()
  }

  def "Mockito API Test static mock with no response defined"() {
    given:
    def mock = Mockito.mockStatic(StaticClass)

    when:
    def result = StaticClass.staticMethod()

    then:
    result == null
    mock.verify {
      StaticClass.staticMethod()
    }

    cleanup:
    mock.close()
  }

  def "Mockito and Spock API Test static mock on same class Mockito first"() {
    given:
    def mock = Mockito.mockStatic(StaticClass)
    mock.when {
      StaticClass.staticMethod()
    }.thenReturn(MOCK_VALUE)

    when:
    MockStatic(StaticClass) {
      staticMethod() >> MOCK_VALUE2
    }

    then:
    CannotCreateMockException ex = thrown()
    ex.message.contains("static mocking is already registered in the current thread")

    cleanup:
    mock.close()
  }

  def "Mockito and Spock API Test static mock on same class inverse Spock first"() {
    given:
    MockStatic(StaticClass) {
      staticMethod() >> MOCK_VALUE2
    }

    when:
    Mockito.mockStatic(StaticClass)

    then:
    MockitoException ex = thrown()
    ex.message.contains("static mocking is already registered in the current thread")
  }

  def "Mockito and Spock API Test static mock on different classes Mockito first"() {
    given:
    def mock = Mockito.mockStatic(StaticClass)
    mock.when {
      StaticClass.staticMethod()
    }.thenReturn(MOCK_VALUE)

    MockStatic(StaticClass2) {
      staticMethod() >> MOCK_VALUE2
    }

    when:
    def result = StaticClass.staticMethod()

    then:
    result == MOCK_VALUE
    mock.verify {
      StaticClass.staticMethod()
    }

    when:
    result = StaticClass2.staticMethod()

    then:
    result == MOCK_VALUE2

    cleanup:
    mock.close()
  }

  def "Mockito and Spock API Test static mock on different classes Spock first"() {
    given:
    MockStatic(StaticClass2) {
      staticMethod() >> MOCK_VALUE2
    }

    def mock = Mockito.mockStatic(StaticClass)
    mock.when {
      StaticClass.staticMethod()
    }.thenReturn(MOCK_VALUE)

    when:
    def result = StaticClass.staticMethod()

    then:
    result == MOCK_VALUE
    mock.verify {
      StaticClass.staticMethod()
    }

    when:
    result = StaticClass2.staticMethod()

    then:
    result == MOCK_VALUE2

    cleanup:
    mock.close()
  }

  def "Mockito API Test static mock with default response defined"() {
    given:
    def mock = Mockito.mockStatic(StaticClass, Answers.CALLS_REAL_METHODS)

    when:
    def result = StaticClass.staticMethod()

    then:
    result == REAL_VALUE
    mock.verify {
      StaticClass.staticMethod()
    }

    cleanup:
    mock.close()
  }

  def "MockStatic Spock API"() {
    given:
    MockStatic(StaticClass)

    when:
    def result = StaticClass.staticMethod()

    then:
    result == null
    1 * StaticClass.staticMethod()
    mockUtil.isStaticMock(StaticClass)
  }

  def "Spock API mocking same class twice shall fail"() {
    given:
    MockStatic(StaticClass)

    when:
    MockStatic(StaticClass)

    then:
    CannotCreateMockException ex = thrown()
    ex.message.contains("static mocking is already registered in the current thread")
  }

  def "Spock API mocking two different classes"() {
    given:
    MockStatic(StaticClass) {
      staticMethod() >> MOCK_VALUE
    }
    MockStatic(StaticClass2) {
      staticMethod() >> MOCK_VALUE2
    }

    expect:
    StaticClass.staticMethod() == MOCK_VALUE
    StaticClass2.staticMethod() == MOCK_VALUE2
  }

  def "static mock and real instance at the same time"() {
    given:
    MockStatic(StaticClass) {
      staticMethod() >> MOCK_VALUE
    }
    StaticClass instMock = new StaticClass()

    expect:
    StaticClass.staticMethod() == MOCK_VALUE
    instMock.instanceMethod() == REAL_VALUE
  }

  def "static and instance mock at the same time"() {
    given:
    MockStatic(StaticClass) {
      staticMethod() >> MOCK_VALUE
    }
    StaticClass instMock = Mock()

    expect:
    StaticClass.staticMethod() == MOCK_VALUE
    instMock.instanceMethod() == null
  }

  def "static and instance mock at the same time with interactions"() {
    given:
    MockStatic(StaticClass) {
      staticMethod() >> MOCK_VALUE
    }
    StaticClass instMock = Mock() {
      instanceMethod() >> MOCK_VALUE2
    }

    expect:
    StaticClass.staticMethod() == MOCK_VALUE
    instMock.instanceMethod() == MOCK_VALUE2
  }

  def "static and instance mock at the same time with Mockito mock maker"() {
    given:
    MockStatic(StaticClass) {
      staticMethod() >> MOCK_VALUE
    }
    StaticClass instMock = Mock(mockMaker: MockMakers.mockito) {
      instanceMethod() >> MOCK_VALUE2
    }

    expect:
    StaticClass.staticMethod() == MOCK_VALUE
    instMock.instanceMethod() == MOCK_VALUE2
  }

  def "MockStatic interactions in given"() {
    given:
    MockStatic(StaticClass)
    StaticClass.staticMethod() >> MOCK_VALUE

    expect:
    StaticClass.staticMethod() == MOCK_VALUE
  }

  def "MockStatic interactions in when"() {
    given:
    MockStatic(StaticClass)

    when:
    StaticClass.staticMethod() >> MOCK_VALUE
    def result = StaticClass.staticMethod()

    then:
    result == MOCK_VALUE
  }

  def "MockStatic can activate the static mocks on different Thread"() {
    given:
    MockStatic(StaticClass) {
      staticMethod() >> MOCK_VALUE
    }

    when:
    def executor = Executors.newSingleThreadExecutor()
    def future = executor.submit({
      assert StaticClass.staticMethod() == REAL_VALUE
      assert !mockUtil.isStaticMock(StaticClass)
      def callableResult = withActiveThreadAwareMocks {
        assert StaticClass.staticMethod() == MOCK_VALUE
        assert mockUtil.isStaticMock(StaticClass)
        return StaticClass.staticMethod()
      }
      assert StaticClass.staticMethod() == REAL_VALUE
      assert !mockUtil.isStaticMock(StaticClass)
      return callableResult
    } as Callable)
    def result = future.get()

    then:
    StaticClass.staticMethod() == MOCK_VALUE
    result == MOCK_VALUE
    mockUtil.isStaticMock(StaticClass)

    cleanup:
    executor.shutdown()
  }

  def "MockStatic can activate two mocked static classes on different Thread"() {
    given:
    MockStatic(StaticClass) {
      staticMethod() >> MOCK_VALUE
    }
    MockStatic(StaticClass2) {
      staticMethod() >> MOCK_VALUE2
    }

    when:
    def executor = Executors.newSingleThreadExecutor()
    def future = executor.submit({
      assert StaticClass.staticMethod() == REAL_VALUE
      assert StaticClass2.staticMethod() == REAL_VALUE
      def callableResult = withActiveThreadAwareMocks {
        assert StaticClass.staticMethod() == MOCK_VALUE
        assert StaticClass2.staticMethod() == MOCK_VALUE2
        return StaticClass.staticMethod()
      }
      assert StaticClass.staticMethod() == REAL_VALUE
      assert StaticClass2.staticMethod() == REAL_VALUE
      return callableResult
    } as Callable)
    def result = future.get()

    then:
    StaticClass.staticMethod() == MOCK_VALUE
    StaticClass2.staticMethod() == MOCK_VALUE2
    result == MOCK_VALUE

    cleanup:
    executor.shutdown()
  }

  def "Static Mocks can be activated twice on the same thread"() {
    given:
    MockStatic(StaticClass)

    when:
    runWithThreadAwareMocks {
      assert StaticClass.staticMethod() == null
    }

    then:
    StaticClass.staticMethod() == null
  }

  def "No mock"() {
    expect:
    StaticClass.staticMethod() == REAL_VALUE
  }

  def "MockStatic Spock API with response"() {
    given:
    MockStatic(StaticClass)

    when:
    def result = StaticClass.staticMethod()

    then:
    result == MOCK_VALUE
    1 * StaticClass.staticMethod() >> MOCK_VALUE

    when: "After first response it shall fallback to null"
    result = StaticClass.staticMethod()

    then:
    result == null
  }

  def "MockStatic Spock API with closure response"() {
    given:
    MockStatic(StaticClass) {
      staticMethod() >> MOCK_VALUE
      StaticClass.staticMethod2() >> MOCK_VALUE
    }

    expect:
    StaticClass.staticMethod() == MOCK_VALUE
    StaticClass.staticMethod2() == MOCK_VALUE
  }

  def "MockStatic Spock API without response"() {
    given:
    MockStatic(StaticClass)

    when:
    def result = StaticClass.staticMethod()

    then:
    result == null
    1 * StaticClass.staticMethod()
  }

  def "MockStatic with explicit mock-maker"() {
    given:
    MockStatic(mockMaker: MockMakers.mockito, StaticClass)

    when:
    def result = StaticClass.staticMethod()

    then:
    1 * StaticClass.staticMethod() >> MOCK_VALUE
    0 * StaticClass._
    result == MOCK_VALUE
  }

  def "MockStatic with explicit mock-maker with closure response"() {
    given:
    MockStatic(mockMaker: MockMakers.mockito, StaticClass) {
      staticMethod() >> MOCK_VALUE
    }

    when:
    def result = StaticClass.staticMethod()

    then:
    result == MOCK_VALUE
  }

  def "StubStatic Spock API without response"() {
    given:
    StubStatic(StaticClass)

    when:
    def result = StaticClass.staticMethod()

    then:
    result == ""
  }

  def "StubStatic Spock API with closure response"() {
    given:
    StubStatic(StaticClass) {
      staticMethod() >> MOCK_VALUE
    }

    expect:
    StaticClass.staticMethod() == MOCK_VALUE
    StaticClass.staticMethod2() == ""
  }

  def "StubStatic with explicit mock-maker"() {
    given:
    StubStatic(mockMaker: MockMakers.mockito, StaticClass)

    expect:
    StaticClass.staticMethod() == ""
    StaticClass.staticMethod2() == ""
  }

  def "StubStatic with explicit mock-maker with closure response"() {
    given:
    StubStatic(mockMaker: MockMakers.mockito, StaticClass) {
      staticMethod() >> MOCK_VALUE
    }

    when:
    def result = StaticClass.staticMethod()

    then:
    result == MOCK_VALUE
  }

  def "SpyStatic Spock API without response"() {
    given:
    SpyStatic(StaticClass)

    when:
    def result = StaticClass.staticMethod()

    then:
    result == REAL_VALUE
  }

  def "SpyStatic Spock API with closure response"() {
    given:
    SpyStatic(StaticClass) {
      staticMethod() >> MOCK_VALUE
    }

    expect:
    StaticClass.staticMethod() == MOCK_VALUE
    StaticClass.staticMethod2() == REAL_VALUE
  }

  def "SpyStatic Spock API with response"() {
    given:
    SpyStatic(StaticClass)

    when:
    def result = StaticClass.staticMethod()

    then:
    result == MOCK_VALUE
    1 * StaticClass.staticMethod() >> MOCK_VALUE

    when:
    result = StaticClass.staticMethod2()

    then:
    result == REAL_VALUE
  }

  def "SpyStatic with explicit mock-maker"() {
    given:
    SpyStatic(mockMaker: MockMakers.mockito, StaticClass)

    when:
    def result = StaticClass.staticMethod()

    then:
    1 * StaticClass.staticMethod() >> MOCK_VALUE
    0 * StaticClass._
    result == MOCK_VALUE
  }

  def "SpyStatic with explicit mock-maker with closure response"() {
    given:
    SpyStatic(mockMaker: MockMakers.mockito, StaticClass) {
      staticMethod() >> MOCK_VALUE
    }

    when:
    def result = StaticClass.staticMethod()

    then:
    result == MOCK_VALUE
  }

  def "Static mock with default response"() {
    given:
    MockStatic(defaultResponse: { IMockInvocation invocation ->
      if (invocation.method.name == "staticMethod") {
        return MOCK_VALUE
      }
    } as IDefaultResponse, StaticClass)

    when:
    def result = StaticClass.staticMethod()

    then:
    result == MOCK_VALUE
  }

  def "Static mock for non-supporting mock-maker shall fail"() {
    when:
    MockStatic(mockMaker: MockMakers.javaProxy, StaticClass)

    then:
    CannotCreateMockException ex = thrown()
    ex.message.endsWith("java-proxy: Cannot mock classes.")
  }

  def "no static type specified"() {
    when:
    MockStatic(null)

    then:
    InvalidSpecException ex = thrown()
    ex.message == "Mock object type cannot be inferred. Please specify a type explicitly (e.g. 'MockStatic(Person)')."
  }

  def "Static type in options"() {
    given:
    MockStatic(null, type: StaticClass)

    when:
    def result = StaticClass.staticMethod()

    then:
    result == null
  }

  def "Invalid mockito static mock shall throw spock exception instead of Mockito exception"() {
    when:
    MockStatic(Thread)
    then:
    CannotCreateMockException ex = thrown()
    ex.message.contains("It is not possible to mock static methods of java.lang.Thread")
  }

  static class StaticClass {

    String instanceMethod() {
      return REAL_VALUE
    }

    static String staticMethod() {
      return REAL_VALUE
    }

    static String staticMethod2() {
      return REAL_VALUE
    }
  }

  static class StaticClass2 {

    static String staticMethod() {
      return REAL_VALUE
    }
  }
}
