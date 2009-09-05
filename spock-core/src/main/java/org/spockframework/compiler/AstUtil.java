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

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;
import org.spockframework.util.*;
import org.spockframework.compiler.model.Method;

import spock.lang.Predef;

import java.util.*;

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
    if (expr instanceof MethodCallExpression)
      return ((MethodCallExpression)expr).getObjectExpression();
    if (expr instanceof StaticMethodCallExpression)
      return new ClassExpression(((StaticMethodCallExpression)expr).getOwnerType());
    if (expr instanceof PropertyExpression)
      return ((PropertyExpression)expr).getObjectExpression();
    
    return null;
  }

  public static boolean isPlaceholderVariableRef(Expression expr) {
    if (!(expr instanceof PropertyExpression)) return false;

    PropertyExpression propExpr = (PropertyExpression)expr;
    if (!(propExpr.getObjectExpression() instanceof ClassExpression)) return false;

    ClassExpression classExpr = (ClassExpression)propExpr.getObjectExpression();
    return Predef.class.getName().equals(classExpr.getType().getName())
        && Predef._.toString().equals(propExpr.getPropertyAsString());
  }

  public static boolean isInteraction(Statement stat) {
    BinaryExpression binExpr = AstUtil.getExpression(stat, BinaryExpression.class);
    if (binExpr == null) return false;
    int type = binExpr.getOperation().getType();
    return type == Types.MULTIPLY || type == Types.RIGHT_SHIFT
        || type == Types.RIGHT_SHIFT_UNSIGNED;
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
    if (!(stat instanceof ExpressionStatement)) return null;
    Expression expr = ((ExpressionStatement)stat).getExpression();
    if (!type.isInstance(expr)) return null;
    return type.cast(expr);
  }

  public static boolean isSynthetic(MethodNode method) {
    return method.isSynthetic()
        || (method.getModifiers() & Opcodes.ACC_SYNTHETIC) != 0;
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

  public static List<Expression> getArguments(Expression expr) {
    if (expr instanceof MethodCallExpression)
      return getArguments((MethodCallExpression)expr);
    else if (expr instanceof StaticMethodCallExpression)
      return getArguments((StaticMethodCallExpression)expr);
    else return null;
  }
  
  @SuppressWarnings("unchecked")
  public static List<Expression> getArguments(MethodCallExpression expr) {
    // seems getArguments() could either return a TupleExpression or an ArgumentListExpression
    // we cast to the supertype to (hopefully) be on the safe side
    return ((TupleExpression)expr.getArguments()).getExpressions();
  }

  @SuppressWarnings("unchecked")
  public static List<Expression> getArguments(StaticMethodCallExpression expr) {
    // it seems that getArguments() could either return a TupleExpression or
    // an ArgumentListExpression; we cast to the supertype to be on the safe side
    return ((TupleExpression)expr.getArguments()).getExpressions();
  }

  public static boolean isPredefDeclOrCall(Expression expr, String methodName, int minArgs, int maxArgs) {
    return expr instanceof BinaryExpression
        && isPredefDecl((BinaryExpression)expr, methodName, minArgs, maxArgs)
        || expr instanceof MethodCallExpression
        && isPredefCall((MethodCallExpression) expr, methodName, minArgs, maxArgs)
        || expr instanceof StaticMethodCallExpression
        && isPredefCall((StaticMethodCallExpression)expr, methodName, minArgs, maxArgs);
  }

  public static boolean isPredefDecl(BinaryExpression expr, String methodName, int minArgs, int maxArgs) {
    return (expr instanceof DeclarationExpression
        || expr instanceof FieldInitializationExpression)
        && isPredefDeclOrCall(expr.getRightExpression(), methodName, minArgs, maxArgs);
  }

  public static boolean isPredefCall(Expression expr, String methodName, int minArgs, int maxArgs) {
    return expr instanceof MethodCallExpression
        && isPredefCall((MethodCallExpression)expr, methodName, minArgs, maxArgs)
        || expr instanceof StaticMethodCallExpression
        && isPredefCall((StaticMethodCallExpression)expr, methodName, minArgs, maxArgs);
  }

  // call of the form "Predef.<methodName>(...)" or "<methodName>(...)"
  public static boolean isPredefCall(MethodCallExpression expr, String methodName, int minArgs, int maxArgs) {
    Expression target = expr.getObjectExpression();
    return
        (isPredefClassExpression(target) || expr.isImplicitThis())
        && methodName.equals(expr.getMethodAsString()) // getMethodAsString() may return null
        && getArguments(expr).size() >= minArgs
        && getArguments(expr).size() <= maxArgs;
  }

  private static boolean isPredefClassExpression(Expression target) {
    return target instanceof ClassExpression && target.getType().getName().equals(Predef.class.getName());
  }

  // call of the form "<methodName>(...)" and method imported statically (import potentially added by EarlyTransform)
  public static boolean isPredefCall(StaticMethodCallExpression expr, String methodName, int minArgs, int maxArgs) {
    return
        // we currently don't a requirement on owner type because Predef member
        // that used to occur in a field initializer has the Speck class as owner type,
        // but we don't know the Speck class name here; thus we might recognize a bit too much,
        // but this shouldn't be a problem in practice
        // expr.getOwnerType().getName().equals(Predef.class.getName())
        expr.getMethod().equals(methodName)
        && getArguments(expr).size() >= minArgs
        && getArguments(expr).size() <= maxArgs;
  }

   public static void expandPredefDeclOrCall(Expression predefDeclOrCall, Expression... additionalArgs) {
    if (predefDeclOrCall instanceof BinaryExpression)
      expandPredefDecl((BinaryExpression)predefDeclOrCall, additionalArgs);
    else
      expandPredefCall(predefDeclOrCall, additionalArgs);
  }

  /*
   * Expands a field or local variable declaration of the form
   * "def x = predefMethodCall(...)". Assumes isPredefDecl(...).
   */
  public static void expandPredefDecl(BinaryExpression predefDecl, Expression... additionalArgs) {
    Expression predefCall = predefDecl.getRightExpression();
    String name = getInferredName(predefDecl);
    Expression type = getType(predefCall, getInferredType(predefDecl));

    doExpandPredefCall(predefCall, name, type, additionalArgs);
  }

  public static void expandPredefCall(Expression predefCall, Expression... additionalArgs) {
    // IDEA: use line/column information as name
    doExpandPredefCall(predefCall, "(unnamed)", getType(predefCall, null), additionalArgs);
  }

  private static Expression getType(Expression predefCall, Expression inferredType) {
    List<Expression> args = AstUtil.getArguments(predefCall);
    assert args != null;

    if (args.isEmpty()) {
      if (inferredType == null)
        throw new SyntaxException(predefCall, "Type cannot be inferred; please specify one explicitely");
      return inferredType;
    }

    return args.get(0);
  }

  private static String getInferredName(BinaryExpression predefDecl) {
    Expression left = predefDecl.getLeftExpression();
    return left instanceof Variable ? ((Variable)left).getName() : "(unknown)";
  }

  private static ClassExpression getInferredType(BinaryExpression predefDecl) {
    ClassNode type = predefDecl.getLeftExpression().getType();
    return type == null || type == ClassHelper.DYNAMIC_TYPE ?
        null : new ClassExpression(type);
  }

  private static void doExpandPredefCall(Expression predefCall, String name,
      Expression type, Expression... additionalArgs) {
    checkIsSafeToMutateArgs(predefCall);

    List<Expression> args = getArguments(predefCall);
    args.clear();
    args.add(type);
    args.add(new ConstantExpression(name));
    args.addAll(Arrays.asList(additionalArgs));
  }

  private static void checkIsSafeToMutateArgs(Expression methodCall) {
    Expression args = methodCall instanceof MethodCallExpression ?
        ((MethodCallExpression)methodCall).getArguments() :
        ((StaticMethodCallExpression)methodCall).getArguments();

    if (args == ArgumentListExpression.EMPTY_ARGUMENTS)
      throw new InternalSpockError("checkIsSafeToMutateArgs");
  }

  public static boolean hasAssertionMessage(AssertStatement stat) {
    Expression msg = stat.getMessageExpression();
    if (msg == null) return false; // should not happen
    if (!(msg instanceof ConstantExpression)) return true;
    return !((ConstantExpression)msg).isNullExpression();
  }

  public static boolean isThisExpression(Expression expr) {
    return expr instanceof VariableExpression
        && ((VariableExpression)expr).isThisExpression();
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
}

