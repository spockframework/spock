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

package org.spockframework.smoke.mock

import org.spockframework.mock.IMockInvocation

import spock.lang.Specification

class MockingAndBridgeMethods extends Specification {
  def "bridge methods are not intercepted"() {
    def comparator = Mock([:], MyComparator)

    when:
    comparator.compare((Object) 0, (Object) 0) // call bridge method

    then:
    1 * comparator.compare(0, 0) >> { IMockInvocation inv ->
      assert inv.getMethod().getParameterTypes() == [Integer, Integer]
    }
  }

  static class MyComparator implements Comparator<Integer> {
    int compare(Integer a, Integer b) {
      0
    }
  }
}


