package org.spockframework.docs.interaction

import groovy.transform.SelfType
import spock.lang.Specification
import spock.mock.MockInteractionSupport
import spock.lang.Interactions

class ExternalMockInteractionsDocSpec extends Specification {

  // tag::support-fixture[]
  static class OrderFixtures implements MockInteractionSupport {
    final Specification specification

    OrderFixtures(Specification specification) {
      this.specification = specification
    }

    PaymentGateway happyGateway() {
      PaymentGateway gateway = Mock()      // created and auto-attached to the spec
      gateway.charge(42) >> true           // stubbing
      1 * gateway.audit("processed")       // required interaction
      return gateway
    }
  }
  // end::support-fixture[]

  // tag::support-usage[]
  def "a MockInteractionSupport fixture creates and configures mocks"() {
    given:
    PaymentGateway gateway = new OrderFixtures(this).happyGateway()

    when:
    boolean charged = gateway.charge(42)
    gateway.audit("processed")

    then:
    charged
  }
  // end::support-usage[]

  // tag::interactions-helper[]
  static class GatewayInteractions {
    @Interactions
    void expectCharged(PaymentGateway gateway) {
      gateway.charge(42) >> true
      1 * gateway.audit("processed")
    }
  }
  // end::interactions-helper[]

  // tag::interactions-usage[]
  def "an @Interactions helper declares interactions on a passed-in mock"() {
    given:
    PaymentGateway gateway = Mock()
    GatewayInteractions interactions = new GatewayInteractions()

    when:
    interactions.expectCharged(gateway)    // strongly-typed receiver: rewritten to pass the spec
    boolean charged = gateway.charge(42)
    gateway.audit("processed")

    then:
    charged
  }
  // end::interactions-usage[]

  interface PaymentGateway {
    boolean charge(int amount)

    void audit(String message)
  }
}

interface GreetingService {
  String greet(String name)
}

// tag::trait-fixture[]
@SelfType(Specification)                       // guarantees `this` is a Specification
trait GreetingInteractions {
  void expectHello(GreetingService service) {
    1 * service.greet("world") >> "hello"
  }
}
// end::trait-fixture[]

// tag::trait-usage[]
class GreetingSpec extends Specification implements GreetingInteractions {
  def "a @SelfType(Specification) trait mixes interaction helpers into the spec"() {
    given:
    GreetingService service = Mock()

    when:
    expectHello(service)                       // trait method, inherited by the spec
    def reply = service.greet("world")

    then:
    reply == "hello"
  }
}
// end::trait-usage[]
