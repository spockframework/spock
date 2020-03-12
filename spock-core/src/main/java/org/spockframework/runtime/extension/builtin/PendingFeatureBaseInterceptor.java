package org.spockframework.runtime.extension.builtin;

import org.opentest4j.TestAbortedException;

/**
 * @author Leonard Brünings
 */
public class PendingFeatureBaseInterceptor {
  protected final Class<? extends Throwable>[] expectedExceptions;
  protected final String reason;
  protected final String annotationUsed;
  protected final boolean secondary;

  public PendingFeatureBaseInterceptor(Class<? extends Throwable>[] expectedExceptions, String reason,
                                       String annotationUsed, boolean secondary) {
    this.expectedExceptions = expectedExceptions;
    this.reason = reason;
    this.annotationUsed = annotationUsed;
    this.secondary = secondary;
  }

  protected boolean isExpected(Throwable e) {
    for (Class<? extends Throwable> exception : expectedExceptions) {
      if(exception.isInstance(e)) {
        return true;
      }
    }
    return false;
  }
  protected AssertionError featurePassedUnexpectedly(StackTraceElement[] stackTrace) {
    AssertionError assertionError = new AssertionError("Feature is marked with " + annotationUsed + " but passes unexpectedly");
    if (stackTrace != null) {
      assertionError.setStackTrace(stackTrace);
    }
    return assertionError;
  }

  protected TestAbortedException testAborted(StackTraceElement[] stackTrace) {
    TestAbortedException testAbortedException = new TestAbortedException("Feature not yet implemented correctly."
      + ("".equals(reason) ? "" : " Reason: " + reason));
    testAbortedException.setStackTrace(stackTrace);
    return testAbortedException;
  }
}
