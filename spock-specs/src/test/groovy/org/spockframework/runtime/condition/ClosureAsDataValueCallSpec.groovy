package org.spockframework.runtime.condition
import org.spockframework.EmbeddedSpecification
import spock.lang.Issue

class ClosureAsDataValueCallSpec extends EmbeddedSpecification {

  @Issue("http://issues.spockframework.org/detail?id=274")
  def "multi line expression failss"() {
    when:
    runner.runSpecBody("""
    def "multi line expression failing"() {
      expect:
      1 == wibble()

      where:
      wibble = { 1 }
    }
    """)

    then:
    notThrown(Exception)
  }
}

