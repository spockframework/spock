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

import org.spockframework.util.InternalSpockError;

/**
 * A scope for interactions defined outside a then-block
 *
 * @author Peter Niederwieser
 */
public class GlobalInteractionScope implements IInteractionScope {
  protected final List<IMockInteraction> interactions = new ArrayList<IMockInteraction>();

  public void addInteraction(IMockInteraction interaction) {
    interactions.add(interaction);
  }

  public IMockInteraction match(IMockInvocation invocation) {
    for (IMockInteraction interaction : interactions) {
      if (interaction.matches(invocation))
        return interaction;
    }

    return null;
  }

  public void verifyInteractions() {
    throw new InternalSpockError("Tried to verify global interaction scope");
  }
}
