/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.mock.constraint;

import org.spockframework.mock.IMockInvocation;
import org.spockframework.runtime.Condition;
import org.spockframework.util.CollectionUtil;

/**
 *
 * @author Peter Niederwieser
 */
public class EqualPropertyNameConstraint extends PropertyNameConstraint {
  private final String propertyName;

  public EqualPropertyNameConstraint(String propertyName) {
    this.propertyName = propertyName;
  }

  @Override
  public boolean isSatisfiedBy(IMockInvocation invocation) {
    return propertyName.equals(getPropertyName(invocation));
  }

  @Override
  public String describeMismatch(IMockInvocation invocation) {
    Condition condition = new Condition(CollectionUtil.listOf(getPropertyName(invocation), propertyName, false),
      String.format("propertyName == \"%s\"", propertyName), null, null,
      null, null);
    return condition.getRendering();
  }
}
