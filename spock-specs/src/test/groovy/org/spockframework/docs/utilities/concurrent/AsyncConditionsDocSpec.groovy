package org.spockframework.docs.utilities.concurrent

import spock.lang.Specification
import spock.util.concurrent.AsyncConditions

class AsyncConditionsDocSpec extends Specification {

  // tag::async-conditions-spec[]
  def "example of single passing explicit evaluation"() {
    def conditions = new AsyncConditions()          // <1>

    when:
    Thread.start {          // <2>
      conditions.evaluate {          //<3>
        assert true
      }
    }

    then:
    conditions.await()          // <4>
  }

  def "example of multiple passing implicit evaluations"() {
    AsyncConditions conditions = new AsyncConditions(3)          // <5>

    when:
    Thread.start {
      conditions.evaluate {          // <6>
        true
        true
      }
      conditions.evaluate {          // <6>
        true
      }
    }

    and:
    Thread.start {
      conditions.evaluate {          // <6>
        true
      }
    }

    then:
    conditions.await(2.5)          // <7>
  }
  // end::async-conditions-spec[]
}
