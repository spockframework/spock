package org.spockframework.runtime.extension.builtin;

/**
 * The context (delegate) for a {@link spock.lang.Retry} condition.
 */
public class RetryConditionContext {

  private final Object instance;
  private final Throwable failure;

  RetryConditionContext(Object instance, Throwable failure) {
    this.instance = instance;
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
    return instance;
  }

}
