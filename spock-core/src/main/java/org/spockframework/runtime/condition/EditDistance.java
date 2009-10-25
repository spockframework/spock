/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.runtime.condition;

import org.spockframework.util.Tuple2;

/**
 * This class is a direct port of the equally named class in the Specs project (http://code.google.com/p/specs/).
 * The original code is here:
 * http://code.google.com/p/specs/source/browse/trunk/src/main/scala/org/specs/util/EditDistance.scala
 *
 * @author Peter Niederwieser
 */
public class EditDistance {
  private static final String firstSeparator = "(";
  private static final String secondSeparator = ")";
  private static final int shortenSize = 20;

  private final String s1;
  private final String s2;

  private final int[][] matrix;

  public EditDistance(String s1, String s2) {
    this.s1 = s1;
    this.s2 = s2;
    matrix = new int[s1.length() + 1][];
    initMatrix();
  }

  public int getDistance() {
    return matrix[s1.length()][s2.length()];
  }

  public Tuple2<String, String> showDistance() {
    Tuple2<String, String> diffs = findOperations(getDistance(), s1.length(), s2.length(), "", "");
    return Tuple2.of(DiffShortener.shorten(diffs.get0(), firstSeparator, secondSeparator, shortenSize),
        DiffShortener.shorten(diffs.get1(), firstSeparator, secondSeparator, shortenSize));
  }

  private void initMatrix() {
    for (int i = 0; i < s1.length() + 1; i++) {
      matrix[i] = new int[s2.length() + 1];
      for (int j = 0; j < s2.length() + 1; j++) {
        if (i == 0) matrix[i][j] = j; // j insertions
        else if (j == 0) matrix[i][j] = i; // i suppressions
        else matrix[i][j] = min(
              matrix[i - 1][j] + 1, // suppression
              matrix[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1), // substitution
              matrix[i][j - 1] + 1);  // insertion
      }
    }
  }

  private Tuple2<String, String> findOperations(int dist, int i, int j, String s1mod, String s2mod) {
    if (i == 0 && j == 0) return Tuple2.of("", "");
    if (i == 1 && j == 1)
      return dist == 0 ?
          Tuple2.of(s1.charAt(0) + s1mod, s2.charAt(0) + s2mod) :
          Tuple2.of(modify(s1mod, s1.charAt(0)), modify(s2mod, s2.charAt(0)));
    if (j < 1) return Tuple2.of(modifyString(s1mod, s1.substring(0, i)), modifyString(s2mod, ""));
    if (i < 1) return Tuple2.of(modifyString(s1mod, ""), modifyString(s2mod, s2.substring(0, j)));

    int suppr = matrix[i - 1][j];
    int subst = matrix[i - 1][j - 1];
    int ins = matrix[i][j - 1];

    if (suppr < subst) return findOperations(suppr, i - 1, j, modify(s1mod, s1.charAt(i - 1)), modifyString(s2mod, ""));
    if (ins < subst) return findOperations(ins, i, j - 1, modifyString(s1mod, ""), modify(s2mod, s2.charAt(j - 1)));
    if (subst < dist)
      return findOperations(subst, i - 1, j - 1, modify(s1mod, s1.charAt(i - 1)), modify(s2mod, s2.charAt(j - 1)));
    return findOperations(subst, i - 1, j - 1, s1.charAt(i - 1) + s1mod, s2.charAt(j - 1) + s2mod);
  }

  private static String modify(String s, char c) {
    return modifyString(s, String.valueOf(c));
  }

  private static String modifyString(String s, String mod) {
    return (firstSeparator + mod + secondSeparator + s).replace(secondSeparator + firstSeparator, "");
  }

  // Note: in the original (Scala) implementation, "ins" is a by-name parameter; not sure why
  private static int min(int suppr, int subst, int ins) {
    return suppr < subst ? suppr : ins < subst ? ins : subst;
  }
}


