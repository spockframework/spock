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

package org.spockframework.mock.runtime;

import org.jetbrains.annotations.NotNull;
import org.spockframework.mock.*;
import org.spockframework.util.ExceptionUtil;

import java.util.*;

/**
 * A scope for interactions defined outside a then-block
 *
 * @author Peter Niederwieser
 */
public class InteractionScope implements IInteractionScope {
  private static final int MAX_PREVIOUS_INVOCATIONS = 5;
  private final List<IMockInteraction> interactions = new ArrayList<>();
  private final List<IMockInvocation> unmatchedInvocations = new ArrayList<>();
  private int currentRegistrationZone = 0;
  private int currentExecutionZone = 0;
  private final Deque<IMockInvocation> previousInvocationsInReverseOrder = new ArrayDeque<>(MAX_PREVIOUS_INVOCATIONS);

  @Override
  public void addInteraction(final IMockInteraction interaction) {
    MockInteractionDecorator decorator = createMockInteractionDecorator(interaction);
    ListIterator<IMockInteraction> it = interactions.listIterator();
    while (it.hasNext()) {
      IMockInteraction next = it.next();
      if (next.isThisInteractionOverridableBy(decorator)) {
        it.set(decorator);
        return;
      }
    }

    interactions.add(decorator);
  }

  @NotNull
  private MockInteractionDecorator createMockInteractionDecorator(IMockInteraction interaction) {
    return new MockInteractionDecorator(interaction) {
      final int myRegistrationZone = currentRegistrationZone;

      @Override
      public Object accept(IMockInvocation invocation) {
        WrongInvocationOrderError wrongInvocationOrderError = null;
        if (currentExecutionZone > myRegistrationZone) {
          wrongInvocationOrderError = new WrongInvocationOrderError(decorated, invocation, previousInvocationsInReverseOrder);
        }
        currentExecutionZone = myRegistrationZone;
        if (previousInvocationsInReverseOrder.size() == MAX_PREVIOUS_INVOCATIONS) {
          previousInvocationsInReverseOrder.removeLast();
        }
        previousInvocationsInReverseOrder.addFirst(invocation);

        Object result = null;
        Throwable invocationException = null;
        try {
          result = super.accept(invocation);
        } catch (AssertionError | Exception e) {
          invocationException = e;
        }
        ExceptionUtil.throwWithSuppressed(invocationException, wrongInvocationOrderError);
        return result;
      }

      @Override
      public String describeMismatch(IMockInvocation invocation) {
        return interaction.describeMismatch(invocation);
      }
    };
  }

  @Override
  public void addOrderingBarrier() {
    currentRegistrationZone++;
  }

  @Override
  public void addUnmatchedInvocation(IMockInvocation invocation) {
    if (invocation.getMockObject().isVerified()) {
      unmatchedInvocations.add(invocation);
    }
  }

  @Override
  public IMockInteraction match(IMockInvocation invocation) {
    IMockInteraction firstMatch = null;
    for (IMockInteraction interaction : interactions)
      if (interaction.matches(invocation)) {
        if (!interaction.isExhausted()) return interaction;
        if (firstMatch == null) firstMatch = interaction;
      }

    return firstMatch;
  }

  @Override
  public void verifyInteractions() {
    List<IMockInteraction> unsatisfiedInteractions = new ArrayList<>();

    for (IMockInteraction interaction : interactions)
      if (!interaction.isSatisfied()) unsatisfiedInteractions.add(interaction);

    if (!unsatisfiedInteractions.isEmpty())
      throw new TooFewInvocationsError(unsatisfiedInteractions, unmatchedInvocations);
  }
}
