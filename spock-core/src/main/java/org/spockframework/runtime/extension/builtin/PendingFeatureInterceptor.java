package org.spockframework.runtime.extension.builtin;

import org.opentest4j.TestAbortedException;
import org.spockframework.runtime.extension.*;

/**
 * @author Leonard Brünings
 */
class PendingFeatureInterceptor extends PendingFeatureBaseInterceptor implements IMethodInterceptor {

  public PendingFeatureInterceptor(Class<? extends Throwable>[] handledExceptions, String reason, String annotationUsedByExtension) {
    super(handledExceptions, reason, annotationUsedByExtension);
  }

  @Override
  public void intercept(IMethodInvocation invocation) throws Throwable {
    try {
      invocation.proceed();
    } catch (TestAbortedException | PendingFeatureSuccessfulError e) {
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
    throw featurePassedUnexpectedly(null);
  }

}
