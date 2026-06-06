/*
 * Copyright 2026 the original author or authors.
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

package org.spockframework.smoke.parameterization

import org.spockframework.EmbeddedSpecification
import spock.lang.Issue

@Issue("https://github.com/spockframework/spock/issues/138")
class WhereBlockVariables extends EmbeddedSpecification {
  def "final where-block variable can be used in data table cells"() {
    when:
    runner.runFeatureBody '''
expect:
input == expected

where:
final sep = "/"
input              | expected
"a${sep}b"         | "a/b"
"x${sep}y${sep}z"  | "x/y/z"
'''

    then:
    noExceptionThrown()
  }

  def "final where-block variable is evaluated once and reused across providers"() {
    when:
    runner.runFeatureBody '''
expect:
a.is(b)            // same instance in every iteration => single evaluation

where:
final marker = new Object()
a << [marker, marker]
b << [marker, marker]
'''

    then:
    noExceptionThrown()
  }

  def "final where-block variable can be used in a derived data variable"() {
    when:
    runner.runFeatureBody '''
expect:
greeting == expected

where:
final prefix = "Hello, "
name     | expected
"world"  | "Hello, world"
"spock"  | "Hello, spock"

greeting = prefix + name
'''

    then:
    noExceptionThrown()
  }

  def "where-block variable is not visible in the feature body"() {
    when:
    runner.runFeatureBody '''
expect:
sep == "/"          // `sep` is a where-block local, unbound here

where:
final sep = "/"
x << [1, 2]
'''

    then:
    thrown(Throwable)
  }

  def "where-block variable initializer can read a @Shared field"() {
    when:
    runner.runSpecBody '''
@Shared
String base = "abc"

def feature() {
  expect:
  value == "abc/x"

  where:
  final sep = "/"
  value = base + sep + "x"
}
'''

    then:
    noExceptionThrown()
  }

  def "final where-block variable works with combined data providers"() {
    when:
    runner.runFeatureBody '''
expect:
a.endsWith("-")
b in [1, 2]

where:
final sep = "-"
a << ["x${sep}", "y${sep}"]
combined:
b << [1, 2]
'''

    then:
    noExceptionThrown()
  }
}
