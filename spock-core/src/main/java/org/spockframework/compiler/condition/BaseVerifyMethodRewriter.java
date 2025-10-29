/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.compiler.condition;

import org.codehaus.groovy.ast.stmt.Statement;
import org.spockframework.compiler.*;
import org.spockframework.compiler.model.Method;

import java.util.List;

import static org.spockframework.util.Assert.notNull;

abstract class BaseVerifyMethodRewriter extends StatementReplacingVisitorSupport implements IVerifyMethodRewriter {

  final IRewriteResources resources;
  protected final Method method;

  BaseVerifyMethodRewriter(Method method, IRewriteResources resources) {
    this.resources = notNull(resources);
    this.method = notNull(method);
  }

  abstract void defineErrorCollector(boolean methodHasCondition, boolean methodHasDeepNonGroupedCondition,
                                     List<Statement> statements);

  abstract ISpecialMethodCall createSpecialMethodCall();

  @Override
  public void rewrite() {
    DeepBlockRewriter deep = new DeepBlockRewriter(resources, createSpecialMethodCall());
    deep.visit(method.getFirstBlock());
    method.getStatements().addAll(method.getFirstBlock().getAst());

    defineErrorCollector(deep.isConditionFound(), deep.isDeepNonGroupedConditionFound(),
      method.getStatements());
    if (deep.isConditionFound()) {
      resources.getErrorRecorders().defineValueRecorder(method.getStatements());
    }
  }
}
