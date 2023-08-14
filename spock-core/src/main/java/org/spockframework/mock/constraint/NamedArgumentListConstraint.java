/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.mock.constraint;

import org.spockframework.mock.*;
import org.spockframework.util.*;

import java.util.*;

/**
 *
 * @author Peter Niederwieser
 */
public class NamedArgumentListConstraint implements IInvocationConstraint {
  // NOTE: why not use List<String> here?
  private final List<Object> argNames;
  private final List<IArgumentConstraint> argConstraints;
  private final boolean isMixed;

  public NamedArgumentListConstraint(List<Object> argNames, List<IArgumentConstraint> argConstraints, boolean isMixed) {
    this.isMixed = isMixed;
    Assert.that(argNames.size() == argConstraints.size());
    this.argNames = argNames;
    this.argConstraints = argConstraints;
  }

  @Override
  public boolean isDeclarationEqualTo(IInvocationConstraint other) {
    if (other instanceof NamedArgumentListConstraint) {
      NamedArgumentListConstraint o = (NamedArgumentListConstraint) other;
      return Objects.equals(this.argNames, o.argNames) &&
        CollectionUtil.areListsEqual(this.argConstraints, o.argConstraints, IArgumentConstraint::isDeclarationEqualTo) &&
        this.isMixed == o.isMixed;
    }
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean isSatisfiedBy(IMockInvocation invocation) {
    List<Object> args = invocation.getArguments();

    if ((args.size() == 1 || isMixed) && args.get(0) instanceof Map) {
      return matchesArgMap(new HashMap((Map) args.get(0)));
    }

    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public String describeMismatch(IMockInvocation invocation) {
    List<Object> args = invocation.getArguments();
    if ((args.size() == 1 || isMixed) && args.get(0) instanceof Map)
      return describeMismatchArgMap(new HashMap((Map)args.get(0)));

    return "<named arguments expected>";
  }

  private String describeMismatchArgMap(Map argMap) {

    StringBuilder result = new StringBuilder("One or more arguments(s) didn't match:\n");
    for (int i = 0; i < argConstraints.size(); i++) {
      Object name = argNames.get(i);
      IArgumentConstraint matcher = argConstraints.get(i);
      if (!argMap.containsKey(name)) {
        result.append("[").append(name).append("]: <missing>\n");
      } else {
        Object arg = argMap.remove(name);
        if (!matcher.isSatisfiedBy(arg)) {
          int prev = result.length();
          result.append("[").append(name).append("]: ");
          int indent = result.length() - prev;
          result.append(TextUtil.changeSubsequentIndent(matcher.describeMismatch(arg), indent, "\n"))
            .append("\n");
        }
      }
    }
    for (Object arg : argMap.keySet()) {
      result.append("[").append(arg).append("]: <unexpected argument>\n");
    }
    return result.toString();
  }

  private boolean matchesArgMap(Map argMap) {
    if (argConstraints.size() != argMap.size()) {
      return false;
    }
    for (int i = 0; i < argConstraints.size(); i++) {
      Object name = argNames.get(i);
      IArgumentConstraint matcher = argConstraints.get(i);
      if (!argMap.containsKey(name) || !matcher.isSatisfiedBy(argMap.remove(name)))
        return false;
    }

    return true;
  }
}
