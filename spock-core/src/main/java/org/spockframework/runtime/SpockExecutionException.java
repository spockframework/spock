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

/**
 * Indicates that a problem occurred during Spec execution.
 *
 * @author Peter Niederwieser
 */
public class SpockExecutionException extends SpockException {
  private volatile String msg;

  public SpockExecutionException(String msg) {
    this(msg, null);
  }

  public SpockExecutionException(String msg, Throwable throwable) {
    super(throwable);
    this.msg = msg;
  }

  public SpockExecutionException withArgs(Object... args) {
    msg = String.format(msg, args);
    return this;
  }

  @Override
  public String getMessage() {
    return msg;
  }
}
