/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.spockframework.compiler.SpecCompileException
import spock.lang.FailsWith
import spock.lang.Specification

/**
 *
 * @author Peter Niederwieser
 */
class InvalidMockCreation extends EmbeddedSpecification {
  def "field w/ missing type"() {
    when:
    compiler.compileSpecBody("""
def list = Mock()
    """)

    then:
    thrown(SpecCompileException)
  }

  // for "field w/ incompatible type" and "field w/ wrong argument" see Specs below

  def "local w/ missing type"() {
    when:
    compiler.compileFeatureBody("""
setup:
def list = Mock()
    """)

    then:
    thrown(SpecCompileException)
  }

  def "local w/ incompatible type"() {
    when: List list = Mock(Map)
    then: thrown(GroovyCastException)
  }

  def "local w/ wrong argument"() {
    when: def list = Mock(1)
    then: thrown(MissingMethodException)
  }

  def "expr w/ missing type"() {
    when:
    compiler.compileFeatureBody("""
setup: Mock()
    """)

    then:
    thrown(SpecCompileException)
  }

  def "expr w/ wrong argument"() {
    when: Mock(1)
    then: thrown(MissingMethodException)
  }
}

class InvalidMockCreation2 extends Specification {
  List list = Mock(Map)

  @FailsWith(GroovyCastException)
  def setup() {}

  def "field w/ incompatible type"() {
    expect: true // triggers setup()
  }
}

class InvalidMockCreation3 extends Specification {
  def list = Mock(1)

  @FailsWith(MissingMethodException)
  def setup() {}

  def "field w/ wrong argument"() {
    expect: true // triggers setup()
  }
}