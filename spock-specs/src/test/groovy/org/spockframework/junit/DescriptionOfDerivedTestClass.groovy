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

package org.spockframework.junit

import org.junit.runner.notification.RunListener
import org.junit.runners.JUnit4
import org.spockframework.EmbeddedSpecification

class DescriptionOfDerivedTestClass extends EmbeddedSpecification {
  Class derivedClass

  def setup() {
    def classes = compiler.compile("""
import org.junit.*

class BaseTest {
  @Before
  void before() {
    throw new RuntimeException()
  }

  @Test
  void foo() {}
}

class DerivedTest extends BaseTest {
  @Test
  void bar() {}
}
    """)

    derivedClass = classes.find { it.name == "DerivedTest" }
  }

  def "Description of inherited test method has class name of derived class"() {
    def desc = new JUnit4(derivedClass).description

    expect:
    desc.children.size() == 2
    desc.children*.getClassName() == [derivedClass.name] * 2
  }

  def "Description of inherited before method has class name of derived class"() {
    RunListener listener = Mock()
    runner.listeners << listener
    runner.throwFailure = false

    when:
    runner.runClass(derivedClass)

    then:
    2 * listener.testFailure( { it.description.className == derivedClass.name } )
  }
}


