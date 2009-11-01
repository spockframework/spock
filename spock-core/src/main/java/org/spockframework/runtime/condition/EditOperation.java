/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.runtime.condition;

public class EditOperation {
  public enum Kind { INSERT, DELETE, SUBSTITUTE, SKIP }

  private final Kind kind;
  private int length;

  EditOperation(Kind kind, int length) {
    this.length = length;
    this.kind = kind;
  }

  public Kind getKind() {
    return kind;
  }

  public int getLength() {
    return length;
  }

  public void incLength(int amount) {
    length += amount;
  }
}
