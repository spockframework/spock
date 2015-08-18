package org.spockframework.runtime.extension.builtin;

import org.junit.AssumptionViolatedException;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;

/**
 * @author Leonard Brünings
 */
class PendingFeatureInterceptor implements IMethodInterceptor {
  @Override
  public void intercept(IMethodInvocation invocation) throws Throwable {
    try {
      invocation.proceed();
    } catch (AssertionError e) {
      throw new AssumptionViolatedException("Feature not yet implemented correctly.");
    }
    throw new AssertionError("Feature is marked with @PendingFeature but passes unexpectedly");
  }
}
