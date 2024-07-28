
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

import org.spockframework.runtime.SpockTimeoutError
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class ReuseableBlockingVariableSpec extends Specification {

  def "variable is read after it is written"() {
    def list = new ReuseableBlockingVariable<List<Integer>>()

    when:
    Thread.start {
      list.set([1, 2, 3])
    }
    Thread.yield()
    sleep(500)

    then:
    list.get() == [1, 2, 3]
  }

  def "variable is read before it is written"() {
    def list = new ReuseableBlockingVariable<List<String>>()

    when:
    Thread.start {
      list.set([1, 2, 3])
      Thread.yield()
      sleep(500)
    }

    then:
    list.get() == [1, 2, 3]
  }

  def "read times out if no write occurs"() {
    def list = new ReuseableBlockingVariable<String>()

    when:
    list.get()

    then:
    thrown(SpockTimeoutError)
  }

  def "timeout is configurable"() {
    def list = new ReuseableBlockingVariable<String>(1, TimeUnit.MILLISECONDS)

    when:
    list.get()

    then:
    thrown(SpockTimeoutError)
  }

  def "reset allows to use the same blocking variable several times in a row"() {
    def blockingVar = new ReuseableBlockingVariable<String>(1, TimeUnit.MILLISECONDS)

    when:
    blockingVar.set("foo")
    then:
    blockingVar.get() == "foo"
    blockingVar.get() == "foo"
    blockingVar.get() == "foo"

    when:
    blockingVar.reset()
    blockingVar.get()
    then:
    thrown(SpockTimeoutError)

    when:
    blockingVar.set("bar")
    then:
    blockingVar.get() == "bar"
  }
}
