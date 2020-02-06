package org.spockframework.runtime.extension.builtin;

import org.opentest4j.TestAbortedException;

/**
 * @author Leonard Brünings
 */
public class PendingFeatureBaseInterceptor {
  protected final Class<? extends Throwable>[] expectedExceptions;
  protected final String reason;
  protected final String annotationUsed;

  public PendingFeatureBaseInterceptor(Class<? extends Throwable>[] expectedExceptions, String reason, String annotationUsed) {
    this.expectedExceptions = expectedExceptions;
    this.reason = reason;
    this.annotationUsed = annotationUsed;
  }

  protected boolean isExpected(Throwable e) {
    for (Class<? extends Throwable> exception : expectedExceptions) {
      if(exception.isInstance(e)) {
        return true;
      }
    }
    return false;
  }
  protected AssertionError featurePassedUnexpectedly() {
    return new AssertionError("Feature is marked with " + annotationUsed + " but passes unexpectedly");
  }

  protected TestAbortedException assumptionViolation() {
    return new TestAbortedException("Feature not yet implemented correctly."
      + ("".equals(reason) ? "" : " Reason: " + reason));
  }
}
