/*
 * Copyright 2025 the original author or authors.
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

import org.opentest4j.MultipleFailuresError;

import java.util.List;

public class SpockMultipleFailuresError extends MultipleFailuresError {
  private static final long serialVersionUID = 1L;
  private final MultipleFailuresError original;

  public SpockMultipleFailuresError(String heading, List<? extends Throwable> failures) {
    this(heading, failures, null);
  }

  static SpockMultipleFailuresError from(MultipleFailuresError original, List<? extends Throwable> failures) {
    return new SpockMultipleFailuresError("", failures, original);
  }

  private SpockMultipleFailuresError(String heading, List<? extends Throwable> failures, MultipleFailuresError original) {
    super(heading, failures);
    this.original = original;
    if (original == null) return;

    setStackTrace(original.getStackTrace());
    if (original.getCause() != null) {
      initCause(original.getCause());
    }
    for (Throwable suppressed : original.getSuppressed()) {
      addSuppressed(suppressed);
    }
  }

  boolean isTransformed() {
    return original != null;
  }

  @Override
  public String getMessage() {
    // getMessage already includes the failures, so it cannot be used as a new heading.
    return original == null ? super.getMessage() : original.getMessage();
  }
}
