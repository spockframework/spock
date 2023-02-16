package org.spockframework.runtime

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.model.BlockKind
import org.spockframework.runtime.model.ErrorInfo
import org.spockframework.runtime.model.SpecInfo
import org.spockframework.runtime.model.parallel.ExecutionMode
import spock.lang.Execution
import spock.lang.Ignore
import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Execution(
  value = ExecutionMode.SAME_THREAD,
  reason = "Uses a shared field on a delegate"
)
class RunListenerSpec extends EmbeddedSpecification {

  IRunListener runListener = Mock()

  def "IRunListener methods are called in correct order and cardinality"() {
    given:
    RunListenerDelegate.delegate = runListener
    runner.addPackageImport(Specification.package)
    runner.addClassImport(RegisterRunListener)

    when:
    runner.runWithImports '''
@RegisterRunListener
class ASpec extends Specification {
  def "a test"() {
      expect: true
  }
}
'''

    then:
    1 * runListener.beforeSpec(_)
    then:
    1 * runListener.beforeFeature(_)
    then:
    1 * runListener.beforeIteration(_)
    then:
    1 * runListener.afterIteration(_)
    then:
    1 * runListener.afterFeature(_)
    then:
    1 * runListener.afterSpec(_)
    then:
    0 * runListener._

    cleanup:
    RunListenerDelegate.delegate = null
  }

  def "IRunListener is called for skipped features"() {
    given:
    RunListenerDelegate.delegate = runListener
    runner.addPackageImport(Specification.package)
    runner.addClassImport(RegisterRunListener)
    runner.addClassImport(Ignore)

    when:
    runner.runWithImports '''
@RegisterRunListener
class ASpec extends Specification {
  @Ignore
  def "a test"() {
      expect: true
  }

  @Ignore
  def "data driven test"() {
      expect: true
      where:
      i << [1, 2]
  }
}
'''


    then:
    1 * runListener.beforeSpec(_)
    then:
    2 * runListener.featureSkipped(_)
    then:
    1 * runListener.afterSpec(_)
    then:
    0 * runListener._

    cleanup:
    RunListenerDelegate.delegate = null
  }

  @Issue("https://github.com/spockframework/spock/issues/1662")
  def "IRunListener is called for skipped specs"() {
    given:
    RunListenerDelegate.delegate = runListener
    runner.addPackageImport(Specification.package)
    runner.addClassImport(RegisterRunListener)
    runner.addClassImport(Ignore)

    when:
    runner.runWithImports '''
@RegisterRunListener
@Ignore
class ASpec extends Specification {
  def "a test"() {
      expect: true
  }
}
'''

    then:
    1 * runListener.specSkipped(_)
    then:
    0 * runListener._

    cleanup:
    RunListenerDelegate.delegate = null
  }

  def "IRunListener gets called for errors"() {
    given:
    RunListenerDelegate.delegate = runListener
    runner.addPackageImport(Specification.package)
    runner.addClassImport(RegisterRunListener)
    runner.throwFailure = false

    when:
    runner.runWithImports '''
@RegisterRunListener
class ASpec extends Specification {
  def "a test"() {
      expect: "failing expect"
      false

      cleanup: "failing cleanup"
      throw new RuntimeException("failing cleanup")
  }
}
'''

    then:
    1 * runListener.beforeSpec(_)
    then:
    1 * runListener.beforeFeature(_)
    then:
    1 * runListener.beforeIteration(_)
    then:
    1 * runListener.error(_) >> { ErrorInfo errorInfo ->
      with(errorInfo.errorContext.currentBlock) {
        it.kind == BlockKind.EXPECT
        it.texts == ["failing expect"]
      }
      assert errorInfo.exception instanceof AssertionError
      assert errorInfo.exception.suppressed[0].message == "failing cleanup"
    }
    then:
    1 * runListener.afterIteration(_)
    then:
    1 * runListener.afterFeature(_)
    then:
    1 * runListener.afterSpec(_)
    then:
    0 * runListener._

    cleanup:
    RunListenerDelegate.delegate = null
  }

  @Unroll("IRunListener.error gets called for #errorLocation")
  def "IRunListener gets called for different error locations"() {
    given:
    RunListenerDelegate.delegate = runListener
    runner.addPackageImport(Specification.package)
    runner.addClassImport(RegisterRunListener)
    runner.throwFailure = false

    when:
    runner.runWithImports """
@RegisterRunListener
class ASpec extends Specification {
  def setupSpec() {
    assert "$errorLocation" != "setupSpec"
  }
  def setup() {
    assert "$errorLocation" != "setup"
  }

  def "a test"() {
    assert "$errorLocation" != "feature start"

    given: "setup label"
    assert "$errorLocation" != "feature setup"

    expect: "expect label"
    "$errorLocation" != "feature expect"

    when: "when label"
    assert "$errorLocation" != "feature when"

    then: "then label"
    "$errorLocation" != "feature then"

    cleanup: "cleanup label"
    assert "$errorLocation" != "feature cleanup"
  }

  def cleanup() {
    assert "$errorLocation" != "cleanup"
  }

  def cleanupSpec() {
    assert "$errorLocation" != "cleanupSpec"
  }
}
"""

    then:
    1 * runListener.error(_) >> { ErrorInfo errorInfo ->
      if (block != null) {
        with(errorInfo.errorContext.currentBlock) {
          it.kind == block
          it.texts == blockTexts
        }
      } else {
        assert errorInfo.errorContext.currentBlock == null
      }
    }

    cleanup:
    RunListenerDelegate.delegate = null

    where:
    errorLocation     | block             | blockTexts
    "setupSpec"       | null              | []
    "setup"           | null              | []
    "feature start"   | null              | []
    "feature setup"   | BlockKind.SETUP   | ["setup label"]
    "feature expect"  | BlockKind.EXPECT  | ["expect label"]
    "feature when"    | BlockKind.WHEN    | ["when label"]
    "feature then"    | BlockKind.THEN    | ["then label"]
    "feature cleanup" | BlockKind.CLEANUP | ["cleanup label"]
    "cleanup"         | null              | []
    "cleanupSpec"     | null              | []
  }
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(RegisterRunListenerExtension)
@interface RegisterRunListener {}

class RegisterRunListenerExtension implements IAnnotationDrivenExtension<RegisterRunListener> {
  @Override
  void visitSpecAnnotation(RegisterRunListener annotation, SpecInfo spec) {
    spec.addListener(new RunListenerDelegate())
  }
}

class RunListenerDelegate implements IRunListener {
  @Delegate
  static IRunListener delegate
}
