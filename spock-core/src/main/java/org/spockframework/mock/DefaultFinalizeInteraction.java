/*
 * Copyright 2013 the original author or authors.
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
package org.spockframework.mock;

import java.util.function.Supplier;

public class DefaultFinalizeInteraction extends DefaultInteraction {
  public static final DefaultFinalizeInteraction INSTANCE = new DefaultFinalizeInteraction();

  private DefaultFinalizeInteraction() {}

  @Override
  public String getText() {
    return "Object.finalize() interaction";
  }

  @Override
  public boolean matches(IMockInvocation invocation) {
    return "finalize".equals(invocation.getMethod().getName())
        && invocation.getMethod().getParameterTypes().isEmpty();

  }

  @Override
  public Supplier<Object> accept(IMockInvocation invocation) {
    return () -> null;
  }
}
