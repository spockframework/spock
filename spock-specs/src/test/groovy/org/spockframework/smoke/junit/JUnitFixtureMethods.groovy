/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.junit

import org.spockframework.EmbeddedSpecification

import spock.lang.*

class JUnitFixtureMethods extends EmbeddedSpecification {
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
        "beforeClass", "setupSpec", 
        "before", "setup" , "cleanup", "after", 
        "before", "setup" , "cleanup", "after",
        "cleanupSpec", "afterClass"
    ]
  }
  
  @Unroll("multiple of #fixtureType")
  def "multiple of same type"() {
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
  
  @Unroll("inheritance for #fixtureType")
  def "inheritance"() {
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
  
  @Unroll("inheritance with shadowed methods for #fixtureType")
  def "inheritance with shadowed methods"() {
    when:
    run """
    abstract class Parent extends Specification {
      $RECORD_INVOCATION_METHOD
      
      ${this."$fixtureType"("$fixtureType","${fixtureType}_parent")}
    }
    
    class Child extends Parent {
      ${this."$fixtureType"("$fixtureType","${fixtureType}_child")}
      
      def feature1() { expect: true }
    }
  """
    
    then:
    invocations == [fixtureType + '_child']
    
    where:
    fixtureType << ["beforeClass", "before", "after", "afterClass"]
  }
  
  @Unroll("invalid signature ignored because - #invalidBecause")
  def "invalid signatures are ignored"() {
    when:
    run "$prefix method($params) { record('method') }"
    
    then:
    invocations.empty
    
    where:
    prefix                        | params | invalidBecause
    "@BeforeClass void"           | ""     | "non static class method"
    "@Before static void"         | ""     | "static non class method"
    "@Before def"                 | ""     | "non void return"
    "@Before void"                | "a"    | "non zero-arg"
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

  def "exceptions thrown by fixture methods are handled correctly"() {
    runner.throwFailure = false

    when:
    def result = runSpecBody("""
      $declaration void $name() { throw new RuntimeException("$name") }

      def foo() { expect: !$failFeature }
    """)

    then:
    def e = result.failures[exceptionPos].exception
    e instanceof RuntimeException
    e.message == name

    where:
    name          | declaration           | failFeature | exceptionPos
    "beforeClass" | "@BeforeClass static" | true        | 0
    "beforeClass" | "@BeforeClass static" | false       | 0
    "before"      | "@Before"             | true        | 0
    "before"      | "@Before"             | false       | 0
    "after"       | "@After"              | true        | 1
    "after"       | "@After"              | false       | 0
    "afterClass"  | "@AfterClass static"  | true        | 1
    "afterClass"  | "@AfterClass static"  | false       | 0
  }
  
  protected beforeClass(name = "beforeClass", recordName = null) { "@BeforeClass static void $name() { record('${recordName?:name}') }" }
  protected before(name = "before", recordName = null) { "@Before void $name() { record('${recordName?:name}') }" }
  protected after(name = "after", recordName = null) { "@After void $name() { record('${recordName?:name}') } "}
  protected afterClass(name = "afterClass", recordName = null) { "@AfterClass static void $name() { record('${recordName?:name}') } "}
  
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

  protected run(String source) {
    addImports()
    runner.runWithImports(source)
  }
  
}