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

import org.spockframework.compiler.AstUtil;

import java.util.*;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.stmt.Statement;

/**
 * AST node representing a Spec method (one of fixture method, feature method, helper method).
 *
 * @author Peter Niederwieser
 */
public abstract class Method extends Node<Spec, MethodNode> {
  private Block firstBlock;
  private Block lastBlock;

  public Method(Spec parent, MethodNode code) {
    super(parent, code);
    setName(code.getName());
  }

  public Block getFirstBlock() {
    return firstBlock;
  }

  public Block getLastBlock() {
    return lastBlock;
  }

  public List<Statement> getStatements() {
    return AstUtil.getStatements(getAst());
  }

  public Iterable<Block> getBlocks() {
    return new BlockIterable(firstBlock);
  }

  public Block addBlock(Block block) {
    if (firstBlock == null)
      firstBlock = lastBlock = block;
    else {
      lastBlock.setNext(block);
      block.setPrevious(lastBlock);
      lastBlock = block;
    }
    return block;
  }

  @Override
  public void accept(ISpecVisitor visitor) throws Exception {
    visitor.visitMethod(this);
    for (Block b: getBlocks()) b.accept(visitor);
    visitor.visitMethodAgain(this);
  }
}

class BlockIterable implements Iterable<Block> {
  private final Block first;

  BlockIterable(Block first) {
    this.first = first;
  }

  @Override
  public Iterator<Block> iterator() {
    return new Iterator<Block>() {
      Block block = first;

      @Override
      public boolean hasNext() {
        return block != null;
      }

      @Override
      public Block next() {
        if (hasNext()) {
          Block result = block;
          block = block.getNext();
          return result;
        } else
          throw new NoSuchElementException();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("remove");
      }
    };
  }
}
