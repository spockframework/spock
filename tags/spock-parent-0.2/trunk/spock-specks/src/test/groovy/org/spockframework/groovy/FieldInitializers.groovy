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

import org.junit.runner.RunWith
import org.spockframework.util.inspector.AstInspector
import org.codehaus.groovy.control.CompilePhase
import spock.lang.*


/**
 * A ...
 
 * @author Peter Niederwieser
 */
@Speck
@RunWith(Sputnik)
class FieldInitializers {
  def inspector = new AstInspector()

  def setup() {
    inspector.compilePhase = CompilePhase.SEMANTIC_ANALYSIS
  }

  def defaultInitializers() {
    inspector.load("""
class Foo {
  def x
  int y
  Integer z
}
""")

    def x = inspector.getField("x")
    def y = inspector.getField("y")
    def z = inspector.getField("z")

    expect:
      x.initialExpression == null
      y.initialExpression == null
      z.initialExpression == null
  }

}