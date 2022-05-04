package spock.testkit.testsources

import spock.lang.Tag

@Tag("childSpec")
class ChildTagSpec extends SimpleTagSpec {

  @Tag("shared")
  @Tag("child")
  def complex() {
    expect: true
  }

  def plain() {
    expect: true
  }
}
