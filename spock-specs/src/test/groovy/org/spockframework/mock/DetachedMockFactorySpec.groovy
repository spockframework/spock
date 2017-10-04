package org.spockframework.mock

import spock.lang.*
import spock.mock.DetachedMockFactory

class DetachedMockFactorySpec extends Specification {

  @Subject
  DetachedMockFactory factory = new DetachedMockFactory()

  def "Mock(class)"() {
    given:
    IMockMe mock = factory.Mock(IMockMe)
    attach(mock)

    when:
    mock.foo(2)

    then:
    1 * mock.foo(2)
    getMockName(mock) == 'IMockMe'

    cleanup:
    detach(mock)
  }

  def "Mock(options, class)"() {
    given:
    IMockMe mock = factory.Mock(IMockMe, name: 'customName')
    attach(mock)

    when:
    mock.foo(2)

    then:
    1 * mock.foo(2)
    getMockName(mock) == 'customName'

    cleanup:
    detach(mock)
  }

  def "Stub(class)"() {
    given:
    IMockMe stub = factory.Stub(IMockMe)
    attach(stub)
    stub.foo(2) >> 4

    expect:
    stub.foo(2) == 4
    stub.foo(1) == 0
    getMockName(stub) == 'IMockMe'

    cleanup:
    detach(stub)
  }

  def "Stub(options, class)"() {
    given:
    IMockMe stub = factory.Stub(IMockMe, name: 'customName')
    attach(stub)
    stub.foo(2) >> 4

    expect:
    stub.foo(2) == 4
    stub.foo(1) == 0
    getMockName(stub) == 'customName'

    cleanup:
    detach(stub)
  }

  def "Spy(class)"() {
    given:
    IMockMe spy = factory.Spy(MockMe.class)
    attach(spy)

    when:
    int result = spy.foo(2)

    then:
    result == 1
    1 * spy.foo(2)

    cleanup:
    detach(spy)
  }

  def "Spy(options, class)"() {
    given:
    IMockMe spy = factory.Spy(MockMe.class, constructorArgs: [42])
    attach(spy)

    when:
    int result = spy.foo(2)

    then:
    result == 42
    1 * spy.foo(2)

    cleanup:
    detach(spy)
  }

  def "Spy(obj)" () {
      given:
      IMockMe spy = factory.Spy(new MockMe(42))
      attach(spy)

      when:
      int result = spy.foo(2)

      then:
      result == 42
      1 * spy.foo(2)

      cleanup:
      detach(spy)
  }

  @Issue("https://github.com/spockframework/spock/issues/769")
  def "can spy on instances of classes with no default constructor"() {
    given:
    def spy = factory.Spy(new NoDefaultConstructor(42))
    attach(spy)

    expect:
    spy.value == 42

    when:
    def result = spy.value

    then:
    1 * spy.getValue() >> 7
    result == 7

    cleanup:
    detach(spy)
  }

  private String getMockName(IMockMe mock) {
    new MockUtil().asMock(mock).name
  }

  void attach(Object mock) {
    new MockUtil().attachMock(mock, this)
  }

  void detach(Object mock) {
    if(mock != null) {
      new MockUtil().detachMock(mock)
    }
  }
}

interface IMockMe {
  int foo(int i)
}

class MockMe implements IMockMe {
  private int defaultAnswer = 1

  MockMe(){}

  MockMe(int defaultAnswer) {
    this.defaultAnswer = defaultAnswer
  }

  int foo(int i) {
    defaultAnswer
  }
}

class NoDefaultConstructor {
  int value
  NoDefaultConstructor(int value) {
    this.value = value
  }
}
