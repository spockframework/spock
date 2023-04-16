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
import spock.lang.Order;

import java.util.Collection;

/**
 * Spec orderer for usef-defined, manual specification and/or feature method ordering, to be used in connection with
 * {@link Order @Order} annotations. See the Spock user manual for more details.
 */
public class AnnotatationBasedSpecOrderer extends SpecOrderer {
  /**
   * Create an annotation-based spec orderer
   * @see Order
   */
  public AnnotatationBasedSpecOrderer() {
    super(true, true);
  }

  @Override
  protected void orderSpecs(Collection<SpecInfo> specs) {
    for (SpecInfo spec : specs) {
      Order orderAnnotation = spec.getAnnotation(Order.class);
      spec.setExecutionOrder(orderAnnotation == null ? 0 : orderAnnotation.value());
    }
  }

  @Override
  protected void orderFeatures(Collection<FeatureInfo> features) {
    for (FeatureInfo feature : features) {
      Order orderAnnotation = feature.getFeatureMethod().getAnnotation(Order.class);
      feature.setExecutionOrder(orderAnnotation == null ? 0 : orderAnnotation.value());
    }
  }
}
