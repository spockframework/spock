package org.spockframework.runtime;

import org.spockframework.runtime.model.*;
import org.spockframework.util.Nullable;

class ErrorContext implements IErrorContext {
  private final SpecInfo spec;
  private final FeatureInfo feature;
  private final IterationInfo iteration;
  private final BlockInfo block;

  private ErrorContext(@Nullable SpecInfo spec, @Nullable FeatureInfo feature, @Nullable IterationInfo iteration, @Nullable BlockInfo block) {
    this.spec = spec;
    this.feature = feature;
    this.iteration = iteration;
    this.block = block;
  }

  static ErrorContext from(SpecificationContext context) {
    return new ErrorContext(
      context.getCurrentSpec(),
      context.getCurrentFeatureOrNull(),
      context.getCurrentIterationOrNull(),
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

  @Override
  public String toString() {
    return "ErrorContext{Spec: " + (spec == null ? "null" : spec.getDisplayName()) +
      ", Feature: " + (feature == null ? "null" : feature.getDisplayName()) +
      ", Iteration: " + (iteration == null ? "null" : iteration.getDisplayName()) +
      ", Block: " + (block == null ? "null" : (block.getKind() + " " + block.getTexts()))
      + "}";
  }
}
