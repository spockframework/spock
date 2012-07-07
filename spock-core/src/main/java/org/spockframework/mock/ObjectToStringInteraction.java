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

public class ObjectToStringInteraction extends DefaultInteraction {
  public static final ObjectToStringInteraction INSTANCE = new ObjectToStringInteraction();
  
  private ObjectToStringInteraction() {}

  public String getText() {
    return "default toString() interaction";
  }

  public boolean matches(IMockInvocation invocation) {
    return invocation.getMethod().getName().equals("toString")
        && invocation.getMethod().getParameterTypes().isEmpty();
  }

  public Object accept(IMockInvocation invocation) {
    StringBuilder builder = new StringBuilder();
    builder.append("Mock for type '");
    builder.append(invocation.getMockObject().getType().getSimpleName());
    builder.append("'");

    String name = invocation.getMockObject().getName();
    if (name != null) {
      builder.append(" named '");
      builder.append(name);
      builder.append("'");
    }

    return builder.toString();
  }
}
