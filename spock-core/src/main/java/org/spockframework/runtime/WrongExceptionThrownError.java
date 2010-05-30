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

package org.spockframework.runtime;

public class WrongExceptionThrownError extends SpockAssertionError {
  private final Class<? extends Throwable> expected;
  private final Throwable actual;

  public WrongExceptionThrownError(Class<? extends Throwable> expected, Throwable actual) {
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
    return String.format("Expected exception %s, but %s", expected.getName(),
        actual == null ? "no exception was thrown" : ("got " + actual.getClass().getName()));
  }
}
