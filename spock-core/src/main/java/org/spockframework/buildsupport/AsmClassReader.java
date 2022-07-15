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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import org.spockframework.util.*;

/**
 * Wrapper for org.objectweb.asm.ClassReader that works with ASM 2.2.3, 3.0, 3.1, and 3.2.
 */
class AsmClassReader {
  private static final Method acceptMethod;
  private static final Object acceptMethodSecondArg;

  private final ClassReader reader;

  static {
    Method m = ReflectionUtil.getMethodBySignature(ClassReader.class, "accept", ClassVisitor.class, boolean.class); // ASM 2.2.3
    Object arg = true;

    if (m == null) {
      m = ReflectionUtil.getMethodBySignature(ClassReader.class, "accept", ClassVisitor.class, int.class); // ASM 3.0 and higher
      arg = 1 | 2 | 4; // ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES
    }

    if (m == null)
      throw new InternalSpockError(
"failed to find method org.objectweb.asm.ClassReader.accept(); seems like an incompatible version of ASM is on the class path");

    acceptMethod = m;
    acceptMethodSecondArg = arg;
  }

  AsmClassReader(InputStream stream) throws IOException {
    reader = new ClassReader(stream);
  }

  void accept(SpecClassFileVisitor visitor) {
    ReflectionUtil.invokeMethod(reader, acceptMethod, visitor, acceptMethodSecondArg);
  }
}
