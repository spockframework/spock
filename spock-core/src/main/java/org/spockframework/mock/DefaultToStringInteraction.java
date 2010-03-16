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

public class DefaultToStringInteraction extends DefaultInteraction {
  private static final Method toStringMethod;

  static {
    try {
      toStringMethod = Object.class.getMethod("toString");
    } catch (NoSuchMethodException e) {
      throw new InternalSpockError(e);
    }
  }

  public String getText() {
    return "default Object.toString() interaction";
  }

  public boolean matches(IMockInvocation invocation) {
    return toStringMethod.equals(invocation.getMethod());
  }

  public Object accept(IMockInvocation invocation) {
    acceptedCount++;
    return "Mock with id " + System.identityHashCode(invocation.getMockObject()); // TODO: improve 
  }
}
