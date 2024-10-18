/*
 * Copyright 2013 the original author or authors.
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

package org.spockframework.runtime

import org.spockframework.runtime.model.parallel.Resources
import spock.lang.*

@ResourceLock(Resources.SYSTEM_OUT)
@ResourceLock(Resources.SYSTEM_ERR)
class StandardStreamsCapturerSpec extends Specification {
  def capturer = new StandardStreamsCapturer()
  def listener = Mock(IStandardStreamsListener)

  def setup() {
    capturer.addStandardStreamsListener(listener)
    capturer.start()
    capturer.muteStandardStreams()
  }

  def cleanup() {
    capturer.stop()
  }

  def "captures standard out/err while installed"() {
    when:
    println("some message")
    System.err.println("some error message")

    then:
    1 * listener.standardOut("some message\n")
    then:
    1 * listener.standardErr("some error message\n")
    then:
    0 * _

    when:
    capturer.stop()
    println("another message")
    System.err.println("another error message")

    then:
    0 * _
  }

  def "supports multiple listeners"() {
    def listener2 = Mock(IStandardStreamsListener)
    capturer.addStandardStreamsListener(listener2)

    when:
    println("some message")

    then:
    1 * listener.standardOut("some message\n")
    1 * listener2.standardOut("some message\n")
  }

  def "allows to unregister listeners"() {
    when:
    capturer.removeStandardStreamsListener(listener)
    println("some message")

    then:
    0 * _
  }
}
