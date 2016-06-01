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

package org.spockframework.runtime.extension;

import org.spockframework.runtime.SpockException;

public class ExtensionException extends SpockException {
  private String message;

  public ExtensionException(String message) {
    this(message, null);
  }

  public ExtensionException(String message, Throwable cause) {
    super(cause);
    this.message = message;
  }

  public ExtensionException withArgs(Object... args) {
    message = String.format(message, args);
    return this;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
