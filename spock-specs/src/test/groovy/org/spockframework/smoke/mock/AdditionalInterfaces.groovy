package org.spockframework.smoke.mock

import org.spockframework.mock.CannotCreateMockException
import spock.lang.Specification

class AdditionalInterfaces extends Specification {

  def "java stubs"() {
    given:
    def stub = Stub(List, additionalInterfaces: [Closeable])

    expect:
    stub instanceof List
    stub instanceof Closeable
  }

  def "java mocks"() {
    given:
    def mock = Mock(List, additionalInterfaces: [Closeable])

    expect:
    mock instanceof List
    mock instanceof Closeable
  }

  def "java spies"() {
    given:
    def spy = Spy(ArrayList, additionalInterfaces: [Closeable])

    expect:
    spy instanceof List
    spy instanceof Closeable
  }

  def "groovy stubs"() {
    given:
    def stub = GroovyStub(List, additionalInterfaces: [Closeable])

    expect:
    stub instanceof List
    stub instanceof Closeable
  }

  def "groovy mocks"() {
    given:
    def mock = GroovyMock(List, additionalInterfaces: [Closeable])

    expect:
    mock instanceof List
    mock instanceof Closeable
  }

  def "groovy spies"() {
    given:
    def spy = GroovySpy(ArrayList, additionalInterfaces: [Closeable])

    expect:
    spy instanceof List
    spy instanceof Closeable
  }


  def "groovy mocks for final class cannot have additionalInterfaces"() {
    when:
    GroovyMock(String, additionalInterfaces: [Closeable])

    then:
    thrown(CannotCreateMockException)
  }


  def "groovy global mocks cannot have additionalInterfaces"() {
    when:
    GroovyMock(ArrayList, additionalInterfaces: [Closeable], global: true)

    then:
    thrown(CannotCreateMockException)
  }
}
