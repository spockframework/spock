package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Leonard Brünings
 */
class PendingFeatureIterationInterceptor extends PendingFeatureBaseInterceptor implements IMethodInterceptor {

  public PendingFeatureIterationInterceptor(Class<? extends Throwable>[] expectedExceptions) {
    super(expectedExceptions);
  }

  @Override
  public void intercept(IMethodInvocation invocation) throws Throwable {

    AtomicBoolean pass = new AtomicBoolean(false);
    invocation.getFeature().getFeatureMethod().addInterceptor(new InnerIterationInterceptor(pass, expectedExceptions));
    invocation.proceed();
    if (pass.get()) {
      throw assumptionViolation();
    } else {
      throw featurePassedUnexpectedly();
    }
  }

  private static class InnerIterationInterceptor extends PendingFeatureBaseInterceptor implements IMethodInterceptor {
    private final AtomicBoolean pass;

    public InnerIterationInterceptor(AtomicBoolean pass, Class<? extends Throwable>[] expectedExceptions) {
      super(expectedExceptions);
      this.pass = pass;
    }

    @Override
    public void intercept(IMethodInvocation invocation) throws Throwable {
      try {
        invocation.proceed();
      } catch (AssertionError e) {
        pass.set(true);
      } catch (Throwable e) {
        if (isExpected(e)) {
          pass.set(true);
        } else {
          throw e;
        }
      }
    }
  }
}
