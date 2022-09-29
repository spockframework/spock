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

import spock.lang.Specification

/**
 *
 * @author Peter Niederwieser
 */
class InteractionScopes extends Specification {
  List list = Mock()
  def obj1 = new Object()

  def "local interactions are valid during execution of associated when-block"() {
    assert list.get(0) == null
    helper2()

    when:
    assert list.get(0) == obj1
    helper1()

    then:
    list.get(0) >> obj1
    list.get(0) == null

    expect:
    list.get(0) == null
    helper2()

    when:
    assert list.get(0) == null
    then:
    list.get(0) == null
  }

  def helper1() {
    assert list.get(0) == obj1
    true
  }

  def helper2() {
    assert list.get(0) == null
    true
  }

  def "global interactions are valid from the point of their definition to the end of the iteration"() {
    assert list.get(0) == null
    list.get(0) >> obj1
    assert list.get(0) == obj1

    expect:
    list.get(0) == obj1
    helper1()

    when:
    assert list.get(0) == obj1

    then:
    list.get(0) == obj1

    cleanup:
    assert list.get(0) == obj1
  }
}
