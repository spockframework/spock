/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.util

import spock.lang.*

class CollectionUtilSpec extends Specification {
  def "copyArray"() {
    def array = [1, 2, 3] as Object[]

    expect:
    CollectionUtil.copyArray(array, from, to) == result

    where:
    from   << [0, 1, 0, 3]
    to     << [3, 2, 0, 3]
    res    << [[1, 2, 3], [2], [], []]
    result = res as Object[]
  }

  def "reverse an empty list"() {
    when:
    def reversed = CollectionUtil.reverse([])
    
    then:
    toList(reversed) == []

  }
  
  def "reverse a non-empty list"() {
    when:
    def reversed = CollectionUtil.reverse([1, 2, 3])

    then:
    toList(reversed) == [3, 2, 1]
  }

  def "concatenate zero iterables"() {
    when:
    def iterable = CollectionUtil.concat()

    then:
    toList(iterable) == []
  }

  def "concatenate multiple iterables"() {
    when:
    def iterable = CollectionUtil.concat([1, 2, 3], [], [4])

    then:
    toList(iterable) == [1, 2, 3, 4]
  }

  def toList(iterable) {
    iterable.collect { it }
  }
}
