package org.spockframework.smoke.mock

import org.spockframework.EmbeddedSpecification
import org.spockframework.mock.TooFewInvocationsError

class TraitInteractionsSpec extends EmbeddedSpecification {
  def "an unsatisfied cardinality interaction declared in a @SelfType(Specification) trait is enforced on the mixing spec"() {
    when:
    runner.runWithImports('''
      import groovy.transform.SelfType

      @SelfType(Specification)
      trait OrderInteractions {
        void expectCharge(List<String> gateway) {
          1 * gateway.add("charge")
        }
      }

      class OrderSpec extends Specification implements OrderInteractions {
        def "charges once"() {
          given:
          List<String> gateway = Mock()

          when:
          expectCharge(gateway)
          // gateway.add("charge") is never invoked -> interaction unsatisfied

          then:
          true
        }
      }
    ''')

    then:
    thrown(TooFewInvocationsError)
  }

  def "a satisfied cardinality interaction declared in a trait passes"() {
    when:
    runner.runWithImports('''
      import groovy.transform.SelfType

      @SelfType(Specification)
      trait OrderInteractions {
        void expectCharge(List<String> gateway) {
          1 * gateway.add("charge")
        }
      }

      class OrderSpec extends Specification implements OrderInteractions {
        def "charges once"() {
          given:
          List<String> gateway = Mock()

          when:
          expectCharge(gateway)
          gateway.add("charge")

          then:
          true
        }
      }
    ''')

    then:
    noExceptionThrown()
  }

  def "a stubbing interaction declared in a trait takes effect on the spec"() {
    when:
    runner.runWithImports('''
      import groovy.transform.SelfType

      @SelfType(Specification)
      trait StubbingTrait {
        void stubContains(List<String> collaborator) {
          collaborator.contains("x") >> true
        }
      }

      class StubbingSpec extends Specification implements StubbingTrait {
        def "stub takes effect"() {
          given:
          List<String> collaborator = Mock()

          when:
          stubContains(collaborator)

          then:
          collaborator.contains("x")
        }
      }
    ''')

    then:
    noExceptionThrown()
  }

  def "a trait method can create and stub a mock that routes through the mixing spec"() {
    when:
    runner.runWithImports('''
      import groovy.transform.SelfType

      interface Greeter { String greet() }

      @SelfType(Specification)
      trait MockFactoryTrait {
        Greeter createGreeter() {
          Greeter g = Mock(Greeter)
          g.greet() >> "hi"
          return g
        }
      }

      class FactorySpec extends Specification implements MockFactoryTrait {
        def "created mock is stubbed"() {
          when:
          Greeter g = createGreeter()

          then:
          g.greet() == "hi"
        }
      }
    ''')

    then:
    noExceptionThrown()
  }

  def "a static trait method cannot declare interactions"() {
    when: "a static method has no instance to reach the spec through"
    compiler.compileWithImports('''
      import groovy.transform.SelfType

      @SelfType(Specification)
      trait BadStaticTrait {
        static void stub(List<String> l) { 1 * l.add("x") }
      }
    ''')

    then:
    Exception e = thrown()
    e.message.contains("static scope")
  }

  def "a trait whose self-type extends Specification is also supported"() {
    when:
    runner.runWithImports('''
      import groovy.transform.SelfType

      abstract class BaseSpec extends Specification {}

      @SelfType(BaseSpec)
      trait OrderInteractions {
        void expectCharge(List<String> gateway) {
          1 * gateway.add("charge")
        }
      }

      class OrderSpec extends BaseSpec implements OrderInteractions {
        def "charges once"() {
          given:
          List<String> gateway = Mock()

          when:
          expectCharge(gateway)

          then:
          true
        }
      }
    ''')

    then:
    thrown(TooFewInvocationsError)
  }

  def "a trait method can call an @Interactions helper, passing the spec along"() {
    when:
    runner.runWithImports('''
      import groovy.transform.SelfType
      import spock.lang.Interactions

      class StubFixtures {
        @Interactions
        void stubGet(List<String> collaborator) {
          collaborator.get(0) >> "item"
        }
      }

      @SelfType(Specification)
      trait ComposingTrait {
        void applyStubs(List<String> collaborator) {
          StubFixtures fixtures = new StubFixtures()
          fixtures.stubGet(collaborator)
        }
      }

      class ComposingSpec extends Specification implements ComposingTrait {
        def "the helper's stub takes effect through the trait"() {
          given:
          List<String> collaborator = Mock()

          when:
          applyStubs(collaborator)

          then:
          collaborator.get(0) == "item"
        }
      }
    ''')

    then:
    noExceptionThrown()
  }

  def "interactions declared in a trait are NOT relocated out of a then-block"() {
    when: "unlike @Interactions helpers, trait interaction methods are not moved before the when-block"
    runner.runWithImports('''
      import groovy.transform.SelfType

      @SelfType(Specification)
      trait StubbingTrait {
        void stubGet(List<String> collaborator) {
          collaborator.get(0) >> "item"
        }
      }

      class StubbingSpec extends Specification implements StubbingTrait {
        def "the stub registers too late to affect the when-action"() {
          given:
          List<String> collaborator = Mock()

          when:
          def result = collaborator.get(0)

          then: "the trait helper runs in place (after the action), so the stub did not apply"
          stubGet(collaborator)
          result == null
        }
      }
    ''')

    then:
    noExceptionThrown()
  }
}
