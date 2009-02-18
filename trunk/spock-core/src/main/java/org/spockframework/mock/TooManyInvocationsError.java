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

/**
 * A ...
 *
 * @author Peter Niederwieser
 */
public class TooManyInvocationsError extends Error {
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
    return String.format("Interaction expected at most %d invocations, but got one more.\n" +
        "Interaction: %s\nInvocation: %s", interaction.getAcceptedCount(), interaction, invocation);
  }
}
