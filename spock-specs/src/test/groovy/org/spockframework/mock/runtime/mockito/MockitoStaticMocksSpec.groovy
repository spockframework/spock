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
import org.spockframework.mock.MockUtil
import org.spockframework.runtime.InvalidSpecException
import spock.lang.Issue
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
    SpyStatic(StaticClass)
    StaticClass.staticMethod() >> MOCK_VALUE2

    then:
    CannotCreateMockException ex = thrown()
    ex.message.contains("static mocking is already registered in the current thread")

    cleanup:
    mock.close()
  }

  def "Mockito and Spock API Test static mock on same class inverse Spock first"() {
    given:
    SpyStatic(StaticClass)
    StaticClass.staticMethod() >> MOCK_VALUE2

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

    SpyStatic(StaticClass2)
    StaticClass2.staticMethod() >> MOCK_VALUE2

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
    SpyStatic(StaticClass2)
    StaticClass2.staticMethod() >> MOCK_VALUE2

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

  def "SpyStatic Spock API"() {
    given:
    SpyStatic(StaticClass)

    when:
    def result = StaticClass.staticMethod()

    then:
    result == REAL_VALUE
    1 * StaticClass.staticMethod()
    mockUtil.isStaticMock(StaticClass)
  }

  def "Spock API mocking same class twice shall fail"() {
    given:
    SpyStatic(StaticClass)

    when:
    SpyStatic(StaticClass)

    then:
    CannotCreateMockException ex = thrown()
    ex.message.contains("static mocking is already registered in the current thread")
  }

  def "Spock API mocking two different classes"() {
    given:
    SpyStatic(StaticClass)
    StaticClass.staticMethod() >> MOCK_VALUE

    SpyStatic(StaticClass2)
    StaticClass2.staticMethod() >> MOCK_VALUE2


    expect:
    StaticClass.staticMethod() == MOCK_VALUE
    StaticClass2.staticMethod() == MOCK_VALUE2
  }

  def "static mock and real instance at the same time"() {
    given:
    SpyStatic(StaticClass)
    StaticClass.staticMethod() >> MOCK_VALUE

    StaticClass instMock = new StaticClass()

    expect:
    StaticClass.staticMethod() == MOCK_VALUE
    instMock.instanceMethod() == REAL_VALUE
  }

  def "static and instance mock at the same time"() {
    given:
    SpyStatic(StaticClass)
    StaticClass.staticMethod() >> MOCK_VALUE

    StaticClass instMock = Mock()

    expect:
    StaticClass.staticMethod() == MOCK_VALUE
    instMock.instanceMethod() == null
  }

  def "static and instance mock at the same time with interactions"() {
    given:
    SpyStatic(StaticClass)
    StaticClass.staticMethod() >> MOCK_VALUE

    StaticClass instMock = Mock() {
      instanceMethod() >> MOCK_VALUE2
    }

    expect:
    StaticClass.staticMethod() == MOCK_VALUE
    instMock.instanceMethod() == MOCK_VALUE2
  }

  def "static and instance mock at the same time with Mockito mock maker"() {
    given:
    SpyStatic(StaticClass)
    StaticClass.staticMethod() >> MOCK_VALUE

    StaticClass instMock = Mock(mockMaker: MockMakers.mockito) {
      instanceMethod() >> MOCK_VALUE2
    }

    expect:
    StaticClass.staticMethod() == MOCK_VALUE
    instMock.instanceMethod() == MOCK_VALUE2
  }

  def "SpyStatic interactions in given"() {
    given:
    SpyStatic(StaticClass)
    StaticClass.staticMethod() >> MOCK_VALUE

    expect:
    StaticClass.staticMethod() == MOCK_VALUE
  }

  def "SpyStatic interactions in when"() {
    given:
    SpyStatic(StaticClass)

    when:
    StaticClass.staticMethod() >> MOCK_VALUE
    def result = StaticClass.staticMethod()

    then:
    result == MOCK_VALUE
  }

  def "SpyStatic can activate the static mocks on different Thread"() {
    given:
    SpyStatic(StaticClass)
    StaticClass.staticMethod() >> MOCK_VALUE

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

  def "SpyStatic can activate two mocked static classes on different Thread"() {
    given:
    SpyStatic(StaticClass)
    StaticClass.staticMethod() >> MOCK_VALUE

    SpyStatic(StaticClass2)
    StaticClass2.staticMethod() >> MOCK_VALUE2

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
    SpyStatic(StaticClass)
    StaticClass.staticMethod() >> MOCK_VALUE

    when:
    runWithThreadAwareMocks {
      assert StaticClass.staticMethod() == MOCK_VALUE
    }

    then:
    StaticClass.staticMethod() == MOCK_VALUE
  }

  def "No mock"() {
    expect:
    StaticClass.staticMethod() == REAL_VALUE
  }

  def "SpyStatic Spock API with response"() {
    given:
    SpyStatic(StaticClass)

    when:
    def result = StaticClass.staticMethod()

    then:
    result == MOCK_VALUE
    1 * StaticClass.staticMethod() >> MOCK_VALUE

    when: "After first response it shall fallback to null"
    result = StaticClass.staticMethod()

    then:
    result == REAL_VALUE
  }

  def "SpyStatic Spock API with closure response"() {
    given:
    SpyStatic(StaticClass)
    StaticClass.staticMethod() >> MOCK_VALUE
    StaticClass.staticMethod2() >> MOCK_VALUE


    expect:
    StaticClass.staticMethod() == MOCK_VALUE
    StaticClass.staticMethod2() == MOCK_VALUE
  }

  def "SpyStatic Spock API without response"() {
    given:
    SpyStatic(StaticClass)

    when:
    def result = StaticClass.staticMethod()

    then:
    result == REAL_VALUE
    1 * StaticClass.staticMethod()
  }

  def "SpyStatic with explicit mock-maker"() {
    given:
    SpyStatic(StaticClass, MockMakers.mockito)

    when:
    def result = StaticClass.staticMethod()

    then:
    1 * StaticClass.staticMethod() >> MOCK_VALUE
    0 * StaticClass._
    result == MOCK_VALUE
  }

  def "SpyStatic with explicit mock-maker with closure response"() {
    given:
    SpyStatic(StaticClass, MockMakers.mockito)
    StaticClass.staticMethod() >> MOCK_VALUE

    when:
    def result = StaticClass.staticMethod()

    then:
    result == MOCK_VALUE
  }

  def "Static mock for non-supporting mock-maker shall fail"() {
    when:
    SpyStatic(StaticClass, MockMakers.javaProxy)

    then:
    CannotCreateMockException ex = thrown()
    ex.message.endsWith("java-proxy: Cannot mock classes.")
  }

  def "no static type specified"() {
    when:
    SpyStatic(null)

    then:
    InvalidSpecException ex = thrown()
    ex.message == "The type must not be null."
  }

  def "Invalid mockito static mock shall throw spock exception instead of Mockito exception"() {
    when:
    SpyStatic(Thread)
    then:
    CannotCreateMockException ex = thrown()
    ex.message.contains("It is not possible to mock static methods of java.lang.Thread")
  }

  @Issue("https://github.com/spockframework/spock/issues/2161")
  def "SpyStatic with varargs in method"() {
    given:
    SpyStatic(StaticClass)
    StaticClass.staticVarargsMethod("test") >> true
    StaticClass.staticVarargsMethod("test2") >> false

    expect:
    StaticClass.staticVarargsMethod("test")
    !StaticClass.staticVarargsMethod("test2")
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

    @SuppressWarnings('unused')
    static boolean staticVarargsMethod(String str, String... varargs) {
      return true
    }
  }

  static class StaticClass2 {

    static String staticMethod() {
      return REAL_VALUE
    }
  }
}
