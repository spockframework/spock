
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

package org.spockframework.smoke.mock

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import org.spockframework.mock.TooFewInvocationsError
import org.spockframework.mock.TooManyInvocationsError

import spock.lang.*

class InvokingMocksFromMultipleThreads extends Specification {
  def numThreads = 10
  def list = Mock(List)
  def latch = new CountDownLatch(10)

  def "invoking a mock from multiple threads"() {
    when:
    numThreads.times { threadId ->
      Thread.start {
        try {
          100.times { count ->
            list.add(count)
            Thread.sleep(1)
          }
        } finally {
          latch.countDown()
        }
      }
    }
    latch.await(10, TimeUnit.SECONDS)

    then:
    interaction {
      100.times { count -> numThreads * list.add(count) }
    }
  }

  @FailsWith(TooManyInvocationsError)
  def "invoking a mock from multiple threads - too many invocations"() {
    when:
    numThreads.times { threadId ->
      Thread.start {
        try {
          100.times { count ->
            list.add(count)
            if (threadId == 0 && count == 99) list.add(count)
            Thread.sleep(1)
          }
        } catch(TooManyInvocationsError ignored) {
          // catching this avoids irritating build output
        } finally {
          latch.countDown()
        }
      }
    }
    latch.await(10, TimeUnit.SECONDS)

    then:
    interaction {
      100.times { numThreads * list.add(it) }
    }
  }

  @FailsWith(TooFewInvocationsError)
  def "invoking a mock from multiple threads - too few invocations"() {
    when:
    numThreads.times { threadId ->
      Thread.start {
        try {
          100.times { count ->
            if (!(threadId == 0 && count == 99)) list.add(count)
            Thread.sleep(1)
          }
        } catch(TooFewInvocationsError ignored) {
          // catching this avoids irritating build output
        } finally {
          latch.countDown()
        }
      }
    }
    latch.await(10, TimeUnit.SECONDS)

    then:
    interaction {
      100.times { count -> numThreads * list.add(count) }
    }
  }
}
