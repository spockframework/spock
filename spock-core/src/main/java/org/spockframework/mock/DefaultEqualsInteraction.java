/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spockframework.mock;

import java.lang.reflect.Method;

import org.spockframework.util.InternalSpockError;

public class DefaultEqualsInteraction extends DefaultInteraction {
  private static final Method equalsMethod;

  static {
    try {
      equalsMethod = Object.class.getMethod("equals", Object.class);
    } catch (NoSuchMethodException e) {
      throw new InternalSpockError(e);
    }
  }

  public String getText() {
    return "default Object.equals() interaction";
  }

  public boolean matches(IMockInvocation invocation) {
    return equalsMethod.equals(invocation.getMethod());
  }

  public Object accept(IMockInvocation invocation) {
    acceptedCount++;
    return invocation.getMockObject() == invocation.getArguments().get(0);
  }
}
