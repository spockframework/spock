/*
 * Copyright 2009 the original author or authors.
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

import java.util.List;

import org.spockframework.util.UnreachableCodeError;

abstract class DefaultInteraction implements IMockInteraction {
  public int getLine() {
    return -1;
  }

  public int getColumn() {
    return -1;
  }

  public List<IMockInvocation> getAcceptedInvocations() {
    throw new UnreachableCodeError("getAcceptedInvocations");
  }

  public int computeSimilarityScore(IMockInvocation invocation) {
    throw new UnreachableCodeError("computeSimilarityScore");
  }

  public boolean isSatisfied() {
    return true;
  }

  public boolean isExhausted() {
    return false;
  }

  public boolean isRequired() {
    return false;
  }
}
