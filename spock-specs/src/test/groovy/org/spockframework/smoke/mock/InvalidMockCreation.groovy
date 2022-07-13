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

import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.InvalidSpecException
import spock.lang.Ignore

/**
 * @author Peter Niederwieser
 */
class InvalidMockCreation extends EmbeddedSpecification {
  def "field w/ missing type"() {
    when:
    runner.runSpecBody("""
def list = Mock()

def foo() { expect: true }
    """)

    then:
    thrown(InvalidSpecException)
  }

  def "field w/ incompatible type"() {
    when:
    runner.runSpecBody("""
List list = Mock(Map)

def foo() { expect: true }
    """)

    then:
    thrown(GroovyCastException)
  }

  def "field w/ wrong argument"() {
    when:
    runner.runSpecBody("""
List list = Mock(1)

def foo() { expect: true }
    """)

    then:
    thrown(MissingMethodException)
  }

  def "local w/ missing type"() {
    when:
    def list = Mock()

    then:
    thrown(InvalidSpecException)
  }

  @Ignore("TODO: doesn't throw an exception in Groovy 2.0")
  def "local w/ incompatible type"() {
    when:
    List list = Mock(Map)

    then:
    thrown(GroovyCastException)
  }

  def "local w/ wrong argument"() {
    when:
    def list = Mock(1)

    then:
    thrown(MissingMethodException)
  }

  def "expr w/ missing type"() {
    when:
    Mock()

    then:
    thrown(InvalidSpecException)
  }

  def "expr w/ wrong argument"() {
    when:
    Mock(1)

    then:
    thrown(MissingMethodException)
  }
}
