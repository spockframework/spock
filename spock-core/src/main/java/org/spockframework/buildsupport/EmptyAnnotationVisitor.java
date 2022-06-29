/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.buildsupport;

import org.objectweb.asm.*;

class EmptyAnnotationVisitor extends AnnotationVisitor {
  public EmptyAnnotationVisitor() {
    super(Opcodes.ASM4);
  }

  @Override
  public void visit(String s, Object o) {}

  @Override
  public void visitEnum(String s, String s1, String s2) {}

  @Override
  public AnnotationVisitor visitAnnotation(String s, String s1) {
    return this;
  }

  @Override
  public AnnotationVisitor visitArray(String s) {
    return this;
  }

  @Override
  public void visitEnd() {}
}
