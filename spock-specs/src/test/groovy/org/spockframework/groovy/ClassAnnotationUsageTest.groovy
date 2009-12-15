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

package org.spockframework.groovy

import org.codehaus.groovy.control.CompilePhase
import org.spockframework.util.inspector.AstInspector

class ClassAnnotationUsageTest extends GroovyTestCase {
  static classes
  static inspector = new AstInspector()
  static source = """
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

import org.spockframework.groovy.GroovyAnnotationWithSourceRetention
import org.spockframework.groovy.GroovyAnnotationWithClassRetention
import org.spockframework.groovy.GroovyAnnotationWithRuntimeRetention
import org.spockframework.groovy.JavaAnnotationWithSourceRetention
import org.spockframework.groovy.JavaAnnotationWithClassRetention
import org.spockframework.groovy.JavaAnnotationWithRuntimeRetention

@GroovyAnnotationWithSourceRetention
class GroovySource {}

@GroovyAnnotationWithClassRetention
class GroovyClass {}

@GroovyAnnotationWithRuntimeRetention
class GroovyRuntime {}

@JavaAnnotationWithSourceRetention
class JavaSource {}

@JavaAnnotationWithClassRetention
class JavaClass {}

@JavaAnnotationWithRuntimeRetention
class JavaRuntime {}
  """

  static {
    // compile the classes
    def compiler = new GroovyClassLoader()
    compiler.parseClass(source)
    classes = compiler.loadedClasses

    // prepare AST inspection
    // why isn't AnnotationVisitor run as part of semantic analysis?
    inspector.compilePhase = CompilePhase.CLASS_GENERATION
    inspector.load(source)
  }

  void testGroovySourceRetention() {
    check("GroovySource", GroovyAnnotationWithSourceRetention, true, false, false)
  }

  void testGroovyClassRetention() {
    check("GroovyClass", GroovyAnnotationWithClassRetention, false, true, false)
  }

  void testGroovyRuntimeRetention() {
    check("GroovyRuntime", GroovyAnnotationWithRuntimeRetention, false, false, true)
  }

  void testJavaSourceRetention() {
    check("JavaSource", JavaAnnotationWithSourceRetention, true, false, false)
  }

  void testJavaClassRetention() {
    check("JavaClass", JavaAnnotationWithClassRetention, false, true, false)
  }

  void testJavaRuntimeRetention() {
    check("JavaRuntime", JavaAnnotationWithRuntimeRetention, false, false, true)
  }

  // Checks the annotation's AST and runtime representation.
  def check(className, annType, sourceRet, classRet, runtimeRet) {
    checkAstInfo(className, annType, sourceRet, classRet, runtimeRet)
    checkRuntimeInfo(className, annType, sourceRet, classRet)
  }

  // Checks the annotation's AST representation.
  def checkAstInfo(className, annType, sourceRet, classRet, runtimeRet) {
    def classNode = inspector.getClass(className)

    // annotation present?
    assertEquals(1, classNode.annotations.size())

    // retention policy set correctly?
    def annNode = classNode.annotations[0]
    assertEquals(sourceRet, annNode.hasSourceRetention())
    assertEquals(classRet, annNode.hasClassRetention())
    assertEquals(runtimeRet, annNode.hasRuntimeRetention())

    // annotation type resolved correctly?
    assertSame(annType, annNode.classNode.typeClass)
  }

  // Checks the annotation's runtime representation. Ideally one should also
  // check the class file (to differentiate between source and class retention);
  // however, this is out of the scope of this test.
  def checkRuntimeInfo(className, annType, sourceRet, classRet) {
    def classObj = classes.find { it.name == className }
    assertNotNull(classObj)

    // annotation only available if it has runtime retention?
    if (sourceRet || classRet)
      assertEquals(0, classObj.annotations.size())
    else { // runtimeRet
      assertEquals(1, classObj.annotations.size())
      def annObj = classObj.annotations[0]
      // annotation of expected type?
      assertTrue(annType.isInstance(annObj))
    }
  }
}


