/**
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

package org.spockframework.buildsupport;

import org.objectweb.asm.*;

class SpecClassFileVisitor extends ClassVisitor {
  private final AnnotationVisitor annVisitor = new EmptyAnnotationVisitor();

  private boolean hasSpecMetadataAnnotation = false;
  private boolean isAbstract;

  SpecClassFileVisitor() {
    super(Opcodes.ASM9);
  }

  public boolean isSpec() {
    return hasSpecMetadataAnnotation;
  }

  public boolean isRunnableSpec() {
    return isSpec() && !isAbstract;
  }

  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    if ("Lorg/spockframework/runtime/model/SpecMetadata;".equals(desc))
      hasSpecMetadataAnnotation = true;
    return annVisitor;
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    isAbstract = (access & Opcodes.ACC_ABSTRACT) != 0;
  }

  @Override
  public void visitSource(String s, String s1) {}

  @Override
  public void visitOuterClass(String s, String s1, String s2) {}

  @Override
  public void visitAttribute(Attribute attribute) {}

  @Override
  public void visitInnerClass(String s, String s1, String s2, int i) {}

  @Override
  public FieldVisitor visitField(int i, String s, String s1, String s2, Object o) {
    return null; // allowed as per contract
  }

  @Override
  public MethodVisitor visitMethod(int i, String s, String s1, String s2, String[] strings) {
    return null; // allowed as per contract
  }

  @Override
  public void visitEnd() {}
}
