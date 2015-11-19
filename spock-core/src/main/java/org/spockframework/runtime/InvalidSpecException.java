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

/**
 * Indicates that a Spec has done something it is not supposed to do. It is up
 * to the Spec author to correct the problem.
 *
 * @author Peter Niederwieser
 */
public class InvalidSpecException extends SpockException {
  private String msg;

  public InvalidSpecException(String msg) {
    this(msg, null);
  }

  public InvalidSpecException(String msg, Throwable throwable) {
    super(throwable);
    this.msg = msg;
  }

  public InvalidSpecException withArgs(Object... args) {
    msg = String.format(msg, args);
    return this;
  }

  @Override
  public String getMessage() {
    return msg;
  }
}
