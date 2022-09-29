package org.spockframework.runtime

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.extension.*
import org.spockframework.runtime.model.SpecInfo
import spock.lang.Specification

import java.lang.annotation.*

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
