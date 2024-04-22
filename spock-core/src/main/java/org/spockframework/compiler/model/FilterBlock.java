/*
 * Copyright 2024 the original author or authors.
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

/**
 * AST node representing a filter-block in a feature method.
 */
public class FilterBlock extends Block {
  public FilterBlock(Method parent) {
    super(parent);
    setName("filter");
  }

  @Override
  public void accept(ISpecVisitor visitor) throws Exception {
    visitor.visitAnyBlock(this);
    visitor.visitFilterBlock(this);
  }

  @Override
  public BlockParseInfo getParseInfo() {
    return BlockParseInfo.FILTER;
  }
}
