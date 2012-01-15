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

package org.spockframework.smoke.mock

import spock.lang.Specification
import spock.lang.FailsWith
import org.spockframework.mock.TooFewInvocationsError

class ArgumentMatching extends Specification {
  def "match identical argument"() {
    List list = Mock()

    when: list.add(arg)
    then: 1 * list.add(arg)

    where: arg << [1, "foo", new Object()]
  }

  def "match equal argument"() {
    List list = Mock()

    when: list.add(arg1)
    then: 1 * list.add(arg2)

    where:
      arg1 << [1, [1,2,3] as Set, null]
      arg2 << [1.0, [3,2,1] as Set, null]
  }
  
  def "match empty argument list"() {
    List list = Mock()
    
    when: list.size()
    then: 1 * list.size()
  }
  
  @FailsWith(TooFewInvocationsError)
  def "expect fewer arguments than are provided"() {
    Overloads overloads = Mock()
    
    when:
    overloads.m("one")
    
    then:
    1 * overloads.m()
  }

  @FailsWith(TooFewInvocationsError)
  def "expect more arguments than are provided"() {
    Overloads overloads = Mock()

    when:
    overloads.m()

    then:
    1 * overloads.m("one")
  }

  @FailsWith(TooFewInvocationsError)
  def "expect fewer arguments than are provided (w/ varargs)"() {
    Varargs2 varargs = Mock()

    when:
    varargs.m("one")

    then:
    1 * varargs.m()
  }

  @FailsWith(TooFewInvocationsError)
  def "expect more arguments than are provided (w/ varargs)"() {
    Varargs2 varargs = Mock()

    when:
    varargs.m()

    then:
    1 * varargs.m("one")
  }
}

// can't be nested interfaces due to Groovy 1.7 bug
private interface Overloads {
  void m()
  void m(String one)
}

private interface Varargs2 {
  void m(String... strings)
}