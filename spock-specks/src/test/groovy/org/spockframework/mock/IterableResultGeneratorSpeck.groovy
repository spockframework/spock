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

package org.spockframework.mock

import org.junit.runner.RunWith
import org.spockframework.mock.IterableResultGenerator
import spock.lang.*

class IterableResultGeneratorSpeck extends Specification {
  def "generate results from non-empty list" () {
    def gen = new IterableResultGenerator([1,2,3])

    expect:
    gen.generate(null) == 1
    gen.generate(null) == 2
    gen.generate(null) == 3
    gen.generate(null) == 3
  }

  def "generate results from empty list"() {
    def gen = new IterableResultGenerator([])

    expect:
    gen.generate(null) == null
    gen.generate(null) == null
  }

  def "generate results from string"() {
    def gen = new IterableResultGenerator("abc")

    expect:
    gen.generate(null) == "a"
    gen.generate(null) == "b"
    gen.generate(null) == "c"
    gen.generate(null) == "c"
  }
}