/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime;

import org.spockframework.runtime.model.*;

/**
 *
 * @author Peter Niederwieser
 */
public interface IRunListener {
  /**
   * Called before each spec of a spec run.
   */
  void beforeSpec(SpecInfo spec);

  /**
   * Called before each feature of a spec.
   */
  void beforeFeature(FeatureInfo feature);

  /**
   * Called before each iteration of a data-driven feature.
   * All data values have been computed successfully
   * at this point. Not called for non-data-driven features.
   */
  void beforeIteration(IterationInfo iteration);

  /**
   * Called after each iteration of a data-driven feature.
   * Not called for non-data-driven features.
   */
  void afterIteration(IterationInfo iteration);

  /**
   * Called after each feature of a spec.
   */
  void afterFeature(FeatureInfo feature);

  /**
   * Called after each spec of a spec run.
   */
  void afterSpec(SpecInfo spec);

  /**
   * Called for every error that occurs during a spec run.
   * May be called multiple times for the same method, for example if both
   * an expect-block and a cleanup-block of a feature method fail.
   */
  void error(ErrorInfo error);

  // IDEA: might be able to remove remaining two methods and
  // call before/after instead, since skip should be set on SpecInfo/FeatureInfo anyway
  
  /**
   * Called if a spec is skipped, for example because it is marked
   * with @Ignore.
   */
  void specSkipped(SpecInfo spec);

  /**
   * Called if a feature is skipped, for example because it is marked
   * with @Ignore.
   */
  void featureSkipped(FeatureInfo feature);
}
