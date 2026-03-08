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

package org.spockframework.mock;

import org.spockframework.util.*;

import java.io.IOException;
import java.util.*;

/**
 * Thrown to indicate that a mandatory interaction matched too many invocations.
 *
 * @author Peter Niederwieser
 */
public class TooManyInvocationsError extends InteractionNotSatisfiedError {
  private static final long serialVersionUID = 1L;

  private final transient IMockInteraction interaction;
  private final transient List<IMockInvocation> acceptedInvocations;
  private transient List<IMockInteraction> unsatisfiedInteractions;
  private String message;

  public TooManyInvocationsError(IMockInteraction interaction, List<IMockInvocation> acceptedInvocations) {
    Assert.notNull(interaction);
    this.interaction = interaction;
    this.acceptedInvocations = acceptedInvocations;
  }

  public IMockInteraction getInteraction() {
    return interaction;
  }

  public List<IMockInvocation> getAcceptedInvocations() {
    return acceptedInvocations;
  }

  public void enrichWithScopeContext(List<IMockInteraction> unsatisfiedInteractions) {
    this.unsatisfiedInteractions = unsatisfiedInteractions;
    this.message = null;
  }

  @Override
  public synchronized String getMessage() {
    if (message != null) return message;

    IMultiset<IMockInvocation> uniqueInvocations = new LinkedHashMultiset<>();
    for (IMockInvocation invocation : CollectionUtil.reverse(acceptedInvocations)) {
      uniqueInvocations.add(invocation);
    }

    StringBuilder builder = new StringBuilder();
    builder.append("Too many invocations for:\n\n");
    builder.append(interaction);
    builder.append("\n\n");
    builder.append("Matching invocations (ordered by last occurrence):\n\n");

    int count = 0;
    for (Map.Entry<IMockInvocation, Integer> entry : uniqueInvocations.entrySet()) {
      builder.append(entry.getValue());
      builder.append(" * ");
      builder.append(entry.getKey());
      if (count++ == 0) {
        builder.append("   <-- this triggered the error");
      }
      builder.append("\n");
    }
    builder.append("\n");

    if (unsatisfiedInteractions != null && !unsatisfiedInteractions.isEmpty()) {
      appendUnmatchedInteractions(builder);
    }

    message = builder.toString();
    return message;
  }

  private void appendUnmatchedInteractions(StringBuilder builder) {
    Set<IMockInvocation> acceptedPool = new LinkedHashSet<>(acceptedInvocations);

    // Filter to unsatisfied interactions where at least one accepted invocation matches target+method
    List<IMockInteraction> relevantUnsatisfied = new ArrayList<>();
    for (IMockInteraction unsatisfied : unsatisfiedInteractions) {
      for (IMockInvocation invocation : acceptedPool) {
        if (unsatisfied.matchesTargetAndMethod(invocation)) {
          relevantUnsatisfied.add(unsatisfied);
          break;
        }
      }
    }

    if (relevantUnsatisfied.isEmpty()) {
      return;
    }

    builder.append("Unmatched invocations (ordered by similarity):\n\n");

    for (IMockInteraction unsatisfied : relevantUnsatisfied) {
      builder.append(unsatisfied);
      builder.append('\n');
      List<InteractionDiagnostics.ScoredInvocation> scored = InteractionDiagnostics.scoreMatchingInvocations(unsatisfied, acceptedPool);
      InteractionDiagnostics.appendMismatchDescriptions(builder, unsatisfied, scored);
    }

    builder.append('\n');
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    // create the message so that it is available for serialization
    getMessage();
    out.defaultWriteObject();
  }
}
