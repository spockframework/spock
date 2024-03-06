/*
 * Copyright 2012 the original author or authors.
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

import org.spockframework.util.*;

/**
 * A response strategy that returns zero, false, or null, depending on the method's return type.
 */
@Beta
public class ZeroOrNullResponse implements IDefaultResponse {
  public static final ZeroOrNullResponse INSTANCE = new ZeroOrNullResponse();

  private ZeroOrNullResponse() {}

  @Override
  public Object respond(IMockInvocation invocation) {
    IMockInteraction interaction = DefaultJavaLangObjectInteractions.INSTANCE.match(invocation);
    if (interaction != null) return interaction.accept(invocation).get();

    Class<?> returnType = invocation.getMethod().getReturnType();
    return ReflectionUtil.getDefaultValue(returnType);
  }
}
