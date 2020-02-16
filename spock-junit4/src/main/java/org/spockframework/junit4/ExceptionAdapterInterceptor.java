package org.spockframework.junit4;

import org.spockframework.runtime.extension.*;

import org.junit.internal.AssumptionViolatedException;
import org.junit.runners.model.MultipleFailureException;
import org.opentest4j.*;

public class ExceptionAdapterInterceptor implements IMethodInterceptor {
  @Override
  public void intercept(IMethodInvocation invocation) throws Throwable {
    try {
      invocation.proceed();
    } catch (AssumptionViolatedException assumption) {
      throw new TestAbortedException(assumption.getMessage(), assumption);
    } catch (MultipleFailureException mfe) {
      throw new MultipleFailuresError("There were multiple errors", mfe.getFailures());
    }
  }
}
