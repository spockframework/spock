package org.spockframework.smoke.condition

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.SpockComparisonFailure
import spock.lang.Issue
import spock.lang.PendingFeature

class CircularToStringFailureSpec extends EmbeddedSpecification {

  @Issue("https://github.com/spockframework/spock/issues/2413")
  @PendingFeature
  def "SpockComparisonFailure.toString() does not throw StackOverflowError for circular toString()"() {
    when:
    runner.runFeatureBody '''
        given:
        def ref = new org.spockframework.smoke.condition.CircularToStringFailureSpec.CircularRef()
        ref.self = ref

        expect:
        ref == "not equal"
    '''

    then:
    SpockComparisonFailure failure = thrown()

    when:
    failure.toString()

    then:
    notThrown(StackOverflowError)
  }

  static class CircularRef {
    CircularRef self

    @Override
    String toString() {
      "CircularRef(self=" + String.valueOf(self) + ")"
    }
  }
}
