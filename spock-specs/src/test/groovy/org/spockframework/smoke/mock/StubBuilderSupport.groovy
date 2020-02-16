package org.spockframework.smoke.mock

import spock.lang.Specification

class StubBuilderSupport extends Specification {

  def "Stubs support builder pattern"() {
    given:
    BuilderExample builder = Stub() {
      build() >> "world"
    }

    expect:
    builder.withKey("foo").withValue("bar").build() == "world"
  }

  def "builder pattern does not apply for methods returning Object"() {
    given:
    BuilderExample builder = Stub() {
      build() >> "world"
    }

    expect:
    builder.someMethodReturningObject().getClass() == Object
  }

  interface BuilderExample {
    BuilderExample withValue(String v)
    BuilderExample withKey(String v)
    String build()
    Object someMethodReturningObject()
  }
}
