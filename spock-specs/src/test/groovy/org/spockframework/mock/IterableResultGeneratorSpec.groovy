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

import org.spockframework.util.ReflectionUtil

import spock.lang.Specification

class IterableResultGeneratorSpec extends Specification {
  IMockInvocation inv = Mock()

  def "generate results from non-empty list" () {
    def gen = new IterableResultGenerator([1,2,3])
    def method = ReflectionUtil.getMethodByName(Object, "hashCode")
    inv.getMethod() >> new StaticMockMethod(method)

    expect:
    gen.generate(inv) == 1
    gen.generate(inv) == 2
    gen.generate(inv) == 3
    gen.generate(inv) == 3
  }

  def "generate results from empty list"() {
    def gen = new IterableResultGenerator([])
    def method = ReflectionUtil.getMethodByName(Object, "hashCode")
    inv.getMethod() >> new StaticMockMethod(method)

    expect:
    gen.generate(inv) == null
    gen.generate(inv) == null
  }

  def "generate results from string"() {
    def gen = new IterableResultGenerator("abc")
    def method = ReflectionUtil.getMethodByName(Object, "toString")
    inv.getMethod() >> new StaticMockMethod(method)

    expect:
    gen.generate(inv) == "a"
    gen.generate(inv) == "b"
    gen.generate(inv) == "c"
    gen.generate(inv) == "c"
  }
}