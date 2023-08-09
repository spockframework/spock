/*
 * Copyright 2009 the original author or authors.
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

import org.spockframework.runtime.GroovyRuntimeUtil
import spock.lang.Requires
import spock.lang.Specification
import spock.lang.Issue
import spock.lang.FailsWith

/**
 * @author Peter Niederwieser
 */
class JavaMocksForGroovyClasses extends Specification {
  MockMe mockMe = Mock()

  def "mock declared method"() {
    when:
    def result = mockMe.foo(42)

    then:
    1 * mockMe.foo(42) >> "result"
    result == "result"
  }

  def "mock declared property that is read with property syntax"() {
    when:
    def value = mockMe.bar

    then:
    1 * mockMe.bar >> "value"
    value == "value"
  }

  @Issue("https://github.com/spockframework/spock/issues/380")
  def "mock declared property that is written with property syntax"() {
    when:
    mockMe.bar = "value"

    then:
    1 * mockMe.setBar("value")
  }

  def "mock declared property that is read with method syntax"() {
    when:
    def value = mockMe.getBar()

    then:
    1 * mockMe.bar >> "value"
    value == "value"
  }

  def "mock declared property that is written with method syntax"() {
    when:
    mockMe.setBar("value")

    then:
    1 * mockMe.setBar("value")
  }

  def "mock call to GroovyObject.getProperty"() {
    when:
    def value = mockMe.getProperty("bar")

    then:
    1 * mockMe.getProperty("bar") >> "value"
    0 * mockMe._
    value == "value"

    when:
    def value2 = mockMe.getProperty("bar")

    then:
    1 * mockMe.getProperty("bar") >> "value2"
    0 * mockMe._
    value2 == "value2"
  }

  def "mock call to GroovyObject.setProperty"() {
    when:
    mockMe.setProperty("bar", "value")

    then:
    1 * mockMe.setProperty("bar", "value")
  }

  def "mock call to GroovyObject.invokeMethod"() {
    when:
    def result = mockMe.invokeMethod("foo", [1] as Object[])

    then:
    1 * mockMe.invokeMethod("foo", [1] as Object[]) >> "result"
    result == "result"
  }

  def "mock call to GroovyObject.setMetaClass"() {
    def metaClass = new ExpandoMetaClass(String)

    when:
    mockMe.setMetaClass(metaClass)

    then:
    1 * mockMe.setMetaClass(metaClass)
  }

  def "call to GroovyObject.getMetaClass returns meta class for mocked type"() {
    expect:
    mockMe.getMetaClass() == GroovySystem.metaClassRegistry.getMetaClass(MockMe)
  }

  def "cannot mock call to GroovyObject.getMetaClass"() {
    when:
    mockMe.getMetaClass()

    then:
    0 * mockMe.getMetaClass()
  }

  def "cannot mock GDK method"() {
    when:
    mockMe.any()

    then:
    0 * mockMe.any()
  }

  // TODO: swallowed when mocking static inner class because the latter implements methodMissing/propertyMissing
  @FailsWith(MissingMethodException)
  def "dynamic methods are considered to not exist"() {
    when:
    mockMe.someMethod()

    then:
    1 * mockMe.someMethod()
  }

  def "cannot mock GDK property"() {
    when:
    mockMe.properties

    then:
    0 * mockMe.properties
  }

  // TODO: swallowed when mocking static inner class because the latter implements methodMissing/propertyMissing
  @Requires({ GroovyRuntimeUtil.isGroovy2() }) //different exception in Groovy 2 and 3
  @FailsWith(MissingPropertyException)
  def "dynamic properties are considered to not exist (Groovy 2)"() {
    when:
    mockMe.someProperty

    then:
    1 * mockMe.someProperty
  }

  // TODO: swallowed when mocking static inner class because the latter implements methodMissing/propertyMissing
  @Requires({ GroovyRuntimeUtil.isGroovy3orNewer() })
  @FailsWith(MissingMethodException)
  def "dynamic properties are considered to not exist"() {
    when:
    mockMe.someProperty

    then:
    1 * mockMe.someProperty
  }

  def 'test setter on interface'() {
    given:
    def sut = Mock(IGMock)
    when:
    sut.foo = 'val'
    then:
    1 * sut.setFoo(_)
  }

  def 'test getter on interface'() {
    given:
    def sut = Mock(IGMock)
    when:
    sut.foo
    then:
    1 * sut.getFoo()
  }

  @Issue("https://github.com/spockframework/spock/issues/1158")
  def 'test setter on abstract'() {
    given:
    def sut = Mock(AGMock)

    when:
    sut.foo = 'val'

    then:
    1 * sut.setFoo(_)
  }

  def 'test getter on abstract'() {
    given:
    def sut = Mock(AGMock)

    when:
    sut.foo

    then:
    1 * sut.getFoo()
  }

  @Issue("https://github.com/spockframework/spock/issues/1158")
  def 'test setter on concrete'() {
    given:
    def sut = Mock(GMock)

    when:
    sut.foo = 'val'

    then:
    1 * sut.setFoo(_)
  }

  def 'test getter on concrete'() {
    given:
    def sut = Mock(GMock)

    when:
    sut.foo

    then:
    1 * sut.getFoo()
  }

  @Issue("https://github.com/spockframework/spock/issues/1256")
  def "Mock object boolean (is) accessor via dot-notation" () {
    given:
    ExampleData mockData = Mock(ExampleData)

    when: "query via property syntax"
    def result = mockData.current ? "Data is current" : "Data is not current"

    then: "calls mock"
    1 * mockData.isCurrent() >> true

    and:
    result == "Data is current"
  }

  @Issue("https://github.com/spockframework/spock/issues/1256")
  def "Mock object boolean (get) accessor via dot-notation" () {
    given:
    ExampleData mockData = Mock(ExampleData)

    when: "query via property syntax"
    def result = mockData.enabled ? "Data is current" : "Data is not current"

    then: "calls mock"
    1 * mockData.getEnabled() >> true

    and:
    result == "Data is current"
  }

  @Issue("https://github.com/spockframework/spock/issues/1256")
  def "Mock object boolean (get + is) accessor via dot-notation" () {
    given:
    ExampleData mockData = Mock(ExampleData)

    when: "query via property syntax"
    def result = mockData.active ? "Data is current" : "Data is not current"

    then: "calls mock, preferring 'get' to 'is' for boolean getters (surprise!)"
    1 * mockData.getActive() >> true
    0 * mockData.isActive()

    and:
    result == "Data is current"
  }

  @Issue("https://github.com/spockframework/spock/issues/1256")
  def "Mock object non-boolean (get + is) accessor via dot-notation" () {
    given:
    ExampleData mockData = Mock(ExampleData)

    when: "query via property syntax"
    def result = mockData.name ? "Data is current" : "Data is not current"

    then: "calls mock, preferring 'get' to 'is' for non-boolean getters"
    1 * mockData.getName() >> "X"
    0 * mockData.isName()

    and:
    result == "Data is current"
  }

  @Issue("https://github.com/spockframework/spock/issues/1256")
  def "Mock object non-boolean (is) pseudo accessor via dot-notation" () {
    given:
    ExampleData mockData = Mock(ExampleData)

    when: "query via property syntax"
    mockData.dummy

    then: "non-boolean 'is' getter is illegal"
    GroovyRuntimeException exception = thrown()
    exception instanceof MissingPropertyException || exception instanceof MissingMethodException
  }

  @Issue("https://github.com/spockframework/spock/issues/1256")
  def "Mock object boolean accessor via method" () {
    given:
    ExampleData mockData = Mock(ExampleData)

    when: "query via method syntax"
    def result = mockData.isCurrent() ? "is enabled" : "is not enabled"

    then: "calls mock"
    1 * mockData.isCurrent() >> true

    and:
    result == "is enabled"
  }
}

class MockMe {
  String foo(int i) {}
  String bar
}

interface IGMock {
  String getFoo()
  void setFoo(String val)
}

abstract class AGMock implements IGMock {
  abstract String getFoo();
  abstract void setFoo(String val);
}

class GMock extends AGMock {
  private String prop
  String getFoo() {
    return prop
  }

  void setFoo(String val) {
    this.prop = val
  }
}

class ExampleData {
  boolean isCurrent() {
    false
  }

  boolean getEnabled() {
    false
  }

  boolean isActive() {
    false
  }

  boolean getActive() {
    false
  }

  String isName() {
    null
  }

  String getName() {
    null
  }

  String isDummy() {
    null
  }
}
