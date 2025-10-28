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

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import spock.lang.VerifyAll;

import java.lang.annotation.Annotation;

import org.spockframework.compiler.IRewriteResources;

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class VerifyAllMethodTransform extends BaseVerifyMethodTransform {

  public VerifyAllMethodTransform() {
    super(VerifyAll.class);
  }

  @Override
  IVerifyMethodRewriter createRewriter(MethodNode methodNode, IRewriteResources resources) {
    return new VerifyAllMethodRewriter(methodNode, resources);
  }
}
