/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.mock;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import org.spockframework.util.TextUtil;

/**
 * Indicates that a required interaction has matched too many invocations.
 * 
 * @author Peter Niederwieser
 */
public class TooManyInvocationsError extends InteractionNotSatisfiedError {
  private final IMockInteraction interaction;
  private final IMockInvocation invocation;

  public TooManyInvocationsError(IMockInteraction interaction, IMockInvocation invocation) {
    this.interaction = interaction;
    this.invocation = invocation;
  }

  public IMockInteraction getInteraction() {
    return interaction;
  }

  public IMockInvocation getInvocation() {
    return invocation;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Too many invocations for:\n\n");
    int numInvoked = interaction.getAcceptedCount() + 1;
    builder.append(String.format("%s   (%d %s)\n\n",
        interaction, numInvoked, numInvoked == 1 ? "invocation" : "invocations"));
    builder.append(String.format("Last invocation: %s.%s(%s)\n", invocation.getMockObjectName(),
        invocation.getMethod().getName(), TextUtil.concat(toString(invocation.getArguments()), ", ")));
    return builder.toString();
  }

  private List<String> toString(List<Object> objects) {
    List<String> result = new ArrayList<String>();
    for (Object obj : objects) result.add(DefaultGroovyMethods.inspect(obj));
    return result;
  }
}
