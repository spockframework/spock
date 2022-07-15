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

package org.spockframework.runtime;

import org.opentest4j.*;

/**
 * Thrown when a condition of the form 'expr1 == expr2' fails.
 * Inherits from JUnit's ComparisonFailure (rather than
 * ConditionNotSatisfiedError) to enable IDE support for
 * diff'ing actual and expected values.
 *
 * @author Peter Niederwieser
 */
public class SpockComparisonFailure extends AssertionFailedError {
  private static final long serialVersionUID = 2L;

  private final Condition condition;

  public SpockComparisonFailure(Condition condition, Object expected, Object actual) {
    super(null, expected, actual);
    this.condition = condition;
  }

  public Condition getCondition() {
    return condition;
  }

  @Override
  public String getMessage() {
    return "Condition not satisfied:\n\n" + condition.getRendering();
  }

  @Override
  public String toString() {
    return getMessage();
  }
}
