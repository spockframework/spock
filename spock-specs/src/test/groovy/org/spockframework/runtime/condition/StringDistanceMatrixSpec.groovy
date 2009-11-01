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

package org.spockframework.runtime.condition

import spock.lang.Specification
import spock.lang.Shared

class StringDistanceMatrixSpec extends Specification {
  @Shared Random random = new Random()
  @Shared chars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + [' '] * 10

  def "compute distance"() {
    def matrix = new StringDistanceMatrix("asdf", str)

    expect:
    matrix.getDistance() == d

    where:
    str << ["xsdf", "axdf", "asxf", "asdx", "", "a", "as", "asd", "asdf", "xasdf", "asdfx", "xasdfx"]
    d   << [ 1    , 1     , 1     , 1     , 4 , 3  , 2   , 1    , 0     , 1      , 1      , 2       ]
  }

  def "computed path has correct distance"() {
    def matrix = new StringDistanceMatrix(str1, str2)
    
    expect:
    computeDistance(matrix.computePath()) == matrix.getDistance()

    where:
    num << (0..99)
    str1 = randomString(num)
    str2 = editedString(str1)
  }

  def computeDistance(List operations) {
    operations.sum 0, { it.getKind() == EditOperation.Kind.SKIP ? 0 : it.getLength() }
  }

  def randomChar() {
    chars[random.nextInt(chars.size())]
  }

  def randomString(int length) {
    def result = new StringBuilder()
    length.times { result.append(randomChar()) }
    result.toString()
  }

  def editedString(String str) {
    StringBuilder result = new StringBuilder()

    str.toCharArray().each {
      switch (random.nextInt(4)) {
        case 0: // skip
          result.append(it)
          break
        case 1: // substitute
          result.append(randomChar())
          break
        case 2: // delete
          break
        case 3: // insert
          result.append(randomChar())
          result.append(it)
      }
    }

    result.toString()
  }
}
