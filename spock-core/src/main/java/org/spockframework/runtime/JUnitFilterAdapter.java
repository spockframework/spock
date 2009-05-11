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

import org.junit.runner.manipulation.Filter;
import org.junit.runner.Description;

import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.FeatureInfo;

/**
 * Adapts an org.junit.runner.manipulation.Filter to an IMethodInfoFilter.
 *
 * @author Peter Niederwieser
 */
public class JUnitFilterAdapter implements IFeatureFilter {
  private final Filter filter;

  public JUnitFilterAdapter(Filter filter) {
    this.filter = filter;
  }

  public boolean matches(FeatureInfo method) {
    return filter.shouldRun((Description)method.getMetadata());
  }
}
