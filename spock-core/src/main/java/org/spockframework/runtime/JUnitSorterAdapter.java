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

import org.spockframework.runtime.model.FeatureInfo;

import org.junit.runner.manipulation.Sorter;

/**
 * Adapts an org.junit.runner.manipulation.Sorter to a IMethodInfoSorter.
 * @author Peter Niederwieser
 */
public class JUnitSorterAdapter implements IFeatureSortOrder {
  private final Sorter sorter;

  public JUnitSorterAdapter(Sorter sorter) {
    this.sorter = sorter;
  }

  @Override
  public int compare(FeatureInfo m1, FeatureInfo m2) {
    return sorter.compare(m1.getDescription(), m2.getDescription());
  }
}
