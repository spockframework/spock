/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.junit4.junit

import spock.lang.Unroll

import org.intellij.lang.annotations.Language

class JUnitFixtureMethods extends JUnitBaseSpec {
  static invocations = []
  static RECORD_INVOCATION_METHOD = "static record(methodName) { JUnitFixtureMethods.invocations << methodName }"

  def setup() {
    invocations.clear()
  }

  def "lifecycle"() {
    when:
    runSpecBody """
      ${beforeClass()}
      def setupSpec() { record("setupSpec") }
      ${before()}
      def setup() { record("setup") }

      def feature1() { expect: true }
      def feature2() { expect: true }

      def cleanup() { record("cleanup") }
      ${after()}
      def cleanupSpec() { record("cleanupSpec") }
      ${afterClass()}
    """

    then:
    invocations == [
      "setupSpec", "beforeClass",
      "setup", "before", "cleanup", "after",
      "setup", "before", "cleanup", "after",
      "cleanupSpec", "afterClass"
    ]
  }

  def "multiple of #fixtureType"() {
    when:
    runSpecBody """
      ${this."$fixtureType"("m1")}
      ${this."$fixtureType"("m2")}

      def feature1() { expect: true }
    """

    then:
    invocations.contains("m1")
    invocations.contains("m2")

    where:
    fixtureType << ["beforeClass", "before", "after", "afterClass"]
  }

  def "inheritance for #fixtureType"() {
    when:
    run """
      abstract class Parent extends Specification {
        $RECORD_INVOCATION_METHOD

        ${this."$fixtureType"("parent")}
      }

      class Child extends Parent {
        ${this."$fixtureType"("child")}

        def feature1() { expect: true }
      }
    """

    then:
    invocations == order

    where:
    fixtureType   | order
    "beforeClass" | ["parent", "child"]
    "before"      | ["parent", "child"]
    "after"       | ["child", "parent"]
    "afterClass"  | ["child", "parent"]
  }

  def "inheritance with overriding for #fixtureType"() {
    when:
    run """
      abstract class Parent extends Specification {
        $RECORD_INVOCATION_METHOD

        ${this."$fixtureType"("parent")}
      }

      class Child extends Parent {
        ${this."$fixtureType"("child")}

        def feature1() { expect: true }
      }
    """

    then:
    invocations == order

    where:
    fixtureType    | order
    "beforeClass2" | ["child"]
    "before2"      | ["child"]
    "after2"       | ["child"]
    "afterClass2"  | ["child"]
  }

  def "same method with more than one fixture annotation"() {
    when:
    runSpecBody """
      static mode = "before"

      @Before @After void f() { record mode }
      def feature() { when: mode = "after"; then: true }
      @BeforeClass @AfterClass static void sf() { record mode + "Class" }
    """

    then:
    invocations == ["beforeClass", "before", "after", "afterClass"]
  }

  @Unroll("exceptions thrown by [#declaration] methods with failing feature [#failFeature]")
  def "exceptions thrown by fixture methods are handled correctly"() {
    runner.throwFailure = false

    when:
    def result = runSpecBody("""
      $declaration void $name() { throw new RuntimeException("$name") }

      def foo() { expect: !$failFeature }
    """)

    then:
    def e = result.failures[exceptionPos].exception
    if (suppressed) {
      !!(e = e.suppressed[0])
    }
    e instanceof RuntimeException
    e.message == name

    where:
    name          | declaration           | failFeature | exceptionPos | suppressed
    "beforeClass" | "@BeforeClass static" | true        | 0            | false
    "beforeClass" | "@BeforeClass static" | false       | 0            | false
    "before"      | "@Before"             | true        | 0            | false
    "before"      | "@Before"             | false       | 0            | false
    "after"       | "@After"              | true        | 0            | true
    "after"       | "@After"              | false       | 0            | false
    "afterClass"  | "@AfterClass static"  | true        | 1            | false
    "afterClass"  | "@AfterClass static"  | false       | 0            | false
  }

  protected beforeClass(name = "beforeClass") { "@BeforeClass static void $name() { record('$name') }" }

  protected before(name = "before") { "@Before void $name() { record('$name') }" }

  protected after(name = "after") { "@After void $name() { record('$name') } " }

  protected afterClass(name = "afterClass") { "@AfterClass static void $name() { record('$name') } " }

  protected beforeClass2(log) { "@BeforeClass static void beforeClass() { record('$log') }" }

  protected before2(log) { "@Before void before() { record('$log') }" }

  protected after2(log) { "@After void after() { record('$log') } " }

  protected afterClass2(log) { "@AfterClass static void afterClass() { record('$log') } " }

  protected addImports() {
    runner.addPackageImport(getClass().package)
    runner.addPackageImport(org.junit.Before.package)
  }

  protected runSpecBody(String specBody) {
    addImports()
    runner.runSpecBody """
      $RECORD_INVOCATION_METHOD

      $specBody
    """
  }

  protected run(@Language('Groovy') String source) {
    addImports()
    runner.runWithImports(source)
  }

}
