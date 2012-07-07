package org.spockframework.smoke.condition

import spock.lang.Specification
import org.spockframework.runtime.UnallowedExceptionThrownError
import spock.lang.FailsWith

class NegativeExceptionConditions extends Specification {
  def "no exception allowed, none thrown"() {
    when:
    def x = 1

    then:
    noExceptionThrown()
  }

  @FailsWith(UnallowedExceptionThrownError)
  def "no exception allowed, IOException thrown"() {
    when:
    throw new IOException()

    then:
    noExceptionThrown()
  }

  def "no IOException allowed, none thrown"() {
    when:
    def x = 1

    then:
    notThrown(IOException)
  }

  @FailsWith(UnallowedExceptionThrownError)
  def "no IOException allowed, IOException thrown"() {
    when:
    throw new IOException()

    then:
    notThrown(IOException)
  }

  @FailsWith(RuntimeException)
  def "no IOException allowed, other exception thrown"() {
    when:
    throw new RuntimeException()

    then:
    notThrown(IOException)
  }
}
