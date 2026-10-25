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

package org.spockframework.runtime.condition

import spock.lang.*

import java.util.concurrent.ThreadLocalRandom

import static org.spockframework.runtime.condition.EditOperation.Kind.*

@See(["https://en.wikipedia.org/wiki/Levenshtein_distance", "https://www.levenshtein.net/"])
class EditDistanceSpec extends Specification {
  @Shared chars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + [' '] * 10

  def "path from 'sitting' to 'kitten'"() {
    def path = new EditDistance("sitting", "kitten").calculatePath()

    expect:
    path.size() == 5
    path[0] == new EditOperation(SUBSTITUTE, 1)
    path[1] == new EditOperation(SKIP, 3)
    path[2] == new EditOperation(SUBSTITUTE, 1)
    path[3] == new EditOperation(SKIP, 1)
    path[4] == new EditOperation(DELETE, 1)
  }

  def "path from 'Sunday' to 'Saturday'"() {
    def path = new EditDistance("Sunday", "Saturday").calculatePath()

    expect:
    path.size() == 5
    path[0] == new EditOperation(SKIP, 1)
    path[1] == new EditOperation(INSERT, 2)
    path[2] == new EditOperation(SKIP, 1)
    path[3] == new EditOperation(SUBSTITUTE, 1)
    path[4] == new EditOperation(SKIP, 3)
  }

  def "path from 'levenshtein' to 'meilenstein'"() {
    def path = new EditDistance("levenshtein", "meilenstein").calculatePath()

    expect:
    path.size() == 7
    path[0] == new EditOperation(SUBSTITUTE, 1)
    path[1] == new EditOperation(SKIP, 1)
    path[2] == new EditOperation(SUBSTITUTE, 1)
    path[3] == new EditOperation(INSERT, 1)
    path[4] == new EditOperation(SKIP, 3)
    path[5] == new EditOperation(DELETE, 1)
    path[6] == new EditOperation(SKIP, 4)
  }

  def "compute distance"() {
    def dist = new EditDistance("asdf", str)

    expect:
    dist.getDistance() == d

    where:
    str << ["xsdf", "axdf", "asxf", "asdx", "", "a", "as", "asd", "asdf", "xasdf", "asdfx", "xasdfx"]
    d   << [ 1    , 1     , 1     , 1     , 4 , 3  , 2   , 1    , 0     , 1      , 1      , 2       ]
  }

  def "computed path has correct distance"() {
    def dist = new EditDistance(str1, str2)

    expect:
    computeDistance(dist.calculatePath()) == dist.getDistance()

    where:
    num << (0..99)
    str1 = randomString(num)
    str2 = editedString(str1)
  }

  def "calculated paths can be modified independently"() {
    given:
    def dist = new EditDistance("sitting", "kitten")
    def expected = dist.calculatePath()

    when:
    dist.calculatePath().clear()
    dist.calculatePath().first().incLength(1)

    then:
    dist.calculatePath() == expected
  }

  def computeDistance(List operations) {
    operations.sum 0, { it.getKind() == EditOperation.Kind.SKIP ? 0 : it.getLength() }
  }

  def randomChar() {
    chars[ThreadLocalRandom.current().nextInt(chars.size())]
  }

  def randomString(int length) {
    def result = new StringBuilder()
    length.times { result.append(randomChar()) }
    result.toString()
  }

  def editedString(String str) {
    StringBuilder result = new StringBuilder()

    str.toCharArray().each {
      switch (ThreadLocalRandom.current().nextInt(4)) {
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
