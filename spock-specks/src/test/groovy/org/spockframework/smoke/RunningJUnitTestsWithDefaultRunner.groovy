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
 
package org.spockframework.smoke

import org.spockframework.EmbeddedSpecification

class RunningJUnitTestsWithDefaultRunner extends EmbeddedSpecification {
  def "failing beforeClass method"() {
    runner.throwFailure = false

    when:
    def result = runner.run("""
import org.junit.*

class Foo {
  @BeforeClass
  static void beforeClass() {
    throw new Error()
  }

  @Test
  void test() {}
}
    """)

    then:
    result.runCount == 0
    result.failureCount == 1
    result.ignoreCount == 0

    def desc = result.failures[0].description
    desc.isSuite() // failure description is description of the test class
    desc.className == "Foo"
  }
}