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

import spock.lang.Specification

import java.util.Random

/**
 * Cross-checks {@link EditDistance} against a straightforward dynamic-programming Levenshtein
 * implementation on many random inputs. The binary alphabet maximizes ties between equally
 * expensive alignments, i.e. the degenerate cases a divide-and-conquer diff algorithm has to
 * survive.
 */
class EditDistanceCrossCheckSpec extends Specification {
  private static final char[] ALPHABET = 'ab'.toCharArray()

  def "path cost matches Levenshtein distance for random inputs (seed #@seed)"() {
    given:
    Random random = new Random(seed)

    expect: "for every pair, path cost and rendered alignment match the reference distance"
    200.times {
      String str1 = randomString(random, random.nextInt(13))
      String str2 = randomString(random, random.nextInt(13))
      def dist = new EditDistance(str1, str2)
      int expectedDistance = levenshtein(str1, str2)

      assert pathCost(dist.calculatePath()) == expectedDistance
      assert dist.getDistance() == expectedDistance
      assert rendersToOriginalStrings(str1, str2, dist.calculatePath())
    }

    where:
    seed << (0..4)
  }

  def "structured edge cases match Levenshtein distance"() {
    given:
    List<List<String>> cases = [
      ["", ""], ["", "a"], ["a", ""], ["a", "a"], ["a", "b"],
      ["ab", "ba"], ["abc", "cba"], ["aa", "aa"], ["aa", "aaa"], ["aaa", "a"],
      ["abab", "baba"], ["aaaa", "aa"], ["ab", "baab"], ["aabb", "bbaa"],
      ["aaaaaaaaaa", "bbbbbbbbbb"], ["aaaaaaaaaa", "aaaaabaaaa"],
      ["ab" * 10, "ba" * 10], ["mississippi", "mispissippi"]
    ]

    expect:
    cases.each { pair ->
      def dist = new EditDistance(pair[0], pair[1])
      assert dist.getDistance() == levenshtein(pair[0], pair[1])
      assert pathCost(dist.calculatePath()) == levenshtein(pair[0], pair[1])
    }
  }

  private static int pathCost(List<EditOperation> path) {
    path.sum(0) { it.kind == EditOperation.Kind.SKIP ? 0 : it.length }
  }

  private static boolean rendersToOriginalStrings(String str1, String str2, List<EditOperation> path) {
    def renderer = new EditPathRenderer()
    String[] lines = renderer.render(str1, str2, path).split('\n', -1)
    stripRendering(lines[0]) == str1 && stripRendering(lines[1]) == str2
  }

  private static String stripRendering(String line) {
    line.replace('-', '').replace('(', '').replace(')', '').replace('~', '')
  }

  private static String randomString(Random random, int length) {
    StringBuilder result = new StringBuilder(length)
    length.times { result.append(ALPHABET[random.nextInt(ALPHABET.length)]) }
    result.toString()
  }

  private static int levenshtein(String str1, String str2) {
    int[] prev = new int[str2.length() + 1]
    int[] cur = new int[str2.length() + 1]
    for (int j = 0; j <= str2.length(); j++) {
      prev[j] = j
    }
    for (int i = 1; i <= str1.length(); i++) {
      cur[0] = i
      for (int j = 1; j <= str2.length(); j++) {
        cur[j] = Math.min(prev[j] + 1,
          Math.min(cur[j - 1] + 1, prev[j - 1] + (str1.charAt(i - 1) == str2.charAt(j - 1) ? 0 : 1)))
      }
      int[] swap = prev
      prev = cur
      cur = swap
    }
    prev[str2.length()]
  }
}
