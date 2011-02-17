package org.spockframework.smoke.condition

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.SpockComparisonFailure

import spock.lang.Issue

class DiffedObjectRendering extends EmbeddedSpecification {
  @Issue("http://issues.spockframework.org/detail?id=170")
  def "can handle null values"() {
    when:
    runner.runFeatureBody("expect: 1 == null")

    then:
    SpockComparisonFailure failure = thrown()
    failure.actual == "1\n"
    failure.expected == "null\n"
  }
}
