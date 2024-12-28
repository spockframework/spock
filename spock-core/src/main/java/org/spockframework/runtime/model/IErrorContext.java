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
  SpecInfo getSpec();

  @Nullable
  FeatureInfo getFeature();

  @Nullable
  IterationInfo getIteration();

  @Nullable
  BlockInfo getBlock();
}
