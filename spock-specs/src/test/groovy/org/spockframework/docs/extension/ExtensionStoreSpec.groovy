package org.spockframework.docs.extension

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.IStandardStreamsListener
import org.spockframework.runtime.StandardStreamsCapturer
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.ISpockExecution
import org.spockframework.runtime.extension.IStore.Namespace
import org.spockframework.runtime.model.SpecInfo
import org.spockframework.runtime.model.parallel.Resources
import spock.lang.AutoCleanup
import spock.lang.ResourceLock

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


@ResourceLock(Resources.SYSTEM_OUT)
@ResourceLock(Resources.SYSTEM_ERR)
class ExtensionStoreSpec extends EmbeddedSpecification {

  @AutoCleanup("stop")
  StandardStreamsCapturer outputCapturer = new StandardStreamsCapturer()
  def lines = new StringBuilder()
  def listener = Mock(IStandardStreamsListener) {
    standardOut(_) >> { String line -> lines.append(line.normalize()) }
  }

  def setup() {
    outputCapturer.addStandardStreamsListener(listener)
    outputCapturer.start()
    outputCapturer.muteStandardStreams()
  }

  def "extension store example"() {
    given:
    runner.extensionClasses << ExceptionCounterGlobalExtension
    runner.throwFailure = false

    when:
    runner.runWithImports("""
     import spock.lang.Specification
// tag::example-spec[]
     class ASpec extends Specification {
       def "a failing test"() {
         expect: false
       }

       def "a test failing with an exception"() {
         given:
         if(1==1) throw new IllegalStateException()

         expect: true
       }
     }

     class BSpec extends Specification {

       def "illegal parameter for List"() {
         given:
         def value = new ArrayList<>(-1)

         expect:
         value.empty
       }

       def "illegal parameter for Map"() {
         given:
         def value = new HashMap(-1)

         expect:
         value.empty
       }
     }
// end::example-spec[]
""")

    then:
/* tag::example-result[] */    lines.toString() == '''\
========================
==Exception statistics==
========================
1x java.lang.IllegalStateException
2x java.lang.IllegalArgumentException
========================
''' // end::example-result[]
  }

}
// tag::example-extension[]
class ExceptionCounterGlobalExtension implements IGlobalExtension {
  private static final Namespace EXAMPLE_NS = Namespace.create(ExceptionCounterGlobalExtension)    // <1>
  private static final String ACCUMULATOR = "accumulator"
  private static final IMethodInterceptor INTERCEPTOR =  {
    try {
      it.proceed()
    } catch (Exception e) {
      it.getStore(EXAMPLE_NS)                                                                      // <6>
        .get(ACCUMULATOR, ConcurrentHashMap)                                                       // <7>
        .computeIfAbsent(e.class.name, { __ -> new AtomicInteger() })                              // <8>
        .incrementAndGet()
      throw e
    }
  }

  @Override
  void visitSpec(SpecInfo spec) {
    spec.allFeatures.featureMethod*.addInterceptor(INTERCEPTOR)                                    // <2>
  }

  @Override
  void executionStart(ISpockExecution spockExecution) {                                            // <3>
    spockExecution.getStore(EXAMPLE_NS)                                                            // <4>
      .put(ACCUMULATOR, new ConcurrentHashMap())                                                   // <5>
  }

  @Override
  void executionStop(ISpockExecution spockExecution) {                                             // <9>
    def results = spockExecution.getStore(EXAMPLE_NS)                                              // <10>
      .get(ACCUMULATOR, ConcurrentHashMap)

    if (!results.isEmpty()) {
      println "========================"
      println "==Exception statistics=="
      println "========================"
      results.toSorted().each { exceptionName, counter ->
        println "${counter}x $exceptionName"
      }
      println "========================"
    }
  }
}
// end::example-extension[]
