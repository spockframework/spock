package org.spockframework.runtime;

import org.spockframework.lang.ISpecificationContext;
import org.spockframework.runtime.model.*;

class ErrorContext implements IErrorContext {
  private final SpecInfo spec;
  private final FeatureInfo feature;
  private final IterationInfo iteration;
  private final BlockInfo block;

  private ErrorContext(SpecInfo spec, FeatureInfo feature, IterationInfo iteration, BlockInfo block) {
    this.spec = spec;
    this.feature = feature;
    this.iteration = iteration;
    this.block = block;
  }

  static ErrorContext from(ISpecificationContext context) {
    if (context.isSharedContext()) {
      return new ErrorContext(context.getCurrentSpec(), null, null, null);
    }
    return new ErrorContext(
      context.getCurrentSpec(),
      context.getCurrentFeature(),
      context.getCurrentIteration(),
      context.getCurrentBlock()
    );
  }

  @Override
  public SpecInfo getCurrentSpec() {
    return spec;
  }

  @Override
  public FeatureInfo getCurrentFeature() {
    return feature;
  }

  @Override
  public IterationInfo getCurrentIteration() {
    return iteration;
  }

  @Override
  public BlockInfo getCurrentBlock() {
    return block;
  }
}
