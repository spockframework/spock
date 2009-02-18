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

package org.spockframework.util;

/**
 * A ...
 *
 * @author Peter Niederwieser
 */
public class Some<T> implements Option<T> {
  private final T value;

  private Some(T value) {
    this.value = value;
  }

  public boolean hasValue() {
    return true;
  }

  public T getValue() {
    return value;
  }

  public static <T> Some<T> value(T value) {
    return new Some<T>(value);
  }
}
