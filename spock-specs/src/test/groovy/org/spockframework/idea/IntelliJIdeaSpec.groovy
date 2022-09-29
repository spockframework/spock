/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.idea

import spock.lang.Ignore
import spock.lang.Rollup
import spock.lang.Specification
import spock.lang.Unroll

// tested with IDEA 12.0.1
@Ignore
class IntelliJIdeaSpec extends Specification {
  // both inference and formatting work
  @Rollup
  def "single-pipe data tables"() {
    expect:
    Math.max(a, b) == c

    where:
    a | b  | c
    1 | 4  | 4
    5 | 30 | 30
    1 | 1  | 1
  }

  // formatting works, inference doesn't
  @Rollup
  def "double-pipe data tables"() {
    expect:
    Math.max(a, b) == c

    where:
    a | b  || c
    1 | 4  || 4
    5 | 30 || 30
    1 | 1  || 1
  }

  // unroll inference works
  @Unroll("max of #a and #b is #c")
  def "unroll with naming pattern in annotation"() {
    expect:
    Math.max(a, b) == c

    where:
    a | b  | c
    1 | 4  | 4
    5 | 30 | 30
    1 | 1  | 1
  }

  // unroll inference doesn't work
  def "max of #a and #b is #c"() {
    expect:
    Math.max(a, b) == c

    where:
    a | b  | c
    1 | 4  | 4
    5 | 30 | 30
    1 | 1  | 1
  }

  // unroll inference doesn't work
  @Unroll("max of #a and #b is #c")
  def "double-pipe data tables with naming pattern in annotation"() {
    expect:
    Math.max(a, b) == c

    where:
    a | b  || c
    1 | 4  || 4
    5 | 30 || 30
    1 | 1  || 1
  }

  // unroll inference doesn't work
  def "max of #a and #b is #c (2)"() {
    expect:
    Math.max(a, b) == c

    where:
    a | b  || c
    1 | 4  || 4
    5 | 30 || 30
    1 | 1  || 1
  }
}
