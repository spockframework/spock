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

import org.spockframework.mock.TooFewInvocationsError

import spock.lang.FailsWith
import spock.lang.Specification
import spock.lang.Issue

/**
 *
 * @author Peter Niederwieser
 */
class GroovyClassBasedMocks extends Specification {
  MockMe mockMe = Mock()

  def "mock declared method"() {
    when:
    mockMe.foo(42)
    then:
    1 * mockMe.foo(42)
  }

  def "mock declared property that is read with property syntax"() {
    when:
    mockMe.bar
    then:
    1 * mockMe.bar
  }

  @Issue("http://issues.spockframework.org/detail?id=258")
  def "mock declared property that is written with property syntax"() {
    when:
    mockMe.bar = "bar"
    then:
    1 * mockMe.setBar("bar")
  }

  def "mock declared property that is read with method syntax"() {
    when:
    mockMe.getBar()
    then:
    1 * mockMe.bar
  }

  def "mock declared property that is written with method syntax"() {
    when:
    mockMe.setBar("bar")
    then:
    1 * mockMe.setBar("bar")
  }

  def "mock call to GroovyObject.getProperty"() {
    when:
    mockMe.getProperty("foo")

    then:
    1 * mockMe.getProperty("foo")
  }

  def "mock call to GroovyObject.setProperty"() {
    when:
    mockMe.setProperty("foo", 42)

    then:
    1 * mockMe.setProperty("foo", 42)
  }

  def "mock call to GroovyObject.invokeMethod"() {
    when:
    mockMe.invokeMethod("foo", [1] as Object[])

    then:
    1 * mockMe.invokeMethod("foo", [1] as Object[])
  }

  def "mock call to GroovyObject.setMetaClass"() {
    def metaClass = new ExpandoMetaClass(String)

    when:
    mockMe.setMetaClass(metaClass)

    then:
    1 * mockMe.setMetaClass(metaClass)
  }

  def "call to GroovyObject.getMetaClass returns meta class for Mock class"() {
    expect:
    mockMe.getMetaClass() == GroovySystem.metaClassRegistry.getMetaClass(mockMe.getClass())
  }

  def "cannot mock call to GroovyObject.getMetaClass"() {
    when:
    mockMe.getMetaClass()

    then:
    0 * mockMe.getMetaClass()
  }

  @FailsWith(value=TooFewInvocationsError, reason="not yet implemented")
  def "mock GDK method"() {
    when:
    mockMe.any()
    then:
    1 * mockMe.any()
  }

  @FailsWith(value=TooFewInvocationsError, reason="not yet implemented")
  def "mock dynamic method"() {
    when:
    mockMe.someMethod()
    then:
    1 * mockMe.someMethod()
  }

  static class MockMe {
    def foo(int i) {}
    String bar
  }
}

