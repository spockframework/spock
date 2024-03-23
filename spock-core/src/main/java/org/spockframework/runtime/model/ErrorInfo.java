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

package org.spockframework.runtime.model;

import org.spockframework.util.Nullable;

public class ErrorInfo {
  private final MethodInfo method;
  private final Throwable error;
  private final IErrorContext errorContext;

  public ErrorInfo(MethodInfo method, Throwable error) {
    this(method, error, null);
  }

  public ErrorInfo(MethodInfo method, Throwable error, @Nullable IErrorContext errorContext) {
    this.method = method;
    this.error = error;
    this.errorContext = errorContext;
  }

  public MethodInfo getMethod() {
    return method;
  }

  public Throwable getException() {
    return error;
  }

  @Nullable
  public IErrorContext getErrorContext() {
    return errorContext;
  }
}
