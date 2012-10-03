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

package org.spockframework.mock;

import java.util.*;

import org.spockframework.util.Assert;
import org.spockframework.util.HashMultiset;
import org.spockframework.util.IMultiset;

/**
 * Thrown to indicate that one or more mandatory interactions matched too few invocations.
 *
 * @author Peter Niederwieser
 */
public class TooFewInvocationsError extends InteractionNotSatisfiedError {
  private final List<IMockInteraction> interactions;
  private final List<IMockInvocation> unmatchedInvocations;
  private String message;

  public TooFewInvocationsError(List<IMockInteraction> interactions, List<IMockInvocation> unmatchedInvocations) {
    Assert.notNull(interactions);
    Assert.that(interactions.size() > 0);
    this.interactions = interactions;
    this.unmatchedInvocations = unmatchedInvocations;
  }

  @Override
  public synchronized String getMessage() {
    if (message != null) return message;

    IMultiset<IMockInvocation> unmatchedMultiInvocations = new HashMultiset<IMockInvocation>(unmatchedInvocations);

    StringBuilder builder = new StringBuilder();

    for (IMockInteraction interaction : interactions) {
      builder.append("Too few invocations for:\n\n");
      builder.append(interaction);
      builder.append("\n\n");
      List<ScoredInvocation> scoredInvocations = scoreInvocations(interaction, unmatchedMultiInvocations);
      builder.append("Unmatched invocations (ordered by similarity):\n\n");
      if (scoredInvocations.isEmpty()) {
        builder.append("None\n");
      } else {
        for (ScoredInvocation invocation : scoredInvocations) {
          builder.append(invocation.count);
          builder.append(" * ");
          builder.append(invocation.invocation);
          builder.append('\n');
        }
      }
      builder.append('\n');
    }

    message = builder.toString();
    return message;
  }

  private List<ScoredInvocation> scoreInvocations(IMockInteraction interaction, IMultiset<IMockInvocation> invocations) {
    List<ScoredInvocation> result = new ArrayList<ScoredInvocation>();
    for (Map.Entry<IMockInvocation, Integer> entry : invocations.entrySet()) {
      result.add(new ScoredInvocation(entry.getKey(), entry.getValue(), interaction.computeSimilarityScore(entry.getKey())));
    }
    Collections.sort(result);
    return result;
  }

  private static class ScoredInvocation implements Comparable<ScoredInvocation> {
    final IMockInvocation invocation;
    final int count;
    final int score;

    private ScoredInvocation(IMockInvocation invocation, int count, int score) {
      this.invocation = invocation;
      this.count = count;
      this.score = score;
    }

    public int compareTo(ScoredInvocation other) {
      return score - other.score;
    }
  }
}
