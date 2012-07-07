package org.spockframework.runtime;

import org.spockframework.util.Nullable;

public class UnallowedExceptionThrownError extends SpockAssertionError {
  private final Class<? extends Throwable> unallowed;
  private final Throwable actual;

  public UnallowedExceptionThrownError(@Nullable Class<? extends Throwable> unallowed, Throwable actual) {
    super(actual);
    this.unallowed = unallowed;
    this.actual = actual;
  }

  public Class<? extends Throwable> getUnallowed() {
    return unallowed;
  }

  public Throwable getActual() {
    return actual;
  }

  @Override
  public String getMessage() {
    if (unallowed == null) {
      return String.format("Expected no exception to be thrown, but got '%s'", actual.getClass().getName());
    }
    return String.format("Expected no exception of type '%s' to be thrown, but got it nevertheless",
        unallowed.getName());
  }
}
