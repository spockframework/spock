/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.runtime;

import org.spockframework.runtime.model.*;

public class MasterRunListener implements IRunListener {
  private final SpecInfo spec;

  public MasterRunListener(SpecInfo spec) {
    this.spec = spec;
  }

  @Override
  public void beforeSpec(SpecInfo spec) {
    for (IRunListener listener : spec.getListeners()) {
      listener.beforeSpec(spec);
    }
  }

  @Override
  public void beforeFeature(FeatureInfo feature) {
    for (IRunListener listener : spec.getListeners()) {
      listener.beforeFeature(feature);
    }
  }

  @Override
  public void beforeIteration(IterationInfo iteration) {
    for (IRunListener listener : spec.getListeners()) {
      listener.beforeIteration(iteration);
    }
  }

  @Override
  public void afterIteration(IterationInfo iteration) {
    for (IRunListener listener : spec.getListeners()) {
      listener.afterIteration(iteration);
    }
  }

  @Override
  public void afterFeature(FeatureInfo feature) {
    for (IRunListener listener : spec.getListeners()) {
      listener.afterFeature(feature);
    }
  }

  @Override
  public void afterSpec(SpecInfo spec) {
    for (IRunListener listener : spec.getListeners()) {
      listener.afterSpec(spec);
    }
  }

  @Override
  public void error(ErrorInfo error) {
    for (IRunListener listener : spec.getListeners()) {
      listener.error(error);
    }
  }

  @Override
  public void specSkipped(SpecInfo spec) {
    for (IRunListener listener : spec.getListeners()) {
      listener.specSkipped(spec);
    }
  }

  @Override
  public void featureSkipped(FeatureInfo feature) {
    for (IRunListener listener : spec.getListeners()) {
      listener.featureSkipped(feature);
    }
  }
}
