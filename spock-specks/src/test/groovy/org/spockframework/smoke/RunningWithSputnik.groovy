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
import org.junit.runner.JUnitCore
import org.junit.runner.RunWith

@Speck
@RunWith(Sputnik)
@Issue("13")
public class RunningWithSputnik {
  def "failing setupSpeck method"() {
    def clazz = new GroovyClassLoader().parseClass("""
import spock.lang.*
import org.junit.runner.RunWith

@Speck
@RunWith(Sputnik)
class Foo {
  def setupSpeck() { throw new Exception() }
  def feature() { expect: true }
}
    """)

    when:
    def result = JUnitCore.runClasses(clazz)

    then:    
    result.failureCount == 1
    result.runCount == 0 // we don't currently call notifier.fireTestStarted()/fireTestFinished() for setupSpeck()
  }

  def "failing cleanupSpeck method"() {
    def clazz = new GroovyClassLoader().parseClass("""
import spock.lang.*
import org.junit.runner.RunWith

@Speck
@RunWith(Sputnik)
class Foo {
  def cleanupSpeck() { throw new Exception() }
  def feature() { expect: true }
}
    """)

    when:
    def result = JUnitCore.runClasses(clazz)

    then:
    result.failureCount == 1
    result.runCount == 1 // we don't currently call notifier.fireTestStarted()/fireTestFinished() for cleanupSpeck()
  }
}