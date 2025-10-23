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
class GroovyMockClassWithPropertyMissingSpec extends Specification {

  @Issue('https://github.com/spockframework/spock/issues/2196')
  def "Class with propertyMissing() works normally"() {
    given:
    def c = new ClassWithPropertyMissing()
    expect:
    c.prop1 == "prop1"
  }

  @Issue('https://github.com/spockframework/spock/issues/2196')
  def "Class with propertyMissing() setter works normally"() {
    given:
    def c = new ClassWithPropertyMissing()
    
    when:
    c.prop1 = "value"
    then:
    def ex = thrown(IllegalStateException)
    ex.message == "propertyMissing(prop1,value) called."
  }

  @Issue('https://github.com/spockframework/spock/issues/2196')
  def "Class with propertyMissing() as global spy"() {
    given:
    def c = GroovySpy(ClassWithPropertyMissing, global: true)

    expect:
    c.prop1 == "prop1"
  }

  @Issue('https://github.com/spockframework/spock/issues/2196')
  def "Class with propertyMissing() setter as global spy"() {
    given:
    def c = GroovySpy(ClassWithPropertyMissing, global: true)

    when:
    c.prop1 = "value"
    then:
    def ex = thrown(IllegalStateException)
    ex.message == "propertyMissing(prop1,value) called."
  }

  @Issue('https://github.com/spockframework/spock/issues/2196')
  def "Spy on synthetic property of a Class with propertyMissing"() {
    given:
    def c = GroovySpy(ClassWithPropertyMissing, global: true)

    when:
    def result = c.prop1
    then:
    1 * c.getProp1() >> "OtherValue"
    0 * _
    result == "OtherValue"
  }

  @Issue('https://github.com/spockframework/spock/issues/2196')
  def "Spy on synthetic property setter of a Class with propertyMissing"() {
    given:
    def c = GroovySpy(ClassWithPropertyMissing, global: true)

    when:
    c.prop1 = null
    then:
    1 * c.setProp1(null) >> ""
    0 * _
  }
}

class ClassWithPropertyMissing {
  def propertyMissing(String name) {
    name
  }

  def propertyMissing(String name, Object value) {
    if (value != null) {
      throw new IllegalStateException("propertyMissing($name,$value) called.")
    }
  }
}
