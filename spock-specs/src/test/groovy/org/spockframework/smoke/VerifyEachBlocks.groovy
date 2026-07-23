/*
 * Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.smoke


import org.opentest4j.MultipleFailuresError
import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.SpockAssertionError
import spock.lang.Snapshot
import spock.lang.Snapshotter

import java.util.function.IntConsumer

class VerifyEachBlocks extends EmbeddedSpecification {

  def "verifyEach succeeds"() {
    given:
    def range = (1..10)

    expect:
    verifyEach(range) {
      it > 0
    }

    and: 'parameter is the same as the delegate'
    verifyEach(range) {
      it == delegate
    }
  }

  def "verifyEach handles a single failed element verification"(@Snapshot Snapshotter snap) {
    given:
    def range = (1..10)

    when:
    verifyEach(range) {
      it < 10
    }

    then:
    SpockAssertionError e = thrown()
    snap.assertThat(e.message).matchesSnapshot()
  }

  def "verifyEach handles multiple failed element verifications"(@Snapshot Snapshotter snap) {
    given:
    def range = (1..10)

    when:
    verifyEach(range) {
      it % 2 == 0
    }

    then:
    MultipleFailuresError e = thrown()
    snap.assertThat(e.message).matchesSnapshot()
  }

  def "rendering of the item can be customized with the namer"(@Snapshot Snapshotter snap) {
    given:
    def range = (1..10)

    when:
    verifyEach(range, { "int($it)" }) {
      it < 10
    }

    then:
    SpockAssertionError e = thrown()
    snap.assertThat(e.message).matchesSnapshot()
  }

  def "verifyEach fails handles exception"(@Snapshot Snapshotter snap) {
    given:
    def range = (1..10)

    when:
    verifyEach(range) {
      if (it == 5) {
        throw new RuntimeException("x == $it").tap {
          // remove stack trace to make snapshot stable
          it.stackTrace = []
        }
      }
    }

    then:
    SpockAssertionError e = thrown()
    snap.assertThat(e.message).matchesSnapshot()
  }

  def "verifyEach fails handles exceptions"(@Snapshot Snapshotter snap) {
    given:
    def range = (1..10)

    when:
    verifyEach(range) {
      if (it in (5..6)) {
        throw new RuntimeException("x == $it").tap {
          // remove stack trace to make snapshot stable
          it.stackTrace = []
        }
      }
    }

    then:
    MultipleFailuresError e = thrown()
    snap.assertThat(e.message).matchesSnapshot()
  }

  def "verifyEach fails handles nested exceptions"(@Snapshot Snapshotter snap) {
    given:
    def range = (1..10)

    when:
    verifyEach(range) {
      checks(it)
    }

    then:
    MultipleFailuresError e = thrown()
    snap.assertThat(e.message).matchesSnapshot()
  }

  def "verifyEach can have an optional index parameter"() {
    given:
    def range = (1..10)

    expect:
    verifyEach(range) { it, index ->
      it  == index + 1
    }
  }

  def "verifyEach supports object arrays"() {
    given:
    Integer[] array = [1, 2, 3]

    expect:
    verifyEach(array) {
      it > 0
    }

    and: 'parameter is the same as the delegate'
    verifyEach(array) {
      it == delegate
    }
  }

  def "verifyEach on an array handles a failed element verification"() {
    given:
    Integer[] array = [1, 2, 3]

    when:
    verifyEach(array) {
      it < 3
    }

    then:
    SpockAssertionError e = thrown()
    e.message.contains('item[2] 3')
  }

  def "verifyEach on an array supports the namer"() {
    given:
    Integer[] array = [1, 2, 3]

    when:
    verifyEach(array, { "int($it)" }) {
      it < 3
    }

    then:
    SpockAssertionError e = thrown()
    e.message.contains('int(3)')
  }

  def "verifyEach on an array can have an optional index parameter"() {
    given:
    Integer[] array = [1, 2, 3]

    expect:
    verifyEach(array) { it, index ->
      it == index + 1
    }
  }

  def "verifyEach on a null array fails with a clear message"() {
    when:
    verifyEach((Integer[]) null) {
      it > 0
    }

    then:
    SpockAssertionError e = thrown()
    e.message == "Target of 'verifyEach' block must not be null"
  }

  void checks(int x) {
    doCheck(x, this.&nestedException)
    doCheck(x, this.&nestedAssertion)
  }

  void doCheck(int x, IntConsumer consumer) {
    consumer.accept(x)
  }

  void nestedException(int x) {
    if (x == 5) {
      throw new RuntimeException("x == 5").tap {
        // remove stack trace to make snapshot stable
        it.stackTrace = []
      }
    }
  }

  void nestedAssertion(int x) {
    assert x != 7
  }
}
