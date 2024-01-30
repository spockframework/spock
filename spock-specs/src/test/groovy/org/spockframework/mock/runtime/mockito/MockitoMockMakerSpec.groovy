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

package org.spockframework.mock.runtime.mockito

import org.mockito.MockMakers
import org.mockito.Mockito
import org.mockito.exceptions.base.MockitoException
import org.spockframework.mock.CannotCreateMockException
import org.spockframework.mock.MockUtil
import org.spockframework.mock.runtime.ByteBuddyTestClassLoader
import org.spockframework.runtime.GroovyRuntimeUtil
import spock.lang.Issue
import spock.lang.Requires
import spock.lang.Specification
import spock.mock.DetachedMockFactory

import java.lang.reflect.Proxy
import java.util.concurrent.Callable

import static spock.mock.MockMakers.mockito

@SuppressWarnings("GroovyAssignabilityCheck")
class MockitoMockMakerSpec extends Specification {
  private static final String ID = "mockito"

  def "Verify simple ID and IMockMakerSettings"() {
    expect:
    mockito.mockMakerId.toString() == ID
    mockito.toString() == "$ID default mock maker settings"
  }

  def "Verify ID and IMockMakerSettings with Mockito settings"() {
    when:
    def set = mockito {}

    then:
    set.mockMakerId.toString() == ID
    set.toString() == "$ID mock maker settings"
    set instanceof MockitoMockMakerSettings
  }

  def "Use specific MockMaker mockito"() {
    when:
    Runnable m = Mock(mockMaker: mockito)
    def mockClass = m.getClass()

    then:
    !Proxy.isProxyClass(mockClass)
    mockClass.name.contains('$MockitoMock$')
  }

  def "Use specific MockMaker mockito check is mock"() {
    given:
    def mockUtil = new MockUtil()

    when:
    Runnable m = Mock(mockMaker: mockito)

    then:
    mockUtil.isMock(m)
    def mockObj = mockUtil.asMock(m)
    mockObj != null
    mockObj.getName() == "m"
    mockObj.instance == m
    mockObj.type == Runnable
    mockObj.specification == this
  }

  def "Use specific MockMaker mockito with DetachedMockFactory"() {
    given:
    def factory = new DetachedMockFactory()

    when:
    def m = factory.Mock(mockMaker: mockito, Runnable)
    def mockClass = m.getClass()

    then:
    !Proxy.isProxyClass(mockClass)
    mockClass.name.contains('$MockitoMock$')
  }

  def "Use specific MockMaker mockito intercept call for interface"() {
    given:
    Callable m = Mock(mockMaker: mockito)

    when:
    def result = m.call()

    then:
    1 * m.call() >> 1
    result == 1
  }

  def "Use specific MockMaker mockito intercept call for class"() {
    when:
    ArrayList m = Mock(mockMaker: mockito)
    m.get(_) >> 1

    then:
    m.get(0) == 1
    m.getClass().name == "java.util.ArrayList"
  }

  def "Final classes are supported"() {
    when:
    StringBuilder sb = Mock(mockMaker: mockito)

    then:
    sb.getClass().getName() == "java.lang.StringBuilder"

    when:
    def res = sb.append(true)

    then:
    1 * sb.append(true) >> sb
    0 * sb._
    res == sb
  }

  def "Final method is supported"() {
    given:
    ClassWithFinalMethod mock = Mock(mockMaker: mockito)

    when:
    def res = mock.finalMethod()

    then:
    1 * mock.finalMethod() >> "MockValue"
    0 * mock._
    res == "MockValue"
  }

  class ClassWithFinalMethod {

    final String finalMethod() {
      return "realValue"
    }
  }

  def "Use constructor args"() {
    given:
    def startList = [1, 2, 3]

    when:
    ArrayList<Integer> spy = Spy(mockMaker: mockito, constructorArgs: [startList])

    then:
    spy.get(0) == 1
    spy.get(1) == 2
    spy.get(2) == 3
    spy == startList

    when:
    spy.clear()

    then:
    1 * spy.clear() >> {}

    expect:
    //The clear should have been intercepted
    spy == startList

    when:
    spy.clear()

    then:
    spy != startList
    spy.size() == 0
  }

  def "Fails to construct object without default constructor"() {
    when:
    Spy(FileInputStream, mockMaker: mockito, useObjenesis: false)

    then:
    CannotCreateMockException ex = thrown()
    ex.message == """Cannot create mock for class java.io.FileInputStream with mockito: Unable to create instance of 'FileInputStream'.
Please ensure that the target class has a 0-arg constructor."""
  }

  def "Additional interfaces"() {
    when:
    ArrayList<Integer> mock = Mock(mockMaker: mockito, additionalInterfaces: [Runnable])

    then:
    mock instanceof ArrayList
    mock instanceof Runnable

    when:
    mock.run()

    then:
    1 * mock.run()
  }

  def "Additional interfaces with final class shall fail"() {
    when:
    Mock(mockMaker: mockito, additionalInterfaces: [Runnable], StringBuilder)

    then:
    def ex = thrown(CannotCreateMockException)
    ex.message == "Cannot create mock for class java.lang.StringBuilder. mockito: Cannot mock final classes with additional interfaces."
  }

  def "Additional mockito settings serializable"() {
    when:
    Runnable mock = Mock(mockMaker: mockito {
      serializable()
    })

    then:
    mock instanceof Serializable
  }

  def "Additional settings invalid shall catch MockitoException"() {
    when:
    Mock(mockMaker: mockito({
      serializable()
    }), StringBuilder)

    then:
    def ex = thrown(CannotCreateMockException)
    ex.message.normalize().startsWith('''Cannot create mock for class java.lang.StringBuilder with mockito: Mockito cannot mock this class: class java.lang.StringBuilder.
Can not mock final classes with the following settings :
 - explicit serialization (e.g. withSettings().serializable())
 - extra interfaces (e.g. withSettings().extraInterfaces(...))''')
    ex.cause.class.name.startsWith("org.mockito.exceptions.")
  }

  def "Spy method throws exception"() {
    given:
    ClassThrowingException mock = Spy(mockMaker: mockito)

    when:
    mock.throwsException()

    then:
    UnsupportedOperationException ex = thrown()
    ex.message == "TestEx"
  }

  static class ClassThrowingException {
    @SuppressWarnings('GrMethodMayBeStatic')
    void throwsException() {
      throw new UnsupportedOperationException("TestEx")
    }
  }

  def "Mock enum"() {
    given:
    TestEnum e = Mock(mockMaker: mockito)

    when:
    def res = e.method()

    then:
    1 * e.method() >> "C"
    res == "C"
    e.name() == null
  }

  private enum TestEnum {
    A,
    B

    String method() {
      return toString()
    }
  }

  @Issue("https://github.com/spockframework/spock/issues/1501")
  def "Access protected Groovy const should be accessible in Groovy 3&4 with mockito"() {
    when:
    AccessProtectedSubClass mySpy = Spy(mockMaker: mockito)

    then:
    mySpy.accessStaticFlag()
  }

  @Issue("https://github.com/spockframework/spock/issues/1501")
  def "Access protected Groovy field should be accessible in Groovy 3&4 with mockito"() {
    when:
    AccessProtectedSubClass mySpy = Spy(mockMaker: mockito)

    then:
    mySpy.accessNonStaticFlag()
  }

  @Issue("https://github.com/spockframework/spock/issues/1501")
  def "Access protected Java fields should be accessible in Groovy 3&4 with mockito"() {
    when:
    AccessProtectedJavaSubClass mySpy = Spy(mockMaker: mockito)

    then:
    mySpy.accessNonStaticFlag()
  }

  def "Access protected Groovy fields should be accessible in Groovy 3&4 although @Internal annotation is not copied due to inline mock"() {
    when:
    AccessProtectedSubClass mySpy = Spy(mockMaker: mockito { withoutAnnotations() })

    then:
    mySpy.accessNonStaticFlag()
  }

  @Requires({ GroovyRuntimeUtil.isGroovy3orNewer() })
  def "Access protected Groovy fields should still be accessible in Groovy 3&4 when @Internal annotation is not copied due to sub-class mock maker"() {
    when:
    AccessProtectedSubClass mySpy = Spy(mockMaker: mockito { s ->
        withoutAnnotations()
        //Use the Mockito own sub-class mock maker here to make sure we are using.
        mockMaker(MockMakers.SUBCLASS)
    }, additionalInterfaces: [Runnable])

    then: "Mockito does not override the MOP methods, so we should not see @Internal annotation issues"
    mySpy.getClass().getMethod("getProperty", String).getDeclaringClass() == GroovyObject
    mySpy.accessStaticFlag()
  }

  @Requires({ GroovyRuntimeUtil.isGroovy2() })
  def "Access protected Groovy fields should be accessible in Groovy 2 when @Internal annotation is not copied due to sub-class mock maker"() {
    when:
    AccessProtectedSubClass mySpy = Spy(mockMaker: mockito { s ->
        withoutAnnotations()
        //Use the Mockito own sub-class mock maker here to make sure we are using.
        mockMaker(MockMakers.SUBCLASS)
    }, additionalInterfaces: [Runnable])

    then:
    mySpy.getClass().getMethod("getProperty", String).getDeclaringClass() == AccessProtectedBaseClass
    mySpy.accessStaticFlag()
  }

  @Issue("https://github.com/spockframework/spock/issues/1452")
  def "Access protected Groovy fields directly with mockito"() {
    when:
    AccessProtectedSubClass mySpy = Spy(mockMaker: mockito)

    then:
    mySpy.nonStaticFlag
  }

  def "Access protected Groovy fields directly with mockito with constructor args"() {
    when:
    AccessProtectedSubClass mySpy = Spy(mockMaker: mockito, constructorArgs: [])

    then:
    mySpy.nonStaticFlag
  }

  def "Access protected Groovy const field directly with mockito"() {
    when:
    AccessProtectedSubClass mySpy = Spy(mockMaker: mockito)

    then:
    mySpy.staticFlag
  }

  def "Mock class"() {
    when:
    DataClass s = Mock(mockMaker: mockito)

    then:
    !s.bool
    s.string == null
  }

  def "Stub class"() {
    when:
    DataClass s = Stub(mockMaker: mockito)

    then:
    !s.bool
    s.string == ""
  }

  def "Spy class"() {
    when:
    DataClass s = Spy(mockMaker: mockito)

    then:
    s.bool
    s.string == "data"
  }

  def "Spy class with Mockito API"() {
    when:
    DataClass s = Mockito.spy(DataClass)

    then:
    s.bool
    s.string == "data"
  }

  def "Spy class with empty constructorArgs"() {
    when:
    DataClass s = Spy(mockMaker: mockito, constructorArgs: [])

    then:
    s.bool
    s.string == "data"
  }

  def "Spy class with constructorArgs"() {
    when:
    DataClass s = Spy(mockMaker: mockito, constructorArgs: [false, "data2"])

    then:
    !s.bool
    s.string == "data2"
  }

  def "Spy class without default constructor does not work"() {
    when:
    Spy(mockMaker: mockito, ClassWithoutDefaultConstructor)

    then:
    CannotCreateMockException ex = thrown()
    ex.cause.cause.message.contains("Please ensure that the target class has a 0-arg constructor.")
  }

  def "Spy class without default constructor with Mockito API does not work"() {
    when:
    Mockito.spy(ClassWithoutDefaultConstructor)

    then:
    thrown(MockitoException)
  }

  def "Class is not mockable sample for asking org.mockito.plugins.MockMaker.isTypeMockable"() {
    when:
    Mock(mockMaker: mockito, Class)

    then:
    CannotCreateMockException ex = thrown()
    ex.message == "Cannot create mock for class java.lang.Class. mockito: Cannot mock wrapper types, String.class or Class.class"
  }

  def "mockito mock setting without mockito settings shall not fail"() {
    when:
    Runnable mock = Mock(mockMaker: mockito {})

    then:
    mock != null
  }

  def "Mocking interface from different classloader works with mockito MockMaker"() {
    given:
    def mockUtil = new MockUtil()
    def tempClassLoader = new ByteBuddyTestClassLoader()
    def interfaceClass = tempClassLoader.defineInterface("Interface")

    when:
    def m = Mock(interfaceClass, mockMaker: mockito)
    then:
    interfaceClass.isInstance(m)
    mockUtil.isMock(m)
  }

  def "Mocking interface from different classloader works with default MockMaker"() {
    given:
    def mockUtil = new MockUtil()
    def tempClassLoader = new ByteBuddyTestClassLoader()
    def interfaceClass = tempClassLoader.defineInterface("Interface")

    when:
    def m = Mock(interfaceClass)
    then:
    interfaceClass.isInstance(m)
    mockUtil.isMock(m)
  }
  
  def "Mocking with additional interface from different classloader works with mockito MockMaker"() {
    given:
    def mockUtil = new MockUtil()
    def tempClassLoader = new ByteBuddyTestClassLoader()
    def additionalInterfaceClass = tempClassLoader.defineInterface("AdditionalInterface1")

    when:
    Runnable m = Mock(mockMaker: mockito, additionalInterfaces: [additionalInterfaceClass])
    then:
    m instanceof Runnable
    additionalInterfaceClass.isInstance(m)
    mockUtil.isMock(m)
  }
}

class AccessProtectedBaseClass {
  protected static boolean staticFlag = true
  protected boolean nonStaticFlag = true
}

class AccessProtectedSubClass extends AccessProtectedBaseClass {
  boolean accessNonStaticFlag() {
    return nonStaticFlag
  }

  @SuppressWarnings(['GrMethodMayBeStatic'])
  boolean accessStaticFlag() {
    return staticFlag
  }
}

@SuppressWarnings('unused')
class DataClass {
  boolean bool = true
  String string = "data"

  DataClass() {}

  DataClass(boolean boolValue, String stringValue) {
    this.bool = boolValue
    this.string = stringValue
  }
}

class ClassWithoutDefaultConstructor {
  @SuppressWarnings('unused')
  ClassWithoutDefaultConstructor(String arg) {
  }
}
