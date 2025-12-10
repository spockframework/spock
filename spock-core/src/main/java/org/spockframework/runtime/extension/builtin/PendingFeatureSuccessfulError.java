package org.spockframework.runtime.extension.builtin;

/**
 * @since 2.0
 */
public class PendingFeatureSuccessfulError extends AssertionError {
  public PendingFeatureSuccessfulError(String detailMessage) {
    super(detailMessage);
  }
}
