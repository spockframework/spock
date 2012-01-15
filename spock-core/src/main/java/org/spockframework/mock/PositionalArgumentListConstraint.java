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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.spockframework.util.*;

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

    if (areConstraintsSatisfiedBy(args)) return true;
    if (!canBeCalledWithVarArgSyntax(invocation.getMethod())) return false;
    return areConstraintsSatisfiedBy(expandVarArgs(args));
  }

  /**
   * Tells if the given method can be called with vararg syntax from Groovy(!).
   * If yes, we also support vararg syntax in the interaction definition.
   */
  private boolean canBeCalledWithVarArgSyntax(Method method) {
    Class<?>[] paramTypes = method.getParameterTypes();
    return paramTypes.length > 0 && paramTypes[paramTypes.length - 1].isArray();
  }

  private List<Object> expandVarArgs(List<Object> args) {
    List<Object> expanded = new ArrayList<Object>();
    expanded.addAll(args.subList(0, args.size() - 1));
    expanded.addAll(Arrays.asList((Object[]) CollectionUtil.getLastElement(args)));
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
