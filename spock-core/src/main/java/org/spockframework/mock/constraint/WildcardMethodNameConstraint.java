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

package org.spockframework.mock.constraint;

import org.spockframework.mock.*;

/**
 * @author Peter Niederwieser
 */
public class WildcardMethodNameConstraint implements IInvocationConstraint {
  public static final WildcardMethodNameConstraint INSTANCE = new WildcardMethodNameConstraint();

  private WildcardMethodNameConstraint() {}

  private boolean isFinalizeCall(IMockInvocation invocation) {
    return invocation.getMethod().getName().equals("finalize") && invocation.getArguments().size() == 0;
  }

  public boolean isSatisfiedBy(IMockInvocation invocation) {
    return DefaultEqualsHashCodeToStringInteractions.INSTANCE.match(invocation) == null && !isFinalizeCall(invocation);
  }
}