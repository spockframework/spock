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

public class AbstractRunListener implements IRunListener {
  public void beforeSpec(SpecInfo spec) {}
  public void beforeFeature(FeatureInfo feature) {}
  public void beforeIteration(IterationInfo iteration) {}
  public void afterIteration(IterationInfo iteration) {}
  public void afterFeature(FeatureInfo feature) {}
  public void afterSpec(SpecInfo spec) {}
  public void error(ErrorInfo error) {}
  public void specSkipped(SpecInfo spec) {}
  public void featureSkipped(FeatureInfo feature) {}
  public void block(String type, String description) {}
}
