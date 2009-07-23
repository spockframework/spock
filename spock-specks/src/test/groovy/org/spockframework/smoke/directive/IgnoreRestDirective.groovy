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

package org.spockframework.smoke.directive

import org.spockframework.smoke.EmbeddedSpeckRunner
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Peter Niederwieser
 */
class IgnoreRestDirective extends Specification {
  EmbeddedSpeckRunner runner = new EmbeddedSpeckRunner()

  @Unroll("Ignore #ignored methods")
  def "ignore some methods"() {
    when:
    def result = runner.runSpeckBody("""
$first
def foo() {
  expect: "$first" // true if @IgnoreRest is set, false otherwise
}

$second
def bar() {
  expect: "$second"
}

$third
def baz() {
  expect: "$third"
}
    """)

    then:
    result.runCount == 3
    result.ignoreCount == ignored

    where:
    first   << ["",            "@IgnoreRest", "@IgnoreRest"]
    second  << ["@IgnoreRest", "@IgnoreRest", "@IgnoreRest"]
    third   << [""           , ""           , "@IgnoreRest"]
    ignored << [2            , 1            , 0]
  }
}