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

package org.spockframework.mock;

import java.util.List;

/**
 *
 * @author Peter Niederwieser
 */
public class PositionalArgumentListConstraint implements IInvocationConstraint {
  private final List<IArgumentConstraint> argConstraints;

  public PositionalArgumentListConstraint(List<IArgumentConstraint> argConstraints) {
    this.argConstraints = argConstraints;
  }

  public boolean isSatisfiedBy(IMockInvocation invocation) {
    List<Object> args = invocation.getArguments();
    if (args.size() != argConstraints.size()) return false;
    for (int i = 0; i < args.size(); i++)
      if (!argConstraints.get(i).isSatisfiedBy(args.get(i))) return false;
    return true;
  }
}
