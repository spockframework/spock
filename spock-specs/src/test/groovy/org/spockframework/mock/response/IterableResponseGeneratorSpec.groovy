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

package org.spockframework.mock.response

import org.spockframework.util.ReflectionUtil
import org.spockframework.mock.runtime.StaticMockMethod
import org.spockframework.mock.IMockInvocation

import spock.lang.Specification

class IterableResponseGeneratorSpec extends Specification {
  IMockInvocation inv = Mock()

  def "iterate over non-empty list" () {
    def gen = new IterableResponseGenerator([1,2,3])
    def method = ReflectionUtil.getMethodByName(Object, "hashCode")
    inv.getMethod() >> new StaticMockMethod(method, Object)

    expect:
    gen.respond(inv) == 1
    gen.respond(inv) == 2
    gen.respond(inv) == 3
    gen.respond(inv) == 3
  }

  def "iterate over empty list"() {
    def gen = new IterableResponseGenerator([])
    def method = ReflectionUtil.getMethodByName(Object, "hashCode")
    inv.getMethod() >> new StaticMockMethod(method, Object)

    expect:
    gen.respond(inv) == null
    gen.respond(inv) == null
  }

  def "iterate over string"() {
    def gen = new IterableResponseGenerator("abc")
    def method = ReflectionUtil.getMethodByName(Object, "toString")
    inv.getMethod() >> new StaticMockMethod(method, Object)

    expect:
    gen.respond(inv) == "a"
    gen.respond(inv) == "b"
    gen.respond(inv) == "c"
    gen.respond(inv) == "c"
  }
}
