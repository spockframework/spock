/*
 * Copyright 2023 the original author or authors.
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

package org.spockframework.mock;

import groovy.transform.stc.SingleSignatureClosureHint;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.spockframework.compiler.AstUtil;
import spock.mock.MockingApi;

import java.util.*;

public class ClosureParameterTypeFromVariableType extends SingleSignatureClosureHint {
  private final static ClassNode MOCKING_API = new ClassNode(MockingApi.class);
  private final static ClassNode OBJECT = new ClassNode(Object.class);
  private final static Set<String> MOCK_METHODS = new HashSet<>();

  static {
    MOCK_METHODS.add("Mock");
    MOCK_METHODS.add("Stub");
    MOCK_METHODS.add("Spy");
    MOCK_METHODS.add("GroovyMock");
    MOCK_METHODS.add("GroovyStub");
    MOCK_METHODS.add("GroovySpy");
  }

  @Override
  public ClassNode[] getParameterTypes(MethodNode node, String[] options, SourceUnit sourceUnit, CompilationUnit unit, ASTNode usage) {
    ClassNode[] result = new ClassNode[]{OBJECT};
    sourceUnit
      .getAST()
      .getClasses()
      .stream()
      .filter(cn -> cn.isDerivedFrom(MOCKING_API))
      .map(ClassNode::getMethods)
      .flatMap(Collection::stream)
      .map(MethodNode::getCode)
      .forEach(code -> code.visit(new CodeVisitorSupport() {
        private final Deque<DeclarationExpression> currentDeclarations = new ArrayDeque<>();

        @Override
        public void visitDeclarationExpression(DeclarationExpression expression) {
          if (expression.isMultipleAssignmentDeclaration()) {
            super.visitDeclarationExpression(expression);
          } else {
            currentDeclarations.push(expression);
            try {
              super.visitDeclarationExpression(expression);
            } finally {
              currentDeclarations.pop();
            }
          }
        }

        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
          if ((currentDeclarations.peek() != null) && MOCK_METHODS.contains(call.getMethodAsString())) {
            List<Expression> arguments = AstUtil.getArgumentList(call);
            if (arguments.contains(usage)) {
              result[0] = currentDeclarations.peek().getVariableExpression().getType();
              return;
            }
          }
          super.visitMethodCallExpression(call);
        }
      }));
    return new ClassNode[]{result[0]};
  }
}
