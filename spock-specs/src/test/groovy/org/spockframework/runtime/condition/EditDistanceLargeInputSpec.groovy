/*
 * Copyright 2026 the original author or authors.
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

import spock.lang.Shared
import spock.lang.Specification

import java.util.Random

import static org.spockframework.runtime.condition.EditOperation.Kind.SUBSTITUTE

/**
 * Verifies that {@link EditDistance} handles large inputs, which requires an algorithm with a better
 * time and space complexity than the dense-matrix Levenshtein it replaces: for two 10,000-character
 * near-identical strings the old implementation allocated more than 400 MB for the distance matrix
 * and could die with {@link OutOfMemoryError} before returning anything.
 */
class EditDistanceLargeInputSpec extends Specification {
  private static final int LENGTH = 10_000
  // 'z' is excluded here so that substituting it below always changes exactly one character
  private static final char[] ALPHABET = 'abcdefghijklmnopqrstuv'.toCharArray()

  @Shared
  String large1 = randomString(LENGTH)

  @Shared
  String large2 = substituteOneChar(large1)

  def "distance between large near-identical strings is computed"() {
    expect:
    new EditDistance(large1, large2).getDistance() == 1
  }

  def "edit path between large near-identical strings has exactly one substitution"() {
    expect:
    new EditDistance(large1, large2).calculatePath() == [
      new EditOperation(EditOperation.Kind.SKIP, LENGTH.intdiv(2)),
      new EditOperation(SUBSTITUTE, 1),
      new EditOperation(EditOperation.Kind.SKIP, LENGTH.intdiv(2) - 1)
    ]
  }

  def "rendering the edit path of large near-identical strings works"() {
    given:
    def rendered = new EditPathRenderer().render(large1, large2, new EditDistance(large1, large2).calculatePath())

    expect:
    rendered.split('\n').length == 2
    // the one differing region is delimited by '(' and ')' on each of the two lines
    rendered.count('(') == 2
    rendered.contains("(z)")
  }

  def "completely different large strings are handled"() {
    given:
    String s1 = 'a' * LENGTH
    String s2 = randomString(LENGTH)

    when:
    def dist = new EditDistance(s1, s2)

    then:
    dist.getDistance() >= LENGTH / 2

    and: "the path cost equals the distance"
    dist.calculatePath().sum(0, { op -> op.getKind() == EditOperation.Kind.SKIP ? 0 : op.getLength() }) ==
      dist.getDistance()
  }

  private static String randomString(int length) {
    Random random = new Random(0)
    StringBuilder result = new StringBuilder(length)
    length.times { result.append(ALPHABET[random.nextInt(ALPHABET.length)]) }
    result.toString()
  }

  private static String substituteOneChar(String str) {
    char[] chars = str.toCharArray()
    chars[chars.length.intdiv(2)] = 'z' // not in ALPHABET, so exactly one character changes
    new String(chars)
  }
}
