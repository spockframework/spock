package org.spockframework.mock

import spock.lang.*
import spock.mock.DetachedMockFactory

class UnattachedMockSpec extends Specification {

  @Subject
  DetachedMockFactory factory = new DetachedMockFactory()

  def "Mock(class)"() {
    given:
    SomeInterface mock = factory.Mock(SomeInterface)

    when:
    def result = mock.hello()

    then:
    result == null
  }

  def "Stub(class)"() {
    given:
    SomeInterface stub = factory.Stub(SomeInterface)

    when:
    def result = stub.hello()
    def result2 = stub.hello()

    then:
    result == ""
    result2 == ""
  }


  def "Spy(class)"() {
    given:
    SomeInterface spy = factory.Spy(SomeClass.class)

    when:
    def result = spy.hello()

    then:
    result == "world"
  }

  def "Spy(obj)" () {
    given:
    SomeInterface spy = factory.Spy(new SomeClass(answer: "bar"))

    when:
    def result = spy.hello()

    then:
    result == "bar"
  }
}

interface SomeInterface {
  String hello()
}

class SomeClass implements SomeInterface {
  private String answer = "world"

  @Override
  String hello() {
    return answer
  }
}
