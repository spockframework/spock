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

package org.spockframework.smoke

import org.spockframework.EmbeddedSpecification
import org.spockframework.compiler.InvalidSpecCompileException
import spock.lang.FailsWith
import org.spockframework.runtime.ConditionNotSatisfiedError

class AccessingOldValues extends EmbeddedSpecification {
  def "basic usage"() {
    def list = [1,2,3]

    when:
    list << 4

    then:
    list.size() == old(list.size()) + 1
  }

  def "multiple references to old value are possible"() {
    def list = [1,2,3]

    when:
    list << 4

    then:
    old(list.size()) == 3
    old(list.size()) == 3
  }

  def "multiple references are captured individually"() {
    def list = [1,2,3]

    when:
    list << 4

    then:
    old(list.remove(0)) == 1
    old(list.remove(0)) == 2
  }

  def "may occur in nested position"() {
    def list = [1,2,3]

    when:
    list << 4

    then:
    (0..2).every { old(list.size()) == 3 }
  }

  def "can access data-driven value"() {
    when:
    list << "elem"

    then:
    list.size() == old(list.size()) + 1

    where:
    list << [[], [1], [1,2,3]]
  }

  def "refers to an expression's value before entering the previous when-block"() {
    def list = [1,2,3]

    when:
    list << 4

    then:
    old(list.size()) == 3

    when:
    list << 5

    then:
    old(list.size()) == 4
  }

  def "works in presence of exception condition"() {
    def list = [1,2,3]

    when:
    list << 4
    throw new Exception()

    then:
    thrown(Exception)
    list.size() == old(list.size()) + 1
  }

  def "may only occur in a then-block"() {
    when:
    compiler.compileFeatureBody """
    def list = [1,2,3]
    when: old(list.size()) == 3
    then: true
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.line == 2
  }

  def "old() must have exactly one argument"() {
    when:
    compiler.compileFeatureBody """
    when: true
    then: old($oldArguments)
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.message == 'old() must have exactly one argument @ line 2, column 11.'

    where:
    oldArguments << [
      '',
      'null, null'
    ]
  }

  def "may occur outside of a condition"() {
    def list = [1,2,3]

    when:
    list << 4

    then:
    def size = old(list.size())
    size == 3
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "behaves correctly when part of a failing condition"() {
    def x = 1

    when:
    x = 2

    then:
    x == old(x) + 99
  }
}
