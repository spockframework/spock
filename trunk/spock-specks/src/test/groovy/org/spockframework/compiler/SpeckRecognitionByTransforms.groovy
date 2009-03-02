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

import org.junit.runner.RunWith

import org.spockframework.runtime.model.SpeckMetadata
import spock.lang.*
import static spock.lang.Predef.*

/**
 *
 * @author Peter Niederwieser
 */
@Speck
@RunWith (Sputnik)
class SpeckRecognitionByTransforms {
  def "annotation w/ fully qualified name"() {
    when:
    def clazz = compile("""
@spock.lang.Speck
class ASpeck {
  def foo() { _ }
}
    """)

    then:
    bothTransformsRecognizeClassAsSpeck(clazz)
  }

  def "annotation w/ simple name and class import"() {
    when:
    def clazz = compile("""
import spock.lang.Speck

@Speck
class ASpeck {
  def foo() { _ }
}
    """)

    then:
    bothTransformsRecognizeClassAsSpeck(clazz)
  }

  def "annotation w/ simple name and package import"() {
    when:
    def clazz = compile("""
import spock.lang.*

@Speck
class ASpeck {
  def foo() { _ }
}
    """)

    then:
    bothTransformsRecognizeClassAsSpeck(clazz)
  }

  def "annotation w/ simple name and same package"() {
    when:
    def clazz = compile("""
package spock.lang

@Speck
class ASpeck {
  def foo() { _ }
}
    """)

    then:
    bothTransformsRecognizeClassAsSpeck(clazz)
  }

  def "annotation w/ import alias"() {
    when:
    def clazz = compile("""
import spock.lang.Speck as Test

@Test
class ASpeck {
  def foo() { _ }
}
    """)

    then:
    bothTransformsRecognizeClassAsSpeck(clazz)
  }

  def "annotation w/o import"() {
    when:
    compile("""
@Speck
class ASpeck {}
    """)

    then:
    thrown(MultipleCompilationErrorsException)
  }

  def "missing annotation"() {
    when:
    def clazz = compile("""
import spock.lang.Speck

class ASpeck {}
    """)

    then:
    !clazz.isAnnotationPresent(SpeckMetadata)
  }

  private compile(String source) {
    new GroovyClassLoader().parseClass(source)
  }

  private bothTransformsRecognizeClassAsSpeck(Class clazz) {
    // if EarlyTransform has run, _ will resolve to Predef._
    // otherwise, compilation will fail
    notThrown(MultipleCompilationErrorsException)
    // if MainTransform has run, SpeckMetadata will be present
    assert clazz.isAnnotationPresent(SpeckMetadata)
    true
  }
}