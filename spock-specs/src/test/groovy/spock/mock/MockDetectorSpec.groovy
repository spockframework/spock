/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spock.mock

import spock.lang.Specification

class MockDetectorSpec extends Specification {
  def "detects interface based mocks"() {
    expect:
    MockDetector.isMock(Mock(List))
    !MockDetector.isMock([])
  }

  def "detects class based mocks"() {
    expect:
    MockDetector.isMock(Mock(ArrayList))
    !MockDetector.isMock(new ArrayList())
  }

  def "detects all natures of mock object"() {
    expect:
    MockDetector.isMock(Mock(List))
    MockDetector.isMock(Stub(List))
    MockDetector.isMock(Spy(ArrayList))
    MockDetector.isMock(GroovyMock(List))
    MockDetector.isMock(GroovyStub(List))
    MockDetector.isMock(GroovySpy(ArrayList))
  }

  def "provides access to mock object information"() {
    def list = Mock(List)

    expect:
    def mock = MockDetector.asMock(list)
    mock.name == "list"
    mock.type == List
    mock.instance == list
  }

  def "complains if information about non-mock is requested"() {
    when:
    MockDetector.asMock([])

    then:
    thrown(IllegalArgumentException)
  }
}
