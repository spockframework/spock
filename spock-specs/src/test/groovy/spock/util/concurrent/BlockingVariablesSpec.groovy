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

package spock.util.concurrent

import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.SpockTimeoutError

import spock.lang.*

class BlockingVariablesSpec extends Specification {
  def "passing example 1 - variable is read after it is written"() {
    def vars = new BlockingVariables()

    when:
    Thread.start {
      vars.foo = 42
    }
    Thread.sleep(500)

    then:
    vars.foo == 42
  }

  def "passing example 2 - variable is read before it is written"() {
    def vars = new BlockingVariables()

    when:
    Thread.start {
      Thread.sleep(500)
      vars.foo = 42
    }

    then:
    vars.foo == 42
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "failing example 1 - variable has wrong value"() {
    def vars = new BlockingVariables()

    when:
    Thread.start {
      vars.foo = 42
    }

    then:
    vars.foo == 21
  }

  @FailsWith(SpockTimeoutError)
  def "failing example 2 - variable is read but not written"() {
    def vars = new BlockingVariables()

    when:
    Thread.start {
      vars.foo = 42
    }

    then:
    vars.bar == 42
  }

  def "multiple variables"() {
    def vars = new BlockingVariables()

    when:
    Thread.start {
      vars.baz = 3
      vars.bar = 2
    }
    Thread.start {
      vars.foo = 1
    }

    then:
    vars.foo == 1
    vars.bar == 2
    vars.baz == 3
  }

  def "variable may be null"() {
    def vars = new BlockingVariables()

    when:
    Thread.start {
      vars.foo = null
    }

    then:
    vars.foo == null
  }

  def "vars can be used in single thread"() {
    def vars = new BlockingVariables()

    when:
    vars.bar = 2
    vars.foo = 1

    then:
    vars.foo == 1
    vars.bar == 2
  }
}
