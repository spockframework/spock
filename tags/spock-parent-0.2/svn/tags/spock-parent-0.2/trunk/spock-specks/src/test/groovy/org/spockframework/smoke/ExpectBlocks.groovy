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

import spock.lang.*
import static spock.lang.Predef.*
import org.junit.runner.RunWith
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.spockframework.util.SpockSyntaxException

/**
 *
 * @author Peter Niederwieser
 */

@Speck
@RunWith(Sputnik)
class ExpectBlocks {
  def "may not contain exception conditions"() {
    when:
    new GroovyClassLoader().parseClass("""
@spock.lang.Speck
class Foo {
  def foo() {
    expect: thrown(RuntimeException)
  }
}
    """)

    then:
    MultipleCompilationErrorsException e = thrown()
    def error = e.errorCollector.getSyntaxError(0)
    error instanceof SpockSyntaxException
    error.line == 5
  }

  def "may not contain interactions"() {
    when:
    new GroovyClassLoader().parseClass("""
@spock.lang.Speck
class Foo {
  def foo() {
    def l = Mock(List)
    expect: l.size() >> 5
  }
}
    """)

    then:
    MultipleCompilationErrorsException e = thrown()
    def error = e.errorCollector.getSyntaxError(0)
    error instanceof SpockSyntaxException
    error.line == 6
  }
}