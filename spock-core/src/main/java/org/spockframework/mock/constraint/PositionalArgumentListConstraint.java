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

import java.lang.reflect.Array;
import java.util.*;

/**
 *
 * @author Peter Niederwieser
 */
public class PositionalArgumentListConstraint implements IInvocationConstraint {
  private final List<IArgumentConstraint> argConstraints;

  public PositionalArgumentListConstraint(List<IArgumentConstraint> argConstraints, boolean isMixed) {
    this.argConstraints = argConstraints;
    if (isMixed) {
      // The first Argument is a Map and will be handled by another constraint, so just use wildcard here
      argConstraints.add(WildcardArgumentConstraint.INSTANCE);
    }
  }

  @Override
  public boolean isDeclarationEqualTo(IInvocationConstraint other) {
    if (other instanceof PositionalArgumentListConstraint) {
      PositionalArgumentListConstraint o = (PositionalArgumentListConstraint) other;
      return CollectionUtil.areListsEqual(this.argConstraints, o.argConstraints, IArgumentConstraint::isDeclarationEqualTo);
    }
    return false;
  }

  @Override
  public boolean isSatisfiedBy(IMockInvocation invocation) {
    List<Object> args = invocation.getArguments();

    if (areConstraintsSatisfiedBy(args)) return true;
    if (!hasExpandableVarArgs(invocation.getMethod(), args)) return false;
    return areConstraintsSatisfiedBy(expandVarArgs(args));
  }

  @Override
  public String describeMismatch(IMockInvocation invocation) {
    List<Object> args = invocation.getArguments();

    if (argConstraints.isEmpty()) return "<no args expected>";
    int constraintsToArgs = argConstraints.size() - args.size();
    if (constraintsToArgs > 0) return "<too few arguments>";
    if (constraintsToArgs < 0) return "<too many arguments>";

    StringBuilder result = new StringBuilder("One or more arguments(s) didn't match:\n");
    for (int i = 0; i < argConstraints.size(); i++) {
      if (i > 0) {
        result.append("\n");
      }
      result.append(i).append(": ");
      IArgumentConstraint constraint = argConstraints.get(i);
      if (constraint.isSatisfiedBy(args.get(i))) {
        result.append("<matches>");
      } else {
        result.append(TextUtil.changeSubsequentIndent(constraint.describeMismatch(args.get(i)), 3 + (i / 10), "\n"));
      }
    }

    return result.toString();

  }

  /**
   * Tells if the given method call has expandable varargs. Note that Groovy
   * supports vararg syntax for all methods whose last parameter is of array type.
   */
  private boolean hasExpandableVarArgs(IMockMethod method, List<Object> args) {
    List<Class<?>> paramTypes = method.getParameterTypes();
    return !paramTypes.isEmpty()
        && CollectionUtil.getLastElement(paramTypes).isArray()
        && CollectionUtil.getLastElement(args) != null;
  }

  private List<Object> expandVarArgs(List<Object> args) {
    List<Object> expanded = new ArrayList<>(args.subList(0, args.size() - 1));

    Object varArgs = CollectionUtil.getLastElement(args);
    int length = Array.getLength(varArgs);
    for (int i = 0; i < length; i++) {
      expanded.add(Array.get(varArgs, i));
    }

    return expanded;
  }

  private boolean areConstraintsSatisfiedBy(List<Object> args) {
    if (argConstraints.isEmpty()) return args.isEmpty();
    if (argConstraints.size() - args.size() > 1) return false;

    for (int i = 0; i < argConstraints.size() - 1; i++) {
      if (!argConstraints.get(i).isSatisfiedBy(args.get(i))) return false;
    }

    IArgumentConstraint lastConstraint = CollectionUtil.getLastElement(argConstraints);
    if (lastConstraint instanceof SpreadWildcardArgumentConstraint) return true;
    return argConstraints.size() == args.size() && lastConstraint.isSatisfiedBy(CollectionUtil.getLastElement(args));
  }
}
