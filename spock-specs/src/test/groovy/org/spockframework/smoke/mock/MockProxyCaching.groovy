/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.mock

import java.lang.reflect.Proxy
import spock.lang.Specification

class MockProxyCaching extends Specification {
  def "dynamic proxy classes are cached"() {
    def list1 = Mock(List)
    def list2 = Mock(List)

    expect:
    Proxy.isProxyClass(list1.getClass())
    Proxy.isProxyClass(list2.getClass())
    list1.getClass() == list2.getClass()
  }

  def "Byte Buddy or CGLIB proxy classes are cached"() {
    def list1 = Mock(ArrayList)
    def list2 = Mock(ArrayList)

    expect:
    list1.getClass() == list2.getClass()
  }
}
