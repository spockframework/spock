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

package org.spockframework.compiler.model;

import java.util.*;

import org.codehaus.groovy.ast.stmt.Statement;

/**
 * AST node representing a block in a feature method. There are six kinds
 * of blocks: setup-block, expect-block, when-block, then-block, cleanup-block,
 * and where-block.
 *
 * @author Peter Niederwieser
 */
public abstract class Block extends Node<Method, List<Statement>> {
  private final List<String> descriptions = new ArrayList<>(3);
  private Block prev;
  private Block next;

  public Block(Method parent) {
    super(parent, new ArrayList<>());
  }

  public List<String> getDescriptions() {
    return descriptions;
  }

  public Block getPrevious() {
    return prev;
  }

  public void setPrevious(Block block) {
    prev = block;
  }

  public Block getNext() {
    return next;
  }

  public void setNext(Block block) {
    next = block;
  }

  public <T extends Block> T getPrevious(Class<T> blockType) {
    Block block = prev;
    while (block != null && !blockType.isInstance(block)) block = block.prev;
    return blockType.cast(block);
  }

  public <T extends Block> T getNext(Class<T> blockType) {
    Block block = next;
    while (block != null && !blockType.isInstance(block)) block = block.next;
    return blockType.cast(block);
  }

  public boolean isFirst() {
    return prev == null;
  }

  public boolean isLast() {
    return next == null;
  }

  public boolean isFirstInChain() {
    return isFirst() || getClass() != prev.getClass();
  }

  public boolean isLastInChain() {
    return isLast() || getClass() != next.getClass();
  }

  public abstract BlockParseInfo getParseInfo();
}
