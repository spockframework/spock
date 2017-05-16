package org.spockframework.mock

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
class DetachedMockSpec extends Specification {

  @Shared
  MockUtil mockUtil = new MockUtil()

  @Shared
  List listMock

  def setupSpec() {
    listMock = mockUtil.createDetachedMock("listMock", List, MockNature.MOCK, MockImplementation.JAVA, [:], getClass().classLoader)
  }

  def setup () {
    mockUtil.attachMock(listMock, this)
  }

  def cleanup() {
    mockUtil.detachMock(listMock)
  }


  def "Mock returns default answer"() {
    expect:
    listMock.size() == 0
  }

  def "Configure and test mock"() {
    when:
    assert listMock.size() == 1

    then:
    1 * listMock.size() >> 1
  }

  def "Mock returns default answer after being configured and reattached"() {
    expect:
    listMock.size() == 0
  }
}
