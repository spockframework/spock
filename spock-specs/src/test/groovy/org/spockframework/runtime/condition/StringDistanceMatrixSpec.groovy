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

import spock.lang.*

import static org.spockframework.runtime.condition.EditOperation.Kind.*

@See("http://en.wikipedia.org/wiki/Levenshtein_distance")
class StringDistanceMatrixSpec extends Specification {
  @Shared Random random = new Random()
  @Shared chars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + [' '] * 10

  def "matrix for 'sitting' and 'kitten'"() {
    def matrix = new StringDistanceMatrix("sitting", "kitten").matrix

    expect:
    matrix.size() == 8
    matrix[0] == [0, 1, 2, 3, 4, 5, 6]
    matrix[1] == [1, 1, 2, 3, 4, 5, 6]
    matrix[2] == [2, 2, 1, 2, 3, 4, 5]
    matrix[3] == [3, 3, 2, 1, 2, 3, 4]
    matrix[4] == [4, 4, 3, 2, 1, 2, 3]
    matrix[5] == [5, 5, 4, 3, 2, 2, 3]
    matrix[6] == [6, 6, 5, 4, 3, 3, 2]
    matrix[7] == [7, 7, 6, 5, 4, 4, 3]
  }

  def "matrix for 'Sunday' and 'Saturday'"() {
    def matrix = new StringDistanceMatrix("Sunday", "Saturday").matrix

    expect:
    matrix.size() == 7
    matrix[0] == [0, 1, 2, 3, 4, 5, 6, 7, 8]
    matrix[1] == [1, 0, 1, 2, 3, 4, 5, 6, 7]
    matrix[2] == [2, 1, 1, 2, 2, 3, 4, 5, 6]
    matrix[3] == [3, 2, 2, 2, 3, 3, 4, 5, 6]
    matrix[4] == [4, 3, 3, 3, 3, 4, 3, 4, 5]
    matrix[5] == [5, 4, 3, 4, 4, 4, 4, 3, 4]
    matrix[6] == [6, 5, 4, 4, 5, 5, 5, 4, 3]
  }

  def "path from 'sitting' to 'kitten'"() {
    def path = new StringDistanceMatrix("sitting", "kitten").computePath()

    expect:
    path.size() == 5
    path[0] == new EditOperation(SUBSTITUTE, 1)
    path[1] == new EditOperation(SKIP, 3)
    path[2] == new EditOperation(SUBSTITUTE, 1)
    path[3] == new EditOperation(SKIP, 1)
    path[4] == new EditOperation(DELETE, 1)
  }

  def "path from 'Sunday' to 'Saturday'"() {
    def path = new StringDistanceMatrix("Sunday", "Saturday").computePath()

    expect:
    path.size() == 5
    path[0] == new EditOperation(SKIP, 1)
    path[1] == new EditOperation(INSERT, 2)
    path[2] == new EditOperation(SKIP, 1)
    path[3] == new EditOperation(SUBSTITUTE, 1)
    path[4] == new EditOperation(SKIP, 3)
  }

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
