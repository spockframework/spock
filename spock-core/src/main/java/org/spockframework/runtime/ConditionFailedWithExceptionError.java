package org.spockframework.runtime;

/**
 * Indicates that an Exception was thrown while the condition was evaluated.
 *
 * @since 1.1
 */
public class ConditionFailedWithExceptionError extends ConditionNotSatisfiedError {
  public ConditionFailedWithExceptionError(Condition condition, Throwable cause) {
    super(condition, cause);
  }

  @Override
  public String getMessage() {
    return "Condition failed with Exception:\n\n" + getCondition().getRendering();
  }
}
