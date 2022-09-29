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

import org.hamcrest.TypeSafeMatcher
import org.hamcrest.Description

/**
 * Generalization of <tt>org.hamcrest.number.IsCloseTo</tt> to arbitrary
 * <tt>Number</tt>s (in particular <tt>BigDecimal</tt>s). Otherwise almost
 * identical to that class.
 */
class IsCloseTo extends TypeSafeMatcher<Number> {
  private final Number value
  private final Number epsilon

  IsCloseTo(Number value, Number epsilon) {
    this.value = value
    this.epsilon = epsilon
  }

  @Override
  boolean matchesSafely(Number item) {
    delta(item) <= epsilon
  }

  @Override
  void describeMismatchSafely(Number item, Description mismatchDescription) {
    mismatchDescription.appendValue(item)
        .appendText(" differed by ")
        .appendValue(delta(item))
  }

  void describeTo(Description description) {
    description.appendText("a numeric value within ")
        .appendValue(epsilon)
        .appendText(" of ")
        .appendValue(value)
  }

  private Number delta(Number item) {
    // handle special values (infinity, nan)
    if (item == value) return 0

    (item - value).abs()
  }
}

