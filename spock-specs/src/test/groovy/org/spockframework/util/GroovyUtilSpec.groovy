/*
 * Copyright 2013 the original author or authors.
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

package org.spockframework.util

import spock.lang.Specification

class GroovyUtilSpec extends Specification {
  def "tryAll executes all blocks"() {
    def x = 0

    when:
    GroovyUtil.tryAll({
      x += 1
    }, {
      x += 2
    }, {
      x += 3
    })

    then:
    x == 6
  }

  def "tryAll rethrows first exception"() {
    def x = 0

    when:
    GroovyUtil.tryAll({
      x += 1
      throw new IOException()
    }, {
      x += 2
      throw new RuntimeException()
    }, {
      x += 3
      throw new IllegalArgumentException()
    })

    then:
    thrown(IOException)
    x == 6
  }

  def "filter null values"() {
    expect:
    GroovyUtil.filterNullValues([foo: "foo", bar: null, baz: 1]) == [foo: "foo", baz: 1]
    GroovyUtil.filterNullValues([foo: "foo", bar: [bar: null], baz: 1]) == [foo: "foo", bar: [bar: null], baz: 1]
  }

  def "close quietly"() {
    def closeable1 = Mock(MyCloseable)
    def closeable2 = Mock(MyCloseable)

    when:
    GroovyUtil.closeQuietly(closeable1, closeable2)

    then:
    1 * closeable1.close() >> { throw new IOException() }
    1 * closeable2.close() >> { throw new RuntimeException() }
    noExceptionThrown()
  }

  def "close quietly with custom method"() {
    def stoppable1 = Mock(MyStoppable)
    def stoppable2 = Mock(MyStoppable)

    when:
    GroovyUtil.closeQuietly("stop", stoppable1, stoppable2)

    then:
    1 * stoppable1.stop() >> { throw new IOException() }
    1 * stoppable2.stop() >> { throw new RuntimeException() }
    noExceptionThrown()
  }

  interface MyCloseable { void close() }

  interface MyStoppable { void stop() }
}
