/*
 * Copyright 2011 the original author or authors.
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
package org.spockframework.smoke.mock

import spock.lang.Specification
import spock.lang.FailsWith

import org.spockframework.runtime.InvalidSpecException

class SpreadWildcardUsage extends Specification {
  def "match any arguments"() {
    Overloads overloads = Mock()

    when:
    overloads.m()
    overloads.m("one")
    overloads.m("one", "two")

    then:
    3 * overloads.m(*_)
  }

  def "match any remaining arguments"() {
    Overloads overloads = Mock()

    when:
    overloads.m("one")
    overloads.m("one", "two")
    overloads.m("one", "two", "three")

    then:
    3 * overloads.m("one", *_)
  }

  def "mach any remaining arguments (w/ varargs)"() {
    Varargs varargs = Mock()

    when:
    varargs.m("one", "two", "three")

    then:
    1 * varargs.m(*_)

    when:
    varargs.m("one", "two", "three")

    then:
    1 * varargs.m("one", *_)

    when:
    varargs.m("one", "two", "three")

    then:
    1 * varargs.m("one", "two", *_)

    when:
    varargs.m("one", "two", "three")

    then:
    1 * varargs.m("one", "two", "three", *_)

    when:
    varargs.m("one", "two", "three")

    then:
    0 * varargs.m("one", "two", "three", "four", *_)
  }

  @FailsWith(InvalidSpecException)
  def "may only be used at the end of an argument list"() {
    Overloads overloads = Mock()

    when:
    overloads.m("one", "two", "three")

    then:
    1 * overloads.m("one", *_, "three")
  }

  interface Overloads {
    def m()
    def m(String one)
    def m(String one, String two)
    def m(String one, String two, String three)
  }

  interface Varargs {
    def m(String one, String... remaining)
  }
}



