package org.spockframework.mock;

import org.spockframework.util.Nullable;

public class InvocationMatchResult {
  private final boolean accepted;
  private final boolean hasReturnValue;
  private final Object returnValue;

  public InvocationMatchResult(boolean accepted, boolean hasReturnValue, Object returnValue) {
    this.accepted = accepted;
    this.hasReturnValue = hasReturnValue;
    this.returnValue = returnValue;
  }

  public boolean isAccepted() {
    return accepted;
  }

  public boolean hasReturnValue() {
    return hasReturnValue;
  }

  @Nullable
  public Object getReturnValue() {
    return returnValue;
  }
}
