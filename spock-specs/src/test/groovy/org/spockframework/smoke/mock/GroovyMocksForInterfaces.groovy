/*
 * Copyright 2024 the original author or authors.
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

import org.spockframework.mock.CannotCreateMockException
import org.spockframework.runtime.GroovyRuntimeUtil
import org.spockframework.runtime.model.parallel.Resources
import spock.lang.Issue
import spock.lang.Requires
import spock.lang.ResourceLock
import spock.lang.Specification

class GroovyMocksForInterfaces extends Specification {
  def person = GroovyMock(Person)

  def "physical method"() {
    when:
    person.sing("song")

    then:
    1 * person.sing("song")
  }

  def "dynamic method"() {
    when:
    person.greet("henry")

    then:
    1 * person.greet("henry")
  }

  def "DGM method"() {
    when:
    person.dump()

    then:
    1 * person.dump()
  }

  def "get physical property"() {
    when:
    person.name

    then:
    1 * person.getName()

    when:
    person.name

    then:
    1 * person.name
  }

  def "get dynamic property"() {
    when:
    person.age

    then:
    1 * person.getAge()

    when:
    person.age

    then:
    1 * person.age
  }

  def "get DGM property"() {
    when:
    person.properties

    then:
    1 * person.getProperties()

    when:
    person.properties

    then:
    1 * person.properties
  }

  def "set physical property"() {
    when:
    person.name = "fred"

    then:
    1 * person.setName("fred")
  }

  def "set dynamic property"() {
    when:
    person.age = 42

    then:
    1 * person.setAge(42)
  }

  def "set DGM property"() {
    when:
    person.metaClass = null

    then:
    1 * person.setMetaClass(null)
  }

  @ResourceLock(
    value = Resources.META_CLASS_REGISTRY,
    reason = "Global Mocks"
  )
  def "Mock global interface is not supported"() {
    when:
    GroovyMock(Runnable, global: true)

    then:
    CannotCreateMockException ex = thrown()
    ex.message == "Cannot create mock for interface java.lang.Runnable. Global mocking is only possible for classes, but not for interfaces."
  }

  @Issue("https://github.com/spockframework/spock/issues/2131")
  def "Boolean is getters shall be mockable"() {
    expect:
    m.booleanValue
    m.isBooleanValue()

    where:
    m << [
      Stub(BooleanGetters) {
        booleanValue >> true
      },
      Stub(BooleanGetters) {
        isBooleanValue() >> true
      }
    ]
  }

  /**
   * Verify that a boxed Boolean is getter does not have a property representation in Groovy >= 4.
   */
  @Requires({ GroovyRuntimeUtil.MAJOR_VERSION >= 4 })
  def "Boolean boxed is getters Groovy >=4 semantic check"() {
    given:
    def m = createBooleanGetterInterfaceObjPlain()

    expect:
    m.isBooleanValueBoxed()

    when:
    m.booleanValueBoxed
    then:
    thrown(MissingPropertyException)
    //The property for a boxed Boolean does not exist in Groovy >=4
  }

  /**
   * Verify that a boxed Boolean is getter does have a property representation in Groovy <= 3.
   */
  @Requires({ GroovyRuntimeUtil.MAJOR_VERSION <= 3 })
  def "Boolean boxed is getters Groovy <= 3 semantic check"() {
    given:
    def m = createBooleanGetterInterfaceObjPlain()

    expect:
    m.booleanValueBoxed
    m.isBooleanValueBoxed()
  }

  @Issue("https://github.com/spockframework/spock/issues/2131")
  @Requires({ GroovyRuntimeUtil.MAJOR_VERSION >= 4 })
  def "Boolean boxed is getters is not mockable Groovy >= 4"() {
    given:
    def m = Stub(BooleanGetters) {
      isBooleanValueBoxed() >> true
    }

    expect:
    m.isBooleanValueBoxed()

    when:
    m.booleanValueBoxed
    then:
    thrown(MissingPropertyException)
    //According to Groovy >=4 rules the property for a boxed Boolean is getter does not exist.
  }

  @Issue("https://github.com/spockframework/spock/issues/2131")
  @Requires({ GroovyRuntimeUtil.MAJOR_VERSION <= 3 })
  def "Boolean boxed is getters shall be mockable Groovy <= 3"() {
    expect:
    //Groovy <= 3 does not throw an MissingProperty exception
    m.booleanValueBoxed
    m.isBooleanValueBoxed()

    where:
    m << [
      Stub(BooleanGetters) {
        booleanValueBoxed >> true
      },
      Stub(BooleanGetters) {
        isBooleanValueBoxed() >> true
      }
    ]
  }

  @Issue("https://github.com/spockframework/spock/issues/2131")
  def "Boolean get getters shall be mockable"() {
    expect:
    m.isBooleanValue
    m.getIsBooleanValue()

    where:
    m << [
      Stub(BooleanGetters) {
        isBooleanValue >> true
      },
      Stub(BooleanGetters) {
        getIsBooleanValue() >> true
      }
    ]
  }

  @Issue("https://github.com/spockframework/spock/issues/2131")
  def "Boolean boxed get getters shall be mockable"() {
    expect:
    m.isBooleanValueBoxed
    m.getIsBooleanValueBoxed()

    where:
    m << [
      Stub(BooleanGetters) {
        isBooleanValueBoxed >> true
      },
      Stub(BooleanGetters) {
        getIsBooleanValueBoxed() >> true
      }
    ]
  }

  interface Person {
    String getName()

    void setName(String name)

    void sing(String song)
  }

  interface BooleanGetters {
    boolean isBooleanValue()

    Boolean isBooleanValueBoxed()

    boolean getIsBooleanValue()

    Boolean getIsBooleanValueBoxed()
  }

  private BooleanGetters createBooleanGetterInterfaceObjPlain() {
    new BooleanGetters() {
      boolean isBooleanValue() {
        true
      }

      Boolean isBooleanValueBoxed() {
        true
      }

      boolean getIsBooleanValue() {
        true
      }

      Boolean getIsBooleanValueBoxed() {
        true
      }
    }
  }
}
