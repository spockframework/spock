/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

  @Issue("http://issues.spockframework.org/detail?id=258")
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
    value == "value"

    when:
    def value2 = mockMe.getProperty("bar")

    then:
    1 * mockMe.getProperty("bar") >> "value2"
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
}

class MockMe {
  String foo(int i) {}
  String bar
}

