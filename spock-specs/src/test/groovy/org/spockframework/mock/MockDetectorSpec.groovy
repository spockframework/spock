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
package org.spockframework.mock

import spock.lang.Specification

class MockDetectorSpec extends Specification {
  def detector = new MockUtil()

  def "detects interface based mocks"() {
    expect:
    detector.isMock(Mock(List))
    !detector.isMock([])
  }

  def "detects class based mocks"() {
    expect:
    detector.isMock(Mock(ArrayList))
    !detector.isMock(new ArrayList())
  }

  def "detects all natures of mock object"() {
    expect:
    detector.isMock(Mock(List))
    detector.isMock(Stub(List))
    detector.isMock(Spy(ArrayList))
    detector.isMock(GroovyMock(List))
    detector.isMock(GroovyStub(List))
    detector.isMock(GroovySpy(ArrayList))
  }

  def "provides access to mock object information"() {
    def list = Mock(List)

    expect:
    def mock = detector.asMock(list)
    mock.name == "list"
    mock.type == List
    mock.instance == list
  }

  def "complains if information about non-mock is requested"() {
    when:
    detector.asMock([])

    then:
    thrown(IllegalArgumentException)
  }
}
