package org.spockframework.smoke

import spock.lang.*

import groovy.util.logging.Log

@Issue("https://github.com/spockframework/spock/issues/783")
@Log // this triggered the error
class RecorderScope extends Specification{
  def "assert within closure"() {
    given:
    Closure sq = { x ->
      assert 1 == 1
      x * x
    }

    expect:
    sq(2) == 4
  }

  def "assert within external closure"() {
    given:
    Closure sq = externalClosure()

    expect:
    sq(2) == 4
  }

  def "in with blocks"() {
    given:
    def person = new Person()

    expect:
    person.age == 42
    with(person) {
      name == "Fred"
      age == 42
    }
  }

  def "method condition"() {
    expect:
    1 == 1
    with([1,2]) {
      size()== 2
    }
  }

  def "in verifyAll blocks"() {
    given:
    def p = new Person()

    expect:
    verifyAll(p) {
      name == 'Fred'
      age == 42
    }
  }

  Closure externalClosure() {
    Closure sq = { x ->
      assert 1 == 1
      x * x
    }
    return sq
  }

}
