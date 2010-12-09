/*
 * Copyright 2010 the original author or authors.
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

package spock.util.matcher

import org.hamcrest.TypeSafeMatcher
import org.hamcrest.Description

/**
 * Generalization of <tt>org.hamcrest.number.IsCloseTo</tt> to arbitrary
 * <tt>Number</tt>s (in particular <tt>BigDecimal</tt>s). Otherwise almost
 * a 1:1 copy of that class.
 */
public class IsCloseTo extends TypeSafeMatcher<Number> {
    private final Number delta
    private final Number value

  IsCloseTo(Number value, Number error) {
        this.delta = error
        this.value = value
    }

    @Override
    boolean matchesSafely(Number item) {
        actualDelta(item) <= 0
    }

    @Override
    void describeMismatchSafely(Number item, Description mismatchDescription) {
      mismatchDescription.appendValue(item)
                         .appendText(" differed by ")
                         .appendValue(actualDelta(item))
    }

    void describeTo(Description description) {
        description.appendText("a numeric value within ")
                .appendValue(delta)
                .appendText(" of ")
                .appendValue(value)
    }

    private Number actualDelta(Number item) {
      (item - value).abs() - delta
    }
}

