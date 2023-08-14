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
 *
 * @author Peter Niederwieser
 */
public interface IInvocationConstraint {
  /**
   * @param other the other element
   * @return {@code true}, if both elements represent the same {@link IInvocationConstraint} from the declaration point of view
   */
  boolean isDeclarationEqualTo(IInvocationConstraint other);
  boolean isSatisfiedBy(IMockInvocation invocation);
  String describeMismatch(IMockInvocation invocation);
}
