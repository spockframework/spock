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

package org.spockframework.util.inspector;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.*;
import org.intellij.lang.annotations.Language;
import org.spockframework.compiler.AstUtil;

import java.io.File;
import java.io.IOException;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

/**
 * Utility class for inspecting the abstract syntax tree (AST) produced by
 * the Groovy compiler. Provides convenient ways to directly access AST nodes
 * without having to navigate the AST from its root.
 *
 * <p>Nodes representing class/method/property/field declarations are most easily
 * accessed by name. In the case of ambiguity the first node found is returned.
 * Another, more fine-grained but slightly invasive way to access declarations
 * is to annotate them with @Inspect.
 *
 * <p>Individual statements and expressions can be accessed by prepending them
 * with a label, or by wrapping them in an "inspect_" method call. See
 * AstInspectorTest for examples.
 *
 * <p>Code example:
 * <pre>
 * def inspector = new AstInspector(CompilePhase.SEMANTIC_ANALYSIS)
 * inspector.load("def foo() { label: println 'hi!' }")
 * def expr = inspector.getExpression("label")
 * assert expr instanceof MethodCallExpression
 * </pre
 *
 * @author Peter Niederwieser
 */
public class AstInspector {
  private static final String EXPRESSION_MARKER_PREFIX = "inspect_";

  private CompilePhase compilePhase = CompilePhase.CONVERSION;
  private boolean throwOnNodeNotFound = true;
  private final MyClassLoader classLoader;
  private final MyVisitor visitor = new MyVisitor();

  private ModuleNode module;
  private final Map<String, AnnotatedNode> markedNodes = new HashMap<>();
  private final Map<String, ClassNode> classes = new HashMap<>();
  private final Map<String, FieldNode> fields = new HashMap<>();
  private final Map<String, PropertyNode> properties = new HashMap<>();
  private final Map<String, ConstructorNode> constructors = new HashMap<>();
  private final Map<String, MethodNode> methods = new HashMap<>();
  private final Map<String, Statement> statements = new HashMap<>();
  private final Map<String, Expression> expressions = new HashMap<>();

  /**
   * Constructs an AstInspector, configuring the GroovyClassLoader used
   * underneath with default values.
   */
  public AstInspector() {
    classLoader = new MyClassLoader(AstInspector.class.getClassLoader(), null);
  }

  /**
   * Convenience constructor that calls the default constructor and
   * additionally sets the specified compile phase.
   *
   * @see #setCompilePhase
   * @param phase the compile phase up to which compilation should proceed
   */
  public AstInspector(CompilePhase phase) {
    this();
    setCompilePhase(phase);
  }

  /**
   * Constructs an AstInspector, configuring the GroovyClassLoader used
   * underneath with the specified parent class loader and compiler configuration.
   *
   * @param parent the parent class loader for the GroovyClassLoader used underneath
   * @param config the compiler configuration for the GroovyClassLoader used underneath
   */
  public AstInspector(ClassLoader parent, CompilerConfiguration config) {
    classLoader = new MyClassLoader(parent, config);
  }

  /**
   * Sets the compile phase up to which compilation should proceed. Defaults to
   * CompilePhase.CONVERSION (the phase in which the AST is first constructed).
   *
   * @param phase the compile phase up to which compilation should proceed
   * @throws IllegalArgumentException if a compile phase before
   * CompilePhase.CONVERSION is specified
   */
  public void setCompilePhase(CompilePhase phase) {
    if (phase.getPhaseNumber() < CompilePhase.CONVERSION.getPhaseNumber())
      throw new IllegalArgumentException("AST is only available from phase CONVERSION onwards");
    compilePhase = phase;
  }

  /**
   * Controls whether to throw an AstInspectorException or to return <tt>null</tt>
   * when a getXXX() method cannot find a matching AST node. Defaults to <tt>true</tt>.
   *
   * @param flag <tt>true</tt> if an exception should be thrown,
   * <tt>false</tt> otherwise
   */
  public void setThrowOnNodeNotFound(boolean flag) {
    throwOnNodeNotFound = flag;
  }

  /**
   * Compiles the specified source text up to the configured compile
   * phase and stores the resulting AST for subsequent inspection.
   *
   * @param sourceText the source text to compile
   * @throws CompilationFailedException if an error occurs during compilation
   */
  public void load(@Language("Groovy") String sourceText) throws CompilationFailedException {
    reset();

    try {
      classLoader.parseClass(sourceText);
    } catch (AstSuccessfullyCaptured e) {
      indexAstNodes();
      return;
    }

    throw new AstInspectorException("internal error");
  }

  /**
   * Compiles the source text in the specified file up to the
   * configured compile phase and stores the resulting AST for subsequent
   * inspection.
   *
   * @param sourceFile the file containing the source text to compile
   * @throws CompilationFailedException if an error occurs during compilation
   * @throws AstInspectorException if an IOException occurs when reading from
   * the file
   */
  public void load(File sourceFile) throws CompilationFailedException {
    reset();

    try {
      classLoader.parseClass(sourceFile);
    } catch (IOException e) {
      throw new AstInspectorException("cannot read source file", e);
    } catch (AstSuccessfullyCaptured e) {
      indexAstNodes();
      return;
    }

    throw new AstInspectorException("internal error");
  }

  /**
   * Returns the root of the inspected AST.
   *
   * @return the root of the inspected AST
   */
  public ModuleNode getModule() {
    return module;
  }

  /**
   * Returns the first declaration found that
   * is marked with an @Inspect annotation with the specified name.
   *
   * @param name the name specified in the <tt>@Inspect</tt> annotation marking
   * the declaration of interest
   * @return the first declaration found that is marked with an <tt>@Inspect</tt>
   * annotation with the specified name
   */
  public AnnotatedNode getMarkedNode(String name) {
    return getNode(markedNodes, name);
  }

  /**
   * Returns the first class found with the specified
   * simple name.
   *
   * @param name the simple name of the class of interest
   * @return the first class found with the specified simple name
   */
  public ClassNode getClass(String name) {
    return getNode(classes, name);
  }

  /**
   * Returns the first field found with the specified name.
   *
   * @param name the name of the field of interest
   * @return the first field found with the specified name
   */
  public FieldNode getField(String name) {
    return getNode(fields, name);
  }

  /**
   * Returns the first property found with the specified name.
   *
   * @param name the name of the property of interest
   * @return the first property found with the specified name
   */
  public PropertyNode getProperty(String name) {
    return getNode(properties, name);
  }

  /**
   * Returns the first constructor found in the class with the specified simple name.
   *
   * @param className the simple name of the class declaring the constructor of
   * interest
   * @return the first constructor found in the class with the specified simple name
   */
  public ConstructorNode getConstructor(String className) {
    return getNode(constructors, className);
  }

  /**
   * Returns the first method found with the specified name (including both
   * script and class methods).
   *
   * @param name the name of the method of interest
   * @return the first method found with the specified name
   */
  public MethodNode getMethod(String name) {
    return getNode(methods, name);
  }

  /**
   * Returns the top-level statements in a script. If no such statements are
   * found, an empty list is returned.
   *
   * @return the top-level statements in a script
   */
  public List<Statement> getScriptStatements() {
    return getStatements(module.getStatementBlock());
  }

  /**
   * Returns the top-level expressions in a script. If no such expressions are
   * found, an empty list is returned.
   *
   * @return the top-level expressions in a script
   */
  public List<Expression> getScriptExpressions() {
    return getExpressions(getScriptStatements());
  }

  /**
   * Returns the top-level statements in the specified method or constructor.
   * If no such statements are found, an empty
   * list is returned.
   *
   * @param node a MethodNode representing a method or constructor
   * @return the top-level statements in the specified method or constructor
   */
  public List<Statement> getStatements(MethodNode node) {
    return getStatements((BlockStatement)node.getCode());
  }

  /**
   * Returns the top-level expressions in the specified method or constructor.
   * If no such expressions are found, an empty list is returned.
   *
   * @param node a MethodNode representing a method or constructor
   * @return the top-level expressions in the specified method or constructor
   */
  public List<Expression> getExpressions(MethodNode node) {
    return getExpressions(getStatements(node));
  }

  /**
   * Returns the top-level statements in the specified closure definition.
   * If no such statements are found, an empty list is returned.
   *
   * @param expr a ClosureExpression representing a closure definition
   * @return the top-level statements in the specified closure definition
   */
  public List<Statement> getStatements(ClosureExpression expr) {
    return getStatements((BlockStatement)expr.getCode());
  }

  /**
   * Returns the top-level expressions in the specified closure definition.
   * If no such expressions are found, an empty list is returned.
   *
   * @param expr a ClosureExpression representing a closure definition
   * @return the top-level expressions in the specified closure definition
   */
  public List<Expression> getExpressions(ClosureExpression expr) {
    return getExpressions(getStatements(expr));
  }

  /**
   * Returns the first statement found immediately preceded by a label with the
   * specified name.
   *
   * @param name the name of the label immediately preceding the statement of
   * interest
   * @return the first statement found immediately preceded by a label with the
   * specified name
   */
  public Statement getStatement(String name) {
    return getNode(statements, name);
  }

  /**
   * Returns the first expression found that is
   * either immediately preceded by a label with the specified name, or is the
   * single argument in a method call of the form "inspect_name(expression)".
   *
   * <p>Example:
   * <pre>
   * def inspector = new AstInspector()
   * inspector.load("fooBar: foo.bar(inspect_firstArg(a), b)")
   * def fooBar = inspector.getExpression("fooBar")
   * def firstArg = inspector.getExpression("firstArg")
   * </pre>
   *
   * @param name the name of a label immediately preceding the expression of
   * interest, or NAME in a method call "inspect_NAME" wrapping the expression
   * of interest
   * @return the Expression representing the first matching expression
   */
  public Expression getExpression(String name) {
    return getNode(expressions, name);
  }

  @SuppressWarnings("unchecked")
  private void indexAstNodes() {
    visitor.visitBlockStatement(module.getStatementBlock());

    for (MethodNode method : module.getMethods())
      visitor.visitMethod(method);

    for (ClassNode clazz : module.getClasses())
      visitor.visitClass(clazz);
  }

  private void reset() {
    module = null;
    markedNodes.clear();
    classes.clear();
    fields.clear();
    properties.clear();
    constructors.clear();
    methods.clear();
    statements.clear();
    expressions.clear();
  }

  private <T extends ASTNode> void addNode(Map<String, T> map, String name, T node) {
    if (!map.containsKey(name)) map.put(name, node);
  }

  private <T> T getNode(Map<String, T> nodes, String key) {
    T node = nodes.get(key);
    if (node == null && throwOnNodeNotFound)
      throw new AstInspectorException(
        String.format("cannot find a node named '%s' of the requested kind", key));
    return node;
  }

  @SuppressWarnings("unchecked")
  private static List<Statement> getStatements(BlockStatement blockStat) {
    return blockStat == null
      ? emptyList()
      : blockStat.getStatements();
  }

  private static List<Expression> getExpressions(List<Statement> statements) {
     List<Expression> exprs = new ArrayList<>();
    for (Statement stat : statements)
      if (stat instanceof ExpressionStatement)
        exprs.add(((ExpressionStatement)stat).getExpression());
    return exprs;
  }

  private class MyClassLoader extends GroovyClassLoader {
    MyClassLoader(ClassLoader parent, CompilerConfiguration config) {
      super(parent, config);
    }

    @Override
    protected CompilationUnit createCompilationUnit(CompilerConfiguration config, CodeSource source) {
      CompilationUnit unit = super.createCompilationUnit(config, source);
      unit.addPhaseOperation(new CompilationUnit.SourceUnitOperation() {
        @Override
        public void call(SourceUnit source) throws CompilationFailedException {
          module = source.getAST();
          throw new AstSuccessfullyCaptured();
        }
      }, compilePhase.getPhaseNumber());
      return unit;
    }
  }

  private class MyVisitor extends ClassCodeVisitorSupport {
    @Override
    @SuppressWarnings("unchecked")
    public void visitAnnotations(AnnotatedNode node) {
      for (AnnotationNode an : node.getAnnotations()) {
        ClassNode cn = an.getClassNode();

        // this comparison should be good enough, and also works in phase conversion
        if (cn.getNameWithoutPackage().equals(Inspect.class.getSimpleName())) {
          ConstantExpression name = (ConstantExpression)an.getMember("value");
          if (name == null || !(name.getValue() instanceof String))
            throw new AstInspectorException("@Inspect must have a String argument");
          addNode(markedNodes, (String)name.getValue(), node);
          break;
        }
      }

      super.visitAnnotations(node);
    }

    @Override
    public void visitClass(ClassNode node) {
      addNode(classes, node.getNameWithoutPackage(), node);
      super.visitClass(node);
    }

    @Override
    public void visitField(FieldNode node) {
      addNode(fields, node.getName(), node);
      super.visitField(node);
    }

    @Override
    public void visitProperty(PropertyNode node) {
      addNode(properties, node.getName(), node);
      super.visitProperty(node);
    }

    @Override
    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
      // ClassCodeVisitorSupport doesn't seem to visit parameters
      for (Parameter param: node.getParameters()) {
        visitAnnotations(param);
        if (param.getInitialExpression() != null)
          param.getInitialExpression().visit(this);
      }
      super.visitConstructorOrMethod(node, isConstructor);
    }

    @Override
    public void visitConstructor(ConstructorNode node) {
      addNode(constructors, node.getDeclaringClass().getNameWithoutPackage(), node);
      super.visitConstructor(node);
    }

    @Override
    public void visitMethod(MethodNode node) {
      addNode(methods, node.getName(), node);
      super.visitMethod(node);
    }

    @Override
    public void visitStatement(Statement node) {
      if (node.getStatementLabel() != null) {
        addNode(statements, node.getStatementLabel(), node);
        if (node instanceof ExpressionStatement)
          addNode(expressions, node.getStatementLabel(),
            ((ExpressionStatement)node).getExpression());
      }
      super.visitStatement(node);
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression node) {
      if (node.isImplicitThis()) {
        doVisitMethodCall(node);
      }
      super.visitMethodCallExpression(node);
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression node) {
      // note: we don't impose any constraints on the receiver type here
      doVisitMethodCall(node);
      super.visitStaticMethodCallExpression(node);
    }

    private void doVisitMethodCall(Expression node) {
      String methodName = AstUtil.getMethodName(node);
      if (methodName != null && methodName.startsWith(EXPRESSION_MARKER_PREFIX)) {
        ArgumentListExpression args = (ArgumentListExpression) AstUtil.getArguments(node);
        if (args != null && args.getExpressions().size() == 1)
          addNode(expressions, methodName.substring(EXPRESSION_MARKER_PREFIX.length()),
              args.getExpressions().get(0));
      }
    }

    @Override
    protected SourceUnit getSourceUnit() {
      throw new AstInspectorException("internal error");
    }
  }

  private static class AstSuccessfullyCaptured extends Error {}
}
