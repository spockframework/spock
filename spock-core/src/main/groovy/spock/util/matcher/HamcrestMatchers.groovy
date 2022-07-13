/*
 * Copyright 2010 the original author or authors.
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

package spock.util.matcher

import org.hamcrest.Matcher

/**
 * Factory for Hamcrest matchers that ship with Spock.
 *
 * @author Peter Niederwieser
 */
class HamcrestMatchers {
  /**
   * Tells if two numbers are equal within some range of acceptable error.
   * In contrast to the equally named matcher in the Hamcrest library, this
   * matcher also handles <tt>BigDecimal</tt>s (and any other <tt>Number</tt>s),
   * which is important for Groovy interoperability.
   *
   * Example usage:
   *
   * <pre>
   * expect:
   * myPi closeTo(3.1415, 0.01)
   * </pre>
   *
   * @param operand the expected number
   * @param error the range of acceptable error
   * @return a matcher that tells if two numbers are equal within some range of
   * acceptable error
   */

  static Matcher<Number> closeTo(Number operand, Number error) {
    return new IsCloseTo(operand, error)
  }
}
