/*
 * Copyright 2012 the original author or authors.
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

import spock.lang.Specification

import static java.util.concurrent.TimeUnit.*

class PollingConditionsSpec extends Specification {
  PollingConditions conditions = new PollingConditions()

  volatile int x = 0
  volatile int y = 0

  def "basic usage"() {
    setup:
    Thread.start {
      sleep(500)
      x = 21
    }
    Thread.start {
      sleep(1500)
      y = 42
    }

    expect:
    conditions.eventually {
      x == 21
      y == 42
    }
  }
}

