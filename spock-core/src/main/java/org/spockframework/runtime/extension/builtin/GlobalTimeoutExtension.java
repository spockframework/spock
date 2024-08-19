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

package org.spockframework.runtime.extension.builtin;

import spock.lang.Timeout;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.Assert;
import org.spockframework.util.Beta;
import org.spockframework.util.Nullable;

/**
 * Applies a timeout to every feature and fixture depending on the configuration.
 *
 * @author Leonard Brünings
 * @since 2.4
 */
@Beta
public class GlobalTimeoutExtension implements IGlobalExtension {
  private final TimeoutConfiguration timeoutConfiguration;
  private @Nullable TimeoutInterceptor timeoutInterceptor;

  public GlobalTimeoutExtension(TimeoutConfiguration timeoutConfiguration) {
    // TimeoutConfiguration is mutable and will be configured after the extension is created,
    // so we need to store a reference to it and delay until the extension is started to create the interceptor.
    this.timeoutConfiguration = Assert.notNull(timeoutConfiguration, "timeoutConfiguration is null");
  }

  @Override
  public void start() {
    timeoutInterceptor = timeoutConfiguration.globalTimeout == null
        ? null
        : new TimeoutInterceptor(timeoutConfiguration.globalTimeout, timeoutConfiguration);
  }

  @Override
  public void visitSpec(SpecInfo spec) {
    if (timeoutInterceptor == null
        || spec.getReflection().isAnnotationPresent(Timeout.class)) {
      return;
    }

    Stream<MethodInfo> features = spec.getAllFeatures().stream()
        .map(FeatureInfo::getFeatureMethod);
    Stream<MethodInfo> fixtures = timeoutConfiguration.applyGlobalTimeoutToFixtures
        ? StreamSupport.stream(spec.getAllFixtureMethods().spliterator(), false)
        : Stream.empty();

    Stream.concat(features, fixtures)
        .filter(method -> !method.getReflection().isAnnotationPresent(Timeout.class))
        .forEach(method -> method.addInterceptor(timeoutInterceptor));
  }
}
