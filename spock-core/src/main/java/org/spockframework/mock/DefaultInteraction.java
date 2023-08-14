/*
 * Copyright 2009 the original author or authors.
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
package org.spockframework.mock;

import org.spockframework.util.UnreachableCodeError;

import java.util.Collections;
import java.util.List;

abstract class DefaultInteraction implements IMockInteraction {

  @Override
  public boolean isCardinalitySpecified() {
    return false;
  }

  @Override
  public List<IInvocationConstraint> getConstraints() {
    return Collections.emptyList();
  }

  @Override
  public boolean isThisInteractionOverridableBy(IMockInteraction toCheck) {
    return false;
  }

  @Override
  public int getLine() {
    return -1;
  }

  @Override
  public int getColumn() {
    return -1;
  }

  @Override
  public List<IMockInvocation> getAcceptedInvocations() {
    throw new UnreachableCodeError("getAcceptedInvocations");
  }

  @Override
  public int computeSimilarityScore(IMockInvocation invocation) {
    throw new UnreachableCodeError("computeSimilarityScore");
  }

  @Override
  public String describeMismatch(IMockInvocation invocation) {
    throw new UnreachableCodeError("describeMismatch");
  }

  @Override
  public boolean isSatisfied() {
    return true;
  }

  @Override
  public boolean isExhausted() {
    return false;
  }

  @Override
  public boolean isRequired() {
    return false;
  }
}
