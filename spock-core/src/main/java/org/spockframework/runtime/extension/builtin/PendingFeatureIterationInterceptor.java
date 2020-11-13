package org.spockframework.runtime.extension.builtin;

import org.opentest4j.TestAbortedException;
import org.spockframework.runtime.extension.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Leonard Brünings
 */
class PendingFeatureIterationInterceptor extends PendingFeatureBaseInterceptor implements IMethodInterceptor {

  public PendingFeatureIterationInterceptor(Class<? extends Throwable>[] expectedExceptions, String reason, String annotationUsed) {
    super(expectedExceptions, reason, annotationUsed);
  }

  @Override
  public void intercept(IMethodInvocation invocation) throws Throwable {
    AtomicReference<StackTraceElement[]> featureStackTrace = new AtomicReference<>();
    AtomicBoolean success = new AtomicBoolean(false);
    AtomicBoolean expectedFailure = new AtomicBoolean(false);
    AtomicBoolean unexpectedFailure = new AtomicBoolean(false);
    invocation.getFeature().getFeatureMethod().addInterceptor(new InnerIterationInterceptor(
      featureStackTrace, success, expectedFailure, unexpectedFailure, expectedExceptions, reason, annotationUsed));
    invocation.proceed();

    // unexpected failure happened => do nothing, iteration is red
    if (!unexpectedFailure.get()) {
      if (expectedFailure.get()) {
        // no unexpected failure, at least one expected failure and feature is rolled up => abort, feature is pending
        if (!invocation.getFeature().isReportIterations()) {
          throw testAborted(featureStackTrace.get());
        }
      } else {
        // no unexpected failure, no expected failure and at least one success,
        // that is not all iterations are aborted => fail, annotation should be removed
        if (success.get()) {
          throw featurePassedUnexpectedly(featureStackTrace.get());
        }
      }
    }
  }

  private static class InnerIterationInterceptor extends PendingFeatureBaseInterceptor implements IMethodInterceptor {
    private final AtomicReference<StackTraceElement[]> featureStackTrace;
    private final AtomicBoolean success;
    private final AtomicBoolean expectedFailure;
    private final AtomicBoolean unexpectedFailure;

    public InnerIterationInterceptor(AtomicReference<StackTraceElement[]> featureStackTrace, AtomicBoolean success,
                                     AtomicBoolean expectedFailure, AtomicBoolean unexpectedFailure,
                                     Class<? extends Throwable>[] expectedExceptions,
                                     String reason, String annotationUsed) {
      super(expectedExceptions, reason, annotationUsed);
      this.featureStackTrace = featureStackTrace;
      this.success = success;
      this.expectedFailure = expectedFailure;
      this.unexpectedFailure = unexpectedFailure;
    }

    @Override
    public void intercept(IMethodInvocation invocation) throws Throwable {
      try {
        invocation.proceed();
        success.set(true);
      } catch (PendingFeatureSuccessfulError e) {
        throw e;
      } catch (TestAbortedException e) {
        // if no expected failure set a stack trace, set it from an abort
        // that is better than the stack trace in the base interceptor
        featureStackTrace.compareAndSet(null, e.getStackTrace());
        throw e;
      } catch (AssertionError e) {
        // remember stack trace for usage in the abort exception
        featureStackTrace.set(e.getStackTrace());
        expectedFailure.set(true);
        if (invocation.getFeature().isReportIterations()) {
          throw testAborted(e.getStackTrace());
        }
      } catch (Throwable e) {
        if (isExpected(e)) {
          // remember stack trace for usage in the abort exception
          featureStackTrace.set(e.getStackTrace());
          expectedFailure.set(true);
          if (invocation.getFeature().isReportIterations()) {
            throw testAborted(e.getStackTrace());
          }
        } else {
          unexpectedFailure.set(true);
          throw e;
        }
      }
    }
  }
}
