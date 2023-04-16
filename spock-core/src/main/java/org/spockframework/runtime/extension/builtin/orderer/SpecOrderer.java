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

import org.spockframework.runtime.SpecProcessor;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.util.Collection;

/**
 * Abstract base class for specification and feature method orderers, i.e. workers modifying the corresponding execution
 * order properties of spec-info and feature-info instances.
 *
 * @see DefaultSpecOrderer
 * @see RandomSpecOrderer
 * @see AnnotatationBasedSpecOrderer
 * @see AlphabeticalSpecOrderer
 */
public abstract class SpecOrderer implements SpecProcessor {
  protected final boolean orderSpecs;
  protected final boolean orderFeatures;

  /**
   * Create a spec-orderer with a user-defined operational scope
   *
   * @param orderSpecs    modify specification run order (yes/no)?
   * @param orderFeatures modify feature run order within a specification (yes/no)?
   */
  public SpecOrderer(boolean orderSpecs, boolean orderFeatures) {
    this.orderSpecs = orderSpecs;
    this.orderFeatures = orderFeatures;
  }

  /**
   * Dispatch to {@link #orderSpecs(Collection)} and then to {@link #orderFeatures(Collection)} for each spec-info
   *
   * @param specs spec-info instances to be processed
   */
  @Override
  public void process(Collection<SpecInfo> specs) {
    if (orderSpecs)
      orderSpecs(specs);
    if (!orderFeatures)
      return;
    for (SpecInfo spec : specs)
      orderFeatures(spec.getAllFeatures());
  }

  /**
   * Assign values to specification run orders. Implementors are expected to modify the corresponding object state
   * in place, e.g. like this:
   * <pre>
   * for (SpecInfo spec : specs)
   *   spec.setExecutionOrder(random.nextInt());
   * </pre>
   * Or maybe:
   * <pre>
   * AtomicInteger i = new AtomicInteger();
   * specs.stream()
   *   .sorted(myComparator)
   *   .forEach(specInfo -> specInfo.setExecutionOrder(i.getAndIncrement()));
   * </pre>
   *
   * @param specs spec-info instances to be ordered
   */

  protected abstract void orderSpecs(Collection<SpecInfo> specs);

  /**
   * Assign values to feature run orders. Implementors are expected to modify the corresponding object state
   * in place, e.g. like this:
   * <pre>
   * for (FeatureInfo feature : features)
   *   feature.setExecutionOrder(random.nextInt());
   * </pre>
   * Or maybe:
   * <pre>
   * AtomicInteger i = new AtomicInteger();
   * features.stream()
   *   .sorted(myComparator)
   *   .forEach(featureInfo -> featureInfo.setExecutionOrder(i.getAndIncrement()));
   * </pre>
   *
   * @param features feature-info instances to be ordered
   */
  protected abstract void orderFeatures(Collection<FeatureInfo> features);

  public boolean isOrderSpecs() {
    return orderSpecs;
  }

  public boolean isOrderFeatures() {
    return orderFeatures;
  }
}
