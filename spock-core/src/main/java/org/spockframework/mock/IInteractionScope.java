/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.mock;

/**
 * An interaction scope holds a group of interactions that will be verified,
 * and thereafter removed, at the same time.
 *
 * @author Peter Niederwieser
 */
public interface IInteractionScope {
  void addInteraction(IMockInteraction interaction);

  void addOrderingBarrier();

  void addUnmatchedInvocation(IMockInvocation invocation);

  IMockInteraction match(IMockInvocation invocation);

  void verifyInteractions();
}
