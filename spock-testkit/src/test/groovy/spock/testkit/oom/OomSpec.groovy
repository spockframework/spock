package spock.testkit.oom

import spock.lang.Specification

class OomSpec extends Specification {
  def a() {
    expect:
    throw new OutOfMemoryError()
  }
}
