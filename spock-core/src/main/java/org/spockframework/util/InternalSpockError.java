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

package org.spockframework.util;

/**
 *
 * @author Peter Niederwieser
 */
public class InternalSpockError extends Error {
  private Object[] msgArgs;

  public InternalSpockError() {
    super("unexpected error");
  }

  public InternalSpockError(Throwable throwable) {
    super("unexpected error", throwable);
  }

  public InternalSpockError(String msg) {
    super(msg);
  }

  public InternalSpockError(String msg, Throwable throwable) {
    super(msg, throwable);
  }

  public InternalSpockError withArgs(Object... msgArgs) {
    this.msgArgs = msgArgs;
    return this;
  }

  @Override
  public String getMessage() {
    return String.format(super.getMessage(), msgArgs);
  }
}
