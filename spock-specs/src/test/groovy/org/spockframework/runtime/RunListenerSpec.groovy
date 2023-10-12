package org.spockframework.runtime

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.extension.*
import org.spockframework.runtime.model.SpecInfo
import org.spockframework.runtime.model.parallel.ExecutionMode
import spock.lang.Execution
import spock.lang.Ignore
import spock.lang.Issue
import spock.lang.Specification

import java.lang.annotation.*

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
