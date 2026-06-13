package org.spockframework.smoke.mock

import org.spockframework.EmbeddedSpecification
import org.spockframework.mock.TooFewInvocationsError
import org.spockframework.runtime.InvalidSpecException
import spock.lang.Specification
import spock.lang.Interactions
import spock.mock.MockInteractionSupport
import spock.util.EmbeddedSpecCompiler

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

class InteractionsSpec extends EmbeddedSpecification {
  def "annotation targets methods with runtime retention"() {
    expect:
    Interactions.getAnnotation(Retention).value() == RetentionPolicy.RUNTIME
    Interactions.getAnnotation(Target).value().toList() == [ElementType.METHOD]
  }

  def "an @Interactions helper enforces interactions when called from a spec with a typed receiver"() {
    given:
    List<String> gateway = Mock()
    OrderFixtures fixtures = new OrderFixtures()

    when:
    fixtures.stubHappyPath(gateway)
    def result = gateway.get(0)

    then: "the interaction is enforced and the stubbed value is returned"
    result == "item"
  }

  def "the companion overload is directly callable with an explicit spec, even if the type is dynamic"() {
    given:
    List<String> gateway = Mock()
    def fixtures = new OrderFixtures()

    when:
    fixtures.stubHappyPath(this, gateway)
    def result = gateway.get(0)

    then:
    result == "item"
  }

  def "a def-typed receiver invokes the throwing original with a diagnostic message"() {
    given:
    List<String> gateway = Mock()
    def fixtures = new OrderFixtures() // def-typed receiver -> detection misses

    when:
    fixtures.stubHappyPath(gateway)

    then:
    InvalidSpecException e = thrown()
    e.message == "Method 'stubHappyPath' is annotated with @Interactions and can only declare interactions when called from a Specification with a strongly-typed receiver, or when its explicit-spec overload (with a leading Specification argument) is called directly."
  }

  def "a Mock() creation inside an @Interactions method is a compile error"() {
    when:
    compiler.compileWithImports('''
      import spock.lang.Interactions
      class BadFix {
        @Interactions
        void bad() {
          def m = Mock(List)
          1 * m.add(_)
        }
      }
    ''')

    then:
    Exception e = thrown()
    e.message.contains("creation is not allowed in an @Interactions method")
  }

  def "@Interactions on a Specification method is a compile error"() {
    when:
    compiler.compileWithImports('''
      import spock.lang.Specification
      import spock.lang.Interactions
      class BadSpec extends Specification {
        @Interactions
        void helper(List l) { 1 * l.add(_) }
      }
    ''')

    then:
    Exception e = thrown()
    e.message.contains("declared on a Specification")
  }

  def "a pre-existing method matching the companion signature is a compile error"() {
    when:
    compiler.compileWithImports('''
      import spock.lang.Specification
      import spock.lang.Interactions
      class CollideFix {
        @Interactions
        void stub(List l) { 1 * l.add(_) }
        @Interactions
        void stub(Specification s, List l) {}
      }
    ''')

    then:
    Exception e = thrown()
    e.message.contains("companion signature")
  }

  def "an @Interactions method with an unannotated same-name overload is a compile error"() {
    when:
    compiler.compileWithImports('''
      import spock.lang.Interactions
      class MixedFix {
        @Interactions
        void stub(List l) { 1 * l.add(_) }
        String stub(String plain) { return plain }
      }
    ''')

    then:
    Exception e = thrown()
    e.message.contains("all overloads must be annotated")
  }

  def "an @Interactions method must not declare a leading Specification parameter"() {
    when:
    compiler.compileWithImports('''
      import spock.lang.Specification
      import spock.lang.Interactions
      class LeadingFix {
        @Interactions
        void stub(Specification spec, List l) { 1 * l.add(_) }
      }
    ''')

    then:
    Exception e = thrown()
    e.message.contains("must not declare a leading Specification parameter")
  }

  def "an abstract @Interactions method is a compile error"() {
    when:
    compiler.compileWithImports('''
      import spock.lang.Interactions
      abstract class AbstractFix {
        @Interactions
        abstract void stub(List l)
      }
    ''')

    then:
    Exception e = thrown()
    e.message.contains("requires a method body")
  }

  def "an @Interactions method in a trait is a compile error"() {
    when:
    compiler.compileWithImports('''
      import spock.lang.Interactions
      trait BadTrait {
        @Interactions
        void stub(List l) { 1 * l.add(_) }
      }
    ''')

    then:
    Exception e = thrown()
    e.message.contains("declared in a trait")
  }

  def "an @Interactions helper can call another @Interactions helper, passing the spec along"() {
    given:
    List<String> gateway = Mock()
    ComposedFixtures fixtures = new ComposedFixtures()

    when:
    fixtures.stubAll(gateway)
    def result = gateway.get(0)

    then: "the inner helper's interaction took effect through the outer helper"
    result == "item"
  }

  def "a MockInteractionSupport method can call an @Interactions helper, passing the spec along"() {
    given:
    List<String> gateway = Mock()
    def support = new ComposingSupport(this)

    when:
    support.applyStubs(gateway)
    def result = gateway.get(0)

    then:
    result == "item"
  }

  def "a @CompileStatic spec resolves the rewritten companion call"() {
    when:
    def specs = compiler.compileWithImports('''
      import groovy.transform.CompileStatic
      import spock.lang.Specification
      import spock.lang.Interactions

      class Fix {
        @Interactions
        void stub(List<String> l) { 1 * l.add("x") }
      }

      @CompileStatic
      class CsSpec extends Specification {
        def "f"() {
          given:
          List<String> l = Mock()
          Fix fix = new Fix()

          when:
          fix.stub(l)
          l.add("x")

          then:
          true
        }
      }
    ''')

    then:
    specs.find { it.simpleName == "CsSpec" } != null
  }

  def "detection works against a precompiled @Interactions helper in another compilation unit"() {
    given: "the helper is compiled and loaded in its own compilation unit"
    compiler.compile('''
      package ext
      import spock.lang.Interactions
      class ExtHelper {
        @Interactions
        void stub(List<String> gateway) { 1 * gateway.add("x") }
      }
    ''')

    and: "a second compiler whose classpath includes the precompiled helper"
    def crossCompiler = new EmbeddedSpecCompiler(compiler.loader)

    when: "a spec in a separate unit calls the helper with a strongly-typed receiver"
    def result = crossCompiler.transpileSpecBody('''
      def "f"() {
        given:
        List<String> gateway = Mock()
        ext.ExtHelper helper = new ext.ExtHelper()

        when:
        helper.stub(gateway)

        then:
        true
      }
    ''')

    then: "the call is rewritten to select the precompiled companion (RUNTIME-retained annotation read from bytecode)"
    result.source.contains("helper.stub(this, gateway)")
  }

  def "the companion fails with a clear error when called with a null spec"() {
    given:
    List<String> gateway = Mock()
    OrderFixtures fixtures = new OrderFixtures()

    when:
    fixtures.stubHappyPath((Specification) null, gateway)

    then:
    IllegalArgumentException e = thrown()
    e.message == "Cannot declare mock interactions: the Specification passed to this @Interactions method is null."
  }

  def "a static @Interactions helper works when called class-qualified from a spec"() {
    given:
    List<String> gateway = Mock()

    when:
    OrderFixtures.staticStubHappyPath(gateway)
    def result = gateway.get(0)

    then: "the spec is passed as \$spec, so static scope is allowed"
    result == "item"
  }

  def "a statically-imported @Interactions call cannot be resolved at compile time, so it hits the throwing original"() {
    when: "the unqualified call is resolved by Groovy's static-import handling, not our detection"
    runner.runWithImports('''
      import static apackage.ImportFixtures.stub

      class ImportFixtures {
        @Interactions
        static void stub(List l) { 1 * l.add("x") }
      }

      class CallerSpec extends Specification {
        def "f"() {
          given:
          List l = Mock()

          when:
          stub(l)

          then:
          true
        }
      }
    ''')

    then:
    InvalidSpecException e = thrown()
    e.message.contains("strongly-typed receiver")
  }

  def "a statically-imported @Interactions call is left unqualified-original, not rewritten to pass the spec"() {
    when:
    def result = compiler.transpile('''
package apkg
import spock.lang.Specification
import spock.lang.Interactions
import spock.mock.MockInteractionSupport
import static apkg.Fixtures.stub

class Fixtures {
  @Interactions
  static void stub(List l) { 1 * l.add("x") }
}

class CallerSpec extends Specification {
  def "f"() {
    given:
    List l = Mock()

    when:
    stub(l)

    then:
    true
  }
}
''')

    then: "the call site keeps the original (throwing) overload; no spec was prepended"
    result.source.contains("apkg.Fixtures.stub(l)")
    !result.source.contains("stub(this")
  }

  def "an @Interactions stubbing helper placed in a then-block is relocated before the when-block"() {
    when:
    runner.runWithImports('''
      class StubFixtures {
        @Interactions
        void stub(List<String> gateway) {
          gateway.get(0) >> "item"
        }
      }

      class CallerSpec extends Specification {
        def "the stub takes effect on the when-action"() {
          given:
          List<String> gateway = Mock()
          StubFixtures fixtures = new StubFixtures()

          when:
          def result = gateway.get(0)

          then: "the helper, though written in the then-block, was moved before the when-block"
          fixtures.stub(gateway)
          result == "item"
        }
      }
    ''')

    then:
    noExceptionThrown()
  }

  def "an @Interactions cardinality helper placed in a then-block is enforced when satisfied"() {
    when:
    runner.runWithImports('''
      class ExpectFixtures {
        @Interactions
        void expectCharge(List<String> gateway) {
          1 * gateway.add("charge")
        }
      }

      class CallerSpec extends Specification {
        def "the cardinality is enforced against the when-action"() {
          given:
          List<String> gateway = Mock()
          ExpectFixtures fixtures = new ExpectFixtures()

          when:
          gateway.add("charge")

          then:
          fixtures.expectCharge(gateway)
        }
      }
    ''')

    then:
    noExceptionThrown()
  }

  def "an @Interactions cardinality helper placed in a then-block fails when unsatisfied"() {
    when:
    runner.runWithImports('''
      class ExpectFixtures {
        @Interactions
        void expectCharge(List<String> gateway) {
          1 * gateway.add("charge")
        }
      }

      class CallerSpec extends Specification {
        def "the cardinality is enforced against the when-action"() {
          given:
          List<String> gateway = Mock()
          ExpectFixtures fixtures = new ExpectFixtures()

          when:
          gateway.add("not charge")

          then:
          fixtures.expectCharge(gateway)
        }
      }
    ''')

    then:
    thrown(TooFewInvocationsError)
  }

  def "relocation works across a chain of then/and-then blocks"() {
    when:
    runner.runWithImports('''
      class ExpectFixtures {
        @Interactions
        void expectCharge(List<String> gateway) {
          1 * gateway.add("charge")
        }
      }

      class CallerSpec extends Specification {
        def "the helper in a trailing and-block is still relocated"() {
          given:
          List<String> gateway = Mock()
          ExpectFixtures fixtures = new ExpectFixtures()

          when:
          gateway.add("charge")

          then:
          true

          and:
          fixtures.expectCharge(gateway)
        }
      }
    ''')

    then:
    noExceptionThrown()
  }

  def "a non-top-level @Interactions call in a then-block is not relocated"() {
    when: "the call's result is consumed, so it cannot move as a whole statement"
    runner.runWithImports('''
      class StubFixtures {
        @Interactions
        void stub(List<String> gateway) {
          gateway.get(0) >> "item"
        }
      }

      class CallerSpec extends Specification {
        def "the stub registers too late to affect the when-action"() {
          given:
          List<String> gateway = Mock()
          StubFixtures fixtures = new StubFixtures()

          when:
          def result = gateway.get(0)

          then: "the call runs in place (after the action), so the stub did not apply"
          def ignored = fixtures.stub(gateway)
          result == null
        }
      }
    ''')

    then:
    noExceptionThrown()
  }

  static class ComposedFixtures {
    @Interactions
    void stubAll(List<String> gateway) {
      stubOne(gateway)
    }

    @Interactions
    void stubOne(List<String> gateway) {
      1 * gateway.get(0) >> "item"
    }
  }

  static class ComposingSupport implements MockInteractionSupport {
    final Specification specification
    ComposingSupport(Specification specification) { this.specification = specification }

    void applyStubs(List<String> gateway) {
      OrderFixtures fixtures = new OrderFixtures()
      fixtures.stubHappyPath(gateway)
    }
  }

  static class OrderFixtures {
    @Interactions
    void stubHappyPath(List<String> gateway) {
      1 * gateway.get(0) >> "item"
    }

    @Interactions
    static void staticStubHappyPath(List<String> gateway) {
      1 * gateway.get(0) >> "item"
    }
  }
}
