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

/**
 * A block without a label at the beginning of a method. Every Method
 * has a (possibly empty) AnonymousBlock as its first block.
 *
 * @author Peter Niederwieser
 */
public class AnonymousBlock extends Block {
  public AnonymousBlock(Method parent) {
    super(parent);
  }

  @Override
  public void accept(ISpecVisitor visitor) throws Exception {
    visitor.visitAnyBlock(this);
    visitor.visitAnonymousBlock(this);
  }

  @Override
  public BlockParseInfo getParseInfo() {
    return BlockParseInfo.ANONYMOUS;
  }
}
