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
import org.spockframework.runtime.*;
import org.spockframework.util.CollectionUtil;

/**
 *
 * @author Peter Niederwieser
 */
public class EqualArgumentConstraint implements IArgumentConstraint {
  private final Object expected;

  public EqualArgumentConstraint(Object expected) {
    this.expected = expected;
  }

  @Override
  public boolean isDeclarationEqualTo(IArgumentConstraint other) {
    if (other instanceof EqualArgumentConstraint) {
      EqualArgumentConstraint o = (EqualArgumentConstraint) other;
      return this.expected == o.expected;
    }
    return false;
  }

  @Override
  public boolean isSatisfiedBy(Object arg) {
    if (HamcrestFacade.isMatcher(expected)) {
      return HamcrestFacade.matches(expected, arg);
    }

    return GroovyRuntimeUtil.equals(arg, expected);
  }

  @Override
  public String describeMismatch(Object arg) {
    if (HamcrestFacade.isMatcher(expected)) {
      return HamcrestFacade.getFailureDescription(expected, arg, null);
    }
    Condition condition = new Condition(CollectionUtil.listOf(arg, expected, false),
      "argument == expected", null, null,
      null, null);
    return condition.getRendering();
  }
}
