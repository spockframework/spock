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

package org.spockframework.runtime;

import org.spockframework.util.Nullable;

public class WrongExceptionThrownError extends SpockAssertionError {
  private final Class<? extends Throwable> expected;
  private final Throwable actual;

  public WrongExceptionThrownError(Class<? extends Throwable> expected, @Nullable Throwable actual) {
    super(actual);
    this.expected = expected;
    this.actual = actual;
  }

  public Class<? extends Throwable> getExpected() {
    return expected;
  }

  public Throwable getActual() {
    return actual;
  }

  @Override
  public String getMessage() {
    if (actual == null) {
      return String.format("Expected exception of type '%s', but no exception was thrown", expected.getName());
    }
    return String.format("Expected exception of type '%s', but got '%s'", expected.getName(), actual.getClass().getName());
  }
}
