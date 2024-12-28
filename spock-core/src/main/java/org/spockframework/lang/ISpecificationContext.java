/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.lang;

import org.spockframework.mock.IThreadAwareMockController;
import org.spockframework.runtime.extension.IStoreProvider;
import org.spockframework.runtime.model.BlockInfo;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.Beta;
import org.spockframework.mock.IMockController;
import org.spockframework.runtime.model.IterationInfo;
import org.spockframework.util.Nullable;

@Beta
public interface ISpecificationContext extends IStoreProvider {
  @Nullable
  SpecInfo getCurrentSpec();

  FeatureInfo getCurrentFeature();

  IterationInfo getCurrentIteration();

  @Nullable
  BlockInfo getCurrentBlock();

  @Nullable
  Throwable getThrownException();

  IMockController getMockController();

  IThreadAwareMockController getThreadAwareMockController();
}
