/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.smoke.mock

import org.spockframework.mock.TooFewInvocationsError
import spock.lang.FailsWith
import spock.lang.Specification

/**
 * @author Peter Niederwieser
 */
class GlobalInteractions extends Specification {
  def "optional interaction"() {
    def m = Mock(List)
    m.size() >> 1
    expect: m.size() == 1
  }

  @FailsWith(TooFewInvocationsError)
  def "required interaction"() {
    def m = Mock(List)
    2 * m.get(_) >> "foo"
    expect: m.get(3) == "foo"
  }

  def "optional interaction defined in helper method"() {
    def m = optional()
    expect: m.size() == 1
  }

  @FailsWith(TooFewInvocationsError)
  def "required interaction defined in helper method"() {
    def m = required()
    expect: m.get(3) == "foo"
  }

  def optional() {
    def m = Mock(List)
    m.size() >> 1
    m
  }

  def required() {
    def m = Mock(List)
    2 * m.get(_) >> "foo"
    m
  }
}
