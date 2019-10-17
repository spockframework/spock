package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IMethodInvocation;

/**
 * The context (delegate) for a {@link spock.lang.Retry} condition.
 */
public class RetryConditionContext {

  private final IMethodInvocation invocation;
  private final Throwable failure;

  RetryConditionContext(IMethodInvocation invocation, Throwable failure) {
    this.invocation = invocation;
    this.failure = failure;
  }

  /**
   * Returns the {@code Throwable} thrown by the feature method.
   *
   * @return the current failure
   */
  public Throwable getFailure() {
    return failure;
  }

  /**
   * Returns the current {@code Specification} instance.
   *
   * @return the current {@code Specification} instance
   */
  public Object getInstance() {
    return invocation.getInstance();
  }

}
