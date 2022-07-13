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

package org.spockframework.smoke

import spock.lang.Specification

/**
 * @author Peter Niederwieser
 */
class WhenThenBlocks extends Specification {
  def "basic usage"() {
    def x
    when: x = 1
    then: x == 1
  }

  def "named blocks"() {
    def x
    when: "x is assigned 1"
      x = 1
    then: "x is 1"
      x == 1
  }

  def "and-ed blocks"() {
    def x, y
    when: x = 1
    and: y = 2
    then: x == 1
    and: "y is 2"
      y == 2
  }

  def "chained blocks"() {
    def x, y
    when: x = 1
    and: y = 2
    then: x == 1
    then: "y is 2"
      y == 2
  }

  def "combination of and-ed and chained block"() {
    def x, y, z
    when: x = 1; y = 2; z = 3
    then: x == 1
    and: y == 2
    then: z == 3
  }

  def "multiple when-thens"() {
    def x
    when: x = 1
    then: x == 1
    when: x = 2
    then: x == 2
  }

  def "full house"() {
   def x, y
   when: "blah"
     x = 1
   and: "blah"
     y = 2
   then: "blah"
     x == 1
   and: "blah"
     y == 2
   when: x = 42
   then: x == 42
   and: y == 2
  }
}
