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

import java.util.List;

import org.spockframework.util.Assert;

/**
 * Indicates that one or more required interactions have matched too few invocations.
 *
 * @author Peter Niederwieser
 */
public class TooFewInvocationsError extends InteractionNotSatisfiedError {
  private final List<IMockInteraction> interactions;

  public TooFewInvocationsError(List<IMockInteraction> interactions) {
    Assert.notNull(interactions);
    Assert.that(interactions.size() > 0);
    this.interactions = interactions;
    fixupStackTrace();
  }

  public List<IMockInteraction> getUnsatisfiedInteractions() {
    return interactions;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (IMockInteraction interaction : interactions) {
      int numAccepted = interaction.getAcceptedCount();
      builder.append(String.format("%s   (%d %s)\n",
          interaction, numAccepted, numAccepted == 1 ? "invocation" : "invocations"));
    }
    return builder.toString();
  }

  // To facilitate navigation to the unsatisfied interactions, the line number
  // of the (synthetic) MockController.leaveScope() call is replaced by the line
  // number of the first unsatisfied interaction
  private void fixupStackTrace() {
    StackTraceElement[] trace = getStackTrace();

    for (int i = 0; i < trace.length; i++)
      if (isLeaveScopeCall(trace[i])) {
        StackTraceElement[] newTrace = new StackTraceElement[trace.length - i];
        StackTraceElement elem = trace[i];
        trace[i] = new StackTraceElement(elem.getClassName(), elem.getMethodName(), elem.getFileName(),
          interactions.get(0).getLine());
        setStackTrace(trace);
        return;
      }

    Assert.fail("MockController.leaveScope() not found in stacktrace");
  }

  private boolean isLeaveScopeCall(StackTraceElement elem) {
    return elem.getClassName().equals(MockController.class.getName())
      && elem.getMethodName().equals(MockController.LEAVE_SCOPE);
  }
}
