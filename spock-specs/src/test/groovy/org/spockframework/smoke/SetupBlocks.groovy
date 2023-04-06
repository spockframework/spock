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

/**
 *
 * @author Peter Niederwieser
 */
class SetupBlocks extends EmbeddedSpecification {
  def "implicit"() {
    def a = 1
  }

  def "explicit"() {
    setup: def a = 1
  }

  def "implicit and explicit"() {
    def a = 1
    setup: def b = 2
  }

  def "named"() {
    setup: "an a"
    def a = 1
  }

  def "and-ed"() {
    setup: def a = 1
    and: def b = 2
  }

  def "full house"() {
    def a = 1
    def b = 2
    setup: "c"
    def c = 3
    and: "d"
    def d = 4
    and: def e = 5
  }


  def "consecutive setup's"() {
    when:
    compiler.compileFeatureBody """
setup: a = 1
setup: b = 2
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.line == 2
  }

  def "multiple setup's"() {
    when:
    compiler.compileFeatureBody """
setup: a = 1
expect: true
setup: b = 2
      """

    then:
    thrown(InvalidSpecCompileException)
  }

  def "blocks are executed"() {
    def a = 1
    setup: a += 1
    and: "blah"; a += 2
    expect: a == 4
  }

  def "given is alias for setup"() {
    def a
    given: a = 1
    expect: a == 1
  }
}
