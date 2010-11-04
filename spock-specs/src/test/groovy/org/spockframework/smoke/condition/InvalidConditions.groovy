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

package org.spockframework.smoke.condition

import org.codehaus.groovy.syntax.SyntaxException

import org.spockframework.EmbeddedSpecification
import org.spockframework.compiler.InvalidSpecCompileException

class InvalidConditions extends EmbeddedSpecification {
  def "assignments are not allowed in expect-blocks"() {
    when:
    compiler.compileFeatureBody("""
def x = 0

expect:
x $op 0
    """)

    then:
    InvalidSpecCompileException e = thrown()
    e.message.contains("assignment")

    where:
    op << ["=", "+=", "-="]
  }

  def "assignments are not allowed in then-blocks"() {
    when:
    compiler.compileFeatureBody("""
def x = 42

when:
true

then:
x $op 42
    """)

    then:
    InvalidSpecCompileException e = thrown()
    e.message.contains("assignment")

    where:
    op << ["=", "+=", "-="]
  }

  def "assignments are not allowed in explicit conditions"() {
    when:
    compiler.compileFeatureBody("""
def x = 42

setup:
assert x $op 42
    """)

    then:
    SyntaxException e = thrown()
    e.message.contains(op)

    where:
    op << ["=", "+=", "-="]
  }

  def "assignments are allowed if they are part of a variable declaration"() {
    expect:
    def x = 42
    x == 42
  }
}
