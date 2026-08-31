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

package org.spockframework.runtime.condition;

import java.util.ArrayList;
import java.util.List;

import static org.spockframework.runtime.condition.EditOperation.Kind.*;

/**
 * Calculates Levenshtein distance and corresponding edit path between two character sequences.
 * <p>
 * Uses a linear-space divide-and-conquer algorithm ("Optimal Alignments in Linear Space",
 * Eugene W. Myers &amp; Webb Miller, 1988), a.k.a. Hirschberg's technique applied to the
 * Levenshtein dynamic program: space is O(N + M) and time O(N &middot; M). This replaces the
 * previous dense distance matrix, which needed O(N &middot; M) <em>space</em> and therefore could
 * not handle large inputs at all. Common prefixes and suffixes are trimmed away first, so for
 * near-identical inputs &mdash; the typical case in a failed equality condition &mdash; only the
 * small differing middle section is processed.
 * <p>
 * The edit path groups consecutive edits into runs and pairs up deletions and insertions into
 * {@link EditOperation.Kind#SUBSTITUTE} runs, matching the output shape of the previous
 * matrix-based implementation. The distance is derived from the edit path, so the two are always
 * consistent.
 */
public class EditDistance {
  private final CharSequence seq1;
  private final CharSequence seq2;

  private final int distance;
  private final List<EditOperation> path;

  public EditDistance(CharSequence seq1, CharSequence seq2) {
    this.seq1 = seq1;
    this.seq2 = seq2;

    List<EditOperation> operations = new ArrayList<>();
    collectOperations(operations, 0, 0, seq1.length(), seq2.length());
    path = normalize(operations);

    int result = 0;
    for (EditOperation operation : path) {
      if (operation.getKind() != SKIP) result += operation.getLength();
    }
    distance = result;
  }

  public int getDistance() {
    return distance;
  }

  public int getSimilarityInPercent() {
    int maxDistance = Math.max(seq1.length(), seq2.length());
    return (maxDistance - getDistance()) * 100 / maxDistance;
  }

  public List<EditOperation> calculatePath() {
    List<EditOperation> result = new ArrayList<>(path.size());
    for (EditOperation operation : path) {
      result.add(new EditOperation(operation.getKind(), operation.getLength()));
    }
    return result;
  }

  /**
   * Collects the edit operations for the region {@code [from1, to1) x [from2, to2)} in path order,
   * splitting the region at an optimal midpoint.
   */
  private void collectOperations(List<EditOperation> operations, int from1, int from2, int to1, int to2) {
    int start = from1;
    while (start < to1 && from2 < to2 && seq1.charAt(start) == seq2.charAt(from2)) {
      start++;
      from2++;
    }
    addOperation(operations, SKIP, start - from1);

    int end1 = to1;
    int end2 = to2;
    while (end1 > start && end2 > from2 && seq1.charAt(end1 - 1) == seq2.charAt(end2 - 1)) {
      end1--;
      end2--;
    }

    int remaining1 = end1 - start;
    int remaining2 = end2 - from2;

    if (remaining1 == 0) {
      addOperation(operations, INSERT, remaining2);
    } else if (remaining2 == 0) {
      addOperation(operations, DELETE, remaining1);
    } else if (remaining1 == 1) {
      // the one remaining seq1 character either matches one seq2 character (move it) or not;
      // both choices cost the same for every matching position, so the first one is as good as any
      int match = indexOf(seq2, from2, end2, seq1.charAt(start));
      if (match >= 0) {
        addOperation(operations, INSERT, match - from2);
        addOperation(operations, SKIP, 1);
        addOperation(operations, INSERT, end2 - match - 1);
      } else {
        addOperation(operations, SUBSTITUTE, 1);
        addOperation(operations, INSERT, end2 - from2 - 1);
      }
    } else if (remaining2 == 1) {
      // the one remaining seq2 character either matches one seq1 character (move it) or not;
      // deletions are emitted before insertions, like in the remaining1 == 1 case above, so that
      // neighboring regions normalize into as many substitution runs as possible
      int match = indexOf(seq1, start, end1, seq2.charAt(from2));
      if (match >= 0) {
        addOperation(operations, DELETE, match - start);
        addOperation(operations, SKIP, 1);
        addOperation(operations, DELETE, end1 - match - 1);
      } else {
        addOperation(operations, DELETE, end1 - start - 1);
        addOperation(operations, SUBSTITUTE, 1);
      }
    } else {
      // bisect seq1 and find the column an optimal path crosses the bisection row in
      int mid1 = start + remaining1 / 2;
      int mid2 = findMidpoint(start, from2, mid1, end1, end2);

      collectOperations(operations, start, from2, mid1, mid2);
      collectOperations(operations, mid1, mid2, end1, end2);
    }

    if (end1 < to1) addOperation(operations, SKIP, to1 - end1);
  }

  private int findMidpoint(int from1, int from2, int mid1, int to1, int to2) {
    int[] forward = forwardCosts(from1, from2, mid1, to2);
    int[] backward = backwardCosts(mid1, from2, to1, to2);

    int midpoint = from2;
    int minCost = Integer.MAX_VALUE;
    for (int j = 0; j < forward.length; j++) {
      int cost = forward[j] + backward[j];
      if (cost < minCost) {
        minCost = cost;
        midpoint = from2 + j;
      }
    }
    return midpoint;
  }

  /**
   * Computes the costs of converting {@code seq1[from1..to1)} to the prefixes
   * {@code seq2[from2..from2 + j)} for all j, using O(length) space.
   */
  private int[] forwardCosts(int from1, int from2, int to1, int to2) {
    int length = to2 - from2;
    int[] previous = new int[length + 1];
    int[] current = new int[length + 1];
    for (int j = 0; j <= length; j++) {
      previous[j] = j;
    }
    for (int i = from1; i < to1; i++) {
      current[0] = i - from1 + 1;
      for (int j = 1; j <= length; j++) {
        current[j] = min(current[j - 1] + 1,
          previous[j] + 1,
          previous[j - 1] + (seq1.charAt(i) == seq2.charAt(from2 + j - 1) ? 0 : 1));
      }
      int[] swap = previous;
      previous = current;
      current = swap;
    }
    return previous;
  }

  /**
   * Computes the costs of converting {@code seq1[from1..to1)} to the suffixes
   * {@code seq2[from2 + j..to2)} for all j, using O(length) space.
   */
  private int[] backwardCosts(int from1, int from2, int to1, int to2) {
    int length = to2 - from2;
    int[] previous = new int[length + 1];
    int[] current = new int[length + 1];
    for (int j = 0; j <= length; j++) {
      previous[j] = length - j;
    }
    for (int i = to1 - 1; i >= from1; i--) {
      current[length] = to1 - i;
      for (int j = length - 1; j >= 0; j--) {
        current[j] = min(current[j + 1] + 1,
          previous[j] + 1,
          previous[j + 1] + (seq1.charAt(i) == seq2.charAt(from2 + j) ? 0 : 1));
      }
      int[] swap = previous;
      previous = current;
      current = swap;
    }
    return previous;
  }

  private static int min(int a, int b, int c) {
    return Math.min(a, Math.min(b, c));
  }

  private static int indexOf(CharSequence seq, int from, int to, char c) {
    for (int i = from; i < to; i++) {
      if (seq.charAt(i) == c) return i;
    }
    return -1;
  }

  private static void addOperation(List<EditOperation> operations, EditOperation.Kind kind, int length) {
    if (length == 0) return;

    if (!operations.isEmpty() && operations.get(operations.size() - 1).getKind() == kind) {
      operations.get(operations.size() - 1).incLength(length);
    } else {
      operations.add(new EditOperation(kind, length));
    }
  }

  /**
   * Pairs up adjacent deletions and insertions into {@link EditOperation.Kind#SUBSTITUTE} runs,
   * which is how the previous matrix-based implementation rendered fully-differing regions.
   */
  private static List<EditOperation> normalize(List<EditOperation> operations) {
    List<EditOperation> result = new ArrayList<>(operations.size());
    for (int i = 0; i < operations.size(); i++) {
      EditOperation operation = operations.get(i);
      EditOperation.Kind kind = operation.getKind();
      boolean deletionFirst = kind == DELETE;
      if ((deletionFirst || kind == INSERT) && i + 1 < operations.size()
          && operations.get(i + 1).getKind() == (deletionFirst ? INSERT : DELETE)) {
        EditOperation deletion = deletionFirst ? operation : operations.get(++i);
        EditOperation insertion = deletionFirst ? operations.get(++i) : operation;
        int substituted = Math.min(deletion.getLength(), insertion.getLength());
        addOperation(result, DELETE, deletion.getLength() - substituted);
        addOperation(result, SUBSTITUTE, substituted);
        addOperation(result, INSERT, insertion.getLength() - substituted);
      } else {
        addOperation(result, kind, operation.getLength());
      }
    }
    return result;
  }
}
