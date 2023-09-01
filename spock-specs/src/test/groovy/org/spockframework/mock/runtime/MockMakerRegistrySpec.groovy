/*
 * Copyright 2023 the original author or authors.
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

import org.spockframework.mock.CannotCreateMockException
import org.spockframework.mock.IMockObject
import org.spockframework.mock.runtime.mockito.MockitoMockMaker
import org.spockframework.runtime.RunContext
import org.spockframework.util.InternalSpockError
import spock.lang.Shared
import spock.lang.Specification
import spock.mock.MockMakers

import java.util.concurrent.Callable

import static org.spockframework.mock.runtime.IMockMaker.MockMakerId
import static org.spockframework.mock.runtime.IMockMaker.MockMakerCapability
import static org.spockframework.mock.runtime.IMockMaker.IMockCreationSettings
import static org.spockframework.mock.runtime.IMockMaker.IMockMakerSettings
import static org.spockframework.mock.runtime.IMockMaker.IMockabilityResult

class MockMakerRegistrySpec extends Specification {
  private static final MockMakerId TEST_ID = new MockMakerId("test")
  private static final int TEST_PRIORITY = 2

  @Shared
  def defaultMockMakerRegistry = RunContext.get().getMockMakerRegistry()

  def "No MockMaker"() {
    when:
    new MockMakerRegistry([], null)
    then:
    InternalSpockError ex = thrown()
    ex.message == "No IMockMaker implementations found."
  }

  def "Invalid id: null"() {
    given:
    IMockMaker maker = Mock()
    when:
    new MockMakerRegistry([maker], null)
    then:
    IllegalStateException ex = thrown()
    ex.message.endsWith(" has an invalid ID. The ID must not be null.")
  }

  def "Invalid priority"(int inputPriority, String expectedErrorMessage) {
    given:
    IMockMaker maker = Mock {
      id >> TEST_ID
      priority >> inputPriority
      capabilities >> [MockMakerCapability.INTERFACE]
    }
    when:
    new MockMakerRegistry([maker], null)
    then:
    IllegalStateException ex = thrown()
    ex.message == expectedErrorMessage
    where:
    inputPriority | expectedErrorMessage
    -1            | "The IMockMaker '$TEST_ID' has an invalid priority -1. Priorities < 0 are invalid."
    0             | "The IMockMaker '$TEST_ID' has an invalid priority 0. The priority 0 is reserved."
  }

  def "No capabilities - null"() {
    given:
    IMockMaker maker = Mock {
      id >> TEST_ID
      priority >> 5
    }
    when:
    new MockMakerRegistry([maker], null)
    then:
    IllegalStateException ex = thrown()
    ex.message == "The IMockMaker 'Mock for type 'IMockMaker' named 'maker'' does not define any capability."
  }

  def "No capabilities - empty"() {
    given:
    IMockMaker maker = Mock {
      id >> TEST_ID
      priority >> 5
      capabilities >> []
    }
    when:
    new MockMakerRegistry([maker], null)
    then:
    IllegalStateException ex = thrown()
    ex.message == "The IMockMaker 'Mock for type 'IMockMaker' named 'maker'' does not define any capability."
  }

  def "Duplicate mock makers"() {
    given:
    IMockMaker maker1 = Mock {
      id >> TEST_ID
      priority >> TEST_PRIORITY
      capabilities >> [MockMakerCapability.INTERFACE]
    }
    IMockMaker maker2 = Mock {
      id >> TEST_ID
      priority >> TEST_PRIORITY
      capabilities >> [MockMakerCapability.INTERFACE]
    }
    when:
    new MockMakerRegistry([maker1, maker2], null)
    then:
    IllegalStateException ex = thrown()
    ex.message == "Duplicated IMockMaker instances with the same ID found: $TEST_ID"
  }

  def "MockMaker sorting"() {
    given:
    def samePriorityId = new MockMakerId("same-priority")
    def proxy = new JavaProxyMockMaker()
    IMockMaker mockSamePriorityProxy = Mock {
      id >> samePriorityId
      priority >> 200
      capabilities >> [MockMakerCapability.INTERFACE]
    }
    def byteBuddy = new ByteBuddyMockMaker()
    IMockMaker sampleMockMaker = Mock {
      id >> TEST_ID
      priority >> 5
      capabilities >> [MockMakerCapability.INTERFACE]
    }
    def cglib = new CglibMockMaker()
    when:
    def r = new MockMakerRegistry([mockSamePriorityProxy, byteBuddy, cglib, sampleMockMaker, proxy], null)
    then:
    r.makerList == [sampleMockMaker, proxy, byteBuddy, mockSamePriorityProxy, cglib]
  }

  def "MockMaker sorting with preferred"() {
    given:
    def samePriorityId = new MockMakerId("same-priority")
    def proxy = new JavaProxyMockMaker()
    IMockMaker mockSamePriorityProxy = Mock {
      id >> samePriorityId
      priority >> 200
      capabilities >> [MockMakerCapability.INTERFACE]
    }
    def byteBuddy = new ByteBuddyMockMaker()
    IMockMaker sampleMockMaker = Mock {
      id >> TEST_ID
      priority >> 250
      capabilities >> [MockMakerCapability.INTERFACE]
    }
    def cglib = new CglibMockMaker()
    when:
    def r = new MockMakerRegistry([mockSamePriorityProxy, byteBuddy, cglib, sampleMockMaker, proxy], IMockMakerSettings.simple(TEST_ID))
    then:
    r.makerList == [sampleMockMaker, proxy, byteBuddy, mockSamePriorityProxy, cglib]
  }

  def "MockMaker sorting with preferred cglib"() {
    given:
    def proxy = new JavaProxyMockMaker()
    def byteBuddy = new ByteBuddyMockMaker()
    def cglib = new CglibMockMaker()
    when:
    def r = new MockMakerRegistry([byteBuddy, cglib, proxy], MockMakers.cglib)
    then:
    r.makerList == [cglib, proxy, byteBuddy]
  }

  def "MockMaker non existing preferred ID"() {
    when:
    new MockMakerRegistry([new JavaProxyMockMaker()], MockMakers.cglib)
    then:
    IllegalStateException ex = thrown()
    ex.message == "No IMockMaker with ID 'cglib' exists, but was request via mockMaker.preferredMockMaker configuration. Is a runtime dependency missing?"
  }

  def "MockMaker loaded from ServiceLoader"() {
    when:
    def makers = defaultMockMakerRegistry.makerList
    then:
    makers[0] instanceof JavaProxyMockMaker
    makers[1] instanceof ByteBuddyMockMaker
    makers[2] instanceof MockitoMockMaker
    makers[3] instanceof CglibMockMaker
  }

  def "Mock with unknown MockMaker"() {
    when:
    Mock(mockMaker: IMockMakerSettings.simple(new MockMakerId("unknown")), Runnable)
    then:
    CannotCreateMockException ex = thrown()
    ex.message == "Cannot create mock for interface java.lang.Runnable because MockMaker with ID 'unknown' does not exist."
  }

  def "asMockOrNull() asks all IMockMakers"() {
    given:
    IMockMaker maker = Mock {
      id >> MockMakers.byteBuddy.mockMakerId
      priority >> 2
      capabilities >> [MockMakerCapability.INTERFACE]
    }
    IMockObject mockObj = Mock(IMockObject)
    def registry = new MockMakerRegistry([maker], null)
    def obj = "mockObj"
    when: "The registry will ask the maker, because the obj does not implement ISpockMockObject"
    registry.asMockOrNull(obj)
    then:
    1 * maker.asMockOrNull(obj) >> mockObj
  }

  def "Test the default IMockabilityResult.MOCKABLE"() {
    expect:
    IMockabilityResult.MOCKABLE.mockable
    IMockabilityResult.MOCKABLE.notMockableReason == ""
  }

  def "Static Mock settings calls makeMock shall fail"() {
    given:
    IMockCreationSettings settings = Mock {
      staticMock >> true
    }
    when:
    defaultMockMakerRegistry.makeMock(settings)
    then:
    IllegalArgumentException ex = thrown()
    ex.message == "Can't mock static mocks with makeMock()."
  }

  def "Non-static Mock settings calls makeStaticMock shall fail"() {
    given:
    IMockCreationSettings settings = Mock()
    when:
    defaultMockMakerRegistry.makeStaticMock(settings)
    then:
    IllegalArgumentException ex = thrown()
    ex.message == "Can't mock instance mocks with makeStaticMock()."
  }

  def "Static Mock shall currently fail"() {
    given:
    IMockCreationSettings settings = Stub() {
      mockType >> File
      staticMock >> true
      mockMakerSettings >> null
    }
    when:
    defaultMockMakerRegistry.makeStaticMock(settings)
    then:
    CannotCreateMockException ex = thrown()
    ex.message == """Cannot create mock for class java.io.File.
java-proxy: Cannot mock classes.
byte-buddy: Cannot mock static methods.
mockito: Cannot mock static methods.
cglib: Cannot mock static methods.
fancy: Cannot mock static methods."""
  }

  def "makeStaticMock"() {
    given:
    IMockMaker mockMaker = Mock {
      id >> TEST_ID
      priority >> TEST_PRIORITY
      getMockability(_ as IMockCreationSettings) >> IMockabilityResult.MOCKABLE
      capabilities >> [MockMakerCapability.CLASS, MockMakerCapability.STATIC_METHOD]
    }
    IMockCreationSettings settings = Mock {
      mockType >> File
      additionalInterface >> []
      staticMock >> true
    }
    def registry = new MockMakerRegistry([mockMaker], null)
    when:
    def staticMock = registry.makeStaticMock(settings)
    then:
    1 * mockMaker.makeStaticMock(settings) >> Mock(IMockMaker.IStaticMock)
    staticMock != null
  }

  def "makeMock final class"() {
    given:
    IMockMaker mockMaker = Mock {
      id >> TEST_ID
      priority >> TEST_PRIORITY
      getMockability(_ as IMockCreationSettings) >> IMockabilityResult.MOCKABLE
      capabilities >> [MockMakerCapability.CLASS, MockMakerCapability.FINAL_CLASS]
    }
    IMockCreationSettings settings = Mock {
      mockType >> StringBuilder
      additionalInterface >> []
    }
    def registry = new MockMakerRegistry([mockMaker], null)
    when:
    def mock = registry.makeMock(settings)
    then:
    1 * mockMaker.makeMock(settings) >> new StringBuilder()
    mock instanceof StringBuilder
  }

  def "Default MockMakerImpl throws UnsupportedOperationException on makeStaticMock"() {
    given:
    IMockMaker maker = Spy {
      id >> TEST_ID
    }
    when:
    maker.makeStaticMock(Stub(IMockCreationSettings))
    then:
    UnsupportedOperationException ex = thrown()
    ex.message == "Operation not implemented by test, but claimed capability MockMakerCapability.STATIC_METHOD. Settings: Mock for type 'IMockCreationSettings'"
  }

  def "Mock throws CannotCreateMockException on capability INTERFACE"() {
    given:
    IMockMaker mockMaker = Mock {
      id >> TEST_ID
      priority >> TEST_PRIORITY
      capabilities >> [MockMakerCapability.CLASS]
    }
    IMockCreationSettings settings = Mock {
      mockType >> Runnable
      additionalInterface >> []
    }
    def registry = new MockMakerRegistry([mockMaker], null)
    when:
    registry.makeMock(settings)
    then:
    CannotCreateMockException ex = thrown()
    ex.message.contains("$TEST_ID: Cannot mock interfaces.")
  }

  def "Mock throws CannotCreateMockException on capability ADDITIONAL_INTERFACES"() {
    given:
    IMockMaker mockMaker = Mock {
      id >> TEST_ID
      priority >> TEST_PRIORITY
      capabilities >> [MockMakerCapability.INTERFACE]
    }
    IMockCreationSettings settings = Mock {
      mockType >> Runnable
      additionalInterface >> [Callable]
    }
    def registry = new MockMakerRegistry([mockMaker], null)
    when:
    registry.makeMock(settings)
    then:
    CannotCreateMockException ex = thrown()
    ex.message.contains("$TEST_ID: Cannot mock classes with additional interfaces")
  }
}
