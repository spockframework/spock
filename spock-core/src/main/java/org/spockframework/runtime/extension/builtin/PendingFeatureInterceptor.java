package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;

/**
 * @author Leonard Brünings
 */
class PendingFeatureInterceptor extends PendingFeatureBaseInterceptor implements IMethodInterceptor {

  public PendingFeatureInterceptor(Class<? extends Throwable>[] handledExceptions) {
    super(handledExceptions);
  }

  @Override
  public void intercept(IMethodInvocation invocation) throws Throwable {
    try {
      invocation.proceed();
    } catch (AssertionError e) {
      throw assumptionViolation();
    } catch (Throwable e) {
      if (isExpected(e)) {
        throw assumptionViolation();
      } else {
        throw e;
      }
    }
    throw featurePassedUnexpectedly();
  }

}
