/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.mock

import org.spockframework.runtime.GroovyRuntimeUtil
import spock.lang.*
import spock.util.environment.Jvm

import java.lang.reflect.Modifier
import java.util.regex.Pattern

@Isolated
class GroovySpiesThatAreGlobal extends Specification {
  def "mock instance method"() {
    def myList = new ArrayList()
    def anyList = GroovySpy(ArrayList, global: true)

    when:
    myList.add(1)
    myList.size()

    then:
    1 * anyList.add(1)
    1 * anyList.size()
    0 * _
  }

  def "global mocking is undone before next method"() {
    when:
    new ArrayList().add(1)

    then:
    0 * _
  }

  def "calls real method if call isn't mocked"() {
    def myList = new ArrayList()
    GroovySpy(ArrayList, global: true)

    when:
    myList.add(1)
    myList.add(1)

    then:
    myList.size() == 2
  }

  def "calls real method if mocked call provides no result"() {
    def myList = new ArrayList()
    GroovySpy(ArrayList, global: true)

    when:
    myList.add(1)
    myList.add(1)

    then:
    2 * myList.add(1)
    myList.size() == 2
  }

  def "does not call real method if mocked call provides result"() {
    def myList = new ArrayList()
    GroovySpy(ArrayList, global: true)

    when:
    myList.add(1)
    myList.add(1)

    then:
    2 * myList.add(1) >> true
    myList.size() == 0
  }

  def "can call real method when providing result"() {
    def myList = new ArrayList()
    GroovySpy(ArrayList, global: true)

    when:
    myList.add(1)
    myList.add(1)

    then:
    2 * myList.add(1) >> { callRealMethod() }
    myList.size() == 2
  }

  def "can call real method with changed arguments"() {
    def myList = new ArrayList()
    GroovySpy(ArrayList, global: true)

    when:
    myList.add(1)
    myList.add(1)

    then:
    2 * myList.add(1) >> { callRealMethodWithArgs(42) }
    myList.size() == 2
    myList[1] == 42
  }

  def "mock dynamic instance method"() {
    def anyList = GroovySpy(ArrayList, global: true)

    when:
    new ArrayList().foo(42)

    then:
    1 * anyList.foo(42) >> true
  }

  def "mock dynamic instance method called via MOP"() {
    def anyPerson = GroovySpy(Person, global: true)

    when:
    new Person().invokeMethod("foo", [42] as Object[])

    then:
    1 * anyPerson.foo(42) >> "done"
  }

  def "mock dynamic property getter called via MOP"() {
    def anyPerson = GroovySpy(Person, global: true)

    when:
    new Person().getProperty("foo")

    then:
    1 * anyPerson.foo >> "done"
  }

  def "mock dynamic property setter called via MOP"() {
    def anyPerson = GroovySpy(Person, global: true)

    when:
    new Person().setProperty("foo", 42)

    then:
    1 * anyPerson.setFoo(42) >> "done"
  }

  def "mock final instance method"() {
    assert Modifier.isFinal(Person.getMethod("performFinal", String).getModifiers())

    def anyPerson = GroovySpy(Person, global: true)

    when:
    new Person().performFinal("work")

    then:
    1 * anyPerson.performFinal("work")
  }

  def "mock final class"() {
    assert Modifier.isFinal(FinalPerson.getModifiers())

    def anyPerson = GroovySpy(FinalPerson, global: true)

    when:
    new FinalPerson().perform("work")

    then:
    1 * anyPerson.perform("work")
  }

  def "mock static method"() {
    // Use objenesis to create instance for Java 17 and later
    GroovySpy(Collections, global: true, useObjenesis: Jvm.current.java17Compatible)

    when:
    Collections.emptyList()
    Collections.nCopies(42, "elem")

    then:
    1 * Collections.emptyList()
    1 * Collections.nCopies(42, "elem")
    0 * _
  }

  def "mock dynamic static method"() {
    GroovySpy(Collections, global: true, useObjenesis: Jvm.current.java17Compatible)

    when:
    Collections.foo()
    Collections.bar(42, "elem")

    then:
    1 * Collections.foo() >> 0
    1 * Collections.bar(42, "elem") >> 0
    0 * _
  }

  def "mock constructor"() {
    GroovySpy(Person, global: true)

    when:
    new Person("fred", 42)
    new Person("barney", 21)

    then:
    1 * new Person("fred", 42)
    1 * new Person("barney", 21)
    0 * _
  }

  @Issue("https://github.com/spockframework/spock/issues/871")
  def "type casts are not swallowed"() {
    given:
    GroovySpy(PersonWithOverloadedMethods, global: true)

    expect:
    action() == result

    where:
    _ | action                                                           || result
    _ | { PersonWithOverloadedMethods.perform("null string" as String) } || "String"
    _ | { PersonWithOverloadedMethods.perform(null as Pattern) }         || "Pattern"
    _ | { PersonWithOverloadedMethods.perform((String) null) }           || "String"
    _ | { PersonWithOverloadedMethods.perform((Pattern) null) }          || "Pattern"
  }

  @Issue("https://github.com/spockframework/spock/issues/871")
  def "type casts are not swallowed when stubbing"() {
    given:
    GroovySpy(PersonWithOverloadedMethods, global: true)
    PersonWithOverloadedMethods.perform(_) >> "Foo"

    expect:
    action() == "Foo"

    where:
    _ | action
    _ | { PersonWithOverloadedMethods.perform(null as String) }
    _ | { PersonWithOverloadedMethods.perform(null as Pattern) }
    _ | { PersonWithOverloadedMethods.perform((String) null) }
    _ | { PersonWithOverloadedMethods.perform((Pattern) null) }
  }

  def "Accessing non-existing property throws MissingPropertyException"() {
    when:
    Enclosing.Super.NON_EXISTING_FIELD
    then:
    def ex = thrown(MissingPropertyException)
    ex.message.contains("NON_EXISTING_FIELD")
  }

  def "Accessing non-existing property through global spy throws MissingPropertyException"() {
    given:
    GroovySpy(Enclosing.Super, global: true)

    when:
    Enclosing.Super.NON_EXISTING_FIELD
    then:
    def ex = thrown(MissingPropertyException)
    ex.message.contains(" NON_EXISTING_FIELD ")
    def suppressed = ex.getSuppressed()[0].cause
    suppressed instanceof MissingMethodException
    suppressed.message.contains("getNON_EXISTING_FIELD")
  }

  def "Accessing non-existing property setter throws MissingPropertyException"() {
    when:
    Enclosing.Super.NON_EXISTING_FIELD = ""
    then:
    thrown(MissingPropertyException)
  }

  def "Accessing non-existing property setter through global spy throws MissingPropertyException"() {
    given:
    GroovySpy(Enclosing.Super, global: true)

    when:
    Enclosing.Super.NON_EXISTING_FIELD = ""
    then:
    def ex = thrown(MissingPropertyException)
    ex.message.contains(" NON_EXISTING_FIELD ")
    def suppressed = ex.getSuppressed()[0].cause
    suppressed instanceof MissingMethodException
    suppressed.message.contains("setNON_EXISTING_FIELD")
  }

  def "Accessing static field through global spy shall only log single invocation"() {
    given:
    GroovySpy(Enclosing.Super, global: true)

    when:
    def result = Enclosing.Super.STATIC_FIELD
    then:
    1 * _
    0 * _
    result == result
  }

  /**
   * This test should verify that the resolution logic around fields and properties
   * works identically when normal Groovy logic is used and when a global spy
   * intercepts but no stubbing was done.
   */
  def "Accessing real fields through global spy works like in reality"() {
    given:
    if (createSpies) {
      GroovySpy(Enclosing.Super, global: true)
      GroovySpy(Enclosing.Sub, global: true)
      GroovySpy(Enclosing.ShadowingSub, global: true)
    }

    and:
    def supr = new Enclosing.Super()
    def sub = new Enclosing.Sub()
    def shadowingSub = new Enclosing.ShadowingSub()

    expect:
    verifyAll {
      Enclosing.Super.STATIC_FIELD == "super static"
      Enclosing.Super.PUBLIC_STATIC_FIELD == "public super static"
      Enclosing.Super.STATIC_PROPERTY == "super static property"
      Enclosing.Super.getSTATIC_PROPERTY() == "super static property"
      supr.STATIC_FIELD == "super static"
      supr.PUBLIC_STATIC_FIELD == "public super static"
      supr.STATIC_PROPERTY == "super static property"
      supr.getSTATIC_PROPERTY() == "super static property"
      supr.FIELD == "super"
      supr.PUBLIC_FIELD == "public super"
      supr.PROPERTY == "super property"
      Enclosing.Super.retrieveEnclosingField() == "enclosing"
      Enclosing.Super.retrievePublicEnclosingField() == "public enclosing"
      Enclosing.Super.retrieveEnclosingProperty() == "enclosing property"
      Enclosing.Super.retrieveStaticField() == "super static"
      Enclosing.Super.retrievePublicStaticField() == "public super static"
      Enclosing.Super.retrieveStaticProperty() == "super static property"
      supr.retrieveEnclosingField() == "enclosing"
      supr.retrievePublicEnclosingField() == "public enclosing"
      supr.retrieveEnclosingProperty() == "enclosing property"
      supr.retrieveStaticField() == "super static"
      supr.retrievePublicStaticField() == "public super static"
      supr.retrieveStaticProperty() == "super static property"
      supr.retrieveEnclosingFieldFromInstance() == "enclosing"
      supr.retrievePublicEnclosingFieldFromInstance() == "public enclosing"
      supr.retrieveEnclosingPropertyFromInstance() == "enclosing property"
      supr.retrieveStaticFieldFromInstance() == "super static"
      supr.retrievePublicStaticFieldFromInstance() == "public super static"
      supr.retrieveStaticPropertyFromInstance() == "super static property"
      supr.retrieveField() == "super"
      supr.retrievePublicField() == "public super"
      supr.retrieveProperty() == "super property"

      Enclosing.Sub.PUBLIC_STATIC_FIELD == "public super static"
      Enclosing.Sub.STATIC_PROPERTY == "super static property"
      Enclosing.Sub.getSTATIC_PROPERTY() == "super static property"
      sub.PUBLIC_STATIC_FIELD == "public super static"
      sub.STATIC_PROPERTY == "super static property"
      sub.getSTATIC_PROPERTY() == "super static property"
      sub.PUBLIC_FIELD == "public super"
      sub.PROPERTY == "super property"
      Enclosing.Sub.retrieveEnclosingField() == "enclosing"
      Enclosing.Sub.retrievePublicEnclosingField() == "public enclosing"
      Enclosing.Sub.retrieveEnclosingProperty() == "enclosing property"
      Enclosing.Sub.retrieveStaticField() == "super static"
      Enclosing.Sub.retrievePublicStaticField() == "public super static"
      Enclosing.Sub.retrieveStaticProperty() == "super static property"
      sub.retrieveEnclosingField() == "enclosing"
      sub.retrievePublicEnclosingField() == "public enclosing"
      sub.retrieveEnclosingProperty() == "enclosing property"
      sub.retrieveStaticField() == "super static"
      sub.retrievePublicStaticField() == "public super static"
      sub.retrieveStaticProperty() == "super static property"
      sub.retrieveEnclosingFieldFromInstance() == "enclosing"
      sub.retrievePublicEnclosingFieldFromInstance() == "public enclosing"
      sub.retrieveEnclosingPropertyFromInstance() == "enclosing property"
      sub.retrieveStaticFieldFromInstance() == "super static"
      sub.retrievePublicStaticFieldFromInstance() == "public super static"
      sub.retrieveStaticPropertyFromInstance() == "super static property"
      sub.retrieveField() == "super"
      sub.retrievePublicField() == "public super"
      sub.retrieveProperty() == "super property"
      Enclosing.Sub.SUB_STATIC_FIELD == "sub static"
      Enclosing.Sub.PUBLIC_SUB_STATIC_FIELD == "public sub static"
      Enclosing.Sub.SUB_STATIC_PROPERTY == "sub static property"
      Enclosing.Sub.getSUB_STATIC_PROPERTY() == "sub static property"
      sub.SUB_STATIC_FIELD == "sub static"
      sub.PUBLIC_SUB_STATIC_FIELD == "public sub static"
      sub.SUB_STATIC_PROPERTY == "sub static property"
      sub.getSUB_STATIC_PROPERTY() == "sub static property"
      sub.SUB_FIELD == "sub"
      sub.PUBLIC_SUB_FIELD == "public sub"
      sub.SUB_PROPERTY == "sub property"
      sub.getSUB_PROPERTY() == "sub property"
      Enclosing.Sub.retrieveSuperPublicStaticField() == "public super static"
      Enclosing.Sub.retrieveSuperStaticProperty() == "super static property"
      sub.retrieveSuperPublicStaticField() == "public super static"
      sub.retrieveSuperStaticProperty() == "super static property"
      Enclosing.Sub.retrieveSubStaticField() == "sub static"
      Enclosing.Sub.retrievePublicSubStaticField() == "public sub static"
      Enclosing.Sub.retrieveSubStaticProperty() == "sub static property"
      sub.retrieveSubStaticField() == "sub static"
      sub.retrievePublicSubStaticField() == "public sub static"
      sub.retrieveSubStaticProperty() == "sub static property"
      sub.retrieveSuperPublicStaticFieldFromInstance() == "public super static"
      sub.retrieveSuperStaticPropertyFromInstance() == "super static property"
      sub.retrieveSubStaticFieldFromInstance() == "sub static"
      sub.retrievePublicSubStaticFieldFromInstance() == "public sub static"
      sub.retrieveSubStaticPropertyFromInstance() == "sub static property"
      sub.retrieveSuperPublicField() == "public super"
      sub.retrieveSuperProperty() == "super property"
      sub.retrieveSubField() == "sub"
      sub.retrievePublicSubField() == "public sub"
      sub.retrieveSubProperty() == "sub property"

      Enclosing.ShadowingSub.STATIC_FIELD == "shadowing sub static"
      Enclosing.ShadowingSub.PUBLIC_STATIC_FIELD == "public shadowing sub static"
      Enclosing.ShadowingSub.STATIC_PROPERTY == "shadowing sub static property"
      Enclosing.ShadowingSub.getSTATIC_PROPERTY() == "shadowing sub static property"
      shadowingSub.STATIC_FIELD == "shadowing sub static"
      shadowingSub.PUBLIC_STATIC_FIELD == "public shadowing sub static"
      shadowingSub.STATIC_PROPERTY == "shadowing sub static property"
      shadowingSub.getSTATIC_PROPERTY() == "shadowing sub static property"
      shadowingSub.FIELD == "shadowing sub"
      shadowingSub.PUBLIC_FIELD == "public shadowing sub"
      shadowingSub.PROPERTY == "shadowing sub property"
      shadowingSub.getPROPERTY() == "shadowing sub property"
      Enclosing.ShadowingSub.retrieveStaticField() == "super static"
      Enclosing.ShadowingSub.retrievePublicStaticField() == "public super static"
      Enclosing.ShadowingSub.retrieveStaticProperty() == "super static property"
      shadowingSub.retrieveStaticField() == "super static"
      shadowingSub.retrievePublicStaticField() == "public super static"
      shadowingSub.retrieveStaticProperty() == "super static property"
      shadowingSub.retrieveStaticFieldFromInstance() == "super static"
      shadowingSub.retrievePublicStaticFieldFromInstance() == "public super static"
      shadowingSub.retrieveStaticPropertyFromInstance() == "super static property"
      shadowingSub.retrieveField() == "super"
      shadowingSub.retrievePublicField() == "public super"
      shadowingSub.retrieveProperty() == "super property"
      Enclosing.ShadowingSub.retrieveSubStaticField() == "shadowing sub static"
      Enclosing.ShadowingSub.retrievePublicSubStaticField() == "public shadowing sub static"
      Enclosing.ShadowingSub.retrieveSubStaticProperty() == "shadowing sub static property"
      shadowingSub.retrieveSubStaticField() == "shadowing sub static"
      shadowingSub.retrievePublicSubStaticField() == "public shadowing sub static"
      shadowingSub.retrieveSubStaticProperty() == "shadowing sub static property"
      shadowingSub.retrieveSubStaticFieldFromInstance() == "shadowing sub static"
      shadowingSub.retrievePublicSubStaticFieldFromInstance() == "public shadowing sub static"
      shadowingSub.retrieveSubStaticPropertyFromInstance() == "shadowing sub static property"
      shadowingSub.retrieveSuperPublicField() == "public super"
      shadowingSub.retrieveSuperProperty() == "super property"
      shadowingSub.retrieveSubField() == "shadowing sub"
      shadowingSub.retrievePublicSubField() == "public shadowing sub"
      shadowingSub.retrieveSubProperty() == "shadowing sub property"
    }

    when:
    Enclosing.Super.getSTATIC_FIELD() == "super static"

    then:
    thrown(MissingMethodException)

    when:
    Enclosing.Super.getPUBLIC_STATIC_FIELD() == "public super static"

    then:
    thrown(MissingMethodException)

    when:
    supr.getSTATIC_FIELD() == "super static"

    then:
    thrown(MissingMethodException)

    when:
    supr.getPUBLIC_STATIC_FIELD() == "public super static"

    then:
    thrown(MissingMethodException)

    when:
    supr.getFIELD() == "super"

    then:
    thrown(MissingMethodException)

    when:
    supr.getPUBLIC_FIELD() == "public super"

    then:
    thrown(MissingMethodException)

    when:
    supr.getPROPERTY() == "super property"

    then:
    thrown(MissingMethodException)

    when:
    Enclosing.Sub.STATIC_FIELD == "super static"

    then:
    thrown(MissingPropertyException)

    when:
    Enclosing.Sub.getSTATIC_FIELD() == "super static"

    then:
    thrown(MissingMethodException)

    when:
    Enclosing.Sub.getPUBLIC_STATIC_FIELD() == "public super static"

    then:
    thrown(MissingMethodException)

    when:
    sub.STATIC_FIELD == "super static"

    then:
    thrown(MissingPropertyException)

    when:
    sub.getSTATIC_FIELD() == "super static"

    then:
    thrown(MissingMethodException)

    when:
    sub.getPUBLIC_STATIC_FIELD() == "public super static"

    then:
    thrown(MissingMethodException)

    when:
    sub.FIELD == "super"

    then:
    thrown(MissingPropertyException)

    when:
    sub.getFIELD() == "super"

    then:
    thrown(MissingMethodException)

    when:
    sub.getPUBLIC_FIELD() == "public super"

    then:
    thrown(MissingMethodException)

    when:
    sub.getPROPERTY() == "super property"

    then:
    thrown(MissingMethodException)

    when:
    Enclosing.Sub.getSUB_STATIC_FIELD() == "sub static"

    then:
    thrown(MissingMethodException)

    when:
    Enclosing.Sub.getPUBLIC_SUB_STATIC_FIELD() == "public sub static"

    then:
    thrown(MissingMethodException)

    when:
    sub.getSUB_STATIC_FIELD() == "sub static"

    then:
    thrown(MissingMethodException)

    when:
    sub.getPUBLIC_SUB_STATIC_FIELD() == "public sub static"

    then:
    thrown(MissingMethodException)

    when:
    sub.getSUB_FIELD() == "sub"

    then:
    thrown(MissingMethodException)

    when:
    sub.getPUBLIC_SUB_FIELD() == "public sub"

    then:
    thrown(MissingMethodException)

    when:
    Enclosing.Sub.retrieveSuperStaticField() == "super static"

    then:
    thrown(MissingPropertyException)

    when:
    sub.retrieveSuperStaticField() == "super static"

    then:
    thrown(MissingPropertyException)

    when:
    sub.retrieveSuperStaticFieldFromInstance() == "super static"

    then:
    thrown(MissingPropertyException)

    when:
    sub.retrieveSuperField() == "super"

    then:
    thrown(MissingPropertyException)

    when:
    Enclosing.ShadowingSub.getSTATIC_FIELD() == "shadowing sub static"

    then:
    thrown(MissingMethodException)

    when:
    Enclosing.ShadowingSub.getPUBLIC_STATIC_FIELD() == "public shadowing sub static"

    then:
    thrown(MissingMethodException)

    when:
    shadowingSub.getSTATIC_FIELD() == "shadowing sub static"

    then:
    thrown(MissingMethodException)

    when:
    shadowingSub.getPUBLIC_STATIC_FIELD() == "public shadowing sub static"

    then:
    thrown(MissingMethodException)

    when:
    shadowingSub.getFIELD() == "shadowing sub"

    then:
    thrown(MissingMethodException)

    when:
    shadowingSub.getPUBLIC_FIELD() == "public shadowing sub"

    then:
    thrown(MissingMethodException)

    when:
    shadowingSub.retrieveSuperField() == "super"

    then:
    thrown((GroovyRuntimeUtil.MAJOR_VERSION >= 4) ? MissingPropertyException : MissingMethodException)

    when:
    def i = 0
    Enclosing.Super.STATIC_FIELD = "foo ${++i}"

    then:
    Enclosing.Super.STATIC_FIELD == "foo $i"

    when:
    Enclosing.Super.PUBLIC_STATIC_FIELD = "foo ${++i}"

    then:
    Enclosing.Super.PUBLIC_STATIC_FIELD == "foo $i"

    when:
    Enclosing.Super.STATIC_PROPERTY = "foo ${++i}"

    then:
    Enclosing.Super.STATIC_PROPERTY == "foo $i"

    when:
    Enclosing.Super.setSTATIC_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    Enclosing.Super.setPUBLIC_STATIC_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    Enclosing.Super.setSTATIC_PROPERTY("foo ${++i}")

    then:
    Enclosing.Super.getSTATIC_PROPERTY() == "foo $i"

    when:
    supr.STATIC_FIELD = "foo ${++i}"

    then:
    supr.STATIC_FIELD == "foo $i"

    when:
    supr.PUBLIC_STATIC_FIELD = "foo ${++i}"

    then:
    supr.PUBLIC_STATIC_FIELD == "foo $i"

    when:
    supr.STATIC_PROPERTY = "foo ${++i}"

    then:
    supr.STATIC_PROPERTY == "foo $i"

    when:
    supr.setSTATIC_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    supr.setPUBLIC_STATIC_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    supr.setSTATIC_PROPERTY("foo ${++i}")

    then:
    supr.getSTATIC_PROPERTY() == "foo $i"

    when:
    supr.FIELD = "foo ${++i}"

    then:
    supr.FIELD == "foo $i"

    when:
    supr.PUBLIC_FIELD = "foo ${++i}"

    then:
    supr.PUBLIC_FIELD == "foo $i"

    when:
    supr.PROPERTY = "foo ${++i}"

    then:
    supr.PROPERTY == "foo $i"

    when:
    supr.setFIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    supr.setPUBLIC_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    supr.setPROPERTY("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    Enclosing.Super.overwriteEnclosingField("foo ${++i}")

    then:
    Enclosing.Super.retrieveEnclosingField() == "foo $i"

    when:
    Enclosing.Super.overwritePublicEnclosingField("foo ${++i}")

    then:
    Enclosing.Super.retrievePublicEnclosingField() == "foo $i"

    when:
    Enclosing.Super.overwriteEnclosingProperty("foo ${++i}")

    then:
    Enclosing.Super.retrieveEnclosingProperty() == "foo $i"

    when:
    Enclosing.Super.overwriteStaticField("foo ${++i}")

    then:
    Enclosing.Super.retrieveStaticField() == "foo $i"

    when:
    Enclosing.Super.overwritePublicStaticField("foo ${++i}")

    then:
    Enclosing.Super.retrievePublicStaticField() == "foo $i"

    when:
    Enclosing.Super.overwriteStaticProperty("foo ${++i}")

    then:
    Enclosing.Super.retrieveStaticProperty() == "foo $i"

    when:
    supr.overwriteEnclosingField("foo ${++i}")

    then:
    supr.retrieveEnclosingField() == "foo $i"

    when:
    supr.overwritePublicEnclosingField("foo ${++i}")

    then:
    supr.retrievePublicEnclosingField() == "foo $i"

    when:
    supr.overwriteEnclosingProperty("foo ${++i}")

    then:
    supr.retrieveEnclosingProperty() == "foo $i"

    when:
    supr.overwriteStaticField("foo ${++i}")

    then:
    supr.retrieveStaticField() == "foo $i"

    when:
    supr.overwritePublicStaticField("foo ${++i}")

    then:
    supr.retrievePublicStaticField() == "foo $i"

    when:
    supr.overwriteStaticProperty("foo ${++i}")

    then:
    supr.retrieveStaticProperty() == "foo $i"

    when:
    supr.overwriteEnclosingFieldFromInstance("foo ${++i}")

    then:
    supr.retrieveEnclosingFieldFromInstance() == "foo $i"

    when:
    supr.overwritePublicEnclosingFieldFromInstance("foo ${++i}")

    then:
    supr.retrievePublicEnclosingFieldFromInstance() == "foo $i"

    when:
    supr.overwriteEnclosingPropertyFromInstance("foo ${++i}")

    then:
    supr.retrieveEnclosingPropertyFromInstance() == "foo $i"

    when:
    supr.overwriteStaticFieldFromInstance("foo ${++i}")

    then:
    supr.retrieveStaticFieldFromInstance() == "foo $i"

    when:
    supr.overwritePublicStaticFieldFromInstance("foo ${++i}")

    then:
    supr.retrievePublicStaticFieldFromInstance() == "foo $i"

    when:
    supr.overwriteStaticPropertyFromInstance("foo ${++i}")

    then:
    supr.retrieveStaticPropertyFromInstance() == "foo $i"

    when:
    supr.overwriteField("foo ${++i}")

    then:
    supr.retrieveField() == "foo $i"

    when:
    supr.overwritePublicField("foo ${++i}")

    then:
    supr.retrievePublicField() == "foo $i"

    when:
    supr.overwriteProperty("foo ${++i}")

    then:
    supr.retrieveProperty() == "foo $i"

    when:
    Enclosing.Sub.STATIC_FIELD = "foo ${++i}"

    then:
    thrown(MissingPropertyException)

    when:
    Enclosing.Sub.PUBLIC_STATIC_FIELD = "foo ${++i}"

    then:
    Enclosing.Sub.PUBLIC_STATIC_FIELD == "foo $i"

    when:
    Enclosing.Sub.STATIC_PROPERTY = "foo ${++i}"

    then:
    Enclosing.Sub.STATIC_PROPERTY == "foo $i"

    when:
    Enclosing.Sub.setSTATIC_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    Enclosing.Sub.setPUBLIC_STATIC_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    Enclosing.Sub.setSTATIC_PROPERTY("foo ${++i}")

    then:
    Enclosing.Sub.getSTATIC_PROPERTY() == "foo $i"

    when:
    sub.STATIC_FIELD = "foo ${++i}"

    then:
    thrown(MissingPropertyException)

    when:
    sub.PUBLIC_STATIC_FIELD = "foo ${++i}"

    then:
    sub.PUBLIC_STATIC_FIELD == "foo $i"

    when:
    sub.STATIC_PROPERTY = "foo ${++i}"

    then:
    sub.STATIC_PROPERTY == "foo $i"

    when:
    sub.setSTATIC_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    sub.setPUBLIC_STATIC_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    sub.setSTATIC_PROPERTY("foo ${++i}")

    then:
    sub.getSTATIC_PROPERTY() == "foo $i"

    when:
    sub.FIELD = "foo ${++i}"

    then:
    thrown(MissingPropertyException)

    when:
    sub.PUBLIC_FIELD = "foo ${++i}"

    then:
    sub.PUBLIC_FIELD == "foo $i"

    when:
    sub.setFIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    sub.setPUBLIC_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    sub.setPROPERTY("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    sub.PROPERTY = "foo ${++i}"

    then:
    sub.PROPERTY == "foo $i"

    when:
    Enclosing.Sub.overwriteEnclosingField("foo ${++i}")

    then:
    Enclosing.Sub.retrieveEnclosingField() == "foo $i"

    when:
    Enclosing.Sub.overwritePublicEnclosingField("foo ${++i}")

    then:
    Enclosing.Sub.retrievePublicEnclosingField() == "foo $i"

    when:
    Enclosing.Sub.overwriteEnclosingProperty("foo ${++i}")

    then:
    Enclosing.Sub.retrieveEnclosingProperty() == "foo $i"

    when:
    Enclosing.Sub.overwriteStaticField("foo ${++i}")

    then:
    Enclosing.Sub.retrieveStaticField() == "foo $i"

    when:
    Enclosing.Sub.overwritePublicStaticField("foo ${++i}")

    then:
    Enclosing.Sub.retrievePublicStaticField() == "foo $i"

    when:
    Enclosing.Sub.overwriteStaticProperty("foo ${++i}")

    then:
    Enclosing.Sub.retrieveStaticProperty() == "foo $i"

    when:
    sub.overwriteEnclosingField("foo ${++i}")

    then:
    sub.retrieveEnclosingField() == "foo $i"

    when:
    sub.overwritePublicEnclosingField("foo ${++i}")

    then:
    sub.retrievePublicEnclosingField() == "foo $i"

    when:
    sub.overwriteEnclosingProperty("foo ${++i}")

    then:
    sub.retrieveEnclosingProperty() == "foo $i"

    when:
    sub.overwriteStaticField("foo ${++i}")

    then:
    sub.retrieveStaticField() == "foo $i"

    when:
    sub.overwritePublicStaticField("foo ${++i}")

    then:
    sub.retrievePublicStaticField() == "foo $i"

    when:
    sub.overwriteStaticProperty("foo ${++i}")

    then:
    sub.retrieveStaticProperty() == "foo $i"

    when:
    sub.overwriteEnclosingFieldFromInstance("foo ${++i}")

    then:
    sub.retrieveEnclosingFieldFromInstance() == "foo $i"

    when:
    sub.overwritePublicEnclosingFieldFromInstance("foo ${++i}")

    then:
    sub.retrievePublicEnclosingFieldFromInstance() == "foo $i"

    when:
    sub.overwriteEnclosingPropertyFromInstance("foo ${++i}")

    then:
    sub.retrieveEnclosingPropertyFromInstance() == "foo $i"

    when:
    sub.overwriteStaticFieldFromInstance("foo ${++i}")

    then:
    sub.retrieveStaticFieldFromInstance() == "foo $i"

    when:
    sub.overwritePublicStaticFieldFromInstance("foo ${++i}")

    then:
    sub.retrievePublicStaticFieldFromInstance() == "foo $i"

    when:
    sub.overwriteStaticPropertyFromInstance("foo ${++i}")

    then:
    sub.retrieveStaticPropertyFromInstance() == "foo $i"

    when:
    sub.overwriteField("foo ${++i}")

    then:
    sub.retrieveField() == "foo $i"

    when:
    sub.overwritePublicField("foo ${++i}")

    then:
    sub.retrievePublicField() == "foo $i"

    when:
    sub.overwriteProperty("foo ${++i}")

    then:
    sub.retrieveProperty() == "foo $i"

    when:
    Enclosing.Sub.SUB_STATIC_FIELD = "foo ${++i}"

    then:
    Enclosing.Sub.SUB_STATIC_FIELD == "foo $i"

    when:
    Enclosing.Sub.PUBLIC_SUB_STATIC_FIELD = "foo ${++i}"

    then:
    Enclosing.Sub.PUBLIC_SUB_STATIC_FIELD == "foo $i"

    when:
    Enclosing.Sub.SUB_STATIC_PROPERTY = "foo ${++i}"

    then:
    Enclosing.Sub.SUB_STATIC_PROPERTY == "foo $i"

    when:
    Enclosing.Sub.setSUB_STATIC_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    Enclosing.Sub.setPUBLIC_SUB_STATIC_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    Enclosing.Sub.setSUB_STATIC_PROPERTY("foo ${++i}")

    then:
    Enclosing.Sub.getSUB_STATIC_PROPERTY() == "foo $i"

    when:
    sub.SUB_STATIC_FIELD = "foo ${++i}"

    then:
    sub.SUB_STATIC_FIELD == "foo $i"

    when:
    sub.PUBLIC_SUB_STATIC_FIELD = "foo ${++i}"

    then:
    sub.PUBLIC_SUB_STATIC_FIELD == "foo $i"

    when:
    sub.SUB_STATIC_PROPERTY = "foo ${++i}"

    then:
    sub.SUB_STATIC_PROPERTY == "foo $i"

    when:
    sub.setSUB_STATIC_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    sub.setPUBLIC_SUB_STATIC_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    sub.setSUB_STATIC_PROPERTY("foo ${++i}")

    then:
    sub.getSUB_STATIC_PROPERTY() == "foo $i"

    when:
    sub.SUB_FIELD = "foo ${++i}"

    then:
    sub.SUB_FIELD == "foo $i"

    when:
    sub.PUBLIC_SUB_FIELD = "foo ${++i}"

    then:
    sub.PUBLIC_SUB_FIELD == "foo $i"

    when:
    sub.SUB_PROPERTY = "foo ${++i}"

    then:
    sub.SUB_PROPERTY == "foo $i"

    when:
    sub.setSUB_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    sub.setPUBLIC_SUB_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    sub.setSUB_PROPERTY("foo ${++i}")

    then:
    sub.getSUB_PROPERTY() == "foo $i"

    when:
    Enclosing.Sub.overwriteSuperStaticField("foo ${++i}")

    then:
    thrown(MissingPropertyException)

    when:
    Enclosing.Sub.overwriteSuperPublicStaticField("foo ${++i}")

    then:
    Enclosing.Sub.retrieveSuperPublicStaticField() == "foo $i"

    when:
    Enclosing.Sub.overwriteSuperStaticProperty("foo ${++i}")

    then:
    Enclosing.Sub.retrieveSuperStaticProperty() == "foo $i"

    when:
    sub.overwriteSuperStaticField("foo ${++i}")

    then:
    thrown(MissingPropertyException)

    when:
    sub.overwriteSuperPublicStaticField("foo ${++i}")

    then:
    sub.retrieveSuperPublicStaticField() == "foo $i"

    when:
    sub.overwriteSuperStaticProperty("foo ${++i}")

    then:
    sub.retrieveSuperStaticProperty() == "foo $i"

    when:
    Enclosing.Sub.overwriteSubStaticField("foo ${++i}")

    then:
    Enclosing.Sub.retrieveSubStaticField() == "foo $i"

    when:
    Enclosing.Sub.overwritePublicSubStaticField("foo ${++i}")

    then:
    Enclosing.Sub.retrievePublicSubStaticField() == "foo $i"

    when:
    Enclosing.Sub.overwriteSubStaticProperty("foo ${++i}")

    then:
    Enclosing.Sub.retrieveSubStaticProperty() == "foo $i"

    when:
    sub.overwriteSubStaticField("foo ${++i}")

    then:
    sub.retrieveSubStaticField() == "foo $i"

    when:
    sub.overwritePublicSubStaticField("foo ${++i}")

    then:
    sub.retrievePublicSubStaticField() == "foo $i"

    when:
    sub.overwriteSubStaticProperty("foo ${++i}")

    then:
    sub.retrieveSubStaticProperty() == "foo $i"

    when:
    sub.overwriteSuperStaticFieldFromInstance("foo ${++i}")

    then:
    thrown(MissingPropertyException)

    when:
    sub.overwriteSuperPublicStaticFieldFromInstance("foo ${++i}")

    then:
    sub.retrieveSuperPublicStaticFieldFromInstance() == "foo $i"

    when:
    sub.overwriteSuperStaticPropertyFromInstance("foo ${++i}")

    then:
    sub.retrieveSuperStaticPropertyFromInstance() == "foo $i"

    when:
    sub.overwriteSubStaticFieldFromInstance("foo ${++i}")

    then:
    sub.retrieveSubStaticFieldFromInstance() == "foo $i"

    when:
    sub.overwritePublicSubStaticFieldFromInstance("foo ${++i}")

    then:
    sub.retrievePublicSubStaticFieldFromInstance() == "foo $i"

    when:
    sub.overwriteSubStaticPropertyFromInstance("foo ${++i}")

    then:
    sub.retrieveSubStaticPropertyFromInstance() == "foo $i"

    when:
    sub.overwriteSuperField("foo ${++i}")

    then:
    thrown(MissingPropertyException)

    when:
    sub.overwriteSuperPublicField("foo ${++i}")

    then:
    sub.retrieveSuperPublicField() == "foo $i"

    when:
    sub.overwriteSuperProperty("foo ${++i}")

    then:
    sub.retrieveSuperProperty() == "foo $i"

    when:
    sub.overwriteSubField("foo ${++i}")

    then:
    sub.retrieveSubField() == "foo $i"

    when:
    sub.overwritePublicSubField("foo ${++i}")

    then:
    sub.retrievePublicSubField() == "foo $i"

    when:
    sub.overwriteSubProperty("foo ${++i}")

    then:
    sub.retrieveSubProperty() == "foo $i"

    when:
    Enclosing.ShadowingSub.STATIC_FIELD = "foo ${++i}"

    then:
    Enclosing.ShadowingSub.STATIC_FIELD == "foo $i"

    when:
    Enclosing.ShadowingSub.PUBLIC_STATIC_FIELD = "foo ${++i}"

    then:
    Enclosing.ShadowingSub.PUBLIC_STATIC_FIELD == "foo $i"

    when:
    Enclosing.ShadowingSub.STATIC_PROPERTY = "foo ${++i}"

    then:
    Enclosing.ShadowingSub.STATIC_PROPERTY == "foo $i"

    when:
    Enclosing.ShadowingSub.setSTATIC_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    Enclosing.ShadowingSub.setPUBLIC_STATIC_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    Enclosing.ShadowingSub.setSTATIC_PROPERTY("foo ${++i}")

    then:
    Enclosing.ShadowingSub.getSTATIC_PROPERTY() == "foo $i"

    when:
    shadowingSub.STATIC_FIELD = "foo ${++i}"

    then:
    shadowingSub.STATIC_FIELD == "foo $i"

    when:
    shadowingSub.PUBLIC_STATIC_FIELD = "foo ${++i}"

    then:
    shadowingSub.PUBLIC_STATIC_FIELD == "foo $i"

    when:
    shadowingSub.STATIC_PROPERTY = "foo ${++i}"

    then:
    shadowingSub.STATIC_PROPERTY == "foo $i"

    when:
    shadowingSub.setSTATIC_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    shadowingSub.setPUBLIC_STATIC_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    shadowingSub.setSTATIC_PROPERTY("foo ${++i}")

    then:
    shadowingSub.getSTATIC_PROPERTY() == "foo $i"

    when:
    shadowingSub.FIELD = "foo ${++i}"

    then:
    shadowingSub.FIELD == "foo $i"

    when:
    shadowingSub.PUBLIC_FIELD = "foo ${++i}"

    then:
    shadowingSub.PUBLIC_FIELD == "foo $i"

    when:
    shadowingSub.PROPERTY = "foo ${++i}"

    then:
    shadowingSub.PROPERTY == "foo $i"

    when:
    shadowingSub.setFIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    shadowingSub.setPUBLIC_FIELD("foo ${++i}")

    then:
    thrown(MissingMethodException)

    when:
    shadowingSub.setPROPERTY("foo ${++i}")

    then:
    shadowingSub.getPROPERTY() == "foo $i"

    when:
    Enclosing.ShadowingSub.overwriteStaticField("foo ${++i}")

    then:
    Enclosing.ShadowingSub.retrieveStaticField() == "foo $i"

    when:
    Enclosing.ShadowingSub.overwritePublicStaticField("foo ${++i}")

    then:
    Enclosing.ShadowingSub.retrievePublicStaticField() == "foo $i"

    when:
    Enclosing.ShadowingSub.overwriteStaticProperty("foo ${++i}")

    then:
    Enclosing.ShadowingSub.retrieveStaticProperty() == "foo $i"

    when:
    shadowingSub.overwriteStaticField("foo ${++i}")

    then:
    shadowingSub.retrieveStaticField() == "foo $i"

    when:
    shadowingSub.overwritePublicStaticField("foo ${++i}")

    then:
    shadowingSub.retrievePublicStaticField() == "foo $i"

    when:
    shadowingSub.overwriteStaticProperty("foo ${++i}")

    then:
    shadowingSub.retrieveStaticProperty() == "foo $i"

    when:
    shadowingSub.overwriteStaticFieldFromInstance("foo ${++i}")

    then:
    shadowingSub.retrieveStaticFieldFromInstance() == "foo $i"

    when:
    shadowingSub.overwritePublicStaticFieldFromInstance("foo ${++i}")

    then:
    shadowingSub.retrievePublicStaticFieldFromInstance() == "foo $i"

    when:
    shadowingSub.overwriteStaticPropertyFromInstance("foo ${++i}")

    then:
    shadowingSub.retrieveStaticPropertyFromInstance() == "foo $i"

    when:
    shadowingSub.overwriteField("foo ${++i}")

    then:
    shadowingSub.retrieveField() == "foo $i"

    when:
    shadowingSub.overwritePublicField("foo ${++i}")

    then:
    shadowingSub.retrievePublicField() == "foo $i"

    when:
    shadowingSub.overwriteProperty("foo ${++i}")

    then:
    shadowingSub.retrieveProperty() == "foo $i"

    when:
    Enclosing.ShadowingSub.overwriteSubStaticField("foo ${++i}")

    then:
    Enclosing.ShadowingSub.retrieveSubStaticField() == "foo $i"

    when:
    Enclosing.ShadowingSub.overwritePublicSubStaticField("foo ${++i}")

    then:
    Enclosing.ShadowingSub.retrievePublicSubStaticField() == "foo $i"

    when:
    Enclosing.ShadowingSub.overwriteSubStaticProperty("foo ${++i}")

    then:
    Enclosing.ShadowingSub.retrieveSubStaticProperty() == "foo $i"

    when:
    shadowingSub.overwriteSubStaticField("foo ${++i}")

    then:
    shadowingSub.retrieveSubStaticField() == "foo $i"

    when:
    shadowingSub.overwritePublicSubStaticField("foo ${++i}")

    then:
    shadowingSub.retrievePublicSubStaticField() == "foo $i"

    when:
    shadowingSub.overwriteSubStaticProperty("foo ${++i}")

    then:
    shadowingSub.retrieveSubStaticProperty() == "foo $i"

    when:
    shadowingSub.overwriteSubStaticFieldFromInstance("foo ${++i}")

    then:
    shadowingSub.retrieveSubStaticFieldFromInstance() == "foo $i"

    when:
    shadowingSub.overwritePublicSubStaticFieldFromInstance("foo ${++i}")

    then:
    shadowingSub.retrievePublicSubStaticFieldFromInstance() == "foo $i"

    when:
    shadowingSub.overwriteSubStaticPropertyFromInstance("foo ${++i}")

    then:
    shadowingSub.retrieveSubStaticPropertyFromInstance() == "foo $i"

    when:
    shadowingSub.overwriteSuperField("foo ${++i}")

    then:
    thrown(MissingPropertyException)

    when:
    shadowingSub.overwriteSuperPublicField("foo ${++i}")

    then:
    shadowingSub.retrieveSuperPublicField() == "foo $i"

    when:
    shadowingSub.overwriteSuperProperty("foo ${++i}")

    then:
    shadowingSub.retrieveSuperProperty() == "foo $i"

    when:
    shadowingSub.overwriteSubField("foo ${++i}")

    then:
    shadowingSub.retrieveSubField() == "foo $i"

    when:
    shadowingSub.overwritePublicSubField("foo ${++i}")

    then:
    shadowingSub.retrievePublicSubField() == "foo $i"

    when:
    shadowingSub.overwriteSubProperty("foo ${++i}")

    then:
    shadowingSub.retrieveSubProperty() == "foo $i"

    cleanup:
    resetStatics()

    where:
    createSpies << [false, true]
  }

  static class Person {
    String name
    int age

    String perform(String work) { "done" }
    final String performFinal(String work) { "done" }

    Person(String name = "fred", int age = 42) {
      this.name = name
      this.age = age
    }
  }

  static final class FinalPerson {
    String name
    int age

    String perform(String work) { "done" }

    FinalPerson(String name = "fred", int age = 42) {
      this.name = name
      this.age = age
    }
  }

  static class PersonWithOverloadedMethods {
    static String perform(String work) {
      "String"
    }

    static String perform(Pattern work) {
      "Pattern"
    }
  }

  def resetStatics() {
    Enclosing.resetStatics()
    Enclosing.Super.resetStatics()
    Enclosing.Sub.resetStatics()
    Enclosing.ShadowingSub.resetStatics()
  }

  static class Enclosing {
    private static String ENCLOSING_FIELD
    public static String PUBLIC_ENCLOSING_FIELD
    static String ENCLOSING_PROPERTY

    static {
      resetStatics()
    }

    static void resetStatics() {
      ENCLOSING_FIELD = "enclosing"
      PUBLIC_ENCLOSING_FIELD = "public enclosing"
      ENCLOSING_PROPERTY = "enclosing property"
    }

    static class Super {
      private static String STATIC_FIELD
      public static String PUBLIC_STATIC_FIELD
      static String STATIC_PROPERTY
      private String FIELD = "super"
      public String PUBLIC_FIELD = "public super"
      public String PROPERTY = "super property"

      static {
        resetStatics()
      }

      static void resetStatics() {
        STATIC_FIELD = "super static"
        PUBLIC_STATIC_FIELD = "public super static"
        STATIC_PROPERTY = "super static property"
      }

      static retrieveEnclosingField() {
        ENCLOSING_FIELD
      }

      static overwriteEnclosingField(def value) {
        ENCLOSING_FIELD = value
      }

      static retrievePublicEnclosingField() {
        PUBLIC_ENCLOSING_FIELD
      }

      static overwritePublicEnclosingField(def value) {
        PUBLIC_ENCLOSING_FIELD = value
      }

      static retrieveEnclosingProperty() {
        ENCLOSING_PROPERTY
      }

      static overwriteEnclosingProperty(def value) {
        ENCLOSING_PROPERTY = value
      }

      static retrieveStaticField() {
        STATIC_FIELD
      }

      static overwriteStaticField(def value) {
        STATIC_FIELD = value
      }

      static retrievePublicStaticField() {
        PUBLIC_STATIC_FIELD
      }

      static overwritePublicStaticField(def value) {
        PUBLIC_STATIC_FIELD = value
      }

      static retrieveStaticProperty() {
        STATIC_PROPERTY
      }

      static overwriteStaticProperty(def value) {
        STATIC_PROPERTY = value
      }

      def retrieveEnclosingFieldFromInstance() {
        ENCLOSING_FIELD
      }

      def overwriteEnclosingFieldFromInstance(def value) {
        ENCLOSING_FIELD = value
      }

      def retrievePublicEnclosingFieldFromInstance() {
        PUBLIC_ENCLOSING_FIELD
      }

      def overwritePublicEnclosingFieldFromInstance(def value) {
        PUBLIC_ENCLOSING_FIELD = value
      }

      def retrieveEnclosingPropertyFromInstance() {
        ENCLOSING_PROPERTY
      }

      def overwriteEnclosingPropertyFromInstance(def value) {
        ENCLOSING_PROPERTY = value
      }

      def retrieveStaticFieldFromInstance() {
        STATIC_FIELD
      }

      def overwriteStaticFieldFromInstance(def value) {
        STATIC_FIELD = value
      }

      def retrievePublicStaticFieldFromInstance() {
        PUBLIC_STATIC_FIELD
      }

      def overwritePublicStaticFieldFromInstance(def value) {
        PUBLIC_STATIC_FIELD = value
      }

      def retrieveStaticPropertyFromInstance() {
        STATIC_PROPERTY
      }

      def overwriteStaticPropertyFromInstance(def value) {
        STATIC_PROPERTY = value
      }

      def retrieveField() {
        FIELD
      }

      def overwriteField(def value) {
        FIELD = value
      }

      def retrievePublicField() {
        PUBLIC_FIELD
      }

      def overwritePublicField(def value) {
        PUBLIC_FIELD = value
      }

      def retrieveProperty() {
        PROPERTY
      }

      def overwriteProperty(def value) {
        PROPERTY = value
      }
    }

    static class Sub extends Super {
      private static String SUB_STATIC_FIELD
      public static String PUBLIC_SUB_STATIC_FIELD
      static String SUB_STATIC_PROPERTY
      private String SUB_FIELD = "sub"
      public String PUBLIC_SUB_FIELD = "public sub"
      String SUB_PROPERTY = "sub property"

      static {
        resetStatics()
      }

      static void resetStatics() {
        SUB_STATIC_FIELD = "sub static"
        PUBLIC_SUB_STATIC_FIELD = "public sub static"
        SUB_STATIC_PROPERTY = "sub static property"
      }

      static retrieveSuperStaticField() {
        STATIC_FIELD
      }

      static overwriteSuperStaticField(def value) {
        STATIC_FIELD = value
      }

      static retrieveSuperPublicStaticField() {
        PUBLIC_STATIC_FIELD
      }

      static overwriteSuperPublicStaticField(def value) {
        PUBLIC_STATIC_FIELD = value
      }

      static retrieveSuperStaticProperty() {
        STATIC_PROPERTY
      }

      static overwriteSuperStaticProperty(def value) {
        STATIC_PROPERTY = value
      }

      static retrieveSubStaticField() {
        SUB_STATIC_FIELD
      }

      static overwriteSubStaticField(def value) {
        SUB_STATIC_FIELD = value
      }

      static retrievePublicSubStaticField() {
        PUBLIC_SUB_STATIC_FIELD
      }

      static overwritePublicSubStaticField(def value) {
        PUBLIC_SUB_STATIC_FIELD = value
      }

      static retrieveSubStaticProperty() {
        SUB_STATIC_PROPERTY
      }

      static overwriteSubStaticProperty(def value) {
        SUB_STATIC_PROPERTY = value
      }

      def retrieveSuperStaticFieldFromInstance() {
        STATIC_FIELD
      }

      def overwriteSuperStaticFieldFromInstance(def value) {
        STATIC_FIELD = value
      }

      def retrieveSuperPublicStaticFieldFromInstance() {
        PUBLIC_STATIC_FIELD
      }

      def overwriteSuperPublicStaticFieldFromInstance(def value) {
        PUBLIC_STATIC_FIELD = value
      }

      def retrieveSuperStaticPropertyFromInstance() {
        STATIC_PROPERTY
      }

      def overwriteSuperStaticPropertyFromInstance(def value) {
        STATIC_PROPERTY = value
      }

      def retrieveSubStaticFieldFromInstance() {
        SUB_STATIC_FIELD
      }

      def overwriteSubStaticFieldFromInstance(def value) {
        SUB_STATIC_FIELD = value
      }

      def retrievePublicSubStaticFieldFromInstance() {
        PUBLIC_SUB_STATIC_FIELD
      }

      def overwritePublicSubStaticFieldFromInstance(def value) {
        PUBLIC_SUB_STATIC_FIELD = value
      }

      def retrieveSubStaticPropertyFromInstance() {
        SUB_STATIC_PROPERTY
      }

      def overwriteSubStaticPropertyFromInstance(def value) {
        SUB_STATIC_PROPERTY = value
      }

      def retrieveSuperField() {
        FIELD
      }

      def overwriteSuperField(def value) {
        FIELD = value
      }

      def retrieveSuperPublicField() {
        PUBLIC_FIELD
      }

      def overwriteSuperPublicField(def value) {
        PUBLIC_FIELD = value
      }

      def retrieveSuperProperty() {
        PROPERTY
      }

      def overwriteSuperProperty(def value) {
        PROPERTY = value
      }

      def retrieveSubField() {
        SUB_FIELD
      }

      def overwriteSubField(def value) {
        SUB_FIELD = value
      }

      def retrievePublicSubField() {
        PUBLIC_SUB_FIELD
      }

      def overwritePublicSubField(def value) {
        PUBLIC_SUB_FIELD = value
      }

      def retrieveSubProperty() {
        SUB_PROPERTY
      }

      def overwriteSubProperty(def value) {
        SUB_PROPERTY = value
      }
    }

    static class ShadowingSub extends Super {
      private static String STATIC_FIELD
      public static String PUBLIC_STATIC_FIELD
      static String STATIC_PROPERTY
      private String FIELD = "shadowing sub"
      public String PUBLIC_FIELD = "public shadowing sub"
      String PROPERTY = "shadowing sub property"

      static {
        resetStatics()
      }

      static void resetStatics() {
        STATIC_FIELD = "shadowing sub static"
        PUBLIC_STATIC_FIELD = "public shadowing sub static"
        STATIC_PROPERTY = "shadowing sub static property"
      }

      static retrieveSubStaticField() {
        STATIC_FIELD
      }

      static overwriteSubStaticField(def value) {
        STATIC_FIELD = value
      }

      static retrievePublicSubStaticField() {
        PUBLIC_STATIC_FIELD
      }

      static overwritePublicSubStaticField(def value) {
        PUBLIC_STATIC_FIELD = value
      }

      static retrieveSubStaticProperty() {
        STATIC_PROPERTY
      }

      static overwriteSubStaticProperty(def value) {
        STATIC_PROPERTY = value
      }

      def retrieveSubStaticFieldFromInstance() {
        STATIC_FIELD
      }

      def overwriteSubStaticFieldFromInstance(def value) {
        STATIC_FIELD = value
      }

      def retrievePublicSubStaticFieldFromInstance() {
        PUBLIC_STATIC_FIELD
      }

      def overwritePublicSubStaticFieldFromInstance(def value) {
        PUBLIC_STATIC_FIELD = value
      }

      def retrieveSubStaticPropertyFromInstance() {
        STATIC_PROPERTY
      }

      def overwriteSubStaticPropertyFromInstance(def value) {
        STATIC_PROPERTY = value
      }

      def retrieveSuperField() {
        super.FIELD
      }

      def overwriteSuperField(def value) {
        // `super.FIELD = value` is a compile error in Groovy <4
        // we work-around this by using `super."${"FIELD"}" = value` so that it is evaluated at runtime
        // in Groovy <4 this though would result in the own FIELD being read, not the super FIELD
        // hence we manually throw a MissingPropertyException here
        if (GroovyRuntimeUtil.MAJOR_VERSION <= 3) {
          throw new MissingPropertyException("FIELD", Super)
        }
        super."${"FIELD"}" = value
      }

      def retrieveSuperPublicField() {
        super.PUBLIC_FIELD
      }

      def overwriteSuperPublicField(def value) {
        super.PUBLIC_FIELD = value
      }

      def retrieveSuperProperty() {
        super.PROPERTY
      }

      def overwriteSuperProperty(def value) {
        super.PROPERTY = value
      }

      def retrieveSubField() {
        FIELD
      }

      def overwriteSubField(def value) {
        FIELD = value
      }

      def retrievePublicSubField() {
        PUBLIC_FIELD
      }

      def overwritePublicSubField(def value) {
        PUBLIC_FIELD = value
      }

      def retrieveSubProperty() {
        PROPERTY
      }

      def overwriteSubProperty(def value) {
        PROPERTY = value
      }
    }
  }
}
