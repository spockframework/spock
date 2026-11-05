/*
 * Copyright 2026 the original author or authors.
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

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;

public class UnhygienicAstDetector extends ClassCodeVisitorSupport {
  public static final String UNHYGIENIC_AST_DETECTOR_PROPERTY_NAME = "org.spockframework.unhygienic.ast.detector";

  private final SourceUnit sourceUnit;
  private final Deque<String> context = new ArrayDeque<>();
  private final Object MARKER = new Object();

  public UnhygienicAstDetector(SourceUnit sourceUnit) {
    this.sourceUnit = sourceUnit;
  }

  @Override
  protected SourceUnit getSourceUnit() {
    return sourceUnit;
  }

  private void detectUnhygienicAst(ASTNode node, String event) {
    if (node != null) {
      HashMap<String, String> events = node.getNodeMetaData(MARKER);
      if (events == null) {
        events = new HashMap<>();
        node.setNodeMetaData(MARKER, events);
      }
      String contextPath = String.join(" -> ", context);
      String oldContextPath = events.put(event, contextPath);
      if (oldContextPath != null) {
        sourceUnit.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(
          new SyntaxException("Unhygienic AST detected at:\n" + contextPath + "\nold context path:\n" + oldContextPath + "\n" + node, node),
          sourceUnit));
      }
    }
  }

  private void withContext(String value, Runnable runnable) {
    context.push(value);
    try {
      runnable.run();
    } finally {
      context.pop();
    }
  }

  @Override
  public void visitClass(ClassNode node) {
    withContext("class " + node.getText(), () -> {
      detectUnhygienicAst(node, "visitClass");
      super.visitClass(node);
    });
  }

  @Override
  public void visitAnnotations(AnnotatedNode node) {
    // ignore annotations and their members
  }

  @Override
  public void visitPackage(PackageNode node) {
    withContext("package " + node.getText(), () -> {
      detectUnhygienicAst(node, "visitPackage");
      super.visitPackage(node);
    });
  }

  @Override
  public void visitImports(ModuleNode node) {
    withContext("module " + node.getText(), () -> {
      detectUnhygienicAst(node, "visitImports");
      super.visitImports(node);
    });
  }

  @Override
  public void visitConstructor(ConstructorNode node) {
    withContext("constructor " + node.getText(), () -> {
      detectUnhygienicAst(node, "visitConstructor");
      super.visitConstructor(node);
    });
  }

  @Override
  public void visitMethod(MethodNode node) {
    withContext("method " + node.getText(), () -> {
      detectUnhygienicAst(node, "visitMethod");
      super.visitMethod(node);
    });
  }

  @Override
  protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
    withContext("constructor or method " + node.getText(), () -> {
      detectUnhygienicAst(node, "visitConstructorOrMethod");
      super.visitConstructorOrMethod(node, isConstructor);
    });
  }

  @Override
  public void visitField(FieldNode node) {
    withContext("field node " + node.getName(), () -> {
      detectUnhygienicAst(node, "visitField");
      super.visitField(node);
    });
  }

  @Override
  public void visitProperty(PropertyNode node) {
    withContext("property node " + node.getName(), () -> {
      detectUnhygienicAst(node, "visitProperty");
      super.visitProperty(node);
    });
  }

  @Override
  protected void visitClassCodeContainer(Statement code) {
    withContext("class code " + (code == null ? code : code.getText()), () -> {
      detectUnhygienicAst(code, "visitClassCodeContainer");
      super.visitClassCodeContainer(code);
    });
  }

  @Override
  protected void visitObjectInitializerStatements(ClassNode node) {
    withContext("object initializer " + node.getText(), () -> {
      detectUnhygienicAst(node, "visitObjectInitializerStatements");
      super.visitObjectInitializerStatements(node);
    });
  }

  @Override
  public void visitDeclarationExpression(DeclarationExpression expression) {
    withContext("declaration " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitDeclarationExpression");
      super.visitDeclarationExpression(expression);
    });
  }

  @Override
  public void visitAssertStatement(AssertStatement statement) {
    withContext("assert " + statement.getText(), () -> {
      detectUnhygienicAst(statement, "visitAssertStatement");
      super.visitAssertStatement(statement);
    });
  }

  @Override
  public void visitBlockStatement(BlockStatement statement) {
    withContext("block " + statement.getText(), () -> {
      detectUnhygienicAst(statement, "visitBlockStatement");
      super.visitBlockStatement(statement);
    });
  }

  @Override
  public void visitBreakStatement(BreakStatement statement) {
    withContext("break " + statement.getText(), () -> {
      detectUnhygienicAst(statement, "visitBreakStatement");
      super.visitBreakStatement(statement);
    });
  }

  @Override
  public void visitCaseStatement(CaseStatement statement) {
    withContext("case " + statement.getText(), () -> {
      detectUnhygienicAst(statement, "visitCaseStatement");
      super.visitCaseStatement(statement);
    });
  }

  @Override
  public void visitCatchStatement(CatchStatement statement) {
    withContext("catch " + statement.getText(), () -> {
      detectUnhygienicAst(statement, "visitCatchStatement");
      super.visitCatchStatement(statement);
    });
  }

  @Override
  public void visitContinueStatement(ContinueStatement statement) {
    withContext("continue " + statement.getText(), () -> {
      detectUnhygienicAst(statement, "visitContinueStatement");
      super.visitContinueStatement(statement);
    });
  }

  @Override
  public void visitDoWhileLoop(DoWhileStatement statement) {
    withContext("do while " + statement.getText(), () -> {
      detectUnhygienicAst(statement, "visitDoWhileLoop");
      super.visitDoWhileLoop(statement);
    });
  }

  @Override
  public void visitExpressionStatement(ExpressionStatement statement) {
    withContext("expression " + statement.getText(), () -> {
      detectUnhygienicAst(statement, "visitExpressionStatement");
      super.visitExpressionStatement(statement);
    });
  }

  @Override
  public void visitForLoop(ForStatement statement) {
    withContext("for " + statement.getText(), () -> {
      detectUnhygienicAst(statement, "visitForLoop");
      super.visitForLoop(statement);
    });
  }

  @Override
  public void visitIfElse(IfStatement statement) {
    withContext("if " + statement.getText(), () -> {
      detectUnhygienicAst(statement, "visitIfElse");
      super.visitIfElse(statement);
    });
  }

  @Override
  public void visitReturnStatement(ReturnStatement statement) {
    withContext("return " + statement.getText(), () -> {
      detectUnhygienicAst(statement, "visitReturnStatement");
      super.visitReturnStatement(statement);
    });
  }

  @Override
  public void visitSwitch(SwitchStatement statement) {
    withContext("switch " + statement.getText(), () -> {
      detectUnhygienicAst(statement, "visitSwitch");
      super.visitSwitch(statement);
    });
  }

  // comment in if minimum Groovy version is 6.0
  //@Override
  //public void visitYieldStatement(YieldStatement statement) {
  //  withContext("yield " + expression.getText(), () -> {
  //    detectUnhygienicAst(statement, "visitYieldStatement");
  //    super.visitYieldStatement(statement);
  //  });
  //}

  @Override
  public void visitSynchronizedStatement(SynchronizedStatement statement) {
    withContext("synchronized " + statement.getText(), () -> {
      detectUnhygienicAst(statement, "visitSynchronizedStatement");
      super.visitSynchronizedStatement(statement);
    });
  }

  @Override
  public void visitThrowStatement(ThrowStatement statement) {
    withContext("throw " + statement.getText(), () -> {
      detectUnhygienicAst(statement, "visitThrowStatement");
      super.visitThrowStatement(statement);
    });
  }

  @Override
  public void visitTryCatchFinally(TryCatchStatement statement) {
    withContext("try-catch-finally", () -> {
      detectUnhygienicAst(statement, "visitTryCatchFinally");
      super.visitTryCatchFinally(statement);
    });
  }

  @Override
  public void visitWhileLoop(WhileStatement statement) {
    withContext("while " + statement.getText(), () -> {
      detectUnhygienicAst(statement, "visitWhileLoop");
      super.visitWhileLoop(statement);
    });
  }

  @Override
  protected void visitStatement(Statement statement) {
    withContext("statement " + statement.getText(), () -> {
      detectUnhygienicAst(statement, "visitStatement");
      super.visitStatement(statement);
    });
  }

  @Override
  public void visitEmptyStatement(EmptyStatement statement) {
    withContext("empty " + statement.getText(), () -> {
      detectUnhygienicAst(statement, "visitEmptyStatement");
      super.visitEmptyStatement(statement);
    });
  }

  @Override
  public void visitMethodCallExpression(MethodCallExpression call) {
    withContext("method call " + call.getText(), () -> {
      detectUnhygienicAst(call, "visitMethodCallExpression");
      super.visitMethodCallExpression(call);
    });
  }

  @Override
  public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
    withContext("static method call " + call.getText(), () -> {
      detectUnhygienicAst(call, "visitStaticMethodCallExpression");
      super.visitStaticMethodCallExpression(call);
    });
  }

  @Override
  public void visitConstructorCallExpression(ConstructorCallExpression call) {
    withContext("constructor call " + call.getText(), () -> {
      detectUnhygienicAst(call, "visitConstructorCallExpression");
      super.visitConstructorCallExpression(call);
    });
  }

  @Override
  public void visitBinaryExpression(BinaryExpression expression) {
    withContext("binary " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitBinaryExpression");
      super.visitBinaryExpression(expression);
    });
  }

  @Override
  public void visitTernaryExpression(TernaryExpression expression) {
    withContext("ternary " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitTernaryExpression");
      super.visitTernaryExpression(expression);
    });
  }

  // comment in if minimum Groovy version is 6.0
  //@Override
  //public void visitSwitchExpression(SwitchExpression expression) {
  //  withContext("switch " + expression.getText(), () -> {
  //    detectUnhygienicAst(expression, "visitSwitchExpression");
  //    super.visitSwitchExpression(expression);
  //  });
  //}

  @Override
  public void visitShortTernaryExpression(ElvisOperatorExpression expression) {
    withContext("elvis " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitShortTernaryExpression");
      super.visitShortTernaryExpression(expression);
    });
  }

  @Override
  public void visitPostfixExpression(PostfixExpression expression) {
    withContext("postfix " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitPostfixExpression");
      super.visitPostfixExpression(expression);
    });
  }

  @Override
  public void visitPrefixExpression(PrefixExpression expression) {
    withContext("prefix " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitPrefixExpression");
      super.visitPrefixExpression(expression);
    });
  }

  @Override
  public void visitBooleanExpression(BooleanExpression expression) {
    withContext("boolean " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitBooleanExpression");
      super.visitBooleanExpression(expression);
    });
  }

  @Override
  public void visitNotExpression(NotExpression expression) {
    withContext("not " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitNotExpression");
      super.visitNotExpression(expression);
    });
  }

  @Override
  public void visitClosureExpression(ClosureExpression expression) {
    withContext("closure " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitClosureExpression");
      super.visitClosureExpression(expression);
    });
  }

  // comment in if minimum Groovy version is 3.0
  //@Override
  //public void visitLambdaExpression(LambdaExpression expression) {
  //  withContext("lambda " + expression.getText(), () -> {
  //    detectUnhygienicAst(expression, "visitLambdaExpression");
  //    super.visitLambdaExpression(expression);
  //  });
  //}

  @Override
  public void visitTupleExpression(TupleExpression expression) {
    withContext("tuple " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitTupleExpression");
      super.visitTupleExpression(expression);
    });
  }

  @Override
  public void visitListExpression(ListExpression expression) {
    withContext("list " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitListExpression");
      super.visitListExpression(expression);
    });
  }

  @Override
  public void visitArrayExpression(ArrayExpression expression) {
    withContext("array " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitArrayExpression");
      super.visitArrayExpression(expression);
    });
  }

  @Override
  public void visitMapExpression(MapExpression expression) {
    withContext("map " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitMapExpression");
      super.visitMapExpression(expression);
    });
  }

  @Override
  public void visitMapEntryExpression(MapEntryExpression expression) {
    withContext("map entry " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitMapEntryExpression");
      super.visitMapEntryExpression(expression);
    });
  }

  @Override
  public void visitRangeExpression(RangeExpression expression) {
    withContext("range " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitRangeExpression");
      super.visitRangeExpression(expression);
    });
  }

  @Override
  public void visitSpreadExpression(SpreadExpression expression) {
    withContext("spread " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitSpreadExpression");
      super.visitSpreadExpression(expression);
    });
  }

  @Override
  public void visitSpreadMapExpression(SpreadMapExpression expression) {
    withContext("spread map " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitSpreadMapExpression");
      super.visitSpreadMapExpression(expression);
    });
  }

  @Override
  public void visitMethodPointerExpression(MethodPointerExpression expression) {
    withContext("method pointer " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitMethodPointerExpression");
      super.visitMethodPointerExpression(expression);
    });
  }

  // comment in if minimum Groovy version is 3.0
  //@Override
  //public void visitMethodReferenceExpression(MethodReferenceExpression expression) {
  //  withContext("method reference " + expression.getText(), () -> {
  //    detectUnhygienicAst(expression, "visitMethodReferenceExpression");
  //    super.visitMethodReferenceExpression(expression);
  //  });
  //}

  @Override
  public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
    withContext("unary minus " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitUnaryMinusExpression");
      super.visitUnaryMinusExpression(expression);
    });
  }

  @Override
  public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
    withContext("unary plus " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitUnaryPlusExpression");
      super.visitUnaryPlusExpression(expression);
    });
  }

  @Override
  public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
    withContext("bitwise negation " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitBitwiseNegationExpression");
      super.visitBitwiseNegationExpression(expression);
    });
  }

  @Override
  public void visitCastExpression(CastExpression expression) {
    withContext("cast " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitCastExpression");
      super.visitCastExpression(expression);
    });
  }

  @Override
  public void visitConstantExpression(ConstantExpression expression) {
    withContext("constant " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitConstantExpression");
      super.visitConstantExpression(expression);
    });
  }

  @Override
  public void visitClassExpression(ClassExpression expression) {
    withContext("class " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitClassExpression");
      super.visitClassExpression(expression);
    });
  }

  @Override
  public void visitVariableExpression(VariableExpression expression) {
    withContext("variable " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitVariableExpression");
      super.visitVariableExpression(expression);
    });
  }

  @Override
  public void visitPropertyExpression(PropertyExpression expression) {
    withContext("property " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitPropertyExpression");
      super.visitPropertyExpression(expression);
    });
  }

  @Override
  public void visitAttributeExpression(AttributeExpression expression) {
    withContext("attribute " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitAttributeExpression");
      super.visitAttributeExpression(expression);
    });
  }

  @Override
  public void visitFieldExpression(FieldExpression expression) {
    withContext("field " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitFieldExpression");
      super.visitFieldExpression(expression);
    });
  }

  @Override
  public void visitGStringExpression(GStringExpression expression) {
    withContext("gstring " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitGStringExpression");
      super.visitGStringExpression(expression);
    });
  }

  @Override
  public void visitArgumentlistExpression(ArgumentListExpression expression) {
    withContext("argument list " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitArgumentlistExpression");
      super.visitArgumentlistExpression(expression);
    });
  }

  @Override
  public void visitClosureListExpression(ClosureListExpression expression) {
    withContext("closure list " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitClosureListExpression");
      super.visitClosureListExpression(expression);
    });
  }

  @Override
  public void visitBytecodeExpression(BytecodeExpression expression) {
    withContext("bytecode " + expression.getText(), () -> {
      detectUnhygienicAst(expression, "visitBytecodeExpression");
      super.visitBytecodeExpression(expression);
    });
  }

  // comment in if minimum Groovy version is 3.0
  //@Override
  //public void visitEmptyExpression(EmptyExpression expression) {
  //  withContext("empty", () -> {
  //    detectUnhygienicAst(expression, "visitEmptyExpression");
  //    super.visitEmptyExpression(expression);
  //  });
  //}
}
