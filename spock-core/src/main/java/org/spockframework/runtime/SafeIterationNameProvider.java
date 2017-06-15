/*
 * Copyright 2012 the original author or authors.
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

public class SafeIterationNameProvider implements NameProvider<IterationInfo> {
  private final NameProvider<IterationInfo> delegate;
  private int iterationCount;

  public SafeIterationNameProvider(NameProvider<IterationInfo> delegate) {
    this.delegate = delegate;
  }

  @Override
  public String getName(IterationInfo iteration) {
    String safeName = iteration.getParent().isReportIterations() ?
        String.format("%s[%d]", iteration.getParent().getName(), iterationCount++) : iteration.getParent().getName();

    if (delegate == null) return safeName;

    try {
      String name = delegate.getName(iteration);
      if (name != null) return name;
      return safeName;
    } catch (Exception e) {
      return safeName;
    }
  }
}
