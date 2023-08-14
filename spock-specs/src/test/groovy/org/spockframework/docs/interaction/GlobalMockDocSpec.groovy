package org.spockframework.docs.interaction

import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
class GlobalMockDocSpec extends Specification {

  @Issue("https://github.com/spockframework/spock/issues/785")
  def "Global Spy usage"() {
    // tag::global-spy[]
    given:
    def publisher = new Publisher()
    def anySubscriber = GroovySpy(global: true, RealSubscriber)
    publisher.subscribers.add(new RealSubscriber())
    publisher.subscribers.add(new RealSubscriber())

    when:
    publisher.send("message")

    then:
    2 * anySubscriber.receive("message")
    // end::global-spy[]
  }

  def "Global mock mocks also constructor"() {
    // tag::global-mock-constructor[]
    given:
    GroovyMock(global: true, RealSubscriber)
    when:
    def sub = new RealSubscriber()
    then: "The GroovyMock(global: true) will also mock the constructor"
    sub == null
    // end::global-mock-constructor[]
  }

  def "Global mock mocks also constructor2"() {
    // tag::global-mock-constructor-real[]
    given:
    GroovyMock(global: true, RealSubscriber) {
      //Allow that the real constructor is called
      new RealSubscriber(*_) >> { callRealMethod() }
    }
    when:
    def sub = new RealSubscriber()
    then:
    sub instanceof RealSubscriber
    // end::global-mock-constructor-real[]
  }

  static class RealSubscriber implements Subscriber {
    @Override
    void receive(String message) {
    }
  }
}
