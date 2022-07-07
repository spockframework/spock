package spock.testkit.testsources

import spock.lang.Specification
import spock.lang.Tag

@Tag("inherited")
class SimpleTagSpec extends Specification {
  @Tag("shared")
  @Tag("simple")
  def simple() {
    expect: true
  }

  @Tag("complex")
  @Tag("shared")
  def complex() {
    expect: true
  }

  def bland() {
    expect: true
  }
}
