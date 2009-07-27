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

import java.util.LinkedList;

/**
 *
 * @author Peter Niederwieser
 */
// TODO: MockController, IMockFactory's, and mocks need to be thread-safe
public class MockController implements IInvocationMatcher {
  private final IMockFactory factory;
  private final LinkedList<IInteractionScope> scopes = new LinkedList<IInteractionScope>();

  public MockController(IMockFactory factory) {
    this.factory = factory;
    scopes.addFirst(new GlobalInteractionScope());
  }

  public static final String ADD = "add";

  public void add(IMockInteraction interaction) {
    scopes.getFirst().addInteraction(interaction);
  }

  public IMockInteraction match(IMockInvocation invocation) {
    for (IInteractionScope scope : scopes) {
      IMockInteraction match = scope.match(invocation);
      if (match != null) return match;
    }

    return null;
  }

  public static final String ENTER_SCOPE = "enterScope";

  public void enterScope() {
    scopes.addFirst(new ThenBlockInteractionScope());
  }

  public static final String LEAVE_SCOPE = "leaveScope";

  public void leaveScope() {
    IInteractionScope scope = scopes.removeFirst();
    scope.verifyInteractions();
  }

  public static String CREATE = "create";

  public Object create(String mockName, Class<?> mockType) {
    return factory.create(mockName, mockType, this);
  }
}
