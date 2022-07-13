/*
 * Copyright 2010 the original author or authors.
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

import spock.lang.*
import org.spockframework.mock.IMockInvocation

class MockNames extends Specification {
  def fieldMock = Mock(List)
  def fieldMock2

  def setup() {
    fieldMock2 = Mock(List)
  }

  def "a mock's name is inferred from the variable it is assigned to at the point of its creation"() {
    def variableMock = Mock(List)
    def variableMock2
    variableMock2 = Mock(List)

    // need a dummy interaction to get to mock's name
    when:
    fieldMock.get(0)
    fieldMock2.get(0)
    variableMock.get(0)
    variableMock2.get(0)

    then:
    1 * fieldMock._ >> { IMockInvocation inv -> assert inv.mockObject.name == "fieldMock" }
    1 * fieldMock2._ >> { IMockInvocation inv -> assert inv.mockObject.name == "fieldMock2" }
    1 * variableMock._ >> { IMockInvocation inv -> assert inv.mockObject.name == "variableMock" }
    1 * variableMock2._ >> { IMockInvocation inv -> assert inv.mockObject.name == "variableMock2" }
  }

  def "if a mock isn't assigned to a variable at the point of its creation, it doesn't have a name"() {
    when:
    Mock(List).get(0)

    then:
    1 * _ >> { IMockInvocation inv -> assert inv.mockObject.name == null }
  }
}
