/*
 * Copyright 2025 the original author or authors.
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

package org.spockframework.mock;

import org.spockframework.util.Assert;
import org.spockframework.util.IMultiset;

import java.util.*;

import static java.util.Collections.sort;

/**
 * Shared utilities for rendering interaction mismatch diagnostics
 * in {@link TooFewInvocationsError} and {@link TooManyInvocationsError}.
 */
class InteractionDiagnostics {
  private static final int MAX_MISMATCH_DESCRIPTIONS = 5;

  /**
   * Score invocations from a multiset against an interaction, preserving counts.
   */
  static List<ScoredInvocation> scoreInvocations(IMockInteraction interaction, IMultiset<IMockInvocation> invocations) {
    Assert.notNull(interaction);
    List<ScoredInvocation> result = new ArrayList<>();
    for (Map.Entry<IMockInvocation, Integer> entry : invocations.entrySet()) {
      result.add(new ScoredInvocation(entry.getKey(), entry.getValue(), interaction.computeSimilarityScore(entry.getKey())));
    }
    sort(result);
    return result;
  }

  /**
   * Score invocations from a set against an interaction, filtering to only those matching target and method.
   */
  static List<ScoredInvocation> scoreMatchingInvocations(IMockInteraction interaction, Set<IMockInvocation> invocations) {
    Assert.notNull(interaction);
    List<ScoredInvocation> result = new ArrayList<>();
    for (IMockInvocation invocation : invocations) {
      if (interaction.matchesTargetAndMethod(invocation)) {
        result.add(new ScoredInvocation(invocation, 0, interaction.computeSimilarityScore(invocation)));
      }
    }
    sort(result);
    return result;
  }

  /**
   * Append scored invocations with count prefix and mismatch descriptions.
   * Format: {@code count * invocation\ndescribeMismatch\n}
   */
  static void appendScoredInvocations(StringBuilder builder, IMockInteraction interaction, List<ScoredInvocation> scored) {
    int idx = 0;
    for (ScoredInvocation si : scored) {
      builder.append(si.count);
      builder.append(" * ");
      builder.append(si.invocation);
      builder.append('\n');
      if (idx++ < MAX_MISMATCH_DESCRIPTIONS) {
        appendMismatchDescription(builder, interaction, si.invocation);
      }
    }
  }

  /**
   * Append only mismatch descriptions for scored invocations (no count/invocation header).
   */
  static void appendMismatchDescriptions(StringBuilder builder, IMockInteraction interaction, List<ScoredInvocation> scored) {
    int idx = 0;
    for (ScoredInvocation si : scored) {
      if (idx++ < MAX_MISMATCH_DESCRIPTIONS) {
        appendMismatchDescription(builder, interaction, si.invocation);
      }
    }
  }

  private static void appendMismatchDescription(StringBuilder builder, IMockInteraction interaction, IMockInvocation invocation) {
    try {
      builder.append(interaction.describeMismatch(invocation));
    } catch (AssertionError | Exception e) {
      builder.append("<Renderer threw Exception>: ").append(e.getMessage());
    }
    builder.append('\n');
  }

  static class ScoredInvocation implements Comparable<ScoredInvocation> {
    final IMockInvocation invocation;
    final int count;
    final int score;

    ScoredInvocation(IMockInvocation invocation, int count, int score) {
      Assert.notNull(invocation);
      this.invocation = invocation;
      this.count = count;
      this.score = score;
    }

    @Override
    public int compareTo(ScoredInvocation other) {
      int result = score - other.score;
      if (result != 0) return result;
      return invocation.toString().compareTo(other.invocation.toString());
    }
  }
}
