package org.spockframework.smoke.mock

import spock.lang.Issue
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MultiThreaded extends Specification {

  @Issue("https://github.com/spockframework/spock/issues/583")
  def "mocks can block and wait on conditions"() {
    given:
    CountDownLatch latch = new CountDownLatch(2)
    List aList = Mock ()
    def exec = Executors.newFixedThreadPool(2)

    when:
    exec.submit { aList.get(0) }
    exec.submit { aList.get(0) }
    exec.shutdown()
    latch.await()

    then:
    exec.awaitTermination(30, TimeUnit.SECONDS)

    2 * aList.get(_) >> {
      latch.countDown()
      assert latch.await(30, TimeUnit.SECONDS)
      42
    }
  }
}
