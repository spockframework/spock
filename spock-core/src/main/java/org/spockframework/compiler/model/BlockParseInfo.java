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

import org.spockframework.util.InternalSpockError;

/**
 *
 * @author Peter Niederwieser
 */
public enum BlockParseInfo {
  AND {
    @Override
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return method.getLastBlock().getParseInfo().getSuccessors(method);
    }
    @Override
    public Block addNewBlock(Method method) {
      return method.getLastBlock();
    }
    @Override
    public boolean isSupportingBlockListeners() {
      throw new InternalSpockError("AND block should have been replaced by a more specific block");
    }
  },

  ANONYMOUS {
    @Override
    public Block addNewBlock(Method method) {
      return method.addBlock(new AnonymousBlock(method));
    }
    @Override
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return EnumSet.of(SETUP, GIVEN, EXPECT, WHEN, CLEANUP, WHERE, METHOD_END);
    }
    @Override
    public boolean isSupportingBlockListeners() {
      return false;
    }
  },

  SETUP {
    @Override
    public Block addNewBlock(Method method) {
      return method.addBlock(new SetupBlock(method));
    }
    @Override
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return EnumSet.of(AND, EXPECT, WHEN, CLEANUP, WHERE, METHOD_END);
    }
    @Override
    public boolean isSupportingBlockListeners() {
      return true;
    }
  },

  GIVEN {
    @Override
    public Block addNewBlock(Method method) {
      return SETUP.addNewBlock(method);
    }
    @Override
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return SETUP.getSuccessors(method);
    }
    @Override
    public boolean isSupportingBlockListeners() {
      throw new InternalSpockError("GIVEN block should have been replaced by a SETUP block");
    }
  },

  EXPECT {
    @Override
    public Block addNewBlock(Method method) {
      return method.addBlock(new ExpectBlock(method));
    }
    @Override
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return EnumSet.of(AND, WHEN, CLEANUP, WHERE, METHOD_END);
    }
    @Override
    public boolean isSupportingBlockListeners() {
      return true;
    }
  },

  WHEN {
    @Override
    public Block addNewBlock(Method method) {
      return method.addBlock(new WhenBlock(method));
    }
    @Override
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return EnumSet.of(AND, THEN);
    }
    @Override
    public boolean isSupportingBlockListeners() {
      return true;
    }
  },

  THEN {
    @Override
    public Block addNewBlock(Method method) {
      return method.addBlock(new ThenBlock(method));
    }
    @Override
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return EnumSet.of(AND, EXPECT, WHEN, THEN, CLEANUP, WHERE, METHOD_END);
    }
    @Override
    public boolean isSupportingBlockListeners() {
      return true;
    }
  },

  CLEANUP {
    @Override
    public Block addNewBlock(Method method) {
      return method.addBlock(new CleanupBlock(method));
    }
    @Override
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return EnumSet.of(AND, WHERE, METHOD_END);
    }
    @Override
    public boolean isSupportingBlockListeners() {
      return true;
    }
  },

  WHERE {
    @Override
    public Block addNewBlock(Method method) {
      return method.addBlock(new WhereBlock(method));
    }
    @Override
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return EnumSet.of(AND, COMBINED, FILTER, METHOD_END);
    }
    @Override
    public boolean isSupportingBlockListeners() {
      return false;
    }
  },

  COMBINED {
    @Override
    public Block addNewBlock(Method method) {
      return method.getLastBlock();
    }
    @Override
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return WHERE.getSuccessors(method);
    }
    @Override
    public boolean isSupportingBlockListeners() {
      return false;
    }
  },

  FILTER {
    @Override
    public Block addNewBlock(Method method) {
      return method.addBlock(new FilterBlock(method));
    }
    @Override
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return EnumSet.of(AND, METHOD_END);
    }
    @Override
    public boolean isSupportingBlockListeners() {
      return false;
    }
  },

  VERIFY {
    @Override
    public Block addNewBlock(Method method) {
      return method.addBlock(new VerifyBlock(method));
    }
    @Override
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return EnumSet.of(METHOD_END);
    }
    @Override
    public boolean isSupportingBlockListeners() {
      return false;
    }
  },

  METHOD_END {
    @Override
    public Block addNewBlock(Method method) {
      throw new UnsupportedOperationException("addNewBlock");
    }
    @Override
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      throw new UnsupportedOperationException("getSuccessors");
    }
    @Override
    public boolean isSupportingBlockListeners() {
      return false;
    }
    public String toString() {
      return "end-of-method";
    }
  };

  public String toString() {
    return super.toString().toLowerCase(Locale.ROOT);
  }

  /**
   * Adds a new block of this type to the given method.
   * <p>
   * Allows for a block to substitute another block type.
   */
  public abstract Block addNewBlock(Method method);

  /**
   * Returns the block types that can follow this block type in the given method.
   */
  public abstract EnumSet<BlockParseInfo> getSuccessors(Method method);

  /**
   * Indicates whether this block type supports block listeners.
   */
  public abstract boolean isSupportingBlockListeners();
}
