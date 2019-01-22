package org.spockframework.docs.interaction

import org.spockframework.mock.TooFewInvocationsError
import spock.lang.*

import static org.hamcrest.CoreMatchers.endsWith

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
  int messageCount = 0
  void send(String message){
    subscribers*.receive(message)
    messageCount++
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

  def "Mixing Interactions and Conditions"() {
// tag::example3[]
    when:
    publisher.send("hello")

    then:
    1 * subscriber.receive("hello")
    publisher.messageCount == 1
// end::example3[]
  }

  def "Explicit Interaction Blocks"() {
// tag::example4[]
    when:
    publisher.send("hello")

    then:
    interaction {
      def message = "hello"
      1 * subscriber.receive(message)
    }
// end::example4[]
  }

  def "Scope of Interactions"() {
// tag::example5[]
    when:
    publisher.send("message1")

    then:
    1 * subscriber.receive("message1")

    when:
    publisher.send("message2")

    then:
    1 * subscriber.receive("message2")
// end::example5[]
  }

  def "equalArg"() {
    when:
    publisher.send("hello")

    then:
// tag::argConstraints[]
    1 * subscriber.receive("hello")        // an argument that is equal to the String "hello"
// end::argConstraints[]
  }

  def "notArg"() {
    when:
    publisher.send("foo")

    then:
// tag::argConstraints[]
    1 * subscriber.receive(!"hello")       // an argument that is unequal to the String "hello"
// end::argConstraints[]
  }

  @FailsWith(TooFewInvocationsError)
  def "emptyArg"() {
    when:
    publisher.send("any")

    then:
// tag::argConstraints[]
    1 * subscriber.receive()               // the empty argument list (would never match in our example)
// end::argConstraints[]
  }

  def "wildcardArg"() {
    when:
    publisher.send("foo")

    then:
// tag::argConstraints[]
    1 * subscriber.receive(_)              // any single argument (including null)
// end::argConstraints[]
  }

  def "spreadWildcardArg"() {
    when:
    publisher.send("foo")

    then:
// tag::argConstraints[]
    1 * subscriber.receive(*_)             // any argument list (including the empty argument list)
// end::argConstraints[]
  }

  def "notNullArg"() {
    when:
    publisher.send("foo")

    then:
// tag::argConstraints[]
    1 * subscriber.receive(!null)          // any non-null argument
// end::argConstraints[]
  }

  def "typeArg"() {
    when:
    publisher.send("foo")

    then:
// tag::argConstraints[]
    1 * subscriber.receive(_ as String)    // any non-null argument that is-a String
// end::argConstraints[]
  }

  def "hamcrestArg"() {
    when:
    publisher.send("hello")

    then:
// tag::argConstraints[]
    1 * subscriber.receive(endsWith("lo")) // any non-null argument that is-a String
// end::argConstraints[]
  }

  def "codeArg"() {
    when:
    publisher.send("foobar")

    then:
// tag::argConstraints[]
    1 * subscriber.receive({ it.size() > 3 && it.contains('a') })
    // an argument that satisfies the given predicate, meaning that
    // code argument constraints need to return true of false
    // depending on whether they match or not
    // (here: message length is greater than 3 and contains the character a)
// end::argConstraints[]
  }

  def "any1"() {
    when:
    publisher.send("foobar")

    then:
// tag::anyConstraints[]
    1 * subscriber._(*_)     // any method on subscriber, with any argument list
// end::anyConstraints[]
  }

  def "any2"() {
    when:
    publisher.send("foobar")

    then:
// tag::anyConstraints[]
    1 * subscriber._         // shortcut for and preferred over the above

// end::anyConstraints[]
  }

  def "any3"() {
    when:
    subscriber.receive("foobar")

    then:
// tag::anyConstraints[]
    1 * _._                  // any method call on any mock object
// end::anyConstraints[]
  }

  def "any4"() {
    when:
    subscriber.receive("foobar")

    then:
// tag::anyConstraints[]
    1 * _                    // shortcut for and preferred over the above
// end::anyConstraints[]
  }
}


class Person {
  String firstname
  String lastname
  int age
}

class ArgMatcherSpec extends Specification {

  List list = Mock()

  def "verifyAll inside Code argument"() {
    when:
    list.add(new Person(firstname: 'William', lastname: 'Kirk', age: 45))

    then:
// tag::codeConstraint[]
    1 * list.add({
      verifyAll(it, Person) {
        firstname == 'William'
        lastname == 'Kirk'
        age == 45
      }
    })
// end::codeConstraint[]
  }

}
