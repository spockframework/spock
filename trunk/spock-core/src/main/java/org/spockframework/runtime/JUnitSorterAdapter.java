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

import org.junit.runner.manipulation.Sorter;
import org.junit.runner.Description;

import org.spockframework.runtime.model.MethodInfo;

/**
 * Adapts an org.junit.runner.manipulation.Sorter to a IMethodInfoSorter.
 * @author Peter Niederwieser
 */
public class JUnitSorterAdapter implements IMethodInfoSortOrder {
  private final Sorter sorter;

  public JUnitSorterAdapter(Sorter sorter) {
    this.sorter = sorter;
  }

  public int compare(MethodInfo m1, MethodInfo m2) {
    return sorter.compare((Description) m1.getMetadata(), (Description) m2.getMetadata());
  }
}
