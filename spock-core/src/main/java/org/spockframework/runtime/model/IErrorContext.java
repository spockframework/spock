package org.spockframework.runtime.model;

import org.spockframework.util.Nullable;

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
