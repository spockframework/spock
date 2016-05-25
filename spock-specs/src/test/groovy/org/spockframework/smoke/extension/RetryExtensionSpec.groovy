package org.spockframework.smoke.extension

import org.junit.ComparisonFailure
import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.extension.builtin.RetryExtension
import org.spockframework.runtime.model.FeatureInfo
import spock.lang.Retry

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target


class RetryExtensionSpec extends EmbeddedSpecification {

  def "if no failure, test should be run only once"(){
    when:
    runner.runSpecBody("""
      @Shared int runCount = 0;

      @Retry(5)
      def test(){
        runCount++;
        expect:
          runCount == 1
      }
    """)

    then:
    noExceptionThrown()
  }

  def "if there are failures, should restart iteration (max retries is 1 + Retry.value)"(){
    when:
    runner.runSpecBody("""
      @Shared int runCount = 0;

      @Retry(5)
      def test(){
        runCount++;
        expect:
          runCount == 1 + 5
      }
    """)

    then:
    noExceptionThrown()
  }

  def "if there are failures, should restart iteration and report last failure if all attempts was exceeded"(){
    when:
    runner.runSpecBody("""
      @Shared int runCount = 0;

      @Retry(5)
      def test(){
        runCount++;
        expect:
          runCount == 1 + 5 + 1 // should never reach this value
      }
    """)

    then:
    ComparisonFailure failure = thrown()
    failure.actual.trim() == "6"
    failure.expected.trim() == "7"
  }

  def "method level annotation should override spec level "(){
    when:
    runner.runWithImports("""
     @Retry(4)
     class Foo{
        @Shared int runCount1 = 0;
        @Shared int runCount2 = 0;

        @Retry(5)
        def test1(){
          runCount1++;
          expect:
            runCount1 == 1 + 5
        }

        def test2(){
          runCount2++;
          expect:
            runCount2 == 1 + 4
        }
      }
    """)

    then:
    noExceptionThrown()
  }

  def "extensions should be invoked once per attempt, cleanups should be invoked after each attempt"() {
    RegisterCleanupExtension.specAndCleanupCountTL.remove()

    when:
    runner.runSpecBody("""
      @Shared int runCount = 0;

      @Retry(1)
      @org.spockframework.smoke.extension.RegisterCleanup
      def test(){
        runCount++;
        expect:
          runCount == 1 + 1
      }
    """)

    then:
        Map<Object, Integer> specAndCleanupCount = RegisterCleanupExtension.specAndCleanupCountTL.get()
        specAndCleanupCount.size() == 2 // tow attempts
        for (Integer cleanupCount : specAndCleanupCount.values()) {
          assert cleanupCount == 1; // cleanups should not stack
        }
  }
}

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.METHOD, ElementType.TYPE])
@ExtensionAnnotation(RegisterCleanupExtension.class)
@interface RegisterCleanup{

}

public class RegisterCleanupExtension extends AbstractAnnotationDrivenExtension<RegisterCleanup> {

  static final ThreadLocal<Map<Object, Integer>> specAndCleanupCountTL = new ThreadLocal<>();

  @Override
  void visitFeatureAnnotation(RegisterCleanup annotation, FeatureInfo feature) {
    if (specAndCleanupCountTL.get() == null) {
      specAndCleanupCountTL.set(new IdentityHashMap<Object, Integer>())
    }
    feature.addIterationInterceptor(new IMethodInterceptor() {
      @Override
      void intercept(IMethodInvocation invocation) throws Throwable {
        invocation.getIteration().addCleanup(new Runnable() {
          @Override
          void run() {

            Map<Object, Integer> specAndCleanupCount = RegisterCleanupExtension.specAndCleanupCountTL.get()
            def instance = invocation.getInstance()

            if (specAndCleanupCount.get(instance) == null) {
              specAndCleanupCount.put(instance, 0)
            }

            specAndCleanupCount.put(instance, specAndCleanupCount.get(instance) + 1)
          }
        })
        invocation.proceed()
      }
    })
  }
}
