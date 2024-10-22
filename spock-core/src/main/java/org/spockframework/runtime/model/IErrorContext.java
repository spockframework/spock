package org.spockframework.runtime.model;

import org.spockframework.util.Beta;
import org.spockframework.util.Nullable;

/**
 * Provides context information for an error that occurred during the execution of a specification.
 * <p>
 * Depending on the context in which the error occurred, some of the methods may return {@code null}.
 *
 * @since 2.4
 */
@Beta
public interface IErrorContext {
  @Nullable
  SpecInfo getCurrentSpec();

  @Nullable
  FeatureInfo getCurrentFeature();

  @Nullable
  IterationInfo getCurrentIteration();

  @Nullable
  BlockInfo getCurrentBlock();
}
