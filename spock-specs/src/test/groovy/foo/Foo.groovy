package foo

import spock.lang.Specification

import org.junit.platform.commons.annotation.Testable

@Testable
class Foo extends Specification {

  def foo() {
    expect:
    true

    where:
    a | b
  }
}
