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

import org.spockframework.mock.IArgumentConstraint;
import org.spockframework.runtime.Condition;
import org.spockframework.util.CollectionUtil;

import java.util.Objects;

/**
 *
 * @author Peter Niederwieser
 */
public class TypeArgumentConstraint implements IArgumentConstraint {
  private final Class<?> type;
  private final IArgumentConstraint constraint;

  public TypeArgumentConstraint(Class<?> type, IArgumentConstraint constraint) {
    this.type = type;
    this.constraint = constraint;
  }

  @Override
  public boolean isDeclarationEqualTo(IArgumentConstraint other) {
    if (other instanceof TypeArgumentConstraint) {
      TypeArgumentConstraint o = (TypeArgumentConstraint) other;
      return Objects.equals(this.type, o.type) &&
        this.constraint.isDeclarationEqualTo(o.constraint);
    }
    return false;
  }

  @Override
  public boolean isSatisfiedBy(Object argument) {
    return type.isInstance(argument) && constraint.isSatisfiedBy(argument);
  }

  @Override
  public String describeMismatch(Object argument) {
    if (type.isInstance(argument)) {
      return constraint.describeMismatch(argument);
    }
    Condition condition = new Condition(CollectionUtil.listOf(  argument, type, false),
      String.format("argument instanceof %s", type.getName()), null, null,
      null, null);
    return condition.getRendering();
  }
}
