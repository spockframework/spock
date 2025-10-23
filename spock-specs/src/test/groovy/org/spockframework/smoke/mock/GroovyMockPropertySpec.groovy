/*
 * Copyright 2025 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.smoke.mock


import org.spockframework.runtime.model.parallel.Resources
import spock.lang.Issue
import spock.lang.ResourceLock
import spock.lang.Specification
import spock.lang.Stepwise

//We need to execute it sequentially, because we change global metaClass state
@Stepwise
@ResourceLock(
  value = Resources.META_CLASS_REGISTRY,
  reason = "Global Mocks"
)
@SuppressWarnings('GroovyAssignabilityCheck')
class GroovyMockPropertySpec extends Specification {

  @Issue('https://github.com/spockframework/spock/issues/2196')
  def "Class with propertyMissing() works normally"() {
    given:
    def c = new ClassWithProperty()
    expect:
    c.prop1 == null
  }

  @Issue('https://github.com/spockframework/spock/issues/2196')
  def "Class with propertyMissing() setter works normally"() {
    given:
    def c = new ClassWithProperty()

    when:
    c.prop1 = "value"
    then:
    c.prop1 == "value"
  }

  @Issue('https://github.com/spockframework/spock/issues/2196')
  def "Spy on property getter"() {
    given:
    def c = GroovySpy(ClassWithProperty, global: true)

    when:
    def result = c.prop1
    then:
    1 * c.getProp1() >> "value"
    0 * _
    result == "value"
  }

  @Issue('https://github.com/spockframework/spock/issues/2196')
  def "Spy on property getter method"() {
    given:
    def c = GroovySpy(ClassWithProperty, global: true)

    when:
    def result = c.getProp1()
    then:
    1 * c.getProp1() >> "value"
    0 * _
    result == "value"
  }

  @Issue('https://github.com/spockframework/spock/issues/2196')
  def "Spy on property setter"() {
    given:
    def c = GroovySpy(ClassWithProperty, global: true)

    when:
    c.prop1 = "value"
    then:
    1 * c.setProp1("value")
    0 * _
  }

  @Issue('https://github.com/spockframework/spock/issues/2196')
  def "Spy on property setter method"() {
    given:
    def c = GroovySpy(ClassWithProperty, global: true)

    when:
    c.setProp1("value")
    then:
    1 * c.setProp1("value")
    0 * _
  }

  class ClassWithProperty {
    String prop1
  }
}


