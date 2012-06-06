/*
 * Copyright 2010 the original author or authors.
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

import java.util.Arrays;
import java.util.List;

import org.spockframework.util.UnreachableCodeError;

public class DefaultInteractionScope implements IInteractionScope {
  private final DefaultStubbedInteraction defaultStubbedInteraction = new DefaultStubbedInteraction();

  private final List<DefaultInteraction> interactions = Arrays.asList(
      DefaultEqualsInteraction.INSTANCE, DefaultHashCodeInteraction.INSTANCE,
      DefaultToStringInteraction.INSTANCE, defaultStubbedInteraction);
  
  public void addInteraction(IMockInteraction interaction) {
    throw new UnreachableCodeError("addInteraction");
  }

  public void addOrderingBarrier() {
    throw new UnreachableCodeError("addOrderingBarrier()");
  }

  public IMockInteraction match(IMockInvocation invocation) {
    for (IMockInteraction interaction : interactions)
      if (interaction.matches(invocation))
        return interaction;

    return null;
  }

  public void verifyInteractions() {
    throw new UnreachableCodeError("verifyInteractions");
  }

  public List<IMockInvocation> getUnmatchedInvocations() {
    return defaultStubbedInteraction.getInvocations();
  }
}
