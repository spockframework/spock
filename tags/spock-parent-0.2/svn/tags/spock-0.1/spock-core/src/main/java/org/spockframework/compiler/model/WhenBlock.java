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
 * AST node representing a when-block in a feature-method.
 *
 * @author Peter Niederwieser
 */
// IDEA: to support ordering of interactions, we could introduce the following syntax:
// when: ...
// then: ... // comes after preceding interactions
// and:  ... // label not considered for ordering purposes (allows to vary structure/documentation w/o altering semantics)
// then: ... // comes after preceding interactions
//
// IDEA: maybe we could also make it possible to specify "condition A holds; THEN interaction B takes place"
public class WhenBlock extends Block {
  public WhenBlock(Method parent) {
    super(parent);
  }

  @Override
  public void accept(ISpeckVisitor visitor) throws Exception {
    visitor.visitAnyBlock(this);
    visitor.visitWhenBlock(this);
  }

  @Override
  public ThenBlock getNext() {
    return (ThenBlock)super.getNext();
  }

  public BlockParseInfo getParseInfo() {
    return BlockParseInfo.WHEN;
  }
}
