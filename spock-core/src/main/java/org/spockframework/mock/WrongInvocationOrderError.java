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

package org.spockframework.mock;

import java.io.IOException;
import java.util.*;

import static java.util.Collections.unmodifiableList;

/**
 * Thrown if an invocation on a mock object occurs too late. Example:
 *
 * <pre>
 * when:
 * ...
 *
 * then:
 * 1 * foo.me()
 * 1 * bar.me()
 *
 * then: // indicates that subsequent interactions must take place after previous interactions
 * 1 * baz.me()
 * </pre>
 *
 * Assuming the following invocation order:
 *
 * <ol>
 * <li>bar.me()</li>
 * <li>baz.me()</li>
 * <li>foo.me()</li>
 * </ol>
 *
 * A <tt>WrongInvocationOrderError</tt> will be thrown on the third call.
 */
public class WrongInvocationOrderError extends InteractionNotSatisfiedError {
  private static final long serialVersionUID = 1L;

  private final transient IMockInteraction interaction;
  private final transient IMockInvocation lastInvocation;
  private final transient List<IMockInvocation> previousInvocationsInReverseOrder;
  private String message;

  public WrongInvocationOrderError(IMockInteraction interaction, IMockInvocation lastInvocation,
                                   Deque<IMockInvocation> previousInvocationsInReverseOrder) {
    this.interaction = interaction;
    this.lastInvocation = lastInvocation;
    this.previousInvocationsInReverseOrder = unmodifiableList(new ArrayList<>(previousInvocationsInReverseOrder));
  }

  public IMockInteraction getInteraction() {
    return interaction;
  }

  public IMockInvocation getLastInvocation() {
    return lastInvocation;
  }

  public List<IMockInvocation> getPreviousInvocationsInReverseOrder() {
    return previousInvocationsInReverseOrder;
  }

  @Override
  public String getMessage() {
    if (message != null) return message;
    StringBuilder builder = new StringBuilder();
    builder.append("Wrong invocation order for:\n\n");
    builder.append(interaction);
    builder.append("\n\n");
    builder.append("Last invocation: ");
    builder.append(lastInvocation);
    builder.append("\n\n");
    if (previousInvocationsInReverseOrder.size() == 1) {
      builder.append("Previous invocation:\n\t");
      builder.append(previousInvocationsInReverseOrder.get(0));
    } else {
      builder.append("Previous invocations in reverse order:");
      previousInvocationsInReverseOrder.forEach(invocation -> {
        builder.append("\n\t");
        builder.append(invocation);
      });
    }
    builder.append("\n");

    message = builder.toString();
    return message;
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    // create the message so that it is available for serialization
    getMessage();
    out.defaultWriteObject();
  }
}
