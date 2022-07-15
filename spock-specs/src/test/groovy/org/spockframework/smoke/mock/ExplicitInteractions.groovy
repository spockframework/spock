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
class ExplicitInteractions extends Specification {
  List list = Mock()

  def "basic usage"() {
    when:
    list.add(1)

    then:
    interaction {
      elementAdded()
    }
  }

  def elementAdded() {
    1 * list.add(1)
  }

  def "with helper variables"() {
    when:
    list.add(42)

    then:
    interaction {
      def x = 1
      def y = 42
      x * list.add(y)
    }
  }

  def "multiple"() {
    when:
    list.add(42)
    list.remove(0)

    then:
    interaction { 1 * list.add(_) }
    interaction { 1 * list.remove(_) }
  }

  def "in and'ed blocks"() {
    when:
    list.add(42)
    list.remove(0)

    then:
    interaction { 1 * list.add(_) }

    and:
    interaction { 1 * list.remove(_) }
  }

  def "followed by implicit interaction"() {
    when:
    list.add(42)
    list.remove(0)

    then:
    interaction { 1 * list.add(_) }
    1 * list.remove(_)
  }

  def "followed by condition"() {
    when:
    list.add(42)
    def x = 10

    then:
    interaction { 1 * list.add(_) }
    x == 10
  }
}
