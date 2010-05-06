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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.spockframework.util.Assert;
import org.spockframework.util.Util;

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

    if (args.size() == 0) return argConstraints.size() == 0;
    
    int sizeDiff = argConstraints.size() - args.size();

    if (sizeDiff < -1) return false;

    if (!areConstraintsSatisfied(argConstraints.subList(0, args.size() - 1), args)) return false;

    if (sizeDiff == 0 && Util.getLastElement(argConstraints).isSatisfiedBy(Util.getLastElement(args))) return true;

    if (!canBeCalledWithVarArgSyntax(invocation.getMethod())) return false;

    int varArgSize = Array.getLength(Util.getLastElement(args));

    if (argConstraints.size() != args.size() - 1 + varArgSize) return false;

    return areConstraintsSatisfied(argConstraints.subList(args.size() - 1, argConstraints.size()),
        Arrays.asList((Object[]) Util.getLastElement(args)));
  }

  /**
   * Tells if the given method can be called with vararg syntax from Groovy(!).
   * If yes, we also support vararg syntax in the interaction definition.
   */
  private boolean canBeCalledWithVarArgSyntax(Method method) {
    Class<?>[] paramTypes = method.getParameterTypes();
    return paramTypes.length > 0 && paramTypes[paramTypes.length - 1].isArray();
  }

  private boolean areConstraintsSatisfied(List<IArgumentConstraint> argConstraints, List<Object> args) {
    Assert.that(argConstraints.size() <= args.size());
    
    for (int i = 0; i < argConstraints.size(); i++)
      if (!argConstraints.get(i).isSatisfiedBy(args.get(i))) return false;
    return true;
  }
}
