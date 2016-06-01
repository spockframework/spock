package org.spockframework.mock

import spock.lang.Specification
import spock.lang.Subject
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

  private String getMockName(IMockMe mock) {
    new MockUtil().asMock(mock).name
  }

  void attach(Object mock) {
    new MockUtil().attachMock(mock, this)
  }

  void detach(Object mock) {
    new MockUtil().detachMock(mock)
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
