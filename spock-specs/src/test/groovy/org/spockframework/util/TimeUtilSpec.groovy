/*
 * Copyright 2012 the original author or authors.
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

import static java.util.concurrent.TimeUnit.*

class TimeUtilSpec extends Specification {
  def "convert time unit to seconds"() {
    expect:
    TimeUtil.toSeconds(1, MILLISECONDS) == 0.001
    TimeUtil.toSeconds(1, SECONDS) == 1
    TimeUtil.toSeconds(1, MINUTES) == 60

    TimeUtil.toSeconds(42, MILLISECONDS) == 0.042
    TimeUtil.toSeconds(42, SECONDS) == 42
    TimeUtil.toSeconds(42, MINUTES) == 42 * 60
  }
}
