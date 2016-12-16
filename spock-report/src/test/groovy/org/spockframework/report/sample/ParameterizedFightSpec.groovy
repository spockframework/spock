/*
 * Copyright 2013 the original author or authors.
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

package org.spockframework.report.sample

import spock.lang.*

@Issue("1337")
@Title("Fight or Flight (Parameterized)")
class ParameterizedFightSpec extends Specification {
  @Unroll("Ninja should #action when encountering a #opponent opponent")
  def "Ninja should choose action depending on opponent"() {
    given:
    System.err.println opponent

    when:
    println action

    then:
    opponent != 'unknown'

    where:
    action  | opponent
    'flee'  | 'strong'
    'fight' | 'weak'
    'flee'  | 'unknown'
  }

  private void impl() {}
}
