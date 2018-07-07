package org.spockframework.compiler;

import groovyjarjarasm.asm.*;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.classgen.BytecodeExpression;

class ClosureReferenceExpression extends BytecodeExpression {

  ClosureReferenceExpression() {super(ClassHelper.CLOSURE_TYPE);}

  @Override
  public void visit(MethodVisitor methodVisitor) {
    methodVisitor.visitVarInsn(Opcodes.ALOAD,0);
  }
}
