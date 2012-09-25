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

package org.spockframework.mock.runtime;

import org.spockframework.mock.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A scope for interactions defined outside a then-block
 *
 * @author Peter Niederwieser
 */
public class InteractionScope implements IInteractionScope {
  private final List<IMockInteraction> interactions = new ArrayList<IMockInteraction>();
  private final List<IMockInvocation> unmatchedInvocations = new ArrayList<IMockInvocation>();
  private int currentRegistrationZone = 0;
  private int currentExecutionZone = 0;

  public void addInteraction(IMockInteraction interaction) {
    interactions.add(new MockInteractionDecorator(interaction) {
      final int myRegistrationZone = currentRegistrationZone;

      @Override
      public Object accept(IMockInvocation invocation) {
        Object result = super.accept(invocation);
        if (currentExecutionZone > myRegistrationZone)
          throw new WrongInvocationOrderError(decorated, invocation);
        currentExecutionZone = myRegistrationZone;
        return result;
      }
    });
  }

  public void addOrderingBarrier() {
    currentRegistrationZone++;
  }

  public void addUnmatchedInvocation(IMockInvocation invocation) {
    if (invocation.getMockObject().isVerified()) {
      unmatchedInvocations.add(invocation);
    }
  }

  public IMockInteraction match(IMockInvocation invocation) {
    IMockInteraction firstMatch = null;
    for (IMockInteraction interaction : interactions)
      if (interaction.matches(invocation)) {
        if (!interaction.isExhausted()) return interaction;
        if (firstMatch == null) firstMatch = interaction;
      }

    return firstMatch;
  }

  public void verifyInteractions() {
    List<IMockInteraction> unsatisfiedInteractions = new ArrayList<IMockInteraction>();

    for (IMockInteraction interaction : interactions)
      if (!interaction.isSatisfied()) unsatisfiedInteractions.add(interaction);

    if (!unsatisfiedInteractions.isEmpty())
      throw new TooFewInvocationsError(unsatisfiedInteractions, unmatchedInvocations);
  }
}
