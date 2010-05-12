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

import java.util.ArrayList;
import java.util.List;

/**
 * A scope for interactions defined outside a then-block
 *
 * @author Peter Niederwieser
 */
public class InteractionScope implements IInteractionScope {
  private final List<IMockInteraction> interactions = new ArrayList<IMockInteraction>();
  private int currentRegistrationZone = 0;
  private int currentExecutionZone = 0;

  public void addInteraction(IMockInteraction interaction) {
    interactions.add(new MockInteractionDecorator(interaction) {
      int registrationZone = currentRegistrationZone;

      @Override
      public Object accept(IMockInvocation invocation) {
        Object result = super.accept(invocation);
        if (currentExecutionZone > registrationZone)
          throw new WrongInvocationOrderException(decorated, invocation);
        currentExecutionZone = registrationZone;
        return result;
      }
    });
  }

  public void addOrderingBarrier() {
    currentRegistrationZone++;
  }

  public IMockInteraction match(IMockInvocation invocation) {
    for (IMockInteraction interaction : interactions)
      if (interaction.matches(invocation))
        return interaction;

    return null;
  }

  public void verifyInteractions() {
    List<IMockInteraction> unsatisfieds = new ArrayList<IMockInteraction>();

    for (IMockInteraction i : interactions)
      if (!i.isSatisfied()) unsatisfieds.add(i);

    if (!unsatisfieds.isEmpty())
      throw new TooFewInvocationsError(unsatisfieds);
  }
}
