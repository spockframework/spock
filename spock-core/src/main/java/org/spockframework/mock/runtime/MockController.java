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

import java.util.*;

/**
 * @author Peter Niederwieser
 */
public class MockController implements IMockController {
  private final LinkedList<IInteractionScope> scopes = new LinkedList<>();
  private final List<InteractionNotSatisfiedError> errors = new ArrayList<>();

  public MockController() {
    scopes.addFirst(new InteractionScope());
  }

  @Override
  public synchronized Object handle(IMockInvocation invocation) {
    for (IInteractionScope scope : scopes) {
      IMockInteraction interaction = scope.match(invocation);
      if (interaction != null) {
        try {
          return interaction.accept(invocation);
        } catch (InteractionNotSatisfiedError e) {
          errors.add(e);
          throw e;
        }
      }
    }
    for (IInteractionScope scope : scopes) {
      scope.addUnmatchedInvocation(invocation);
    }
    return invocation.getMockObject().getDefaultResponse().respond(invocation);
  }

  public static final String ADD_INTERACTION = "addInteraction";

  public synchronized void addInteraction(IMockInteraction interaction) {
    scopes.getFirst().addInteraction(interaction);
  }

  public static final String ADD_BARRIER = "addBarrier";

  public synchronized void addBarrier() {
    scopes.getFirst().addOrderingBarrier();
  }

  public static final String ENTER_SCOPE = "enterScope";

  public synchronized void enterScope() {
    throwAnyPreviousError();
    scopes.addFirst(new InteractionScope());
  }

  public static final String LEAVE_SCOPE = "leaveScope";

  public synchronized void leaveScope() {
    throwAnyPreviousError();
    IInteractionScope scope = scopes.removeFirst();
    scope.verifyInteractions();
  }

  private void throwAnyPreviousError() {
    if (!errors.isEmpty()) throw errors.get(0);
  }
}
