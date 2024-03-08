package org.spockframework.smoke.mock

import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.CountDownLatch

import static java.util.concurrent.TimeUnit.SECONDS

class MultiThreaded extends Specification {
  @Issue("https://github.com/spockframework/spock/issues/583")
  def "mocks can block and wait on conditions"() {
    given:
    CountDownLatch latch1 = new CountDownLatch(1)
    CountDownLatch latch2 = new CountDownLatch(1)
    CountDownLatch latch3 = new CountDownLatch(2)
    List aList = Mock()

    when:
    Thread.start { aList.get(0); latch3.countDown() }
    Thread.start { aList.get(0); latch3.countDown() }
    assert latch1.await(30, SECONDS)
    assert latch2.await(30, SECONDS)

    then:
    latch3.await(10, SECONDS)

    1 * aList.get(_) >> {
      latch1.countDown()
      assert latch2.await(10, SECONDS)
    }

    1 * aList.get(_) >> {
      latch2.countDown()
      assert latch1.await(10, SECONDS)
    }
  }

  @Issue("https://github.com/spockframework/spock/issues/1899")
  @Timeout(30)
  def "mock response generators do not deadlock when self-referencing"() {
    given:
    def called = false
    Runnable foo = Mock()
    foo.run() >> {
      if (!called) {
        called = true
        Thread.start { foo.run() }.join()
      }
    }

    when:
    foo.run()

    then:
    noExceptionThrown()
  }
}
