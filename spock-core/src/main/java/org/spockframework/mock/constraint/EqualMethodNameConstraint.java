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
import org.spockframework.runtime.Condition;
import org.spockframework.util.CollectionUtil;

import java.util.Objects;

/**
 *
 * @author Peter Niederwieser
 */
public class EqualMethodNameConstraint implements IInvocationConstraint {
  private final String methodName;

  public EqualMethodNameConstraint(String methodName) {
    this.methodName = methodName;
  }

  @Override
  public boolean isDeclarationEqualTo(IInvocationConstraint other) {
    if (other instanceof EqualMethodNameConstraint) {
      EqualMethodNameConstraint o = (EqualMethodNameConstraint) other;
      return Objects.equals(this.methodName, o.methodName);
    }
    return false;
  }

  @Override
  public boolean isSatisfiedBy(IMockInvocation invocation) {
    return invocation.getMethod().getName().equals(methodName);
  }

  @Override
  public String describeMismatch(IMockInvocation invocation) {
    Condition condition = new Condition(CollectionUtil.listOf(invocation.getMethod().getName(), methodName, false),
      String.format("methodName == \"%s\"", methodName), null, null,
      null, null);
    return condition.getRendering();
  }
}
