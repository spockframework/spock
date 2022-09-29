/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke

import org.spockframework.EmbeddedSpecification
import org.spockframework.compiler.InvalidSpecCompileException
import org.spockframework.runtime.SpockComparisonFailure
import spock.lang.Issue

class StaticMethodsInSpecifications extends EmbeddedSpecification {
  def "may contain conditions"() {
    when:
    runner.runSpecBody """
def foo() {
  expect: bar()
}

static void bar() {
  assert 1 + 1 == 3
}
    """

    then:
    thrown(SpockComparisonFailure)
  }

  @Issue("https://github.com/spockframework/spock/issues/158")
  def "may not contain interactions"() {
    when:
    compiler.compileSpecBody """
def foo() {
  List list = Mock()

  when:
  list.add("elem")

  then:
  interaction {
    elementAdded(list)
  }
}

 static elementAdded(list) {
  1 * list.add(_)
}
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.message.contains("Interactions cannot be declared in static scope")
  }

  @Issue("https://github.com/spockframework/spock/issues/158")
  def "may not create mocks"() {
    when:
    compiler.compileSpecBody """
def foo() {
  setup:
  bar()
}

static bar() {
  List list = Mock()
}
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.message.contains("Mocks cannot be created in static scope")
  }
}
