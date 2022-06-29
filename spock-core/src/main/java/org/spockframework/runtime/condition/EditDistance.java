/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.condition;

import java.util.*;

import static org.spockframework.runtime.condition.EditOperation.Kind.*;

/**
 * Calculates Levenshtein distance and corresponding edit path between two character sequences.
 * Inspired from: http://etorreborre.blogspot.com/2008/06/edit-distance-in-scala_245.html
 *
 * Ideas for improvements:
 * - Favor fewer EditOperationS when calculating distance and/or path
 * - Use algorithm with lower time and/or space complexity
 *
 * @author Peter Niederwieser
 */
public class EditDistance {
  private final CharSequence seq1;
  private final CharSequence seq2;

  private final int[][] matrix;

  public EditDistance(CharSequence seq1, CharSequence seq2) {
    this.seq1 = seq1;
    this.seq2 = seq2;
    matrix = new int[seq1.length() + 1][];
    calculateMatrix();
  }

  private void calculateMatrix() {
    for (int i = 0; i < seq1.length() + 1; i++) {
      matrix[i] = new int[seq2.length() + 1];
      for (int j = 0; j < seq2.length() + 1; j++) {
        if (i == 0) matrix[i][j] = j;      // j insertions
        else if (j == 0) matrix[i][j] = i; // i deletions
        else matrix[i][j] = min(
              matrix[i][j - 1] + 1, // insertion
              matrix[i - 1][j] + 1, // deletion
              matrix[i - 1][j - 1] + (seq1.charAt(i - 1) == seq2.charAt(j - 1) ? 0 : 1)); // substitution
      }
    }
  }

  public int[][] getMatrix() {
    return matrix;
  }

  public int getDistance() {
    return matrix[seq1.length()][seq2.length()];
  }

  public int getSimilarityInPercent() {
    int maxDistance = Math.max(seq1.length(), seq2.length());
    return (maxDistance - getDistance()) * 100 / maxDistance;
  }

  public List<EditOperation> calculatePath() {
    LinkedList<EditOperation> ops = new LinkedList<>();
    int i = seq1.length();
    int j = seq2.length();
    int dist = matrix[i][j];

    while (i > 0 && j > 0 && dist > 0) {
      int ins = matrix[i][j - 1];
      int del = matrix[i - 1][j];
      int sub = matrix[i - 1][j - 1];

      if (dist == ins + 1) {
        addOrUpdate(ops, INSERT, 1);
        j--;
      } else if (dist == del + 1) {
        addOrUpdate(ops, DELETE, 1);
        i--;
      } else {
        if (dist == sub) addOrUpdate(ops, SKIP, 1);
        else addOrUpdate(ops, SUBSTITUTE, 1);
        i--; j--;
      }

      dist = matrix[i][j];
    }

    if (i == 0) addOrUpdate(ops, INSERT, j);
    else if (j == 0) addOrUpdate(ops, DELETE, i);
    else addOrUpdate(ops, SKIP, i);

    return ops;
  }

  private void addOrUpdate(LinkedList<EditOperation> ops, EditOperation.Kind kind, int length) {
    if (length == 0) return;

    if (!ops.isEmpty() && ops.getFirst().getKind() == kind)
      ops.getFirst().incLength(length);
    else
      ops.addFirst(new EditOperation(kind, length));
  }

  private static int min(int a, int b, int c) {
    return Math.min(a, Math.min(b, c));
  }
}


