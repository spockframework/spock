/*
 * Copyright 2010 the original author or authors.
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

import org.junit.Rule
import org.junit.rules.TestName

import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

@Issue("https://github.com/spockframework/spock/issues/220")
class RulesAndInheritance extends RulesAndInheritanceBase {
  @Rule
  TestName name2 = new TestName()

  def setup() {
    checkNameInFixtureMethod()
  }

  def cleanup() {
    checkNameInFixtureMethod()
  }


  def "derived feature"() {
    expect:
    name.methodName == "derived feature"
    name2.methodName == "derived feature"
  }
}

abstract class RulesAndInheritanceBase extends Specification {
  @Rule
  TestName name = new TestName()

  @Shared int count = 0

  def setup() {
    count++
    checkNameInFixtureMethod()
  }

  def cleanup() {
    checkNameInFixtureMethod()
  }

  def "base feature"() {
    expect:
    name.methodName == "base feature"
    name2.methodName == "base feature" // check rule in superclass too
  }

  void checkNameInFixtureMethod() {
    if (count == 1) {
      assert name.methodName == "base feature"
      assert name2.methodName == "base feature"
    } else {
      assert name.methodName == "derived feature"
      assert name2.methodName == "derived feature"
    }
  }
}
