package org.spockframework.docs.interaction

import org.spockframework.mock.IDefaultResponse
import org.spockframework.mock.IMockInvocation
import org.spockframework.mock.MockUtil
import spock.lang.Specification
import spock.mock.DetachedMockFactory

class DetachedMockFactoryDocSpec extends Specification {

  def "Create a mock with DetachedMockFactory"() {
    setup:
    def mockUtil = new MockUtil()
    when:
    // tag::DetachedMockFactory-usage-spec[]
    DetachedMockFactory factory = new DetachedMockFactory()
    List listMock = factory.Mock(List.class)
    List listStub = factory.Stub(ArrayList.class)
    // end::DetachedMockFactory-usage-spec[]
    then:
    listMock instanceof List
    listStub instanceof ArrayList
    mockUtil.isMock(listMock)
    mockUtil.isMock(listStub)
  }

  def "Attach Mock"() {
    DetachedMockFactory factory = new DetachedMockFactory()
    List listMock = factory.Mock(List.class)
    // tag::attach-usage[]
    setup:
    def mockUtil = new MockUtil()
    mockUtil.attachMock(listMock, this)
    when:
    //Use the mock here
    listMock.add(1)
    then:
    1 * listMock.add(_)
    cleanup:
    mockUtil.detachMock(listMock)
    // end::attach-usage[]
  }

  // tag::defaultResponse-usage[]
  class CustomResponse implements IDefaultResponse {
    Object respond(IMockInvocation invocation) {
      if (invocation.method.name == "get") {
        return "value"
      }
      return null
    }
  }
  DetachedMockFactory factory = new DetachedMockFactory()
  List yourMock = factory.Mock(List, defaultResponse: new CustomResponse())
  // end::defaultResponse-usage[]

  def "Usage of an IDefaultResponse in a detached mock"() {
    setup:
    def mockUtil = new MockUtil()
    mockUtil.attachMock(yourMock, this)
    expect:
    yourMock.get(0) == "value"
    cleanup:
    mockUtil.detachMock(yourMock)
  }
}

