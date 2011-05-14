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

package org.spockframework.smoke.extension

import spock.lang.*
import org.spockframework.EmbeddedSpecification

import org.junit.*

class JUnitFixtureMethodsExtension extends EmbeddedSpecification {

  static invocations = []
  static RECORD_INVOCATION_METHOD = "static record(methodName) { JUnitFixtureMethodsExtension.invocations << methodName }"
  
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
  
  protected beforeClass(name = "beforeClass") { "@BeforeClass static void $name() { record('$name') }" }
  protected before(name = "before") { "@Before void $name() { record('$name') }" }
  protected after(name = "after") { "@After void $name() { record('$name') } "}
  protected afterClass(name = "afterClass") { "@AfterClass static void $name() { record('$name') } "}
  
  protected addImports() {
    runner.addImport(getClass().package)
    runner.addImport(org.junit.Before.package)
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