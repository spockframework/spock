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

package org.spockframework.mock.runtime;

import org.spockframework.mock.*;

import java.util.List;
import java.util.function.Supplier;

public abstract class MockInteractionDecorator implements IMockInteraction {
  protected final IMockInteraction decorated;

  public MockInteractionDecorator(IMockInteraction decorated) {
    this.decorated = decorated;
  }

  @Override
  public int getLine() {
    return decorated.getLine();
  }

  @Override
  public int getColumn() {
    return decorated.getColumn();
  }

  @Override
  public String getText() {
    return decorated.getText();
  }

  @Override
  public boolean matches(IMockInvocation invocation) {
    return decorated.matches(invocation);
  }

  @Override
  public Supplier<Object> accept(IMockInvocation invocation) {
    return decorated.accept(invocation);
  }

  @Override
  public List<IMockInvocation> getAcceptedInvocations() {
    return decorated.getAcceptedInvocations();
  }

  @Override
  public int computeSimilarityScore(IMockInvocation invocation) {
    return decorated.computeSimilarityScore(invocation);
  }

  @Override
  public String describeMismatch(IMockInvocation invocation) {
    return decorated.describeMismatch(invocation);
  }

  @Override
  public boolean isSatisfied() {
    return decorated.isSatisfied();
  }

  @Override
  public boolean isExhausted() {
    return decorated.isExhausted();
  }

  @Override
  public boolean isRequired() {
    return decorated.isRequired();
  }

  @Override
  public String toString() {
    return decorated.toString();
  }
}
