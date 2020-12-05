package org.spockframework.smoke

import org.spockframework.EmbeddedSpecification
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

class RecorderScopeEmbedded extends EmbeddedSpecification {
  @Issue("https://github.com/spockframework/spock/issues/1232")
  def "nested conditions without top-level condition"() {
    when:
    runner.runSpecBody """
      def foo() {
        expect:
        // there must not be any top-level condition in this test
        with([1,2]) {
          it.size() > 0
          it.each { assert it > 0 }
        }
      }

      def nestedAssert() {
        // there must not be any top-level condition in this test
        tap {
          assert 0
          tap { assert 0 }
        }
      }
    """

    then:
    noExceptionThrown()
  }
}
