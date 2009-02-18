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

package org.spockframework.compiler.model;

import java.util.EnumSet;

/**
 * A ...
 *
 * @author Peter Niederwieser
 */
public enum BlockParseInfo {
  AND {
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return method.getLastBlock().getParseInfo().getSuccessors(method);
    }
    public Block addNewBlock(Method method) {
      return method.getLastBlock();
    }
  },

  ANONYMOUS {
    public Block addNewBlock(Method method) {
      return method.addBlock(new AnonymousBlock(method));
    }
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return ANONYMOUS_SUCC;
    }
  },

  SETUP {
    public Block addNewBlock(Method method) {
      return method.addBlock(new SetupBlock(method));
    }
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return SETUP_SUCC;
    }
  },

  GIVEN {
    public Block addNewBlock(Method method) {
      return SETUP.addNewBlock(method);
    }
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return SETUP.getSuccessors(method);
    }
  },

  EXPECT {
    public Block addNewBlock(Method method) {
      return method.addBlock(new ExpectBlock(method));
    }
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return EXPECT_SUCC;
    }
  },

  WHEN {
    public Block addNewBlock(Method method) {
      return method.addBlock(new WhenBlock(method));
    }
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return WHEN_SUCC;
    }
  },

  THEN {
    public Block addNewBlock(Method method) {
      return method.addBlock(new ThenBlock(method));
    }
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return THEN_SUCC;
    }
  },

  CLEANUP {
    public Block addNewBlock(Method method) {
      return method.addBlock(new CleanupBlock(method));
    }
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return CLEANUP_SUCC;
    }
  },

  WHERE {
    public Block addNewBlock(Method method) {
      return method.addBlock(new WhereBlock(method));
    }
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      return WHERE_SUCC;
    }
  },

  METHOD_END {
    public Block addNewBlock(Method method) {
      throw new UnsupportedOperationException("addNewBlock");
    }
    public EnumSet<BlockParseInfo> getSuccessors(Method method) {
      throw new UnsupportedOperationException("getSuccessors");
    }
    public String toString() {
      return "end-of-method";
    }
  };

  public String toString() {
    return super.toString().toLowerCase();
  }

  public abstract Block addNewBlock(Method method);

  public abstract EnumSet<BlockParseInfo> getSuccessors(Method method);

  // because forward references to enum values are not allowed, we
  // have to add an indirection step
  private static final EnumSet<BlockParseInfo> ANONYMOUS_SUCC =
    EnumSet.of(SETUP, GIVEN, EXPECT, WHEN, CLEANUP, WHERE, METHOD_END);

  private static final EnumSet<BlockParseInfo> SETUP_SUCC =
    EnumSet.of(AND, EXPECT, WHEN, CLEANUP, WHERE, METHOD_END);

  private static final EnumSet<BlockParseInfo> EXPECT_SUCC =
    EnumSet.of(AND, WHEN, CLEANUP, WHERE, METHOD_END);

  private static final EnumSet<BlockParseInfo> WHEN_SUCC =
    EnumSet.of(AND, THEN);

  private static final EnumSet<BlockParseInfo> THEN_SUCC =
    EnumSet.of(AND, EXPECT, WHEN, CLEANUP, WHERE, METHOD_END);

  private static final EnumSet<BlockParseInfo> CLEANUP_SUCC =
    EnumSet.of(AND, WHERE, METHOD_END);

  private static final EnumSet<BlockParseInfo> WHERE_SUCC =
    EnumSet.of(AND, METHOD_END);
}
