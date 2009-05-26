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

package org.spockframework.smoke.directive

import spock.lang.*
import org.junit.runner.RunWith
import org.junit.runner.JUnitCore
import org.spockframework.runtime.InvalidSpeckError

/**
 *
 * @author Peter Niederwieser
 */
@Issue("12")
@Ignore
@Speck
@RunWith(Sputnik)
class IgnoreSpeck {
  def "ignore 1"() {
    expect: false
  }

  def "ignore 2"() {
    expect: false
  }
}

@Speck
@RunWith(Sputnik)
class IgnoreFeatureMethods {
  @Ignore
  def "ignore 1"() {
    expect: false
  }

  @Ignore
  def "ignore 2"() {
    expect: false
  }

  def "not ignored"() {
    expect: true
  }
}

@Issue("20")
@Speck
@RunWith(Sputnik)
class ReportIgnoredMethodsToJUnit {
  def "check"() {
    when:
    def result = JUnitCore.runClasses(IgnoreFeatureMethods)

    then:
    result.runCount == 3
    result.ignoreCount == 2
  }
}

@Speck
@RunWith(Sputnik)
class IgnoreLifecycleMethod {
  def "lifecycle methods cannot be ignored"() {
    def speck = new GroovyClassLoader().parseClass("""
import spock.lang.*
import org.junit.runner.RunWith

@Speck
@RunWith(Sputnik)
class Foo {
  @Ignore
  def setup() {}
}
    """)

    when:
    def result = JUnitCore.runClasses(speck)

    then:
    result.failures[0].exception instanceof InvalidSpeckError
  }
}



