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

package org.spockframework.runtime.extension.builtin.orderer;

import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.util.Collection;

/**
 * No-op spec orderer, used as a default if no other orderer is configured
 */
public class DefaultSpecOrderer extends SpecOrderer {
  /**
   * Create a no-op spec orderer
   */
  public DefaultSpecOrderer() {
    super(false, false);
  }

  @Override
  protected void orderSpecs(Collection<SpecInfo> specs) { }

  @Override
  protected void orderFeatures(Collection<FeatureInfo> features) { }
}
