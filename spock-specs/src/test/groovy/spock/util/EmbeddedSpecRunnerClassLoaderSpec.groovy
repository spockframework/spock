/*
 * Copyright 2024 the original author or authors.
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
package spock.util

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.spockframework.mock.runtime.ByteBuddyTestClassLoader
import spock.lang.Specification

class EmbeddedSpecRunnerClassLoaderSpec extends Specification {

  def "EmbeddedSpecRunner with different ClassLoader"() {
    given:
    def cl = new ByteBuddyTestClassLoader()
    def testInterfaceName = "test.TestIf"
    cl.defineInterface(testInterfaceName)
    def runner = new EmbeddedSpecRunner(cl)

    expect:
    //noinspection GroovyAccessibility
    !runner.compiler.unwrapCompileException

    when:
    def result = runner.runFeatureBody("""
expect:
${testInterfaceName}.class != null
""")
    then:
    result.testsSucceededCount == 1
  }

  def "EmbeddedSpecRunner with different ClassLoader - unable to see Spock"() {
    given:
    def cl = new URLClassLoader([] as URL[], null as ClassLoader)
    def runner = new EmbeddedSpecRunner(cl)

    when: "This should fail, because the Specification class is not visible in the ClassLoader"
    runner.runFeatureBody("""
expect:
true
""")
    then:
    def ex = thrown(MultipleCompilationErrorsException)
    ex.message.contains("unable to resolve class Specification")

    cleanup:
    cl?.close()
  }
}
