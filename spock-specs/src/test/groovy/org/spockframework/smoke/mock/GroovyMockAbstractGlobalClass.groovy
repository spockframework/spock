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

import org.spockframework.mock.TooFewInvocationsError
import org.spockframework.runtime.model.parallel.Resources
import spock.lang.*

//We need to execute it sequentially, because we change global metaClass state
@Stepwise
@ResourceLock(
  value = Resources.META_CLASS_REGISTRY,
  reason = "Global Mocks"
)
@SuppressWarnings('GroovyAssignabilityCheck')
class GroovyMockAbstractGlobalClass extends Specification {
  private static final String NAME = "Name"
  private static final String MOCKED_NAME = "MockedName"

  @Issue('https://github.com/spockframework/spock/issues/464')
  def "Creating a Global GroovyMock from abstract class shall not fail"() {
    given:
    def m = GroovyMock(AbstractClassA, global: true)

    when:
    def result = AbstractClassA.getStaticName()

    then:
    1 * AbstractClassA.getStaticName() >> MOCKED_NAME
    result == MOCKED_NAME
    m instanceof AbstractClassA

    expect:
    AbstractClassA.staticName == null
  }

  @Issue('https://github.com/spockframework/spock/issues/464')
  def "Creating a Global GroovySpy from abstract class shall not fail"() {
    given:
    def m = GroovySpy(AbstractClassA, global: true)

    when:
    def result = AbstractClassA.getStaticName()

    then:
    1 * AbstractClassA.getStaticName() >> MOCKED_NAME
    result == MOCKED_NAME
    m instanceof AbstractClassA

    expect:
    AbstractClassA.staticName == NAME
  }

  @Issue('https://github.com/spockframework/spock/issues/464')
  @FailsWith(TooFewInvocationsError)
  def "Asserting on a Global GroovySpy Class for call made on instance shall fail"() {
    given:
    AbstractClassA m = GroovySpy(global: true)

    when:
    m.getName()

    then:
    1 * AbstractClassA.getName() >> MOCKED_NAME
  }

  @Issue('https://github.com/spockframework/spock/issues/464')
  def "Asserting on a Global GroovySpy object for call made on instance"() {
    given:
    AbstractClassA m = GroovySpy(global: true)

    when:
    def result = m.getName()

    then:
    1 * m.getName() >> MOCKED_NAME
    result == MOCKED_NAME
  }

  @Issue('https://github.com/spockframework/spock/issues/464')
  @FailsWith(TooFewInvocationsError)
  def "Asserting on a Global GroovySpy object for static calls shall fail"() {
    given:
    AbstractClassA m = GroovySpy(global: true)

    when:
    AbstractClassA.staticName

    then: "We are asserting on the instance not the Class, this shall fail"
    1 * m.getStaticName() >> MOCKED_NAME
  }

  @Issue('https://github.com/spockframework/spock/issues/464')
  @FailsWith(TooFewInvocationsError)
  def "Asserting on a Global GroovySpy object for subclass instance call shall fail"() {
    given:
    AbstractClassA m = GroovySpy(global: true)

    when:
    def subObj = new SubClassA()
    subObj.getName()

    then: "Calls from subclasses are not tracked, so it shall fail"
    1 * m.getName() >> MOCKED_NAME
  }

  def "Two global mocks"() {
    given:
    GroovySpy(AbstractClassA, global: true)
    GroovySpy(ClassB, global: true)

    when:
    def result1 = AbstractClassA.getStaticName()
    def result2 = ClassB.getStaticName()

    then:
    1 * AbstractClassA.getStaticName() >> MOCKED_NAME
    1 * ClassB.getStaticName() >> "MockedName2"
    result1 == MOCKED_NAME
    result2 == "MockedName2"

    expect:
    AbstractClassA.staticName == NAME
    ClassB.staticName == NAME
  }

  static abstract class AbstractClassA {
    static String getStaticName() { NAME }

    @SuppressWarnings('GrMethodMayBeStatic')
    String getName() { NAME }
  }

  static class SubClassA extends AbstractClassA {}

  static abstract class ClassB {
    static String getStaticName() { NAME }
  }
}
