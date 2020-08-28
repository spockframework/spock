package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

import static java.util.concurrent.TimeUnit.MILLISECONDS

class ParallelSpec extends EmbeddedSpecification {

  static void incrementBlockAndCheck(AtomicInteger sharedResource, CountDownLatch countDownLatch)
    throws InterruptedException {
    int value = incrementAndBlock(sharedResource, countDownLatch)
    assert value == sharedResource.get()
  }

  static int incrementAndBlock(AtomicInteger sharedResource, CountDownLatch countDownLatch)
    throws InterruptedException {
    int value = sharedResource.incrementAndGet()
    countDownLatch.countDown()
    countDownLatch.await(100, MILLISECONDS)
    return value
  }

  static void storeAndBlockAndCheck(AtomicInteger sharedResource, CountDownLatch countDownLatch)
    throws InterruptedException {
    int value = sharedResource.get()
    countDownLatch.countDown()
    countDownLatch.await(100, MILLISECONDS)
    assert value == sharedResource.get()
  }

}
