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

import net.bytebuddy.dynamic.loading.MultipleParentClassLoader
import org.spockframework.mock.CannotCreateMockException
import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll
import spock.mock.DetachedMockFactory
import spock.mock.MockMakers
import spock.util.EmbeddedSpecRunner

import java.lang.reflect.Proxy
import java.util.concurrent.Callable

class ByteBuddyMockMakerSpec extends Specification {

  def "Verify ID and IMockMakerSettings"() {
    expect:
    MockMakers.byteBuddy.mockMakerId.toString() == "byte-buddy"
    MockMakers.byteBuddy.toString() == "byte-buddy default mock maker settings"
  }

  def "Use specific MockMaker byteBuddy"() {
    when:
    Runnable m = Mock(mockMaker: MockMakers.byteBuddy)
    def mockClass = m.getClass()
    then:
    !Proxy.isProxyClass(mockClass)
    mockClass.name.contains('$SpockMock$')
  }

  def "Use specific MockMaker byteBuddy with DetachedMockFactory"() {
    given:
    def factory = new DetachedMockFactory()
    when:
    def m = factory.Mock(mockMaker: MockMakers.byteBuddy, Runnable)
    def mockClass = m.getClass()
    then:
    !Proxy.isProxyClass(mockClass)
    mockClass.name.contains('$SpockMock$')
  }

  def "Use specific MockMaker byteBuddy intercept call for interface"() {
    given:
    Callable m = Mock(mockMaker: MockMakers.byteBuddy)
    when:
    def result = m.call()
    then:
    1 * m.call() >> 1
    result == 1
  }

  def "Use specific MockMaker byteBuddy intercept call for class"() {
    when:
    ArrayList m = Mock(mockMaker: MockMakers.byteBuddy)
    m.get(_) >> 1
    then:
    m.get(0) == 1
  }

  def "Final classes are not supported"() {
    when:
    Mock(mockMaker: MockMakers.byteBuddy, StringBuilder)
    then:
    def ex = thrown(CannotCreateMockException)
    ex.message == "Cannot create mock for class java.lang.StringBuilder. byte-buddy: Cannot mock final classes."
  }

  def "Mock class"() {
    when:
    DataClass s = Mock(mockMaker: MockMakers.byteBuddy)
    then:
    !s.boolField
    s.stringField == null
  }

  def "Stub class"() {
    when:
    DataClass s = Stub(mockMaker: MockMakers.byteBuddy)
    then:
    !s.boolField
    s.stringField == ""
  }

  def "Spy class"() {
    when:
    DataClass s = Spy(mockMaker: MockMakers.byteBuddy)
    then:
    s.boolField
    s.stringField == "data"
  }

  def "Spy class with empty constructorArgs"() {
    when:
    DataClass s = Spy(mockMaker: MockMakers.byteBuddy, constructorArgs: [])
    then:
    s.boolField
    s.stringField == "data"
  }

  def "Spy class with constructorArgs"() {
    when:
    DataClass s = Spy(mockMaker: MockMakers.byteBuddy, constructorArgs: [false, "data2"])
    then:
    !s.boolField
    s.stringField == "data2"
  }

  def "Spy class without default constructor does work"() {
    when:
    ClassWithoutDefaultConstructor s = Spy(mockMaker: MockMakers.byteBuddy)
    then:
    s.stringField == "data"
  }

  def "Mocking interface from different classloader shall fail for byteBuddy MockMaker"() {
    given:
    def tempClassLoader = new ByteBuddyTestClassLoader()
    def interfaceClass = tempClassLoader.defineInterface("Interface")

    when:
    Mock(interfaceClass, mockMaker: MockMakers.byteBuddy)

    then:
    CannotCreateMockException ex = thrown()
    ex.message.startsWith("Cannot create mock for interface Interface. byte-buddy: The class Interface is not visible by the classloader")
  }

  def "Mocking with additional interface from different classloader shall fail for byteBuddy MockMaker"() {
    given:
    def tempClassLoader = new ByteBuddyTestClassLoader()
    def additionalInterfaceClass = tempClassLoader.defineInterface("AdditionalInterface")

    when:
    Mock(Runnable, mockMaker: MockMakers.byteBuddy, additionalInterfaces: [additionalInterfaceClass])

    then:
    CannotCreateMockException ex = thrown()
    ex.message.startsWith("Cannot create mock for interface java.lang.Runnable. byte-buddy: The class AdditionalInterface is not visible by the classloader")
  }

  @Issue("https://github.com/spockframework/spock/issues/2017")
  def "ByteBuddy Mocks with interfaces that are not visible to the mocked type's classloader"() {
    given:
    def testCl1 = new ByteBuddyTestClassLoader()
    def testCl2 = new ByteBuddyTestClassLoader()
    testCl1.defineInterface("foo.Bar")
    testCl2.defineClass("iam.Mocked")
    def cl = new MultipleParentClassLoader([getClass().classLoader, testCl1, testCl2])
    def runner = new EmbeddedSpecRunner(cl)

    when: "A type is mocked with an additional interface from a classloader outside the mocked type's parent chain"
    runner.addClassImport(MockMakers)
    runner.runFeatureBody("""
def mock = Mock(iam.Mocked, mockMaker: MockMakers.byteBuddy, additionalInterfaces: [foo.Bar])
expect:
true
""")

    then:
    noExceptionThrown()
  }

  @Unroll("For target class #targetClass with additional interfaces #additionalInterfaces, isLocal() == #expected")
  def "Local mocks are detected correctly"(Class<?> targetClass, Collection<Class<?>> additionalInterfaces, boolean expected) {
    expect:
    ByteBuddyMockFactory.isLocalMock(targetClass, additionalInterfaces) == expected

    where:
    targetClass | additionalInterfaces                                    | expected
    DataClass   | []                                                      | true
    DataClass   | [Serializable]                                          | true
    DataClass   | [new ByteBuddyTestClassLoader().defineInterface("foo")] | false
  }
}

@SuppressWarnings('unused')
class DataClass {
  boolean boolField = true
  String stringField = "data"

  DataClass() {}

  DataClass(boolean boolValue, String stringValue) {
    this.boolField = boolValue
    this.stringField = stringValue
  }
}

class ClassWithoutDefaultConstructor {
  String stringField = "data"

  @SuppressWarnings('unused')
  ClassWithoutDefaultConstructor(String arg) {
  }
}
