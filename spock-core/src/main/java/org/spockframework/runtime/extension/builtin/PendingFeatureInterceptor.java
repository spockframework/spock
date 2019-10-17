package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.*;

/**
 * @author Leonard Brünings
 */
class PendingFeatureInterceptor extends PendingFeatureBaseInterceptor implements IMethodInterceptor {

  public PendingFeatureInterceptor(Class<? extends Throwable>[] handledExceptions, String reason) {
    super(handledExceptions, reason);
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
