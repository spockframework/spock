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

import java.util.*;

import org.spockframework.util.Assert;

/**
 * A ...
 *
 * @author Peter Niederwieser
 */
public class NamedArgumentListConstraint implements IInvocationConstraint {
  // TODO: why not use List<String> here?
  private final List<Object> argNames; // name == "_" -> unnamed matcher (considered matching if it matches any arg)
  private final List<IArgumentConstraint> argConstraints;

  public NamedArgumentListConstraint(List<Object> argNames, List<IArgumentConstraint> argConstraints) {
    Assert.that(argNames.size() == argConstraints.size());
    this.argNames = argNames;
    this.argConstraints = argConstraints;
  }

  @SuppressWarnings("unchecked")
  public boolean isSatisfiedBy(IMockInvocation invocation) {
    List<Object> args = invocation.getArguments();

    if (args.size() == 1 && args.get(0) instanceof Map) // NOTE: not sure what type params to use for map
      return matchesArgMap(new HashMap((Map)args.get(0)));

    return matchesArgList(new ArrayList<Object>(args));
  }

  private boolean matchesArgList(List<Object> args) {
    nextMatcher:
    for (int i = 0; i < argConstraints.size(); i++) {
      Object name = argNames.get(i);
      if (!name.equals("_")) return false; // cannot possibly match because we have unnamed args
      IArgumentConstraint matcher = argConstraints.get(i);

      Iterator<Object> argIter = args.iterator();
      while (argIter.hasNext())
        if (matcher.isSatisfiedBy(argIter.next())) {
          argIter.remove();
          continue nextMatcher;
        }

      return false;
    }

    return true;
  }

  private boolean matchesArgMap(Map argMap) {
    // first pass: named matchers
    for (int i = 0; i < argConstraints.size(); i++) {
      Object name = argNames.get(i);
      if (name.equals("_")) continue;
      IArgumentConstraint matcher = argConstraints.get(i);
      if (!argMap.containsKey(name) || !matcher.isSatisfiedBy(argMap.remove(name)))
        return false;
    }

    // second pass: unnamed matchers
    nextMatcher:
    for (int i = 0; i < argConstraints.size(); i++) {
      Object name = argNames.get(i);
      if (!name.equals("_")) continue;
      IArgumentConstraint matcher = argConstraints.get(i);

      @SuppressWarnings("unchecked")
      Iterator<Object> argIter = argMap.values().iterator();
      while (argIter.hasNext())
        if (matcher.isSatisfiedBy(argIter.next())) {
          argIter.remove();
          continue nextMatcher;
        }

      return false;
    }

    return true;
  }
}