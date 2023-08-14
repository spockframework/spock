package org.spockframework.smoke.mock

import org.spockframework.EmbeddedSpecification
import org.spockframework.mock.TooFewInvocationsError
import spock.lang.FailsWith
import spock.lang.Issue
import spock.lang.PendingFeature

class JavaMockInteractionOverridesSpec extends EmbeddedSpecification {
  def ifMock = Mock(InterfaceType)

  def setup() {
    ifMock.methodFromSetup() >> 1
  }

  def "given block two same interactions after each other, the second shall win due to no cardinality"() {
    given:
    ifMock.method() >> 1
    //Second behavior shall win, because the first did no specify cardinality
    ifMock.method() >> 2
    when:
    def result = ifMock.method()
    then: "Only the second behavior shall apply, because nothing had cardinality"
    result == 2
    when:
    result = ifMock.method()
    then:
    result == 2
  }

  def "when block two same interactions after each other, the second shall win due to no cardinality"() {
    when:
    ifMock.method() >> 1
    //Second behavior shall win, because the first did no specify cardinality
    ifMock.method() >> 2
    def result = ifMock.method()
    then: "Only the second behavior shall apply, because nothing had cardinality"
    result == 2
    when:
    result = ifMock.method()
    then:
    result == 2
  }

  def "Second when block overrides first when block"() {
    when:
    ifMock.method() >> 1
    def result = ifMock.method()
    then:
    result == 1
    when:
    ifMock.method() >> 2
    result = ifMock.method()
    then:
    result == 2
  }

  def "then block two same interactions after each other, the second shall win due to no cardinality"() {
    when:
    def result1 = ifMock.method()
    def result2 = ifMock.method()
    then: "Only the second behavior shall apply, because nothing had cardinality"
    ifMock.method() >> 1
    //Second behavior shall win, because the first did not specify any cardinality
    ifMock.method() >> 2
    result1 == 2
    result2 == 2
  }

  def "two interactions after each other, but first has any cardinality, the second shall not win"() {
    given:
    _ * ifMock.method() >> 1
    //Second behavior shall not win, because the first did specify any cardinality
    ifMock.method() >> 2
    when:
    def result = ifMock.method()
    then:
    result == 1
    when:
    result = ifMock.method()
    then:
    result == 1
  }

  def "two interactions with first with cardinality"() {
    given:
    1 * ifMock.method() >> 1
    ifMock.method() >> 2
    when:
    def result = ifMock.method()
    then:
    result == 1
    when:
    result = ifMock.method()
    then:
    result == 2
    when:
    result = ifMock.method()
    then: "Additional calls are allowed, because the second has no cardinality"
    result == 2
  }

  def "given block two interactions with cardinality after each other"() {
    given:
    1 * ifMock.method() >> 1
    1 * ifMock.method() >> 2
    when:
    def result = ifMock.method()
    then:
    result == 1
    when:
    result = ifMock.method()
    then:
    result == 2
  }

  def "when block two interactions with cardinality after each other"() {
    when:
    1 * ifMock.method() >> 1
    1 * ifMock.method() >> 2
    def result = ifMock.method()
    then:
    result == 1
    when:
    result = ifMock.method()
    then:
    result == 2
  }

  def "then block two interactions with cardinality after each other"() {
    when:
    def result1 = ifMock.method()
    def result2 = ifMock.method()
    then:
    1 * ifMock.method() >> 1
    1 * ifMock.method() >> 2
    result1 == 1
    result2 == 2
  }

  def "two then blocks two interactions with cardinality after each other"() {
    when:
    def result1 = ifMock.method()
    def result2 = ifMock.method()
    then:
    1 * ifMock.method() >> 1
    then:
    1 * ifMock.method() >> 2
    result1 == 1
    result2 == 2
  }

  def "setup interaction"() {
    when:
    def result = ifMock.methodFromSetup()
    then:
    result == 1
  }

  @Issue("https://github.com/spockframework/spock/issues/962")
  def "given interaction overrides setup, because nothing had cardinality"() {
    given:
    ifMock.methodFromSetup() >> 2
    when:
    def result = ifMock.methodFromSetup()
    then:
    result == 2
  }

  @Issue("https://github.com/spockframework/spock/issues/962")
  def "when interaction overrides setup, because nothing had cardinality"() {
    when:
    ifMock.methodFromSetup() >> 2
    def result = ifMock.methodFromSetup()
    then:
    result == 2
  }

  @Issue("https://github.com/spockframework/spock/issues/962")
  def "when interaction overrides given, because nothing had cardinality"() {
    given:
    ifMock.method() >> 2
    when:
    ifMock.method() >> 3
    def result = ifMock.method()
    then:
    result == 3
  }

  @Issue("https://github.com/spockframework/spock/issues/962")
  def "when interaction overrides given ans setup, because nothing had cardinality"() {
    given:
    ifMock.methodFromSetup() >> 2
    when:
    ifMock.methodFromSetup() >> 3
    def result = ifMock.methodFromSetup()
    then:
    result == 3
  }

  @FailsWith(TooFewInvocationsError)
  def "given interaction shall not override setup, because given has cardinality"() {
    given: "the given block can never be matched, because the setup defines it without cardinality"
    1 * ifMock.methodFromSetup() >> 2
    when:
    def result = ifMock.methodFromSetup()
    then:
    result == 1
  }

  def "then block interaction without cardinality overrides given"() {
    given:
    ifMock.method() >> 2
    when:
    def result = ifMock.method()
    then:
    ifMock.method() >> 3
    result == 3
  }

  @PendingFeature(reason = "https://github.com/spockframework/spock/pull/1761")
  def "then block interaction without cardinality overrides when"() {
    when:
    ifMock.method() >> 2
    def result = ifMock.method()
    then:
    ifMock.method() >> 3
    result == 3
  }

  def "then block interaction with cardinality overrides given"() {
    given:
    ifMock.method() >> 2
    when:
    def result = ifMock.method()
    then:
    1 * ifMock.method() >> 3
    result == 3
  }

  def "then block interaction with cardinality overrides when"() {
    when:
    ifMock.method() >> 2
    def result = ifMock.method()
    then:
    1 * ifMock.method() >> 3
    result == 3
  }

  def "then block interaction overrides setup"() {
    when:
    def result = ifMock.methodFromSetup()
    then:
    1 * ifMock.methodFromSetup() >> 2
    result == 2
  }

  def "given interaction shall not override setup, because given has cardinality and setup has cardinality"() {
    given:
    runner.addClassImport(InterfaceType)
    runner.runSpecBody("""
  def ifMock = Mock(InterfaceType)

  def setup() {
    1 * ifMock.methodFromSetup() >> 1
  }

   def "test"() {
    given:
    1 * ifMock.methodFromSetup() >> 2
    when:
    def result = ifMock.methodFromSetup()
    then:
    result == 1
    when:
    result = ifMock.methodFromSetup()
    then:
    result == 2
  }
""")
  }

  def "Positional Arg override"() {
    given:
    ifMock.methodArg("A") >> 1
    ifMock.methodArg("A") >> 2
    expect:
    ifMock.methodArg("A") == 2
  }

  def "type arg override"() {
    given:
    ifMock.methodArg(_ as String) >> 1
    ifMock.methodArg(_ as String) >> 2
    expect:
    ifMock.methodArg("A") == 2
  }

  def "type arg wrong type override"() {
    given:
    ifMock.methodArg(_ as String) >> 1
    ifMock.methodArg(_ as Object) >> 2
    expect:
    ifMock.methodArg("A") == 1
  }

  def "type arg exact method do no override"() {
    given:
    ifMock.methodArg(_ as String) >> 1
    ifMock.methodArg("A") >> 2
    expect:
    ifMock.methodArg("A") == 1
  }

  def "Wildcard method override"() {
    given:
    ifMock._ >> 1
    ifMock._ >> 2
    expect:
    ifMock.methodArg("A") == 2
  }

  def "Wildcard Exact method do not override"() {
    given:
    ifMock.methodArg("A") >> 1
    ifMock._ >> 2
    expect:
    ifMock.methodArg("A") == 1
  }

  def "Exact method Wildcard do not override"() {
    given:
    ifMock._ >> 1
    ifMock.methodArg("A") >> 2
    expect:
    ifMock.methodArg("A") == 1
  }

  def "Exact method target constraint do not override"() {
    given:
    ifMock.methodArg("A") >> 1
    _.methodArg("A") >> 2
    expect:
    ifMock.methodArg("A") == 1
  }

  def "target constraint exact method do not override"() {
    given:
    _.methodArg("A") >> 1
    ifMock.methodArg("A") >> 2

    expect:
    ifMock.methodArg("A") == 1
  }

  def "Regex method override"() {
    given:
    ifMock."method.*"(_) >> 1
    ifMock."method.*"(_) >> 2
    expect:
    ifMock.methodArg("A") == 2
  }

  def "method explicit method regex do not override"() {
    given:
    ifMock.methodArg("A") >> 1
    ifMock."method.*"(_) >> 2
    expect:
    ifMock.methodArg("A") == 1
  }

  def "method regex method explicit do not override"() {
    given:
    ifMock."method.*"(_) >> 1
    ifMock.methodArg("A") >> 2
    expect:
    ifMock.methodArg("A") == 1
  }

  def "Spread wildcard args override"() {
    given:
    ifMock.methodArg(*_) >> 1
    ifMock.methodArg(*_) >> 2
    expect:
    ifMock.methodArg("A") == 2
  }

  def "Spread wildcard exact method do no override"() {
    given:
    ifMock.methodArg(*_) >> 1
    ifMock.methodArg("A") >> 2
    expect:
    ifMock.methodArg("A") == 1
  }

  def "property override"() {
    given:
    ifMock.propA >> 1
    ifMock.propA >> 2
    expect:
    ifMock.propA == 2
  }

  def "property regex override"() {
    given:
    ifMock."prop.*" >> 1
    ifMock."prop.*" >> 2
    expect:
    ifMock.propA == 2
  }

  def "property explicit property regex do not override"() {
    given:
    ifMock.propA >> 1
    ifMock."prop.*" >> 2
    expect:
    ifMock.propA == 1
  }

  def "property regex property explicit do not override"() {
    given:
    ifMock."prop.*" >> 1
    ifMock.propA >> 2
    expect:
    ifMock.propA == 1
  }

  def "Positional Arg negate override"() {
    given:
    ifMock.methodArg(!"A") >> 1
    ifMock.methodArg(!"A") >> 2
    expect:
    ifMock.methodArg("B") == 2
  }

  def "Positional Arg negate exact do not override"() {
    given:
    ifMock.methodArg(!"A") >> 1
    ifMock.methodArg("A") >> 2
    expect:
    ifMock.methodArg("B") == 1
  }

  def "Positional Arg Wildcard Args do not override"() {
    given:
    ifMock.methodArg("A") >> 1
    ifMock.methodArg(_) >> 2
    expect:
    ifMock.methodArg("A") == 1
  }

  def "Named Arg override"() {
    given:
    ifMock.methodNamedArg(arg: "A") >> 1
    ifMock.methodNamedArg(arg: "A") >> 2
    expect:
    ifMock.methodNamedArg(arg: "A") == 2
  }

  def "Named Arg do not override"() {
    given:
    ifMock.methodNamedArg(arg: "A") >> 1
    ifMock.methodNamedArg(arg: "B") >> 2
    expect:
    ifMock.methodNamedArg(arg: "A") == 1
  }

  def "Code Arg do not override"() {
    given:
    ifMock.methodArg {} >> 1
    ifMock.methodArg {} >> 2
    expect:
    ifMock.methodArg("A") == 1
  }

  def "Code Arg Exact arg do not override"() {
    given:
    ifMock.methodArg("A") >> 1
    ifMock.methodArg {} >> 2
    expect:
    ifMock.methodArg("A") == 1
  }

  def "Code Arg Exact arg inverse do not override"() {
    given:
    ifMock.methodArg {} >> 1
    ifMock.methodArg("A") >> 2
    expect:
    ifMock.methodArg("A") == 1
  }

  def "Named Arg Positional Arg do not override"() {
    given:
    ifMock.methodNamedArg(arg: "A") >> 1
    ifMock.methodNamedArg("A") >> 2
    expect:
    ifMock.methodNamedArg(arg: "A") == 1
  }

  def "Named Arg Positional arg inverse do not override"() {
    given:
    ifMock.methodNamedArg("A") >> 1
    ifMock.methodNamedArg(arg: "A") >> 2
    expect:
    ifMock.methodNamedArg(arg: "A") == 2
  }

  interface InterfaceType {
    int methodFromSetup()

    int method()

    int methodArg(String arg)

    int methodNamedArg(Map<String, Object> arg)

    int getPropA();
  }
}
