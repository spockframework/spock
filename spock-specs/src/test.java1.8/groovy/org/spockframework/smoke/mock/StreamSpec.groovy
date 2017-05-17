package org.spockframework.smoke.mock

import spock.lang.Specification

import java.util.stream.*

class StreamSpec extends Specification {

  def "Streams are empty"() {
    given:
    StreamService service = Stub()

    expect:
    service.stringStream().count() == 0
    service.intStream().count() == 0
    service.doubleStream().count() == 0
    service.longStream().count() == 0
  }

  interface StreamService {
    Stream<String> stringStream()
    IntStream intStream()
    DoubleStream doubleStream()
    LongStream longStream()
  }
}


