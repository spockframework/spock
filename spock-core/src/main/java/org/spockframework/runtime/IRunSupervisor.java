/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
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
public interface IRunSupervisor {
  void beforeSpec(SpecInfo spec);
  void beforeFeature(FeatureInfo feature);

  /*
   * Called before the next iteration of a parameterized feature is
   * run. All parameterization values have been computed successfully
   * at this point. Not called for non-parameterized features.
   */
  void beforeIteration(FeatureInfo currentFeature, IterationInfo iteration);

  void afterIteration(FeatureInfo currentFeature, IterationInfo iteration);
  void noIterationFound(FeatureInfo currentFeature);
  void afterFeature(FeatureInfo feature);
  void afterSpec(SpecInfo spec);

  void error(MultipleInvokeException multipleInvokeException);

  void error(InvokeException invokeException);

  void error(FeatureInfo currentFeature, IterationInfo currentIteration, ErrorInfo error);

  void specSkipped(SpecInfo spec);
  void featureSkipped(FeatureInfo feature);
}
