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

package org.spockframework.compiler;

import org.spockframework.compiler.model.*;

// IDEA: store context (SpecInfo, method, etc.); e.g. by overriding new visitNode method and making it final
public class AbstractSpecVisitor implements ISpecVisitor {
  @Override
  public void visitSpec(Spec spec) throws Exception {}
  @Override
  public void visitSpecAgain(Spec spec) throws Exception {}
  @Override
  public void visitField(Field field) throws Exception {}
  @Override
  public void visitMethod(Method method) throws Exception {}
  @Override
  public void visitMethodAgain(Method method) throws Exception {}
  @Override
  public void visitAnyBlock(Block block) throws Exception {}
  @Override
  public void visitAnonymousBlock(AnonymousBlock block) throws Exception {}
  @Override
  public void visitSetupBlock(SetupBlock block) throws Exception {}
  @Override
  public void visitExpectBlock(ExpectBlock block) throws Exception {}
  @Override
  public void visitWhenBlock(WhenBlock block) throws Exception {}
  @Override
  public void visitThenBlock(ThenBlock block) throws Exception {}
  @Override
  public void visitVerifyBlock(VerifyBlock block) throws Exception {}
  @Override
  public void visitCleanupBlock(CleanupBlock block) throws Exception {}
  @Override
  public void visitWhereBlock(WhereBlock block) throws Exception {}
  @Override
  public void visitFilterBlock(FilterBlock block) throws Exception {}
}
