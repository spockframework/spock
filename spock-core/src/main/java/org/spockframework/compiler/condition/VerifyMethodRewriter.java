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
import org.spockframework.compiler.IRewriteResources;
import org.spockframework.compiler.ISpecialMethodCall;
import org.spockframework.compiler.SpecialMethodCall;
import org.spockframework.compiler.model.Method;
import org.spockframework.util.Identifiers;

import java.util.List;

public class VerifyMethodRewriter extends BaseVerifyMethodRewriter {

  public VerifyMethodRewriter(Method method, IRewriteResources resources) {
    super(method, resources);
  }

  @Override
  void defineErrorCollector(boolean methodHasCondition, boolean methodHasDeepNonGroupedCondition,
                            List<Statement> statements) {
    if (methodHasDeepNonGroupedCondition) {
      resources.getErrorRecorders().defineErrorRethrower(statements);
    }
  }

  @Override
  ISpecialMethodCall createSpecialMethodCall() {
    return new SpecialMethodCall(Identifiers.WITH, resources.getAstNodeCache());
  }
}
