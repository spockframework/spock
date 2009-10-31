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

package org.spockframework.experimental;

import org.spockframework.util.InternalSpockError;

/**
 * @author Peter Niederwieser
 */
public enum RunStatus2 {
  OK(null, null),

  END_ITERATION(Action.END, Scope.ITERATION),
  END_FEATURE(Action.END, Scope.FEATURE),
  END_SPEC(Action.END, Scope.SPEC),
  END_ALL(Action.END, Scope.ALL),

  ABORT_ITERATION(Action.ABORT, Scope.ITERATION),
  ABORT_FEATURE(Action.ABORT, Scope.FEATURE),
  ABORT_SPEC(Action.ABORT, Scope.SPEC),
  ABORT_ALL(Action.ABORT, Scope.ALL);

  public enum Action {
    END,
    ABORT
  }

  public enum Scope {
    ITERATION,
    FEATURE,
    SPEC,
    ALL
  }

  private Action action;
  private Scope scope;

  private RunStatus2(Action action, Scope scope) {
    this.action = action;
    this.scope = scope;
  }

  public Scope getScope() {
    return scope;
  }

  public Action getAction() {
    return action;
  }

  public RunStatus2 combineWith(RunStatus2 other) {
    Action maxAction = Action.values()[Math.max(action.ordinal(), other.action.ordinal())];
    Scope maxScope = Scope.values()[Math.max(scope.ordinal(), other.scope.ordinal())];
    return getStatusFor(maxAction, maxScope);
  }

  public static RunStatus2 getStatusFor(Action action, Scope scope) {
    for (RunStatus2 status : RunStatus2.values())
      if (status.action == action && status.scope == scope)
        return status;
    throw new InternalSpockError("getStatusFor");
  }
}

