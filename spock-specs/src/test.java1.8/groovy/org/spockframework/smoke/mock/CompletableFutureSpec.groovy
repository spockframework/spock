package org.spockframework.smoke.mock

import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class CompletableFutureSpec extends Specification {

  def "default answer for CompletableFuture should be completed future"() {
    given:
    TestService service = Stub()

    when:
    CompletableFuture<String> receivedFuture = service.future

    then:
    receivedFuture.done
    !receivedFuture.completedExceptionally
    receivedFuture.get() == null
  }

  interface TestService {
    CompletableFuture<String> getFuture()
  }
}
