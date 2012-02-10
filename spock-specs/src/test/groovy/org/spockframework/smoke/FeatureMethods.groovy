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

import org.junit.runner.notification.RunListener

import spock.lang.Specification
import spock.util.EmbeddedSpecRunner
import spock.lang.Issue

/**
 *
 * @author Peter Niederwieser
 */
class FeatureMethods extends Specification {
  def "cannot be called from user code"() {
    when:
    featureMethod()

    then:
    thrown(NoSuchMethodError) // Groovy 1.7 throws MissingMethodException here (which is nicer)
  }

  @Issue("http://issues.spockframework.org/detail?id=229")
  def "don't make it into the class file with their original name"() {
    expect:
    !getClass().getDeclaredMethods().any { it.name == "featureMethod" }
  }
  
  def "are nevertheless reported with their original name"() {
    def runner = new EmbeddedSpecRunner()
    RunListener listener = Mock()
    runner.listeners << listener
    
    when:
    runner.runSpecBody("""
def "original name"() { expect: true }
    """)

    then:
    1 * listener.testStarted({it.methodName == "original name"})
    1 * listener.testFinished({it.methodName == "original name"})
  }

  def "can.have?names#con/tain!ing~any`char(act \\ers?!"() {
    expect: true
  }

  def "can have names containing any characters in embedded specs"() {
    def runner = new EmbeddedSpecRunner()
    RunListener listener = Mock()
    runner.listeners << listener

    when:
    runner.runSpecBody("""
def "can.have?names#con/tain!ing~any`char(act \\\\ers?!"() { expect: true }
    """)

    then:
    1 * listener.testStarted({it.methodName == "can.have?names#con/tain!ing~any`char(act \\ers?!"})
    1 * listener.testFinished({it.methodName == "can.have?names#con/tain!ing~any`char(act \\ers?!"})
  }

  def featureMethod() {
    expect: true
  }
}