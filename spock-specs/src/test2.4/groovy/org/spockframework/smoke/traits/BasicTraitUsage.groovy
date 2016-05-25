/*
* Copyright 2014 the original author or authors.
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

package org.spockframework.smoke.traits

import spock.lang.*

@ConcurrentExecutionMode(ConcurrentExecutionMode.Mode.FORCE_SEQUENTIAL)
class BasicTraitUsage extends Specification implements MyTrait {
  boolean setupEvaluated
  @Shared
  boolean cleanupEvaluated
  @Shared
  boolean setupSpecEvaluated
  boolean beforeEvaluated
  @Shared
  boolean afterEvaluated

  def "call trait method"() {
    def result = multiply(4, 3)

    expect:
    result == 12
  }

  def "mutate trait state"() {
    when:
    x = 99

    then:
    x == 99
  }

  def "call trait method from condition"() {
    expect:
    multiply(4, 3) == 12
  }

  def "access trait state from condition"() {
    expect:
    x == 10
  }

  def "mix in setup() method"() {
    expect:
    setupEvaluated
  }

  def "mix in cleanup() method"() {
    expect:
    cleanupEvaluated // for previous feature method(s)
  }

  def "mix in setupSpec() method"() {
    expect:
    setupSpecEvaluated
  }

  def "mix in @Before method"() {
    expect:
    beforeEvaluated
  }

  def "mix in @After method"() {
    expect:
    afterEvaluated // for previous feature method(s)
  }
}

