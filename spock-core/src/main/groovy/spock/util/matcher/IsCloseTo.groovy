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

