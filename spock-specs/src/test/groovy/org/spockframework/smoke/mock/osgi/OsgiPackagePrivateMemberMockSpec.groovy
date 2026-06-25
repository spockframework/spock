/*
 * Copyright 2026 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.spockframework.smoke.mock.osgi


import spock.lang.Issue
import spock.lang.Specification
import spock.util.EmbeddedSpecRunner

class OsgiPackagePrivateMemberMockSpec extends Specification {
  def runner = new EmbeddedSpecRunner(new OsgiTestClassLoader(this.class.classLoader))

  @SuppressWarnings('UnnecessaryQualifiedReference')
  @Issue("https://github.com/spockframework/spock/issues/2384")
  def "OSGi package private mock shall be mockable with ByteBuddy"() {
    when:
    def res = runner.runSpecBody("""
    def mock = Mock(org.spockframework.smoke.mock.osgi.testclasses.PkgPrivateMemberClass, mockMaker: spock.mock.MockMakers.byteBuddy) {
        packagePrivate() >> "mocked"
    }

    def "mock is called when invoked from Java code"() {
        when:
        def result = new org.spockframework.smoke.mock.osgi.testclasses.InvocationFromJava(mock).invoke()

        then:
        result == "mocked"
    }

    def "mock is called when invoked directly from Groovy"() {
        when:
        def result = mock.packagePrivate()

        then:
        result == "mocked"
    }
""")

    then:
    res.testsSucceededCount == 2
  }
}
