package org.spockframework.runtime;

import org.spockframework.util.InternalSpockError;

/**
 * @author Peter Niederwieser
 */
public enum RunStatus2 {
  OK(null, null),

  END_ITERATION(Action.END, Scope.ITERATION),
  END_FEATURE(Action.END, Scope.FEATURE),
  END_SPECK(Action.END, Scope.SPECK),
  END_ALL(Action.END, Scope.ALL),

  ABORT_ITERATION(Action.ABORT, Scope.ITERATION),
  ABORT_FEATURE(Action.ABORT, Scope.FEATURE),
  ABORT_SPECK(Action.ABORT, Scope.SPECK),
  ABORT_ALL(Action.ABORT, Scope.ALL);

  public enum Action {
    END,
    ABORT
  }

  public enum Scope {
    ITERATION,
    FEATURE,
    SPECK,
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

