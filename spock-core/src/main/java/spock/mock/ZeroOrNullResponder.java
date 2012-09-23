/*
 * Copyright 2012 the original author or authors.
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

package spock.mock;

import org.spockframework.mock.EqualsHashCodeToStringInteractions;
import org.spockframework.mock.IMockInteraction;
import org.spockframework.mock.IMockInvocation;
import org.spockframework.util.ReflectionUtil;
import spock.lang.Beta;

@Beta
public class ZeroOrNullResponder implements IMockInvocationResponder {
  public static final ZeroOrNullResponder INSTANCE = new ZeroOrNullResponder();

  private ZeroOrNullResponder() {}

  public Object respond(IMockInvocation invocation) {
    IMockInteraction interaction = EqualsHashCodeToStringInteractions.INSTANCE.match(invocation);
    if (interaction != null) return interaction.accept(invocation);

    Class<?> returnType = invocation.getMethod().getReturnType();
    return ReflectionUtil.getDefaultValue(returnType);
  }
}
