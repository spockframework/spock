/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.smoke.extension

import org.junit.runner.Request

import org.spockframework.EmbeddedSpecification

class StepwiseExtension extends EmbeddedSpecification {
  def "basic usage"() {
    runner.throwFailure = false

    when:
    def result = runner.runWithImports("""
@Stepwise
class Foo extends Specification {
  def step1() { expect: true }
  def step2() { expect: false }
  def step3() { expect: true }
}
    """)

    then:
    result.runCount == 2
    result.failureCount == 1
    result.ignoreCount == 1
  }

  def "automatically runs excluded methods that lead up to an included method"() {
    def clazz = compiler.compileWithImports("""
@Stepwise
class Foo extends Specification {
  def step1() { expect: true }
  def step2() { expect: true }
  def step3() { expect: true }
}
    """)[0]

    when:
    def result = runner.runRequest(Request.method(clazz, "step3"))

    then:
    result.runCount == 3
    result.failureCount == 0
    result.ignoreCount == 0
  }
}

