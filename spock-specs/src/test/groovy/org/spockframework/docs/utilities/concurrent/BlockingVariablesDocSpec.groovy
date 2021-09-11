package org.spockframework.docs.utilities.concurrent

import spock.lang.Specification
import spock.util.concurrent.BlockingVariable
import spock.util.concurrent.BlockingVariables

class BlockingVariablesDocSpec extends Specification {

  // tag::blocking-variables-spec[]
  def "single variable is read before it is written"() {
    def list = new BlockingVariable<List<Integer>>()          // <1>

    when:
    Thread.start {
      Thread.sleep(25)          // <2>
      println "calling set"
      list.set([1, 2, 3])
    }

    then:
    println "calling get, blocking"
    list.get() == [1, 2, 3]          // <3>
  }

  def "example of multiple variables"() {
    def vars = new BlockingVariables(2.0)          // <4>

    when:
    Thread.start {
      Thread.sleep(50)          // <5>
      println "setting bar and baz"
      vars.bar = 2
      vars.baz = 3
    }
    Thread.start {
      Thread.sleep(25)          // <5>
      println "setting foo"
      vars.foo = 1
    }

    then:
    println "before comparison, blocking"
    vars.foo == 1          // <6>
    vars.bar == 2
    vars.baz == 3
  }
  // end::blocking-variables-spec[]
}
