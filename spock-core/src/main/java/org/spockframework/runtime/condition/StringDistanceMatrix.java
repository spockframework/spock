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

import java.util.LinkedList;
import java.util.List;

import static org.spockframework.runtime.condition.EditOperation.Kind.*;

/**
 * Computes Levenshtein distance and edit path between two strings.
 * Inspired from: http://etorreborre.blogspot.com/2008/06/edit-distance-in-scala_245.html
 *
 * @author Peter Niederwieser
 */
public class StringDistanceMatrix {
  private final String str1;
  private final String str2;

  private final int[][] matrix;

  public StringDistanceMatrix(String str1, String str2) {
    this.str1 = str1;
    this.str2 = str2;
    matrix = new int[str1.length() + 1][];
    computeMatrix();
  }

  private void computeMatrix() {
    for (int i = 0; i < str1.length() + 1; i++) {
      matrix[i] = new int[str2.length() + 1];
      for (int j = 0; j < str2.length() + 1; j++) {
        if (i == 0) matrix[i][j] = j; // j insertions
        else if (j == 0) matrix[i][j] = i; // i deletions
        else matrix[i][j] = min(
              matrix[i - 1][j] + 1, // deletion
              matrix[i - 1][j - 1] + (str1.charAt(i - 1) == str2.charAt(j - 1) ? 0 : 1), // substitution
              matrix[i][j - 1] + 1);  // insertion
      }
    }
  }

  public int[][] getMatrix() {
    return matrix;
  }
  
  public int getDistance() {
    return matrix[str1.length()][str2.length()];
  }

  public int getSimilarityInPercent() {
    int maxDistance = Math.max(str1.length(), str2.length());
    return (maxDistance - getDistance()) * 100 / maxDistance;
  }

  // ideas for improvements:
  // a. favor fewer EditOperationS
  // b. not sure if we can get the following while computing the path (assumption: diagonal is skip):
  // 4 3
  // 3 4
  // if yes, should we favor skip over ins/del?
  public List<EditOperation> computePath() {
    LinkedList<EditOperation> ops = new LinkedList<EditOperation>();
    int i = str1.length();
    int j = str2.length();
    int dist = matrix[i][j];

    while (i > 0 && j > 0 && dist > 0) {
      int ins = matrix[i][j - 1];
      int del = matrix[i - 1][j];
      int sub = matrix[i - 1][j - 1];
      int min = min(ins, del, sub);

      if (min == sub && sub == dist) {
        addOrUpdate(ops, SKIP, 1);
        i--; j--;
      } else if (min == del) {
        addOrUpdate(ops, DELETE, 1);
        i--;
      } else if (min == ins) {
        addOrUpdate(ops, INSERT, 1);
        j--;
      } else if (min == sub) {
        addOrUpdate(ops, SUBSTITUTE, 1);
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


