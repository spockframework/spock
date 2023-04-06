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

class MethodMatching extends Specification {
  List list = Mock()

  def "match equal method name"() {
    when: list.add(1)
    then: 1 * list.add(1)
  }

  @FailsWith(TooFewInvocationsError)
  def "doesn't match unequal method name"() {
    when: list.add(1)
    then: 1 * list.remove(1)
  }

  def "match any method name"() {
    when: list.add(1)
    then: 1 * list._(1)
  }

  def "match method name pattern"() {
    when: list.add(1)
    then: 1 * list./a.\w/(1)
  }

  @FailsWith(TooFewInvocationsError)
  def "doesn't match method name pattern"() {
    when: list.add(1)
    then: 1 * list./b.\w/(1)
  }
}
