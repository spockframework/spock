package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.ConditionNotSatisfiedError

class PendingFeatureIfExtensionSpec extends EmbeddedSpecification {

  def "@PendingFeatureIf marks failing feature as skipped if the condition passes and the test fails"() {
    when:
    def result = runner.runWithImports("""import spock.lang.PendingFeature
import spock.lang.PendingFeatureIf

class Foo extends Specification {
  @PendingFeatureIf({true})
  def bar() {
    expect: false
  }
}
    """)

    then:
    notThrown(AssertionError)
    result.runCount == 1
    result.failureCount == 0
    result.ignoreCount == 0
  }

  def "@PendingFeatureIf marks passing feature as failed if the conditional expression returns true"() {
    when:
    def result = runner.runWithImports("""import spock.lang.PendingFeature
import spock.lang.PendingFeatureIf

class Foo extends Specification {
  @PendingFeatureIf({true})
  def bar() {
    expect: true
  }
}
    """)
    then:
        AssertionError e = thrown(AssertionError)
        e.message == "Feature is marked with @PendingFeatureIf but passes unexpectedly"
  }

  def "@PendingFeatureIf marks failing feature as failed if the conditional expression returns false"() {
    when:
    def result = runner.runWithImports("""import spock.lang.PendingFeature
import spock.lang.PendingFeatureIf

class Foo extends Specification {
  @PendingFeatureIf({false})
  def bar() {
    expect: false
  }
}
    """)
    then:
        AssertionError e = thrown(ConditionNotSatisfiedError)
        e.condition.values[0] == false
  }

  def "@PendingFeatureIf marks passing feature as passed if the conditional expression returns false"() {
    when:
    def result = runner.runWithImports("""import spock.lang.PendingFeature
import spock.lang.PendingFeatureIf

class Foo extends Specification {
  @PendingFeatureIf({false})
  def bar() {
    expect: true
  }
}
    """)
    then:
        notThrown(AssertionError)
        result.runCount == 1
        result.failureCount == 0
        result.ignoreCount == 0
  }

}
