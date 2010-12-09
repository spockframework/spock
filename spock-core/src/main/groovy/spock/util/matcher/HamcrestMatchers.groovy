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
   * <pre>
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
