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

package org.spockframework.compiler

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.spockframework.runtime.model.SpecMetadata
import spock.lang.Specification

/**
 * @author Peter Niederwieser
 */
class SpecRecognitionAtCompileTime extends Specification {
  def "extending class Specification or a subclass thereof"() {
    when:
    def clazz = compile("""
import spock.lang.Specification

class ASpec extends $baseClass {
  def foo() { _ }
}
    """)

    then:
    wasRecognizedAsSpec(clazz)

    where:
    baseClass << ["Specification", "spock.lang.Specification",
        "org.spockframework.compiler.MySpecification", "org.spockframework.compiler.MyMySpecification"]
  }

  def "missing extends clause"() {
    when:
    def clazz = compile("""
class ASpec {}
    """)

    then:
    !clazz.isAnnotationPresent(SpecMetadata)
  }

  // we intentionally don't use EmbeddedSpecCompiler here to avoid confusing matters
  private compile(String source) {
    new GroovyClassLoader().parseClass(source)
  }

  private void wasRecognizedAsSpec(Class clazz) {
    // if EarlyTransform has run, _ will resolve to Predef._
    // otherwise, compilation will fail
    notThrown(MultipleCompilationErrorsException)
    // if SpockTransform has run, SpecMetadata will be present
    assert clazz.isAnnotationPresent(SpecMetadata)
  }
}

class MySpecification extends Specification {}

class MyMySpecification extends MySpecification {}