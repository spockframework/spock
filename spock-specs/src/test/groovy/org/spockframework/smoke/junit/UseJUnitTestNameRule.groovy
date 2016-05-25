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

package org.spockframework.smoke.junit

import spock.lang.*
import org.junit.Rule
import org.junit.rules.TestName

@ConcurrentExecutionMode(ConcurrentExecutionMode.Mode.FORCE_SEQUENTIAL) // TestName rule does not support parallel execution
@Issue("http://issues.spockframework.org/detail?id=108")
class UseJUnitTestNameRule extends Specification {
  @Rule
  TestName name = new TestName()

  def "not data-driven"() {
    expect:
    name.methodName == "not data-driven"
  }

  def "data-driven, not unrolled"() {
    expect:
    name.methodName == "data-driven, not unrolled"

    where:
    i << (1..3)
  }

  @Unroll
  def "data-driven, unrolled w/o name pattern"() {
    expect:
    name.methodName == "data-driven, unrolled w/o name pattern[${i-1}]"

    where:
    i << (1..3)
  }

  @Unroll
  def "data-driven, unrolled w/ name pattern #pattern"() {
    expect:
    name.methodName == "data-driven, unrolled w/ name pattern $pattern"

    where:
    pattern << ["foo", "bar", "baz"]
  }

  @Unroll("data-driven, unrolled w/ closure pattern #pattern")
  def foo() {
    expect:
    name.methodName == "data-driven, unrolled w/ closure pattern $pattern"

    where:
    pattern << ["foo", "bar", "baz"]
  }
}
