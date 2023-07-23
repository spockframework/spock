package org.spockframework.docs.utilities

import org.spockframework.runtime.SpockAssertionError
import spock.lang.FailsWith
import spock.lang.Specification
import spock.util.concurrent.AsyncConditions

class ConditionBlockSpec extends Specification {

  @FailsWith(SpockAssertionError)
  def "evaluate fails with implicit condition using @ConditionBlock semantics"() {
    // tag::ConditionBlock-usage[]
    AsyncConditions conds = new AsyncConditions()

    when:
    Thread.start {
      //The method AsyncConditions.evaluate() is annotated with @ConditionBlock
      conds.evaluate {
        //There is an implicit assert here
        false
      }
    }

    then:
    conds.await()
    // end::ConditionBlock-usage[]
  }
}
