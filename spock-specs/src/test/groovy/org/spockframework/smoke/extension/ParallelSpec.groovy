package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.model.parallel.*
import spock.lang.*

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

import static java.util.concurrent.TimeUnit.MILLISECONDS

@Isolated("Isolate from other tests to have full access to cores for the embedded tests.")
@Requires({Runtime.runtime.availableProcessors() >= 2})
class ParallelSpec extends EmbeddedSpecification {

  def setup() {
    runner.addPackageImport('spock.lang')
    runner.addClassImport(AtomicInteger)
    runner.addClassImport(CountDownLatch)
    runner.addClassImport(ResourceAccessMode)
    runner.addClassImport(ExecutionMode)
    runner.addClassMemberImport(ParallelSpec)
    runner.configurationScript {
      runner {
        optimizeRunOrder false
        parallel {
          enabled true
          fixed(4)
        }
      }
    }
  }

  def "tests can execute in parallel"() {
    when:
    def result = runner.runSpecBody '''
      @Shared
      AtomicInteger atomicInteger = new AtomicInteger()

      @Shared
      CountDownLatch latch = new CountDownLatch(2)

      def a() {
        when:
        incrementAndBlock(atomicInteger, latch, 10000)

        then:
        atomicInteger.get() == 2
      }

      def b() {
        when:
        incrementAndBlock(atomicInteger, latch, 10000)

        then:
        atomicInteger.get() == 2
      }
    '''

    then:
    result.testsSucceededCount == 2
  }

  def "tests can get exclusive access to resources"() {
    when:
    def result = runner.runSpecBody '''
      @Shared
      AtomicInteger atomicInteger = new AtomicInteger()

      @Shared
      CountDownLatch latch = new CountDownLatch(2)

      @ResourceLock("a")
      def writeA() {
        expect: incrementBlockAndCheck(atomicInteger, latch)
      }

      @ResourceLock("a")
      def writeB() {
        expect: incrementBlockAndCheck(atomicInteger, latch)
      }
    '''

    then:
    result.testsSucceededCount == 2
  }

  def "tests can get shared read and exclusive write access to resources"() {
    when:
    def result = runner.runSpecBody '''
      @Shared
      AtomicInteger atomicInteger = new AtomicInteger()

      @Shared
      CountDownLatch latch = new CountDownLatch(3)

      @ResourceLock(value = "a", mode = ResourceAccessMode.READ)
      def readA() {
        expect: storeAndBlockAndCheck(atomicInteger, latch)
      }

      @ResourceLock("a")
      def writeB() {
        given: "delay a bit so that the reads don't pick up the already increased value"
        sleep(20)
        expect: incrementBlockAndCheck(atomicInteger, latch)
      }

      @ResourceLock(value = "a", mode = ResourceAccessMode.READ)
      def readC() {
        expect: storeAndBlockAndCheck(atomicInteger, latch)
      }
    '''

    then:
    result.testsSucceededCount == 3
  }

  // must be changed to write lock when https://github.com/junit-team/junit5/issues/2423 is implemented
  def "a lock on the specification causes same thread execution"() {
    when:
    def result = runner.runWithImports '''
      @ResourceLock(value = "a", mode = ResourceAccessMode.READ)
      class ASpec extends Specification {
        @Shared
        AtomicInteger atomicInteger = new AtomicInteger()

        @Shared
        CountDownLatch latch = new CountDownLatch(2)

        @Shared
        String threadName

        def setupSpec() {
          threadName = Thread.currentThread().name
        }

        def writeA() {
          expect:
          threadName == Thread.currentThread().name
          incrementBlockAndCheck(atomicInteger, latch)
        }

        def writeB() {
          expect:
          threadName == Thread.currentThread().name
          incrementBlockAndCheck(atomicInteger, latch)
        }
      }
    '''

    then:
    result.testsSucceededCount == 2
  }

  def "ResourceLockChildren allows parallel execution"() {
    when:
    def result = runner.runWithImports '''
      @ResourceLockChildren(value = "a", mode = ResourceAccessMode.READ)
      class ASpec extends Specification {
        @Shared
        AtomicInteger atomicInteger = new AtomicInteger()

        @Shared
        CountDownLatch latch = new CountDownLatch(2)

        def writeA() {
        when:
        incrementAndBlock(atomicInteger, latch)

        then:
        atomicInteger.get() == 2
        }

        def writeB() {
        when:
        incrementAndBlock(atomicInteger, latch)

        then:
        atomicInteger.get() == 2
        }
      }
    '''

    then:
    result.testsSucceededCount == 2
  }
  def "Execution can be set to same thread execution"() {
    when:
    def result = runner.runWithImports '''
      @Execution(ExecutionMode.SAME_THREAD)
      class ASpec extends Specification {
        @Shared
        AtomicInteger atomicInteger = new AtomicInteger()

        @Shared
        CountDownLatch latch = new CountDownLatch(2)

        @Shared
        String threadName

        def setupSpec() {
          threadName = Thread.currentThread().name
        }

        def writeA() {
          expect:
          threadName == Thread.currentThread().name
          incrementBlockAndCheck(atomicInteger, latch)
        }

        def writeB() {
          expect:
          threadName == Thread.currentThread().name
          incrementBlockAndCheck(atomicInteger, latch)
        }
      }
    '''

    then:
    result.testsSucceededCount == 2
  }

  def "Execution can be set isolated"() {
    when:
    def result = runner.runWithImports '''
      class ASpec extends Specification {
        static AtomicInteger atomicInteger = new AtomicInteger()

        static CountDownLatch latch = new CountDownLatch(6)

        def writeA() {
          expect:
          storeAndBlockAndCheck(atomicInteger, latch)
        }

        def writeB() {
          expect:
          storeAndBlockAndCheck(atomicInteger, latch)
        }
      }

      @Isolated
      class BSpec extends Specification {

        @Shared
        String threadName

        def setupSpec() {
          threadName = Thread.currentThread().name
        }

        def writeA() {
          expect:
          threadName == Thread.currentThread().name
          incrementBlockAndCheck(ASpec.atomicInteger, ASpec.latch)
        }

        def writeB() {
          expect:
          threadName == Thread.currentThread().name
          incrementBlockAndCheck(ASpec.atomicInteger, ASpec.latch)
        }
      }

      class CSpec extends Specification {

        def writeA() {
          expect:
          storeAndBlockAndCheck(ASpec.atomicInteger, ASpec.latch)
        }

        def writeB() {
          expect:
          storeAndBlockAndCheck(ASpec.atomicInteger, ASpec.latch)
        }
      }
    '''

    then:
    result.testsSucceededCount == 6
  }

  static void incrementBlockAndCheck(AtomicInteger sharedResource, CountDownLatch countDownLatch)
    throws InterruptedException {
    int value = incrementAndBlock(sharedResource, countDownLatch)
    assert value == sharedResource.get()
  }

  static int incrementAndBlock(AtomicInteger sharedResource, CountDownLatch countDownLatch, long timeout = 100)
    throws InterruptedException {
    int value = sharedResource.incrementAndGet()
    countDownLatch.countDown()
    countDownLatch.await(timeout, MILLISECONDS)
    return value
  }

  static void storeAndBlockAndCheck(AtomicInteger sharedResource, CountDownLatch countDownLatch, long timeout = 100)
    throws InterruptedException {
    int value = sharedResource.get()
    countDownLatch.countDown()
    countDownLatch.await(timeout, MILLISECONDS)
    assert value == sharedResource.get()
  }
}
