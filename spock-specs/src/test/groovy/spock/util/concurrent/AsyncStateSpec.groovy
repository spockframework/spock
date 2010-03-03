/*
 * Copyright 2010 the original author or authors.
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

package spock.util.concurrent

import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.SpockTimeoutError

import spock.lang.*

class AsyncStateSpec extends Specification {
  def "passing example 1 - variable is read after it is written"() {
    def state = new AsyncState()

    when:
    Thread.start {
      state.someVar = 42
    }
    Thread.sleep(500)

    then:
    state.someVar == 42
  }

  def "passing example 2 - variable is read before it is written"() {
    def state = new AsyncState()

    when:
    Thread.start {
      Thread.sleep(500)
      state.someVar = 42
    }

    then:
    state.someVar == 42
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "failing example 1 - variable has wrong value"() {
    def state = new AsyncState()

    when:
    Thread.start {
      state.someVar = 42
    }

    then:
    state.someVar == 21
  }

  @FailsWith(SpockTimeoutError)
  def "failing example 2 - variable is read but not written"() {
    def state = new AsyncState()

    when:
    Thread.start {
      state.otherVar = 42
    }

    then:
    state.someVar == 42
  }

  def "state may consist of multiple variables"() {
    def state = new AsyncState()

    when:
    Thread.start {
      state.var3 = 3
      state.var2 = 2
    }
    Thread.start {
      state.var1 = 1
    }

    then:
    state.var1 = 1
    state.var2 = 2
    state.var3 = 3
  }

  def "state variable may contain null value"() {
    def state = new AsyncState()

    when:
    Thread.start {
      state.someVar = null
    }

    then:
    state.someVar == null
  }

  def "state can be used in single thread"() {
    def state = new AsyncState()

    when:
    state.var2 = 2
    state.var1 = 1

    then:
    state.var1 == 1
    state.var2 == 2
  }
}
