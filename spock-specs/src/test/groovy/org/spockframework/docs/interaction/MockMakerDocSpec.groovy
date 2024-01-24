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

package org.spockframework.docs.interaction


import spock.lang.Specification
import spock.mock.MockMakers

class MockMakerDocSpec extends Specification {

  def "Mock with MockMaker"() {
    given:
// tag::mock1[]
    def subscriber = Mock(mockMaker: MockMakers.byteBuddy, Subscriber)
// end::mock1[]

    when:
    subscriber.receive("1")

    then:
    1 * _.receive(_)
  }

  def "mockito"() {
    given:
// tag::mockito[]
    Subscriber subscriber = Mock(mockMaker: MockMakers.mockito)
// end::mockito[]

    when:
    subscriber.receive("1")

    then:
    1 * _.receive(_)
  }

  @SuppressWarnings('UnnecessaryQualifiedReference')
  def "mockito additional settings serializable"() {
    given:
// tag::mock-serializable[]
    Subscriber subscriber = Mock(mockMaker: MockMakers.mockito {
      serializable()
    })
// end::mock-serializable[]

    when:
    subscriber.receive("1")

    then:
    1 * _.receive(_)
    subscriber instanceof Serializable
  }
}
