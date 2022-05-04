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

package org.spockframework.compiler;

import org.spockframework.lang.Wildcard;
import org.spockframework.util.*;
import spock.lang.Specification;

import java.util.*;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.runtime.dgmimpl.arrays.IntegerArrayGetAtMetaMethod;
import org.objectweb.asm.Opcodes;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * Utility methods for AST processing.
 *
 * @author Peter Niederwieser
 */
public abstract class AstUtil {
  private static final Pattern DATA_TABLE_SEPARATOR = Pattern.compile("_{2,}+");
  private static final String GET_METHOD_NAME = "get";
  private static final String GET_AT_METHOD_NAME = new IntegerArrayGetAtMetaMethod().getName();

  /**
   * Tells whether the given node has an annotation of the given type.
   *
   * @param node an AST node
   * @param annotationType an annotation type
   * @return <tt>true</tt> iff the given node has an annotation of the given type
   */
  public static boolean hasAnnotation(ASTNode node, Class<?> annotationType) {
    return getAnnotation(node, annotationType) != null;
  }

  public static AnnotationNode getAnnotation(ASTNode node, Class<?> annotationType) {
    if (!(node instanceof AnnotatedNode)) return null;

    AnnotatedNode annotated = (AnnotatedNode)node;
    @SuppressWarnings("unchecked")
    List<AnnotationNode> annotations = annotated.getAnnotations();
    for (AnnotationNode a : annotations)
      if (a.getClassNode().getName().equals(annotationType.getName())) return a;
    return null;
  }

  /**
   * Returns a list of statements of the given method. Modifications to the returned list
   * will affect the method's statements.
   *
   * @param method a method (node)
   * @return a list of statements of the given method
   */
  @SuppressWarnings("unchecked")
  public static List<Statement> getStatements(MethodNode method) {
    Statement code = method.getCode();
    if (!(code instanceof BlockStatement)) { // null or single statement
      BlockStatement block = new BlockStatement();
      if (code != null) block.addStatement(code);
      method.setCode(block);
    }
    return ((BlockStatement)method.getCode()).getStatements();
  }

  @SuppressWarnings("unchecked")
  public static List<Statement> getStatements(ClosureExpression closure) {
    BlockStatement blockStat = (BlockStatement)closure.getCode();
    return blockStat == null ?
        emptyList() : // it's not possible to add any statements to such a ClosureExpression, so immutable list is OK
        blockStat.getStatements();
  }

  public static boolean isInvocationWithImplicitThis(Expression invocation) {
    if (invocation instanceof PropertyExpression) {
      return ((PropertyExpression) invocation).isImplicitThis();
    }
    if (invocation instanceof MethodCallExpression) {
      return ((MethodCallExpression) invocation).isImplicitThis();
    }
    return false;
  }

  public static boolean hasImplicitParameter(ClosureExpression expr) {
    return expr.getParameters() != null && expr.getParameters().length == 0;
  }

  public static Expression getImplicitParameterRef(ClosureExpression expr) {
    assert hasImplicitParameter(expr);

    DynamicVariable var = new DynamicVariable("it", false);
    return new VariableExpression(var);
  }

  public static Expression getInvocationTarget(Expression expr) {
    if (expr instanceof PropertyExpression)
      return ((PropertyExpression) expr).getObjectExpression();
    if (expr instanceof MethodCallExpression)
      return ((MethodCallExpression) expr).getObjectExpression();
    if (expr instanceof StaticMethodCallExpression)
      return new ClassExpression(((StaticMethodCallExpression) expr).getOwnerType());
    if (expr instanceof ConstructorCallExpression)
      return new ClassExpression(expr.getType());
    return null;
  }

  public static boolean isWildcardRef(Expression expr) {
    VariableExpression varExpr = ObjectUtil.asInstance(expr, VariableExpression.class);
    if (varExpr == null || !varExpr.getName().equals(Wildcard.INSTANCE.toString())) return false;

    Variable accessedVar = varExpr.getAccessedVariable();
    if (!(accessedVar instanceof FieldNode)) return false;

    return ((FieldNode) accessedVar).getOwner().getName().equals(Specification.class.getName());
  }

  /**
   * Checks whether the given statement represents a data table separator.
   * A data table separator is an expression statement consisting of a
   * variable expression whose name is two or more underscores and
   * that does not reference any existing variable, parameter or field.
   *
   * @param stat the statement to check for being a data table separator
   * @return whether the given statement is a data table separator
   */
  public static boolean isDataTableSeparator(Statement stat) {
    VariableExpression varExpr  = getExpression(stat, VariableExpression.class);
    if (varExpr == null || !DATA_TABLE_SEPARATOR.matcher(varExpr.getName()).matches()) return false;

    Variable accessedVar = varExpr.getAccessedVariable();
    return accessedVar instanceof DynamicVariable;
  }

  public static boolean isJavaIdentifier(String id) {
    if (id == null || id.length() == 0
        || !Character.isJavaIdentifierStart(id.charAt(0)))
      return false;

    for (int i = 1; i < id.length(); i++)
      if (!Character.isJavaIdentifierPart(id.charAt(i)))
        return false;

    return true;
  }

  public static <T extends Expression> T getExpression(Statement stat, Class<T> type) {
    ExpressionStatement exprStat = ObjectUtil.asInstance(stat, ExpressionStatement.class);
    if (exprStat == null) return null;
    return ObjectUtil.asInstance(exprStat.getExpression(), type);
  }

  public static boolean isSynthetic(MethodNode method) {
    return method.isSynthetic() || (method.getModifiers() & Opcodes.ACC_SYNTHETIC) != 0;
  }

  /**
   * Tells if the source position for the given AST node is plausible.
   * Does not imply that the source position is correct.
   *
   * @param node an AST node
   * @return <tt>true</tt> iff the AST node has a plausible source position
   */
  public static boolean hasPlausibleSourcePosition(ASTNode node) {
    return node.getLineNumber() > 0 && node.getLastLineNumber() >= node.getLineNumber()
        && node.getColumnNumber() > 0 && node.getLastColumnNumber() > node.getColumnNumber();
  }

  public static String getMethodName(Expression invocation) {
    if (invocation instanceof MethodCallExpression) {
      return ((MethodCallExpression) invocation).getMethodAsString();
    }
    if (invocation instanceof StaticMethodCallExpression) {
      return ((StaticMethodCallExpression) invocation).getMethod();
    }
    return null;
  }

  public static Expression getArguments(Expression invocation) {
    if (invocation instanceof MethodCallExpression)
      return ((MethodCallExpression) invocation).getArguments();
    if (invocation instanceof StaticMethodCallExpression)
      return ((StaticMethodCallExpression) invocation).getArguments();
    if (invocation instanceof ConstructorCallExpression)
      return ((ConstructorCallExpression) invocation).getArguments();
    return null;
  }

  // Note: The returned list may contain SpreadExpression's,
  // which can't be used outside an ArgumentListExpression.
  // To pass an argument list to the Groovy or Spock runtime,
  // use this method together with AstUtil.toArgumentArray().
  public static List<Expression> getArgumentList(Expression invocation) {
    return asArgumentList(getArguments(invocation));
  }

  private static List<Expression> asArgumentList(Expression arguments) {
    // MethodCallExpression.getArguments() may return an ArgumentListExpression,
    // a TupleExpression that wraps a NamedArgumentListExpression,
    // or (at least in 1.6) a NamedArgumentListExpression

    if (arguments instanceof NamedArgumentListExpression)
      // return same result as for NamedArgumentListExpression wrapped in TupleExpression
      return singletonList(arguments);

    return ((TupleExpression) arguments).getExpressions();
  }

  /**
   * Turns an argument list obtained from AstUtil.getArguments() into an Object[] array
   * suitable to be passed to InvokerHelper or SpockRuntime. The main challenge is
   * to handle SpreadExpression's correctly.
   */
  public static Expression toArgumentArray(List<Expression> argList, IRewriteResources resources) {
    List<Expression> normalArgs = new ArrayList<>();
    List<Expression> spreadArgs = new ArrayList<>();
    List<Expression> spreadPositions = new ArrayList<>();

    for (int i = 0; i < argList.size(); i++) {
      Expression arg = argList.get(i);
      if (arg instanceof SpreadExpression) {
        spreadArgs.add(((SpreadExpression) arg).getExpression());
        spreadPositions.add(new ConstantExpression(i));
      } else {
        normalArgs.add(arg);
      }
    }

    if (spreadArgs.isEmpty())
      return new ArrayExpression(ClassHelper.OBJECT_TYPE, argList);

    AstNodeCache astNodeCache = resources.getAstNodeCache();
    return createDirectMethodCall(
      new ClassExpression(astNodeCache.SpockRuntime),
      astNodeCache.SpockRuntime_DespreadList,
      new ArgumentListExpression(
        new ArrayExpression(ClassHelper.OBJECT_TYPE, normalArgs),
        new ArrayExpression(ClassHelper.OBJECT_TYPE, spreadArgs),
        new ArrayExpression(ClassHelper.int_TYPE, spreadPositions)
      ));
  }

  public static void copySourcePosition(ASTNode from, ASTNode to) {
    to.setLineNumber(from.getLineNumber());
    to.setLastLineNumber(from.getLastLineNumber());
    to.setColumnNumber(from.getColumnNumber());
    to.setLastColumnNumber(from.getLastColumnNumber());
  }

  @Nullable
  public static Expression getAssertionMessage(AssertStatement stat) {
    Expression msg = stat.getMessageExpression();
    if (msg == null) return null; // should not happen
    if (!(msg instanceof ConstantExpression)) return msg;
    return ((ConstantExpression) msg).isNullExpression() ? null : msg;
  }

  public static boolean isThisExpression(Expression expr) {
    return expr instanceof VariableExpression
        && ((VariableExpression) expr).isThisExpression();
  }

  public static boolean isSuperExpression(Expression expr) {
    return expr instanceof VariableExpression
        && ((VariableExpression) expr).isSuperExpression();
  }

  public static boolean isThisOrSuperExpression(Expression expr) {
    return isThisExpression(expr) || isSuperExpression(expr);
  }

  public static void setVisibility(MethodNode method, int visibility) {
    int modifiers = method.getModifiers();
    modifiers &= ~(Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE);
    method.setModifiers(modifiers | visibility);
  }

  public static int getVisibility(FieldNode field) {
    return field.getModifiers() & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE);
  }

  public static void setVisibility(FieldNode field, int visibility) {
    int modifiers = field.getModifiers();
    modifiers &= ~(Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE);
    field.setModifiers(modifiers | visibility);
  }

  public static void setFinal(FieldNode field, boolean isFinal) {
    int modifiers = field.getModifiers();
    if (isFinal) {
      modifiers |= Opcodes.ACC_FINAL;
    } else {
      modifiers &= ~(Opcodes.ACC_FINAL);
    }
    field.setModifiers(modifiers);
  }

  public static boolean isJointCompiled(ClassNode clazz) {
    return clazz.getModule().getUnit().getConfig().getJointCompilationOptions() != null;
  }

  public static MethodCallExpression createMethodCall(Expression target, String methodName, Expression arguments) {
    MethodCallExpression result = new MethodCallExpression(target, methodName, arguments);
    // see http://groovy.329449.n5.nabble.com/Problem-with-latest-2-0-beta-3-snapshot-and-Spock-td5496353.html
    // and https://github.com/spockframework/spock/issues/1200
    // and https://issues.apache.org/jira/browse/GROOVY-9651
    result.setImplicitThis(false);
    return result;
  }

  public static MethodCallExpression createDirectMethodCall(Expression target, MethodNode method, Expression arguments) {
    MethodCallExpression result = createMethodCall(target, method.getName(), arguments);
    result.setMethodTarget(method);
    return result;
  }

  public static void deleteMethod(ClassNode clazz, MethodNode method) {
    // as of 1.8.6, this is the best possible implementation
    clazz.getMethods().remove(method);
    clazz.getDeclaredMethods(method.getName()).remove(method);
  }

  public static Expression getVariableName(BinaryExpression assignment) {
    Expression left = assignment.getLeftExpression();
    if (left instanceof Variable) return new ConstantExpression(((Variable) left).getName());
    if (left instanceof FieldExpression) return new ConstantExpression(((FieldExpression) left).getFieldName());
    return ConstantExpression.NULL;
  }

  public static Expression getVariableType(BinaryExpression assignment) {
    ClassNode type = assignment.getLeftExpression().getType();
    return type == null || isDynamicTypedExpression(assignment.getLeftExpression()) ? ConstantExpression.NULL : new ClassExpression(type);
  }

  private static boolean isDynamicTypedExpression(Expression leftExpression) {
    if (leftExpression instanceof VariableExpression) {
      return  ((VariableExpression) leftExpression).isDynamicTyped();
    }
    if (leftExpression instanceof FieldExpression) {
      return  ((FieldExpression) leftExpression).isDynamicTyped();
    }
    if (leftExpression instanceof PropertyExpression) {
      return  ((PropertyExpression) leftExpression).isDynamic();
    }
    return false;
  }

  public static MethodCallExpression createGetAtMethodCall(Expression expression, int index) {
    return createMethodCall(expression, GET_AT_METHOD_NAME, new ConstantExpression(index));
  }
}
