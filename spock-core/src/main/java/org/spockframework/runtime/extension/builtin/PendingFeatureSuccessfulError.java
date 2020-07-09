package org.spockframework.runtime.extension.builtin;

import org.spockframework.util.Beta;

/**
 * @since 2.0
 */
@Beta
public class PendingFeatureSuccessfulError extends AssertionError {
  public PendingFeatureSuccessfulError(String detailMessage) {
    super(detailMessage);
  }
}
