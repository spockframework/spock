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

package org.spockframework.runtime;

import java.util.concurrent.TimeUnit;

public class SpockTimeoutError extends SpockAssertionError {
  private final long timeoutValue;
  private final TimeUnit timeoutUnit;

  public SpockTimeoutError(long timeoutValue, TimeUnit timeoutUnit, String message) {
    super(message);
    this.timeoutValue = timeoutValue;
    this.timeoutUnit = timeoutUnit;
  }

  public SpockTimeoutError(long timeoutValue, TimeUnit timeoutUnit, String message, Throwable cause) {
    super(message, cause);
    this.timeoutValue = timeoutValue;
    this.timeoutUnit = timeoutUnit;
  }

  public long getTimeoutValue() {
    return timeoutValue;
  }

  public TimeUnit getTimeoutUnit() {
    return timeoutUnit;
  }
}
