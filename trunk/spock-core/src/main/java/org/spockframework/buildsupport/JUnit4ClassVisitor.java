package org.spockframework.buildsupport;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.EmptyVisitor;

class JUnit4ClassVisitor extends EmptyVisitor {
  boolean isTestCase = false;

  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    if ("Lorg/junit/runner/RunWith;".equals(desc)) isTestCase = true;
    return new EmptyVisitor();
  }

  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    return new JUnit4MethodVisitor();
  }

  public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
    return null;
  }

  class JUnit4MethodVisitor extends EmptyVisitor {
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      if ("Lorg/junit/Test;".equals(desc)) isTestCase = true;
      return new EmptyVisitor();
    }

    public AnnotationVisitor visitAnnotationDefault() {
      return new EmptyVisitor();
    }
    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
      return new EmptyVisitor();
    }

  }
}
