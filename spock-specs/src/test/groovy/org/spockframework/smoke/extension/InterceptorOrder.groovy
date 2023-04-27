package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification

class InterceptorOrder extends EmbeddedSpecification {
  static List<String> events

  def setup() {
    events = []
  }

  def cleanupSpec() {
    events = null
  }

  def "interceptors are called in the right order"() {
    given:

    when:
    runner.runWithImports '''
import org.spockframework.runtime.extension.*
import org.spockframework.runtime.model.SpecInfo

import java.lang.annotation.*

import static org.spockframework.smoke.extension.InterceptorOrder.events

    @InterceptEverything
    class ExampleSpec extends Specification {
      def setup() { events << "setup" }
      def cleanup() { events << "cleanup" }
      def setupSpec() { events << "setupSpec" }
      def cleanupSpec() { events << "cleanupSpec" }

      def "simple feature"() {
        expect:
        events << "simpleFeature"
       }

      def "parameterized feature"(int i) {
      expect:
      events << "parameterizedFeature"

      where:
      i << [1, 2]
      }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @ExtensionAnnotation(InterceptEverythingExtension)
    @interface InterceptEverything {}

    class InterceptEverythingExtension implements IAnnotationDrivenExtension<InterceptEverything> {
      @Override
      void visitSpecAnnotation(InterceptEverything annotation ,SpecInfo spec) {
        spec.addSharedInitializerInterceptor (interceptor('sharedInitializerInterceptor'))
        spec.sharedInitializerMethod?.addInterceptor (interceptor('sharedInitializerMethodInterceptor'))
        spec.addInterceptor (interceptor('specInterceptor'))
        spec.addSetupSpecInterceptor (interceptor('setupSpecInterceptor'))
        spec.setupSpecMethods*.addInterceptor (interceptor('setupSpecMethodInterceptor'))
        spec.addInitializerInterceptor (interceptor('initializerInterceptor'))
        spec.initializerMethod?.addInterceptor (interceptor('initializerMethodInterceptor'))
        spec.addSetupInterceptor (interceptor('setupInterceptor'))
        spec.setupMethods*.addInterceptor (interceptor('setupMethodInterceptor'))
        spec.addCleanupInterceptor (interceptor('cleanupInterceptor'))
        spec.cleanupMethods*.addInterceptor (interceptor('cleanupMethodInterceptor'))
        spec.addCleanupSpecInterceptor (interceptor('cleanupSpecInterceptor'))
        spec.cleanupSpecMethods*.addInterceptor (interceptor('cleanupSpecMethodInterceptor'))
        spec.features.each {
          it.addInterceptor(interceptor('featureInterceptor'))
          it.addIterationInterceptor(interceptor('iterationInterceptor'))
          it.featureMethod.addInterceptor(interceptor('featureMethodInterceptor'))
        }
      }

      IMethodInterceptor interceptor(String tag) { new InterceptEverythingInterceptor(tag) }
    }

    class InterceptEverythingInterceptor implements IMethodInterceptor {
      static indent = 0
      String tag

      InterceptEverythingInterceptor(String tag) {
        this.tag = tag
      }

      @Override
      void intercept(IMethodInvocation invocation) throws Throwable {
        events << "${' ' * indent++}> ${invocation.method.kind} $tag ${invocation.feature?.name}"
        invocation.proceed()
        events << "${' ' * --indent}< ${invocation.method.kind} $tag ${invocation.feature?.name}"
      }
    }
'''

    then:
    events == [
      '> SHARED_INITIALIZER sharedInitializerInterceptor null',
      '< SHARED_INITIALIZER sharedInitializerInterceptor null',
      '> SPEC_EXECUTION specInterceptor null',
      ' > SETUP_SPEC setupSpecInterceptor null',
      '  > SETUP_SPEC setupSpecMethodInterceptor null',
      'setupSpec',
      '  < SETUP_SPEC setupSpecMethodInterceptor null',
      ' < SETUP_SPEC setupSpecInterceptor null',
      ' > FEATURE_EXECUTION featureInterceptor simple feature',
      '  > INITIALIZER initializerInterceptor simple feature',
      '  < INITIALIZER initializerInterceptor simple feature',
      '  > ITERATION_EXECUTION iterationInterceptor simple feature',
      '   > SETUP setupInterceptor simple feature',
      '    > SETUP setupMethodInterceptor simple feature',
      'setup',
      '    < SETUP setupMethodInterceptor simple feature',
      '   < SETUP setupInterceptor simple feature',
      '   > FEATURE featureMethodInterceptor simple feature',
      'simpleFeature',
      '   < FEATURE featureMethodInterceptor simple feature',
      '   > CLEANUP cleanupInterceptor simple feature',
      '    > CLEANUP cleanupMethodInterceptor simple feature',
      'cleanup',
      '    < CLEANUP cleanupMethodInterceptor simple feature',
      '   < CLEANUP cleanupInterceptor simple feature',
      '  < ITERATION_EXECUTION iterationInterceptor simple feature',
      ' < FEATURE_EXECUTION featureInterceptor simple feature',
      ' > FEATURE_EXECUTION featureInterceptor parameterized feature',
      '  > INITIALIZER initializerInterceptor parameterized feature',
      '  < INITIALIZER initializerInterceptor parameterized feature',
      '  > ITERATION_EXECUTION iterationInterceptor parameterized feature',
      '   > SETUP setupInterceptor parameterized feature',
      '    > SETUP setupMethodInterceptor parameterized feature',
      'setup',
      '    < SETUP setupMethodInterceptor parameterized feature',
      '   < SETUP setupInterceptor parameterized feature',
      '   > FEATURE featureMethodInterceptor parameterized feature',
      'parameterizedFeature',
      '   < FEATURE featureMethodInterceptor parameterized feature',
      '   > CLEANUP cleanupInterceptor parameterized feature',
      '    > CLEANUP cleanupMethodInterceptor parameterized feature',
      'cleanup',
      '    < CLEANUP cleanupMethodInterceptor parameterized feature',
      '   < CLEANUP cleanupInterceptor parameterized feature',
      '  < ITERATION_EXECUTION iterationInterceptor parameterized feature',
      '  > INITIALIZER initializerInterceptor parameterized feature',
      '  < INITIALIZER initializerInterceptor parameterized feature',
      '  > ITERATION_EXECUTION iterationInterceptor parameterized feature',
      '   > SETUP setupInterceptor parameterized feature',
      '    > SETUP setupMethodInterceptor parameterized feature',
      'setup',
      '    < SETUP setupMethodInterceptor parameterized feature',
      '   < SETUP setupInterceptor parameterized feature',
      '   > FEATURE featureMethodInterceptor parameterized feature',
      'parameterizedFeature',
      '   < FEATURE featureMethodInterceptor parameterized feature',
      '   > CLEANUP cleanupInterceptor parameterized feature',
      '    > CLEANUP cleanupMethodInterceptor parameterized feature',
      'cleanup',
      '    < CLEANUP cleanupMethodInterceptor parameterized feature',
      '   < CLEANUP cleanupInterceptor parameterized feature',
      '  < ITERATION_EXECUTION iterationInterceptor parameterized feature',
      ' < FEATURE_EXECUTION featureInterceptor parameterized feature',
      ' > CLEANUP_SPEC cleanupSpecInterceptor null',
      '  > CLEANUP_SPEC cleanupSpecMethodInterceptor null',
      'cleanupSpec',
      '  < CLEANUP_SPEC cleanupSpecMethodInterceptor null',
      ' < CLEANUP_SPEC cleanupSpecInterceptor null',
      '< SPEC_EXECUTION specInterceptor null',
    ]
  }
}
