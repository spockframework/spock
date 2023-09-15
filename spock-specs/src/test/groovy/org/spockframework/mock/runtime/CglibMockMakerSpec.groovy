/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.mock.runtime

import org.spockframework.mock.CannotCreateMockException
import spock.lang.Specification
import spock.mock.DetachedMockFactory
import spock.mock.MockMakers

import java.lang.reflect.Proxy
import java.util.concurrent.Callable

class CglibMockMakerSpec extends Specification {

  def "Verify ID and IMockMakerSettings"() {
    expect:
    MockMakers.cglib.mockMakerId.toString() == "cglib"
    MockMakers.cglib.toString() == "cglib"
  }

  def "Use specific MockMaker cglib"() {
    when:
    Runnable m = Mock(mockMaker: MockMakers.cglib)
    def mockClass = m.getClass()
    then:
    !Proxy.isProxyClass(mockClass)
    mockClass.name.contains('$$EnhancerByCGLIB$$')
  }

  def "Use specific MockMaker cglib with DetachedMockFactory"() {
    given:
    def factory = new DetachedMockFactory()
    when:
    def m = factory.Mock(mockMaker: MockMakers.cglib, Runnable)
    def mockClass = m.getClass()
    then:
    !Proxy.isProxyClass(mockClass)
    mockClass.name.contains('$$EnhancerByCGLIB$$')
  }

  def "Use specific MockMaker cglib intercept call for interface"() {
    given:
    Callable m = Mock(mockMaker: MockMakers.cglib)
    when:
    def result = m.call()
    then:
    1 * m.call() >> 1
    result == 1
  }

  def "Use specific MockMaker cglib intercept call for class"() {
    when:
    ArrayList m = Mock(mockMaker: MockMakers.cglib)
    m.get(_) >> 1
    then:
    m.get(0) == 1
  }

  def "Final classes are not supported"() {
    when:
    Mock(mockMaker: MockMakers.cglib, StringBuilder)
    then:
    def ex = thrown(CannotCreateMockException)
    ex.message == "Cannot create mock for class java.lang.StringBuilder. cglib: Cannot mock final classes."
  }
}
