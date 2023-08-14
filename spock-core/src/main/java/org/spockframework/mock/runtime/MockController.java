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

import org.spockframework.mock.*;
import org.spockframework.util.InternalSpockError;

import java.util.*;

/**
 * @author Peter Niederwieser
 */
public class MockController implements IMockController {
  private final Deque<IInteractionScope> scopes = new LinkedList<>();
  private final List<InteractionNotSatisfiedError> errors = new ArrayList<>();
  private boolean firstScopeFrozen;

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
    IInteractionScope scope;
    if (firstScopeFrozen) {
      //We shall skip the first scope and use the next one, because the current first scope was frozen for interactions
      //See Ticket #1759: Interactions in when blocks are not preserved if the then block contains an interaction
      if (scopes.size() <= 1) {
        throw new InternalSpockError();
      }
      Iterator<IInteractionScope> it = scopes.iterator();
      it.next();
      scope = it.next();
    } else {
      scope = scopes.getFirst();
    }
    scope.addInteraction(interaction);
  }

  public static final String ADD_BARRIER = "addBarrier";

  public synchronized void addBarrier() {
    scopes.getFirst().addOrderingBarrier();
  }

  public static final String ENTER_SCOPE = "enterScope";

  public synchronized void enterScope() {
    throwAnyPreviousError();
    firstScopeFrozen = false;
    scopes.addFirst(new InteractionScope());
  }

  public static final String FREEZE_SCOPE = "freezeScope";

  /**
   * Freeze the currently first scope, which means no more iterations can be added with {@link #addInteraction(IMockInteraction)}.
   * The interactions added after that, are then added to the next scope in the {@code scopes}.
   */
  public synchronized void freezeScope() {
    firstScopeFrozen = true;
  }

  public static final String LEAVE_SCOPE = "leaveScope";

  public synchronized void leaveScope() {
    throwAnyPreviousError();
    firstScopeFrozen = false;
    IInteractionScope scope = scopes.removeFirst();
    scope.verifyInteractions();
  }

  private void throwAnyPreviousError() {
    if (!errors.isEmpty()) throw errors.get(0);
  }
}
