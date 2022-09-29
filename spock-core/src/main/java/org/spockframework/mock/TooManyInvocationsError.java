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
  private String message;

  public TooManyInvocationsError(IMockInteraction interaction, List<IMockInvocation> acceptedInvocations) {
    this.interaction = interaction;
    this.acceptedInvocations = acceptedInvocations;
  }

  public IMockInteraction getInteraction() {
    return interaction;
  }

  public List<IMockInvocation> getAcceptedInvocations() {
    return acceptedInvocations;
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

    message = builder.toString();
    return message;
  }
  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    // create the message so that it is available for serialization
    getMessage();
    out.defaultWriteObject();
  }
}
