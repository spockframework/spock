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

package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.*;
import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.model.*;
import spock.config.RunnerConfiguration;

import java.io.IOException;

/**
 * Inspired from JUnit's MaxCore.
 */
@SuppressWarnings("UnusedDeclaration")
public class OptimizeRunOrderExtension implements IGlobalExtension {
  private final RunnerConfiguration configuration;

  public OptimizeRunOrderExtension(RunnerConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public void visitSpec(SpecInfo spec) {
    if (!configuration.optimizeRunOrder) return;

    final SpecRunHistory history = new SpecRunHistory(spec.getReflection().getName());
    safeLoadFromDisk(history);
    history.sortFeatures(spec);

    spec.addListener(new AbstractRunListener() {
      long specStarted;
      long featureStarted;
      boolean errorOccurred;

      @Override
      public void beforeSpec(SpecInfo spec) {
        specStarted = System.nanoTime();
      }

      @Override
      public void beforeFeature(FeatureInfo feature) {
        featureStarted = System.nanoTime();
        errorOccurred = false;
      }

      @Override
      public void afterFeature(FeatureInfo feature) {
        history.collectFeatureData(feature, System.nanoTime() - featureStarted, errorOccurred);
      }

      @Override
      public void error(ErrorInfo error) {
        errorOccurred = true;
      }

      @Override
      public void afterSpec(SpecInfo spec) {
        history.collectSpecData(spec, System.nanoTime() - specStarted);
        safeSaveToDisk(history);
      }
    });
  }

  private void safeLoadFromDisk(SpecRunHistory history) {
    try {
      history.loadFromDisk();
    } catch (IOException ignored) {}
  }

  private void safeSaveToDisk(SpecRunHistory history) {
    try {
      history.saveToDisk();
    } catch (IOException ignored) {}
  }
}
