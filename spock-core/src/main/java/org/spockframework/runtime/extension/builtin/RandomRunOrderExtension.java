/*
 * Copyright 2023 the original author or authors.
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

package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;
import spock.config.RunnerConfiguration;

import java.util.Random;

public class RandomRunOrderExtension implements IGlobalExtension {
  private static final Random RANDOM = new Random();

  private final RunnerConfiguration config;

  public RandomRunOrderExtension(RunnerConfiguration config) {
    this.config = config;
  }

  @Override
  public void visitSpec(SpecInfo spec) {
    if (config.randomizeSpecRunOrder)
      spec.setExecutionOrder(RANDOM.nextInt());
    if (config.randomizeFeatureRunOrder) {
      for (FeatureInfo featureInfo : spec.getAllFeatures())
        featureInfo.setExecutionOrder(RANDOM.nextInt());
    }
  }
}
