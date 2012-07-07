package org.spockframework.smoke.mock

import java.lang.reflect.Proxy
import spock.lang.Specification
import net.sf.cglib.proxy.Factory

class MockProxyCaching extends Specification {
  def "dynamic proxy classes are cached"() {
    def list1 = Mock(List)
    def list2 = Mock(List)

    expect:
    Proxy.isProxyClass(list1.getClass())
    Proxy.isProxyClass(list2.getClass())
    list1.getClass() == list2.getClass()
  }

  def "CGLIB proxy classes are cached"() {
    def list1 = Mock(ArrayList)
    def list2 = Mock(ArrayList)

    expect:
    list1 instanceof Factory
    list2 instanceof Factory
    list1.getClass() == list2.getClass()
  }
}
