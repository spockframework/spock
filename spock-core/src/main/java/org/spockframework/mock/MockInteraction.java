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

import java.util.List;

/**
 * An anticipated interaction between the SUT and one or more mock objects.
 *
 * @author Peter Niederwieser
 */
public class MockInteraction implements IMockInteraction {
  private final int line;
  private final int column;
  private final String text;
  private final int minCount;
  private final int maxCount;
  private final List<IInvocationConstraint> matchers;
  private final IResultGenerator resultGenerator;
  private int acceptedCount;

  public MockInteraction(int line, int column, String text, int minCount,
      int maxCount, List<IInvocationConstraint> matchers,
      IResultGenerator resultGenerator) {
    this.line = line;
    this.column = column;
    this.text = text;
    this.minCount = minCount;
    this.maxCount = maxCount;
    this.matchers = matchers;
    this.resultGenerator = resultGenerator;
  }

  public boolean matches(IMockInvocation invocation) {
    for (IInvocationConstraint matcher : matchers)
      if (!matcher.isSatisfiedBy(invocation)) return false;
    return true;
  }

  public Object accept(IMockInvocation invocation) {
    acceptedCount++;

    if (acceptedCount > maxCount)
      throw new TooManyInvocationsError(this, invocation);

    return resultGenerator.generate(invocation);
  }

  public boolean isSatisfied() {
    return acceptedCount >= minCount;
  }

  public boolean isExhausted() {
    return acceptedCount >= maxCount;
  }

  public int getLine() {
    return line;
  }

  public int getColumn() {
    return column;
  }

  public String getText() {
    return text;
  }

  public String toString() {
    return String.format("%s   (%d %s)", text, acceptedCount, acceptedCount == 1 ? "invocation" : "invocations");
  }
}


