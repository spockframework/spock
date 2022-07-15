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

package org.spockframework.example

import spock.lang.Rollup
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Peter Niederwieser
 */
class FeatureUnrolling extends Specification {
  @Rollup
  def "without unrolling"() {
    expect:
    name.size() == length

    where:
    name << ["Kirk", "Spock", "Scotty"]
    length << [4, 5, 6]
  }

  def "with unrolling"() {
    expect:
    name.size() == length

    where:
    name << ["Kirk", "Spock", "Scotty"]
    length << [4, 5, 6]
  }

  @Unroll("length of '#name' should be #length")
  def "with unrolling and custom naming pattern"() {
    expect:
    name.size() == length

    where:
    name << ["Kirk", "Spock", "Scotty"]
    length << [4, 5, 6]
  }
}
