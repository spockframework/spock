/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke

import org.spockframework.EmbeddedSpecification
import org.spockframework.compiler.InvalidSpecCompileException

class CompileTimeErrorReporting extends EmbeddedSpecification {
  def "constructor declaration"() {
    when:
    compiler.compileSpecBody """
ASpec() {}
    """

    then:
    thrown(InvalidSpecCompileException)
  }

  def "wrong spelling of 'setup' method"() {
    when:
    compiler.compileSpecBody """
def setUp() {}
    """

    then:
    thrown(InvalidSpecCompileException)
  }

  def "'old' method used outside then-block"() {
    when:
    compiler.compileFeatureBody """
when:
def y = old(x)

then:
true
    """

    then:
    thrown(InvalidSpecCompileException)
  }

  def "multiple thrown clauses"() {
    when:
    compiler.compileFeatureBody """
when:
def x = 42

then:
thrown(IllegalArgumentException)
thrown(IOException)
    """

    then:
    thrown(InvalidSpecCompileException)
  }

  def "non-parameterization in where-block"() {
    when:
    compiler.compileFeatureBody """
expect:
true

where:
println "hi"
    """

    then:
    thrown(InvalidSpecCompileException)
  }
}
