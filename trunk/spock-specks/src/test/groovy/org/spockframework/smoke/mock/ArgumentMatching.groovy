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

import org.junit.runner.RunWith
import org.spockframework.dsl.*
import static org.spockframework.dsl.Predef.*

@Speck
@RunWith(Sputnik)
class ArgumentMatching {
//  def "match equal argument - identical"() {
//    List list = Mock()
//
//    when: list.add(arg)
//    then: list.add(arg)
//
//    where: arg << [1, "foo", new Object()]
//  }
//
//  def "match equal argument - not identical"() {
//    List list = Mock()
//
//    when: list.add(arg1)
//    then: list.add(arg2)
//
//    where:
//      arg1 << [1, [1,2,3] as Set, null]
//      arg2 << [1.0, [3,2,1] as Set, null]
//  }

  def "match identical argument"() {
    List list = Mock()

    when: list.add(arg)
    then: 1 * list.add( { it.is(arg) } )

    where:
      arg << [1, "foo", new Object()]
  }
}