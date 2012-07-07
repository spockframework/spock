package org.spockframework.smoke.condition

import spock.lang.Specification

class ImplicitConditionsInWithBlocks extends Specification {
  def "basic usage"() {
    def list = [1, 2]

    expect:
    with(list) {
      size() == 2
      get(0) == 1
      get(1) == 2
    }
  }

  def "inlining"() {
    expect:
    with([1, 2]) {
      size() == 2
      get(0) == 1
      get(1) == 2
    }
  }

  def "nesting"() {
    def list = [1, 2]

    expect:
    with(list) {
      size() == 2
      get(0) == 1

      def map = [foo: "bar"]
      with(map) {
        size() == 1
        get("foo") == "bar"
      }

      get(1) == 2
    }
  }

  def "in then-block"() {
    when:
    def list = [1, 2]

    then:
    with(list) {
      size() == 2
      get(0) == 1
      get(1) == 2
    }
  }

  def "in nested position"() {
    when:
    def list = [1, 2]

    then:
    1.times {
      1.times {
        with(list) {
          size() == 2
          get(0) == 1
          get(1) == 2
        }
      }
    }
  }

  def "executed several times"() {
    when:
    def list = [1, 2]

    then:
    3.times {
      with(list) {
        size() == 2
        get(0) == 1
        get(1) == 2
      }
    }
  }

  def "in block other than then/expect"() {
    def list = [1, 2]

    setup:
    with(list) {
      size() == 2
      get(0) == 1
      get(1) == 2
    }
  }

  def "in helper method"() {
    def list = [1, 2]

    expect:
    helper(list)
  }

  def "statements in nested blocks aren't turned into conditions"() {
    def list = [1, 2]

    expect:
    with(list) {
      list.any {
        it == 2 // would fail if this was turned into a condition
      }
    }
  }

  void helper(list) {
    with(list) {
      size() == 2
      get(0) == 1
      get(1) == 2
    }
  }
}
