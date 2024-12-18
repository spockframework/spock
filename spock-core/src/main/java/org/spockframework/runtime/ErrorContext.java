/*
 *  Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
  public SpecInfo getSpec() {
    return spec;
  }

  @Override
  public FeatureInfo getFeature() {
    return feature;
  }

  @Override
  public IterationInfo getIteration() {
    return iteration;
  }

  @Override
  public BlockInfo getBlock() {
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
