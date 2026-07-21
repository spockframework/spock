package org.spockframework.smoke.mock

import org.spockframework.EmbeddedSpecification
import org.spockframework.mock.TooFewInvocationsError
import spock.lang.Specification
import spock.mock.MockInteractionSupport
import spock.mock.MockingApi

class MockInteractionSupportSpec extends EmbeddedSpecification {
  def "MockInteractionSupport is a MockingApi interface"() {
    expect:
    MockingApi.isAssignableFrom(MockInteractionSupport)
    MockInteractionSupport.interface
  }

  def "interactions declared in a fixture are enforced on the spec, and stubbed values take effect"() {
    when: "the fixture requires two add('charge') calls, but the feature makes only one"
    runner.runWithImports('''
      import spock.lang.Specification
      import spock.mock.MockInteractionSupport

      class ChargeFixture implements MockInteractionSupport {
        final Specification specification
        ChargeFixture(Specification specification) { this.specification = specification }

        String chargeTwice(List<String> collaborator) {
          2 * collaborator.add("charge")     // required cardinality (deliberately unsatisfied below)
          collaborator.get(0) >> "charged"   // stubbing
          return collaborator.get(0)         // returns the stubbed value so the caller can verify it
        }
      }

      class ChargeSpec extends Specification {
        def "charges"() {
          given:
          List<String> collaborator = Mock()
          def fixture = new ChargeFixture(this)

          when:
          String report = fixture.chargeTwice(collaborator)
          collaborator.add("charge")         // only one of the two required calls

          then: "the stub declared in the fixture took effect on the spec's mock"
          report == "charged"
        }
      }
    ''')

    then: "the unsatisfied 2 * add('charge') is enforced when the spec's scope leaves"
    thrown(TooFewInvocationsError)
  }

  def "stubbing interactions declared in a fixture take effect on the spec"() {
    given:
    def collaborator = Mock(List)
    def fixtures = new StubbingFixture(this)

    when:
    fixtures.stubContains(collaborator)

    then:
    collaborator.contains("x")
  }

  def "a created mock auto-attaches to the located spec and is stubbed"() {
    given:
    def fixtures = new MockCreatingFixture(this)

    when:
    Greeter mock = fixtures.createAndStub()

    then:
    mock.greet() == "hi"
  }

  def "a class that is both a Specification and MockInteractionSupport is a compile error"() {
    when:
    compiler.compileWithImports('''
      import spock.lang.Specification
      import spock.mock.MockInteractionSupport
      abstract class Bad extends Specification implements MockInteractionSupport {}
    ''')

    then:
    Exception e = thrown()
    e.message.contains("must not be both a Specification and a MockInteractionSupport")
  }

  def "a rewritten method fails with a clear error when the located spec is null"() {
    given:
    def fixture = new UnattachedFixture()
    List<String> collaborator = Mock()

    when:
    fixture.expectOneCharge(collaborator)

    then:
    IllegalArgumentException e = thrown()
    e.message == "Cannot declare mock interactions: the owning Specification is null. Attach the MockInteractionSupport to a running Specification through a constructor field."
  }

  def "a static method in a MockInteractionSupport class cannot declare interactions"() {
    when: "the spec is reached via this.getSpecification(), which a static method cannot do"
    compiler.compileWithImports('''
      import spock.lang.Specification
      import spock.mock.MockInteractionSupport
      abstract class Fix implements MockInteractionSupport {
        static void stub(List l) { 1 * l.add(_) }
      }
    ''')

    then:
    Exception e = thrown()
    e.message.startsWith("Interactions cannot be declared in static scope")
  }

  def "a static method in a MockInteractionSupport class cannot create mocks"() {
    when: "the spec is reached via this.getSpecification(), which a static method cannot do"
    compiler.compileWithImports('''
      import spock.lang.Specification
      import spock.mock.MockInteractionSupport
      abstract class Fix implements MockInteractionSupport {
        static List createMock() { def m = Mock(List); return m }
      }
    ''')

    then:
    Exception e = thrown()
    e.message.contains("Mocks cannot be created in static scope")
  }

  static class UnattachedFixture implements MockInteractionSupport {
    Specification getSpecification() { null } // never attached

    void expectOneCharge(List<String> collaborator) {
      1 * collaborator.add("charge")
    }
  }

  static class StubbingFixture implements MockInteractionSupport {
    final Specification specification
    StubbingFixture(Specification specification) { this.specification = specification }

    void stubContains(List<String> collaborator) {
      collaborator.contains("x") >> true
    }
  }

  static class MockCreatingFixture implements MockInteractionSupport {
    final Specification specification
    MockCreatingFixture(Specification specification) { this.specification = specification }

    Greeter createAndStub() {
      Greeter g = Mock()
      g.greet() >> "hi"
      return g
    }
  }

  interface Greeter { String greet() }
}
