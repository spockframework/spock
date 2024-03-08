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
import org.spockframework.util.Checks;
import org.spockframework.util.ExceptionUtil;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * @author Peter Niederwieser
 */
public class MockController implements IMockController, IThreadAwareMockController {
  private final Deque<IInteractionScope> scopes = new LinkedList<>();
  private final List<InteractionNotSatisfiedError> errors = new CopyOnWriteArrayList<>();
  private final List<IMockMaker.IStaticMock> staticMocks = new ArrayList<>();

  public MockController() {
    scopes.addFirst(new InteractionScope());
  }

  @Override
  public Object handle(IMockInvocation invocation) {
    Supplier<Object> resultSupplier = null;
    synchronized (this) {
      for (IInteractionScope scope : scopes) {
        IMockInteraction interaction = scope.match(invocation);
        if (interaction != null) {
          resultSupplier = requireNonNull(interaction.accept(invocation), "interaction must not return null");
          break;
        }
      }
      if (resultSupplier == null) {
        for (IInteractionScope scope : scopes) {
          scope.addUnmatchedInvocation(invocation);
        }
      }
    }
    if (resultSupplier == null) {
      return invocation.getMockObject().getDefaultResponse().respond(invocation).get();
    } else {
      try {
        return resultSupplier.get();
      } catch (InteractionNotSatisfiedError e) {
        errors.add(e);
        throw e;
      }
    }
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

  @Override
  public void registerStaticMock(IMockMaker.IStaticMock staticMock) {
    synchronized (staticMocks) {
      staticMocks.add(requireNonNull(staticMock));
    }
  }

  private void enableThreadAwareMocksOnCurrentThread() {
    enableStaticMocksOnCurrentThread();
  }

  private void disableThreadAwareMocksOnCurrentThread() {
    disableStaticMocksOnCurrentThread();
  }

  private void enableStaticMocksOnCurrentThread() {
    synchronized (staticMocks) {
      for (IMockMaker.IStaticMock mock : staticMocks) {
        mock.enable();
      }
    }
  }

  private void disableStaticMocksOnCurrentThread() {
    synchronized (staticMocks) {
      for (IMockMaker.IStaticMock mock : staticMocks) {
        mock.disable();
      }
    }
  }

  @Override
  public void runWithThreadAwareMocks(Runnable code) {
    enableThreadAwareMocksOnCurrentThread();
    try {
      code.run();
    } finally {
      disableThreadAwareMocksOnCurrentThread();
    }
  }

  @Override
  public <R> R withActiveThreadAwareMocks(Callable<R> code) {
    enableThreadAwareMocksOnCurrentThread();
    try {
      return code.call();
    } catch (Exception ex) {
      return ExceptionUtil.sneakyThrow(ex);
    } finally {
      disableThreadAwareMocksOnCurrentThread();
    }
  }
}
