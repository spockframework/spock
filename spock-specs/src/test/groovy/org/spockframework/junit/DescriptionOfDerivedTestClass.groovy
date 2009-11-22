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

import spock.lang.*
import org.spockframework.EmbeddedSpecification

import org.junit.Test
import org.junit.runners.JUnit4
import org.junit.Before
import org.junit.runner.notification.RunListener

class DescriptionOfDerivedTestClass extends EmbeddedSpecification {
  def "Description of inherited test method has class name of derived class"() {
    def desc = new JUnit4(DerivedDescription).description

    expect:
    desc.children.size() == 2
    desc.children*.getClassName() == [DerivedDescription.name] * 2
  }

  def "Description of inherited setup method has class name of derived class"() {
    RunListener listener = Mock()
    runner.listeners << listener
    runner.throwFailure = false

    when:
    runner.runClass(DerivedDescription)

    then:
    2 * listener.testFailure( { it.description.className == DerivedDescription.name } )
  }
}

class BaseDescription {
  @Before
  void before() {
    throw new RuntimeException()
  }

  @Test
  void foo() {}
}

class DerivedDescription extends BaseDescription {
  @Test
  void bar() {}
}
