package org.spockframework.runtime.extension.builtin;

import org.opentest4j.TestAbortedException;
import org.spockframework.runtime.extension.*;

/**
 * @author Leonard Brünings
 */
class PendingFeatureInterceptor extends PendingFeatureBaseInterceptor implements IMethodInterceptor {

  public PendingFeatureInterceptor(Class<? extends Throwable>[] handledExceptions, String reason,
                                   String annotationUsedByExtension, boolean secondary) {
    super(handledExceptions, reason, annotationUsedByExtension, secondary);
  }

  @Override
  public void intercept(IMethodInvocation invocation) throws Throwable {
    try {
      invocation.proceed();
    } catch (TestAbortedException e) {
      throw e;
    } catch (AssertionError e) {
      throw testAborted(e.getStackTrace());
    } catch (Throwable e) {
      if (isExpected(e)) {
        throw testAborted(e.getStackTrace());
      } else {
        throw e;
      }
    }
    if (!secondary) {
      throw featurePassedUnexpectedly(null);
    }
  }

}
