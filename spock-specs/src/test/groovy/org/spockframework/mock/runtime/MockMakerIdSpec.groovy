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

import spock.lang.Specification

import static org.spockframework.mock.runtime.IMockMaker.MockMakerId

class MockMakerIdSpec extends Specification {

  def "Valid creation"() {
    expect:
    new MockMakerId("valid").toString() == "valid"
  }

  def "Invalid creation"(String input, String expectedErrorMessage) {
    when:
    new MockMakerId(input)
    then:
    IllegalArgumentException ex = thrown()
    ex.message == expectedErrorMessage

    where:
    input          | expectedErrorMessage
    null           | "The ID is null."
    ""             | "The ID is empty."
    "with space"   | "The ID 'with space' is invalid. Valid id must comply to the pattern: ^[a-z][a-z0-9-]*+(?<!-)\$"
    "UpperCase"    | "The ID 'UpperCase' is invalid. Valid id must comply to the pattern: ^[a-z][a-z0-9-]*+(?<!-)\$"
    "0numberStart" | "The ID '0numberStart' is invalid. Valid id must comply to the pattern: ^[a-z][a-z0-9-]*+(?<!-)\$"
    "half-kebab-"  | "The ID 'half-kebab-' is invalid. Valid id must comply to the pattern: ^[a-z][a-z0-9-]*+(?<!-)\$"
  }

  def "Equals/hashcode"() {
    setup:
    def id1 = new MockMakerId("id1")
    def id1_2 = new MockMakerId("id1")
    def id2 = new MockMakerId("id2")
    expect:
    id1.hashCode() == id1_2.hashCode()
    id1.hashCode() != id2.hashCode()
    id1 == id1
    id1 == id1_2
    id1 != id2
  }

  @SuppressWarnings('ChangeToOperator')
  def "compareTo"() {
    setup:
    def id1 = new MockMakerId("id1")
    def id1_2 = new MockMakerId("id1")
    def id2 = new MockMakerId("id2")
    expect:
    id1.compareTo(id1) == 0
    id1.compareTo(id1_2) == 0
    id1.compareTo(id2) == -1
    id2.compareTo(id1) == 1
  }
}
