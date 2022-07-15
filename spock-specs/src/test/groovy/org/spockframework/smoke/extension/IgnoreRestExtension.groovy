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

package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification
import spock.lang.Unroll

/**
 * @author Peter Niederwieser
 */
class IgnoreRestExtension extends EmbeddedSpecification {
  @Unroll("Ignore #ignored methods")
  def "ignore some methods"() {
    when:
    def result = runner.runSpecBody("""
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
    result.testsSucceededCount == run
    result.testsFailedCount == 0
    result.testsSkippedCount == ignored

    where:
    first   << ["",            "@IgnoreRest", "@IgnoreRest"]
    second  << ["@IgnoreRest", "@IgnoreRest", "@IgnoreRest"]
    third   << [""           , ""           , "@IgnoreRest"]
    run     << [1            , 2            , 3            ]
    ignored << [2            , 1            , 0            ]
  }

  def "@IgnoreRest in base and/or derived class"() {
    def classes = compiler.compileWithImports("""
class Base extends Specification {
  $first
  def feature1() {
    expect: "$first"
  }

  def feature2() {
    expect: true
  }
}

class Derived extends Base {
  def feature3() {
    expect: true
  }

  $second
  def feature4() {
    expect: "$second"
  }
}
    """)

    def derived = classes.find { it.simpleName == "Derived" }

    when:
    def result = runner.runClass(derived)

    then:
    result.testsSucceededCount == runCount
    result.testsFailedCount == 0
    result.testsSkippedCount == ignoreCount

    where:
    first       << ["@IgnoreRest", ""           , "@IgnoreRest"]
    second      << [""           , "@IgnoreRest", "@IgnoreRest"]
    runCount    << [1            , 1            , 2            ]
    ignoreCount << [3            , 3            , 2            ]
  }
}

