package org.spockframework.runtime.extension.builtin;

import org.junit.AssumptionViolatedException;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Leonard Brünings
 */
class PendingFeatureIterationInterceptor implements IMethodInterceptor {
  @Override
  public void intercept(IMethodInvocation invocation) throws Throwable {

    AtomicBoolean pass = new AtomicBoolean(false);
    invocation.getFeature().getFeatureMethod().addInterceptor(new InnerIterationInterceptor(pass));
    invocation.proceed();
    if (pass.get()) {
      throw new AssumptionViolatedException("Feature not yet implemented correctly.");
    } else {
      throw new AssertionError("Feature is marked with @PendingFeature but passes unexpectedly");
    }
  }

  private static class InnerIterationInterceptor implements IMethodInterceptor {
    private final AtomicBoolean pass;

    public InnerIterationInterceptor(AtomicBoolean pass) {
      this.pass = pass;
    }

    @Override
    public void intercept(IMethodInvocation invocation) throws Throwable {
      try {
        invocation.proceed();
      } catch (AssertionError e) {
        pass.set(true);
      }
    }
  }
}
