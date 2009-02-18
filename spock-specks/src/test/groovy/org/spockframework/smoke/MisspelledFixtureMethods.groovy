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

package org.spockframework.smoke

import org.junit.runner.RunWith

import org.spockframework.dsl.*
import static org.spockframework.dsl.Predef.*
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.spockframework.util.SpockSyntaxException

/**
 * A ...
 *
 * @author Peter Niederwieser
 */
@Speck
@RunWith (Sputnik)
class MisspelledFixtureMethods {

  GroovyClassLoader loader = new GroovyClassLoader()

  def "misspelled setup causes compile error"() {
    when:
    loader.parseClass("""
import org.spockframework.dsl.*

@Speck
class ASpeck {
  def setUp() {}
}
    """)

    then:
    MultipleCompilationErrorsException e = thrown()
    compilationFailedDueToMisspelling(e)
  }

  def "misspelled cleanup causes compile error"() {
    when:
    loader.parseClass("""
import org.spockframework.dsl.*

@Speck
class ASpeck {
  def cLeanup() {}
}
    """)

    then:
    MultipleCompilationErrorsException e = thrown()
    compilationFailedDueToMisspelling(e)
  }

  def "misspelled setupSpeck causes compile error"() {
    when:
    loader.parseClass("""
import org.spockframework.dsl.*

@Speck
class ASpeck {
  def setupspeck() {}
}
    """)

    then:
    MultipleCompilationErrorsException e = thrown()
    compilationFailedDueToMisspelling(e)
  }

  def "misspelled cleanupSpeck causes compile error"() {
    when:
    loader.parseClass("""
import org.spockframework.dsl.*

@Speck
class ASpeck {
  def CleanupSpeck() {}
}
    """)

    then:
    MultipleCompilationErrorsException e = thrown()
    compilationFailedDueToMisspelling(e)
  }

  def "correctly spelled setup compiles successfully"() {
    when:
    loader.parseClass("""
import org.spockframework.dsl.*

@Speck
class ASpeck {
  def setup() {}
}
    """)

    then:
    notThrown(MultipleCompilationErrorsException)
  }

  private compilationFailedDueToMisspelling(MultipleCompilationErrorsException e) {
    def errors = e.errorCollector.errors
    assert errors.size() == 1
    assert errors[0].cause instanceof SpockSyntaxException
    true
  }
}