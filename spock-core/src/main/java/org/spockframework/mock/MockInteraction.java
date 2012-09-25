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
  private final List<IInvocationConstraint> constraints;
  private final IResponseGenerator responseGenerator;

  private final List<IMockInvocation> acceptedInvocations = new ArrayList<IMockInvocation>();

  public MockInteraction(int line, int column, String text, int minCount,
      int maxCount, List<IInvocationConstraint> constraints,
      IResponseGenerator responseGenerator) {
    this.line = line;
    this.column = column;
    this.text = text;
    this.minCount = minCount;
    this.maxCount = maxCount;
    this.constraints = constraints;
    this.responseGenerator = responseGenerator;

    for (IInvocationConstraint constraint : constraints) {
      if (constraint instanceof IInteractionAware) {
        ((IInteractionAware) constraint).setInteraction(this);
      }
    }
  }

  public boolean matches(IMockInvocation invocation) {
    for (IInvocationConstraint constraint : constraints) {
      if (!constraint.isSatisfiedBy(invocation)) return false;
    }
    return true;
  }

  public Object accept(IMockInvocation invocation) {
    acceptedInvocations.add(invocation);
    if (acceptedInvocations.size() > maxCount) {
      throw new TooManyInvocationsError(this, acceptedInvocations);
    }

    return responseGenerator == null ? null : responseGenerator.respond(invocation);
  }

  public List<IMockInvocation> getAcceptedInvocations() {
    return acceptedInvocations;
  }

  public int computeSimilarityScore(IMockInvocation invocation) {
    int score = 0;
    int weight = constraints.size();
    for (IInvocationConstraint constraint : constraints) {
      if (!constraint.isSatisfiedBy(invocation)) {
        score += weight;
      }
      weight--;
    }
    return score;
  }

  public boolean isSatisfied() {
    return acceptedInvocations.size() >= minCount;
  }

  public boolean isExhausted() {
    return acceptedInvocations.size() >= maxCount;
  }

  public boolean isRequired() {
    return minCount != 0 || maxCount != Integer.MAX_VALUE;
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
    return String.format("%s   (%d %s)", text, acceptedInvocations.size(), acceptedInvocations.size() == 1 ? "invocation" : "invocations");
  }
}


