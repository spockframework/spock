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

public class MasterRunListener implements IRunListener {
  private final SpecInfo spec;

  public MasterRunListener(SpecInfo spec) {
    this.spec = spec;
  }

  public void beforeSpec(SpecInfo spec) {
    for (IRunListener listener : spec.getListeners()) {
      listener.beforeSpec(spec);
    }
  }

  public void block(String type, String description) {
    for (IRunListener listener : spec.getListeners()) {
      listener.block(type, description);
    }
  }

  public void beforeFeature(FeatureInfo feature) {
    for (IRunListener listener : spec.getListeners()) {
      listener.beforeFeature(feature);
    }
  }

  public void beforeIteration(IterationInfo iteration) {
    for (IRunListener listener : spec.getListeners()) {
      listener.beforeIteration(iteration);
    }
  }

  public void afterIteration(IterationInfo iteration) {
    for (IRunListener listener : spec.getListeners()) {
      listener.afterIteration(iteration);
    }
  }

  public void afterFeature(FeatureInfo feature) {
    for (IRunListener listener : spec.getListeners()) {
      listener.afterFeature(feature);
    }
  }

  public void afterSpec(SpecInfo spec) {
    for (IRunListener listener : spec.getListeners()) {
      listener.afterSpec(spec);
    }
  }

  public void error(ErrorInfo error) {
    for (IRunListener listener : spec.getListeners()) {
      listener.error(error);
    }
  }

  public void specSkipped(SpecInfo spec) {
    for (IRunListener listener : spec.getListeners()) {
      listener.specSkipped(spec);
    }
  }

  public void featureSkipped(FeatureInfo feature) {
    for (IRunListener listener : spec.getListeners()) {
      listener.featureSkipped(feature);
    }
  }
}
