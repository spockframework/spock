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
import spock.mock.MockMakers

import java.lang.reflect.Proxy

class JavaProxyMockMakerSpec extends Specification {

  def "Verify ID and IMockMakerSettings"() {
    expect:
    MockMakers.javaProxy.mockMakerId.toString() == "java-proxy"
    MockMakers.javaProxy.toString() == "java-proxy default mock maker settings"
  }

  def "Use specific MockMaker javaProxy"() {
    when:
    Runnable m = Mock(mockMaker: MockMakers.javaProxy)
    def mockClass = m.getClass()
    then:
    Proxy.isProxyClass(mockClass)
  }

  def "Use specific MockMaker javaProxy for class shall fail"() {
    when:
    Mock(mockMaker: MockMakers.javaProxy, ArrayList)
    then:
    CannotCreateMockException ex = thrown()
    ex.message == "Cannot create mock for class java.util.ArrayList. java-proxy: Cannot mock classes."
  }

  def "Constructor args are not supported"() {
    when:
    Spy(mockMaker: MockMakers.javaProxy, constructorArgs: ["A"], Runnable)
    then:
    CannotCreateMockException ex = thrown()
    ex.message == "Cannot create mock for interface java.lang.Runnable. java-proxy: Explicit constructor arguments are not supported."
  }
}
