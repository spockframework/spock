/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.smoke.extension

import java.util.concurrent.TimeUnit

import org.spockframework.runtime.SpockTimeoutError

import spock.lang.*

/**
 *
 * @author Peter Niederwieser
 */
class TimeoutExtension extends Specification {
  @Timeout(1)
  def "in time"() {
    setup: Thread.sleep(500)
  }

  @FailsWith(SpockTimeoutError)
  @Timeout(1)
  def "not in time"() {
    setup: Thread.sleep(1100)
  }

  @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
  def "in time millis"() {
    setup: Thread.sleep(500)
  }

  @FailsWith(SpockTimeoutError)
  @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
  def "not in time millis"() {
    setup: Thread.sleep(1100)
  }
}