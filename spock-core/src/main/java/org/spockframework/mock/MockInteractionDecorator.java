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

package org.spockframework.mock;

public abstract class MockInteractionDecorator implements IMockInteraction {
  protected final IMockInteraction decorated;

  public MockInteractionDecorator(IMockInteraction decorated) {
    this.decorated = decorated;
  }

  public int getLine() {
    return decorated.getLine();
  }

  public int getColumn() {
    return decorated.getColumn();
  }

  public String getText() {
    return decorated.getText();
  }

  public boolean matches(IMockInvocation invocation) {
    return decorated.matches(invocation);
  }

  public Object accept(IMockInvocation invocation) {
    return decorated.accept(invocation);
  }

  public boolean isSatisfied() {
    return decorated.isSatisfied();
  }

  public boolean isExhausted() {
    return decorated.isExhausted();
  }

  public int getAcceptedCount() {
    return decorated.getAcceptedCount();
  }

  @Override
  public String toString() {
    return decorated.toString();
  }
}
