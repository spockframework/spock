/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */  

package org.spockframework.smoke

import org.junit.internal.runners.model.MultipleFailureException

import org.spockframework.EmbeddedSpecification
import org.spockframework.compiler.InvalidSpecCompileException

class CompileTimeErrorReporting extends EmbeddedSpecification {
  def "multiple method declaration errors"() {
    when:
    compiler.compileSpecBody """
ASpec() {} // constructor not allowed

def setUp() {} // wrong spelling

def feature(arg) { // arg not allowed
  expect: true
}
    """

    then:
    MultipleFailureException e = thrown()
    e.failures*.class == [InvalidSpecCompileException] * 3
  }

  def "multiple errors within a method"() {
    when:
    compiler.compileFeatureBody """
when:
def x = 42
def y = old(x) // old used outside of then block

then:
thrown(IllegalArgumentException)
thrown(IOException) // more than one thrown call
thrown(AssertionError) // more than one thrown call

where:
println "hi" // not a parameterization
    """

    then:
    MultipleFailureException e = thrown()
    e.failures*.class == [InvalidSpecCompileException] * 4
  }
}
