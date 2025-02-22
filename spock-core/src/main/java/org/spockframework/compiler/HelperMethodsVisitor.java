package org.spockframework.compiler;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.SourceUnit;
import org.spockframework.compiler.condition.DefaultConditionErrorRecorders;
import org.spockframework.compiler.condition.IConditionErrorRecorders;
import org.spockframework.compiler.condition.VerifyAllMethodRewriter;
import org.spockframework.compiler.condition.VerifyMethodRewriter;
import spock.lang.Verify;
import spock.lang.VerifyAll;

import static org.spockframework.compiler.AstUtil.hasAnnotation;

class HelperMethodsVisitor extends ClassCodeVisitorSupport implements IRewriteResources {

  private final SourceUnit sourceUnit;
  private final AstNodeCache nodeCache;
  private final ErrorReporter errorReporter;
  private final SourceLookup sourceLookup;
  private final IConditionErrorRecorders errorRecorders;

  HelperMethodsVisitor(
    SourceUnit sourceUnit,
    AstNodeCache nodeCache,
    ErrorReporter errorReporter,
    SourceLookup sourceLookup
  ) {
    this.sourceUnit = sourceUnit;
    this.nodeCache = nodeCache;
    this.errorReporter = errorReporter;
    this.sourceLookup = sourceLookup;
    this.errorRecorders = new DefaultConditionErrorRecorders(nodeCache);
  }

  @Override
  protected SourceUnit getSourceUnit() {
    return sourceUnit;
  }

  @Override
  public AstNodeCache getAstNodeCache() {
    return nodeCache;
  }

  @Override
  public String getSourceText(ASTNode node) {
    return sourceLookup.lookup(node);
  }

  @Override
  public ErrorReporter getErrorReporter() {
    return errorReporter;
  }

  @Override
  public IConditionErrorRecorders getErrorRecorders() {
    return errorRecorders;
  }

  @Override
  public void visitMethod(MethodNode node) {
    if (node.isVoidMethod()) {
      if (hasAnnotation(node, Verify.class)) {
        new VerifyMethodRewriter(node, this).rewrite();
      }
      if (hasAnnotation(node, VerifyAll.class)) {
        new VerifyAllMethodRewriter(node, this).rewrite();
      }
    }
    super.visitMethod(node);
  }
}
