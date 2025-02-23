/*
 * Copyright 2025 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.docs.primer

import org.spockframework.EmbeddedSpecification

class VerifyMethodsDocSpec extends EmbeddedSpecification {

  def setup() {
    runner.throwFailure = false
  }

  def "simple @Verify helper method"() {
    when:
    def result = runner.runSpecBody("""
@groovy.transform.Canonical
static class Pc {
    final String vendor
    final int clockRate
    final int ram
    final String os
}

// tag::verify-helper-method[]
@Verify
void matchesPreferredConfiguration(pc) {
  pc.vendor == "Sunny"
  pc.clockRate >= 2333
  pc.ram >= 4096
  pc.os == "Linux"
}
// end::verify-helper-method[]

def "offered PC matches preferred configuration"() {
    expect:
    matchesPreferredConfiguration(new Pc("Sunny", 1666, 4096, "Linux"))
}
""")

    then:
    result.failures[0].exception.message.startsWith(/* tag::verify-helper-method-result[] */"""\
Condition not satisfied:

pc.clockRate >= 2333
|  |         |
|  1666      false
""" /* end::verify-helper-method-result[] */)
  }
}
