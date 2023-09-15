package org.spockframework.docs.primer

import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.smoke.condition.ConditionRenderingSpec
import spock.lang.FailsWith

class ConditionsSpec extends ConditionRenderingSpec {

  def "normal condition rendering"() {
    expect:
    isRendered(/* tag::normal-condition-result[] */"""
foo == bar
|   |  |
|   |  very long bar
|   false
|   3 differences (76% similarity)
|   very long (foo)
|   very long (bar)
very long foo

foobar
""", // end::normal-condition-result[]
      {
        // tag::normal-condition[]
        def foo = 'very long foo'
        def bar = 'very long bar'
        assert foo == bar : "foobar"
        // end::normal-condition[]
      }
    )
  }

  def "condition rendering can be disabled via opt-out operator"() {
    expect:
    isRendered(/* tag::explicit-with-opt-out-operator-and-message-result[] */"""
(foo == bar)

foobar
""", // end::explicit-with-opt-out-operator-and-message-result[]
      {
        // tag::explicit-with-opt-out-operator-and-message[]
        def foo = 'very long foo'
        def bar = 'very long bar'
        assert !!(foo == bar) : "foobar"
        // end::explicit-with-opt-out-operator-and-message[]
      }
    )
  }

  def "condition rendering can be disabled via opt-out operator without message"() {
    expect:
    isRendered(/* tag::explicit-with-opt-out-operator-result[] */"""
(foo == bar)
""",// end::explicit-with-opt-out-operator-result[]
      {
        def foo = 'very long foo'
        def bar = 'very long bar'
        // tag::explicit-with-opt-out-operator[]
        assert !!(foo == bar)
        // end::explicit-with-opt-out-operator[]
    })
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "using each-assert fails normally"(){
    given:
    def aList = []
    // tag::each-assert-normal[]
    expect:
    aList.each { assert it > 0 }
    // end::each-assert-normal[]
  }

  def "using each-assert with opt-out passes"(){
    given:
    def aList = []
    // tag::each-assert-opt-out-operator[]
    expect:
    !!aList.each { assert it > 0 }
    // end::each-assert-opt-out-operator[]
  }
}
