/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.smoke.mock

import spock.lang.Specification
import spock.lang.Issue
import spock.lang.Unroll

@Issue("http://issues.spockframework.org/detail?id=235")
class ChainedResponseGenerators extends Specification {
  Queue queue = Mock()

  def "implicit default response"() {
    expect:
    queue.poll() == null
    queue.poll() == null
  }
  
  def "explicit default response"() {
    queue.poll() >> _ >> _

    expect:
    queue.poll() == null
    queue.poll() == null
    queue.poll() == null
  }
  
  def "chaining constant responses"() {
    queue.poll() >> 1 >> 2 >> 3

    expect:
    queue.poll() == 1
    queue.poll() == 2
    queue.poll() == 3
    queue.poll() == 3
  }
  
  def "chaining iterable responses"() {
    queue.poll() >>> [1, 2] >>> 3 >>> "45"

    expect:
    queue.poll() == 1
    queue.poll() == 2
    queue.poll() == 3
    queue.poll() == "4"
    queue.poll() == "5"
    queue.poll() == "5"
  }
  
  def "chaining code responses"() {
    def count = 0
    queue.poll() >> { throw new UnsupportedOperationException() } >> { throw new IllegalArgumentException() } >> { count++ }

    when:
    queue.poll()

    then:
    thrown(UnsupportedOperationException)

    when:
    queue.poll()

    then:
    thrown(IllegalArgumentException)

    expect:
    queue.poll() == 0
    queue.poll() == 1
  }

  def "chaining different responses"() {
    queue.poll() >> 1 >> _ >> { throw new UnsupportedOperationException() } >> [2, 3] >>> [4, 5]

    expect:
    queue.poll() == 1
    queue.poll() == null

    when:
    queue.poll()

    then:
    thrown(UnsupportedOperationException)

    expect:
    queue.poll() == [2, 3]
    queue.poll() == 4
    queue.poll() == 5
    queue.poll() == 5
  }

  def "chaining in then-block"() {
    when:
    def res1 = queue.poll()
    def res2 = queue.poll()
    def res3 = queue.poll()
    def res4 = queue.poll()
    def res5 = queue.poll()

    then:
    queue.poll() >> 1 >>> [2, 3] >> { throw new UnsupportedOperationException() }

    res1 == 1
    res2 == 2
    res3 == 3
    res4 == null
    res5 == null
    thrown(UnsupportedOperationException)
  }

  def "chaining code responses as a list"() {
    def count = 0
    queue.poll() >>> [{ throw new UnsupportedOperationException() },  { throw new IllegalArgumentException() }, { count++ }]

    when:
    queue.poll()

    then:
    thrown(UnsupportedOperationException)

    when:
    queue.poll()

    then:
    thrown(IllegalArgumentException)

    expect:
    queue.poll() == 0
    queue.poll() == 1
  }

  def "chaining code responses as unitary lists"() {
    def count = 0
    queue.poll() >>> [{ throw new UnsupportedOperationException() }] >>> [{ throw new IllegalArgumentException() }] >>> [{ count++ }]

    when:
    queue.poll()

    then:
    thrown(UnsupportedOperationException)

    when:
    queue.poll()

    then:
    thrown(IllegalArgumentException)

    expect:
    queue.poll() == 0
    queue.poll() == 1
  }

  def "chaining code responses as a unitary list without state changes"() {
    def count = 0
    queue.poll() >>> [{ throw new UnsupportedOperationException() }]

    when:
    queue.poll()

    then:
    thrown(UnsupportedOperationException)
  }

  def "chaining code responses as a unitary list with state changes"() {
    def count = 0
    queue.poll() >>> [{ ++count }]

    expect:
    queue.poll() == 1
  }

  def "chaining code responses with heterogeneous lists"() {
    def count = 1
    queue.poll() >>> [{ throw new UnsupportedOperationException() }, 0, { count++ }, 2, [3, 4]]

    when:
    queue.poll()

    then:
    thrown(UnsupportedOperationException)

    expect:
    queue.poll() == 0
    queue.poll() == 1
    queue.poll() == 2
    queue.poll() == [3, 4]
  }

  @Unroll('#exception_name: chain of #iterations exceptions')
  def "chaining code responses as a list using datatables"() {
    def count = 0
    queue.poll() >>> [ { throw exception } ] * iterations >> { count++ }

    when:
    queue.poll()

    then:
    thrown(exception_name)

    when:
    queue.poll()

    then:
    thrown(exception_name)

    expect:
    queue.poll() == 0
    queue.poll() == 1

    where:

    exception                           | exception_name                | iterations
    new UnsupportedOperationException() | UnsupportedOperationException | 2
    new IllegalArgumentException()      | IllegalArgumentException      | 2
  }
}
