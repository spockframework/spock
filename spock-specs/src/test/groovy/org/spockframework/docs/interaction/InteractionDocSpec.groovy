package org.spockframework.docs.interaction

import spock.lang.Specification

class InteractionDocSpec extends Specification {

  def "Mock 1"() {
    given:
// tag::mock1[]
    def subscriber = Mock(Subscriber)
    def subscriber2 = Mock(Subscriber)
// end::mock1[]

    when:
    subscriber.receive("1")
    subscriber2.receive("2")

    then:
    2 * _.receive(_)
  }


  def "Mock 2"() {
    given:
// tag::mock2[]
    Subscriber subscriber = Mock()
    Subscriber subscriber2 = Mock()
// end::mock2[]

    when:
    subscriber.receive("1")
    subscriber2.receive("2")

    then:
    2 * _.receive(_)
  }
}


// tag::examplesetup[]
class Publisher {
  List<Subscriber> subscribers = []
  void send(String message){
    subscribers*.receive(message)
  }
}

interface Subscriber {
  void receive(String message)
}

// tag::example1[]
class PublisherSpec extends Specification {
  Publisher publisher = new Publisher()
// end::examplesetup[]
  Subscriber subscriber = Mock()
  Subscriber subscriber2 = Mock()

  def setup() {
    publisher.subscribers << subscriber // << is a Groovy shorthand for List.add()
    publisher.subscribers << subscriber2
  }
// end::example1[]
// tag::example2[]
  def "should send messages to all subscribers"() {
    when:
    publisher.send("hello")

    then:
    1 * subscriber.receive("hello")
    1 * subscriber2.receive("hello")
  }
// end::example2[]
}
