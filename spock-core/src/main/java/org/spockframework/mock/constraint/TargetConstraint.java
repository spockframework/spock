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

package org.spockframework.mock.constraint;

import org.spockframework.mock.*;
import org.spockframework.runtime.Condition;
import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.util.CollectionUtil;

/**
 * @author Peter Niederwieser
 */
public class TargetConstraint implements IInvocationConstraint, IInteractionAware {
  private static final MockUtil MOCK_UTIL = new MockUtil();
  private final Object target;
  private IMockInteraction interaction;

  public TargetConstraint(Object target) {
    this.target = target;
  }

  @Override
  public boolean isSatisfiedBy(IMockInvocation invocation) {
    return invocation.getMockObject().matches(target, interaction);
  }

  @Override
  public String describeMismatch(IMockInvocation invocation) {
    // need to explicitly call .toString() otherwise the render service will attempt to output the contents of collections and such
    Condition condition = new Condition(CollectionUtil.<Object>listOf(  invocation.getMockObject().getInstance().toString(), target.toString(), false),
      "instance == target", null, null,
      null, null);
    return condition.getRendering();
  }

  @Override
  public void setInteraction(IMockInteraction interaction) {
    this.interaction = interaction;
    if (interaction.isRequired() && MOCK_UTIL.isMock(target)) {
      IMockObject mockObject = MOCK_UTIL.asMock(target);
      if (!mockObject.isVerified()) {
        String mockName = mockObject.getName() != null ? mockObject.getName() : "unnamed";
        throw new InvalidSpecException("Stub '%s' matches the following required interaction:" +
          "\n\n%s\n\nRemove the cardinality (e.g. '1 *'), or turn the stub into a mock.\n").withArgs(mockName, interaction);
      }
    }
  }
}
