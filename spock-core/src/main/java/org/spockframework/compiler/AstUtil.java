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

import java.util.*;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

import org.spockframework.lang.Wildcard;
import org.spockframework.runtime.SpockRuntime;
import org.spockframework.util.InternalSpockError;
import org.spockframework.util.Nullable;

import spock.lang.Specification;

/**
 * Utility methods for AST processing.
 * 
 * @author Peter Niederwieser
 */
public abstract class AstUtil {
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
        Collections.<Statement> emptyList() : // it's not possible to add any statements to such a ClosureExpression, so immutable list is OK
        blockStat.getStatements();
  }

  public static boolean isMethodInvocation(Expression expr) {
    return expr instanceof MethodCallExpression
        || expr instanceof StaticMethodCallExpression;
  }

  public static Expression getInvocationTarget(Expression expr) {
    if (expr instanceof PropertyExpression)
      return ((PropertyExpression)expr).getObjectExpression();
    if (expr instanceof MethodCallExpression)
      return ((MethodCallExpression)expr).getObjectExpression();
    if (expr instanceof StaticMethodCallExpression)
      return new ClassExpression(((StaticMethodCallExpression)expr).getOwnerType());

    return null;
  }

  public static boolean isWildcardRef(Expression expr) {
    VariableExpression varExpr = AstUtil.asInstance(expr, VariableExpression.class);
    if (varExpr == null || !varExpr.getName().equals(Wildcard.INSTANCE.toString())) return false;

    Variable accessedVar = varExpr.getAccessedVariable();
    if (!(accessedVar instanceof FieldNode)) return false;

    return ((FieldNode) accessedVar).getOwner().getName().equals(Specification.class.getName());
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
    ExpressionStatement exprStat = asInstance(stat, ExpressionStatement.class);
    if (exprStat == null) return null;
    return asInstance(exprStat.getExpression(), type);
  }

  @SuppressWarnings("unchecked")
  public static @Nullable <T> T asInstance(Object obj, Class<T> type) {
    return type.isInstance(obj) ? (T) obj : null;
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

  // Note: The returned list may contain SpreadExpression's,
  // which can't be used outside an ArgumentListExpression.
  // To pass an argument list to the Groovy or Spock runtime,
  // use this method together with AstUtil.toArgumentArray().
  public static List<Expression> getArguments(Expression expr) {
    if (expr instanceof MethodCallExpression)
      return getArguments((MethodCallExpression)expr);
    else if (expr instanceof StaticMethodCallExpression)
      return getArguments((StaticMethodCallExpression)expr);
    else return null;
  }
  
  @SuppressWarnings("unchecked")
  public static List<Expression> getArguments(MethodCallExpression expr) {
    // MethodCallExpression.getArguments() may return an ArgumentListExpression,
    // a TupleExpression that wraps a NamedArgumentListExpression,
    // or (at least in 1.6) a NamedArgumentListExpression

    if (expr.getArguments() instanceof NamedArgumentListExpression)
      // return same result as for NamedArgumentListExpression wrapped in TupleExpression
      return Collections.singletonList(expr.getArguments());
    
    return ((TupleExpression) expr.getArguments()).getExpressions();
  }

  @SuppressWarnings("unchecked")
  public static List<Expression> getArguments(StaticMethodCallExpression expr) {
    // see comments in getArguments(MethodCallExpression)

    if (expr.getArguments() instanceof NamedArgumentListExpression)
      return Collections.singletonList(expr.getArguments());

    return ((TupleExpression)expr.getArguments()).getExpressions();
  }

  /**
   * Turns an argument list obtained from AstUtil.getArguments() into an Object[] array
   * suitable to be passed to InvokerHelper or SpockRuntime. The main challenge is
   * to handle SpreadExpression's correctly.
   */
  public static Expression toArgumentArray(List<Expression> argList, IRewriteResources resources) {
    List<Expression> normalArgs = new ArrayList<Expression>();
    List<Expression> spreadArgs = new ArrayList<Expression>();
    List<Expression> spreadPositions = new ArrayList<Expression>();

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

    return new MethodCallExpression(
        new ClassExpression(resources.getAstNodeCache().SpockRuntime),
        new ConstantExpression(SpockRuntime.DESPREAD_LIST),
        new ArgumentListExpression(
            new ArrayExpression(ClassHelper.OBJECT_TYPE, normalArgs),
            new ArrayExpression(ClassHelper.OBJECT_TYPE, spreadArgs),
            new ArrayExpression(ClassHelper.int_TYPE, spreadPositions)
        ));
  }

  public static boolean isBuiltinMemberAssignmentOrCall(Expression expr, String methodName, int minArgs, int maxArgs) {
    return expr instanceof BinaryExpression
        && isBuiltinMemberAssignment((BinaryExpression)expr, methodName, minArgs, maxArgs)
        || expr instanceof MethodCallExpression
        && isBuiltinMemberCall((MethodCallExpression) expr, methodName, minArgs, maxArgs);
  }

  public static boolean isBuiltinMemberAssignment(BinaryExpression expr, String methodName, int minArgs, int maxArgs) {
    return expr.getOperation().getType() == Types.ASSIGN
        && isBuiltinMemberAssignmentOrCall(expr.getRightExpression(), methodName, minArgs, maxArgs);
  }

  public static boolean isBuiltinMemberCall(Expression expr, String methodName, int minArgs, int maxArgs) {
    return expr instanceof MethodCallExpression
        && isBuiltinMemberCall((MethodCallExpression)expr, methodName, minArgs, maxArgs);
  }

  // call of the form "this.<methodName>(...)" or "<methodName>(...)"
  public static boolean isBuiltinMemberCall(MethodCallExpression expr, String methodName, int minArgs, int maxArgs) {
    return
        (isThisExpression(expr.getObjectExpression()) || isSuperExpression(expr.getObjectExpression()))
        && methodName.equals(expr.getMethodAsString()) // getMethodAsString() may return null
        && getArguments(expr).size() >= minArgs
        && getArguments(expr).size() <= maxArgs;
  }

  public static void expandBuiltinMemberAssignmentOrCall(Expression builtinMemberAssignmentOrCall, Expression... additionalArgs)
      throws InvalidSpecCompileException {
    if (builtinMemberAssignmentOrCall instanceof BinaryExpression)
      expandBuiltinMemberAssignment((BinaryExpression)builtinMemberAssignmentOrCall, additionalArgs);
    else
      expandBuiltinMemberCall(builtinMemberAssignmentOrCall, additionalArgs);
  }

  /*
   * Expands an assignment of the form "(def) x = builtinMethodCall(...)", where x is a field or local variable.
   * Assumes isBuiltinMemberAssignment(...).
   */
  public static void expandBuiltinMemberAssignment(BinaryExpression builtinMemberAssignment, Expression... additionalArgs)
      throws InvalidSpecCompileException {
    Expression builtinMemberCall = builtinMemberAssignment.getRightExpression();
    String name = getInferredName(builtinMemberAssignment);
    Expression type = getType(builtinMemberCall, getInferredType(builtinMemberAssignment));

    doExpandBuiltinMemberCall(builtinMemberCall, name, type, additionalArgs);
  }

  public static void expandBuiltinMemberCall(Expression builtinMemberCall, Expression... additionalArgs)
      throws InvalidSpecCompileException {
    doExpandBuiltinMemberCall(builtinMemberCall, null, getType(builtinMemberCall, null), additionalArgs);
  }

  private static Expression getType(Expression builtinMemberCall, Expression inferredType)
      throws InvalidSpecCompileException {
    List<Expression> args = AstUtil.getArguments(builtinMemberCall);
    assert args != null;

    if (args.isEmpty()) {
      if (inferredType == null)
        // TODO: improve error message (not clear that it originates from Mock() or thrown())
        throw new InvalidSpecCompileException(builtinMemberCall, "Type cannot be inferred; please specify one explicitely");
      return inferredType;
    }

    return args.get(0);
  }

  private static String getInferredName(BinaryExpression builtinMemberAssignment) {
    Expression left = builtinMemberAssignment.getLeftExpression();
    if (left instanceof Variable) return ((Variable)left).getName();
    if (left instanceof FieldExpression) return ((FieldExpression)left).getFieldName();
    return null;
  }

  private static ClassExpression getInferredType(BinaryExpression builtinMemberAssignment) {
    ClassNode type = builtinMemberAssignment.getLeftExpression().getType();
    return type == null || type == ClassHelper.DYNAMIC_TYPE ?
        null : new ClassExpression(type);
  }

  private static void doExpandBuiltinMemberCall(Expression builtinMemberCall, String name,
      Expression type, Expression... additionalArgs) {
    checkIsSafeToMutateArgs(builtinMemberCall);

    List<Expression> args = getArguments(builtinMemberCall);
    args.clear();
    args.add(type);
    args.add(new ConstantExpression(name));
    args.addAll(Arrays.asList(additionalArgs));
  }

  private static void checkIsSafeToMutateArgs(Expression methodCall) {
    Expression args = ((MethodCallExpression)methodCall).getArguments();

    if (args == ArgumentListExpression.EMPTY_ARGUMENTS)
      throw new InternalSpockError("checkIsSafeToMutateArgs");
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
        && ((VariableExpression)expr).isThisExpression();
  }

  public static boolean isSuperExpression(Expression expr) {
    return expr instanceof VariableExpression
        && ((VariableExpression)expr).isSuperExpression();
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

  public static boolean isJointCompiled(ClassNode clazz) {
    return clazz.getModule().getUnit().getConfig().getJointCompilationOptions() != null;
  }
  
  public static MethodCallExpression createDirectMethodCall(Expression target, MethodNode method, Expression arguments) {
    MethodCallExpression result = new MethodCallExpression(target, method.getName(), arguments);
    // don't generate direct method calls for the moment, due to:
    // http://groovy.329449.n5.nabble.com/Problem-with-latest-2-0-beta-3-snapshot-and-Spock-td5496353.html
    //result.setMethodTarget(method);
    return result;
  }

  public static void deleteMethod(ClassNode clazz, MethodNode method) {
    // as of 1.8.6, this is the best possible implementation
    clazz.getMethods().remove(method);
    clazz.getDeclaredMethods(method.getName()).remove(method);
  }
}
