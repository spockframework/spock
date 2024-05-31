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

package org.spockframework.runtime.model;

import org.spockframework.util.Nullable;

import java.util.List;

/**
 * Runtime information about a block in a method of a Spock specification.
 *
 * @author Peter Niederwieser
 */
public class BlockInfo {
  private BlockKind kind;
  private List<String> texts;

  public BlockInfo() {
  }

  public BlockInfo(BlockKind kind, List<String> texts) {
    this.kind = kind;
    this.texts = texts;
  }

  @Nullable
  public BlockKind getKind() {
    return kind;
  }

  public void setKind(BlockKind kind) {
    this.kind = kind;
  }

  @Nullable
  public List<String> getTexts() {
    return texts;
  }

  public void setTexts(List<String> texts) {
    this.texts = texts;
  }
}
