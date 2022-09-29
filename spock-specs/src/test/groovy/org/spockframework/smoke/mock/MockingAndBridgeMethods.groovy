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

package org.spockframework.smoke.mock

import org.spockframework.mock.IMockInvocation
import spock.lang.*

class MockingAndBridgeMethods extends Specification {
  // ByteBuddy Transforms methods so that they are not bridge methods anymore
  boolean isCglib = Boolean.getBoolean("org.spockframework.mock.ignoreByteBuddy")

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

  @Issue('https://github.com/spockframework/spock/issues/122')
  def "bridge methods that override concrete methods in a supertype are still intercepted - check stubbed values"() {
    given:
    def di = Mock(DiscountedItem)
    def dpi = Mock(DiscountedPublicItem)

    when:
    di.getId() >> 333   // bridge method
    dpi.getId() >> 555  // non-bridge method

    then:
    1 * di.getId() == 333
    1 * dpi.getId() == 555
  }


  @Issue('https://github.com/spockframework/spock/issues/122')
  def "bridge methods that override concrete methods in a supertype are still intercepted - check mocking"() {
    given:
    def di = Mock(DiscountedItem)
    def dpi = Mock(DiscountedPublicItem)

    when:
    def diId = di.getId()   // call bridge method
    def dpiId = dpi.getId() // call non-bridge method

    then:
    1 * di.getId() >> { IMockInvocation inv ->
      assert inv.method.method.bridge == isCglib
    }
    1 * dpi.getId() >> { IMockInvocation inv ->
      assert inv.method.method.bridge == false
    }
    diId == 0
    dpiId == 0
  }


  @Issue('https://github.com/spockframework/spock/issues/122')
  def "check mocking of non-bridge methods"() {
    given:
    def di = Mock(DiscountedItem)
    def dpi = Mock(DiscountedPublicItem)

    when:
    def diPrice = di.getPrice()   // call non-bridge method
    def dpiPrice = dpi.getPrice() // call non-bridge method

    then:
    1 * di.getPrice() >> { IMockInvocation inv ->
      assert inv.method.method.bridge == false
    }
    1 * dpi.getPrice() >> { IMockInvocation inv ->
      assert inv.method.method.bridge == false
    }
    diPrice == 0
    dpiPrice == 0
  }

  @Issue('https://github.com/spockframework/spock/issues/122')
  def "bridge methods that override concrete methods in a supertype are still intercepted - check stubbing"() {
    given:
    def di = Mock(DiscountedItem) {
      1 * getId() >> { IMockInvocation inv ->
        assert inv.method.method.bridge == isCglib
        333
      }
    }
    def dpi = Mock(DiscountedPublicItem) {
      1 * getId() >> { IMockInvocation inv ->
        assert inv.method.method.bridge == false
        555
      }
    }

    when:
    def id = di.getId()   // call bridge method
    def pid = dpi.getId() // call non-bridge method

    then:
    id == 333
    pid == 555
  }

  @Issue('https://github.com/spockframework/spock/issues/122')
  def "bridge methods that override concrete methods in a supertype are still intercepted - check spying"() {
    given:
    def di = Spy(DiscountedItem) {
      1 * getId() >> { IMockInvocation inv ->
        assert inv.method.method.bridge == isCglib
        333
      }
    }
    def dpi = Spy(DiscountedPublicItem) {
      1 * getId() >> { IMockInvocation inv ->
        assert inv.method.method.bridge == false
        555
      }
    }

    when:
    def diText = di.toString()   // toString() implementation calls the bridge method getId()
    def dpiText = dpi.toString() // toString() implementation calls the non-bridge method getId()

    then:
    diText == '#333: 9 (discounted)'
    dpiText == '#555: 9 (discounted)'
  }
}
