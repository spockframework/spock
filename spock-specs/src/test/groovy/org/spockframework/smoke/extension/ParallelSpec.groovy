package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.model.parallel.*
import spock.lang.*

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

import static java.util.concurrent.TimeUnit.MILLISECONDS

@Isolated("Isolate from other tests to have full access to cores for the embedded tests.")
@Requires({ Runtime.runtime.availableProcessors() >= 2 })
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

  def "a write lock on the specification causes same thread execution"() {
    when:
    def result = runner.runWithImports '''
      @ResourceLock("a")
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

  def "@ResourceLock on specs is inherited"() {
    when:
    def result = runner.runWithImports '''
      @ResourceLock("a")
      abstract class BaseSpec extends Specification {
        @Shared
        AtomicInteger atomicInteger = new AtomicInteger()

        @Shared
        CountDownLatch latch = new CountDownLatch(2)

        @Shared
        String threadName
      }

      class ASpec extends BaseSpec {

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

  def "@ResourceLock with only READ allows parallel execution"() {
    when:
    def result = runner.runWithImports '''
      @ResourceLock(value = "a", mode = ResourceAccessMode.READ)
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

  def "@ResourceLock on Spec can be combined with @ResourceLock"() {
    when:
    def result = runner.runWithImports '''
      @ResourceLock(value = "a", mode = ResourceAccessMode.READ)
      class ASpec extends Specification {
        @Shared
        AtomicInteger atomicInteger = new AtomicInteger()

        @Shared
        CountDownLatch latch = new CountDownLatch(2)

        def readA() {
          expect: storeAndBlockAndCheck(atomicInteger, latch)
        }

        @ResourceLock("a")
        def writeB() {
          expect: incrementBlockAndCheck(atomicInteger, latch)
        }

        def readC() {
          expect: storeAndBlockAndCheck(atomicInteger, latch)
        }
      }
    '''

    then:
    result.testsSucceededCount == 3
  }

  def "@ResourceLock with only READ allows parallel execution of data-driven features"() {
    when:
    def result = runner.runWithImports '''
      class ASpec extends Specification {
        @Shared
        AtomicInteger atomicInteger = new AtomicInteger()

        @Shared
        CountDownLatch latch = new CountDownLatch(3)

        @ResourceLock(value = "a", mode = ResourceAccessMode.READ)
        def writeA() {
          when:
          incrementAndBlock(atomicInteger, latch)

          then:
          atomicInteger.get() == 3

          where:
          i << (1..3)
        }
      }
    '''

    then:
    result.testsSucceededCount == 4
  }

  def "@ResourceLock with only WRITE forces same thread execution of data-driven features"() {
    when:
    def result = runner.runWithImports '''
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

        @ResourceLock("a")
        def writeA() {
          expect:
          threadName == Thread.currentThread().name
          incrementBlockAndCheck(atomicInteger, latch)

          where:
          i << (1..2)
        }
      }
    '''

    then:
    result.testsSucceededCount == 3
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

  def "@Execution on specs is inherited"() {
    when:
    def result = runner.runWithImports '''
      @Execution(ExecutionMode.SAME_THREAD)
      abstract class BaseSpec extends Specification {
        @Shared
        AtomicInteger atomicInteger = new AtomicInteger()

        @Shared
        CountDownLatch latch = new CountDownLatch(2)

        @Shared
        String threadName

      }

      class ASpec extends BaseSpec {
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

  def "Execution can be isolated"() {
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

  def "@Isolated is inherited"() {
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
      abstract class BaseSpec extends Specification {
        @Shared
        String threadName
      }

      class BSpec extends BaseSpec {
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
