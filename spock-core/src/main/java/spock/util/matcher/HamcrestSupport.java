/*
 * Copyright 2010 the original author or authors.
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

package spock.util.matcher;

import org.hamcrest.Matcher;

import org.spockframework.runtime.InvalidSpecException;

public class HamcrestSupport {
  /**
   * Used to match a value against a (Hamcrest) matcher.
   * Only allowed in places where a condition is expected
   * (expect-block, then-block, after an 'assert' keyword).
   *
   * <p>Basic example:
   *
   * <pre>
   * import static spock.util.matcher.HamcrestSupport.that
   * import static org.hamcrest.CoreMatchers.equalTo // ships with JUnit
   *
   * def foo = 42
   *
   * expect:
   * that(foo, equalTo(42))
   * </pre>
   *
   * Note that Spock supports an even simpler syntax for applying matchers:
   *
   * <pre>
   * expect:
   * foo equalTo(42)
   * </pre>
   *
   * However, the simpler syntax cannot be used in explicit conditions
   * (i.e. after the 'assert' keyword), and may not be as IDE-friendly.
   * That's why this method is provided as an alternative.
   *
   * <h3>When would I use matchers?</h3>
   *
   * <p>Due to Spock's good diagnostic messages and Groovy's expressivity,
   * matchers are less often needed than when, say, writing JUnit tests
   * in Java. However, they come in handy when more complex conditions
   * are required (and possibly repeated throughout a project).
   * In such cases, Spock's Hamcrest integration provides the best of two worlds:
   * the diagnostic messages known from Spock's conditions, and the
   * custom failure messages of Hamcrest matchers.
   *
   * <h3>Third-party matchers</h3>
   *
   * <p>The matchers that ship with JUnit aren't very useful per se.
   * Instead, you will want to use matchers from Hamcrest
   * (http://code.google.com/p/hamcrest/) or other libraries. Both Hamcrest
   * 1.1 and 1.2 are supported. You can also write your own matchers,
   * building up a matcher library that's specific to the needs of your project.
   *
   * @param value the actual value
   * @param matcher a matcher describing the expected value
   * @param <T> the actual value's type
   */
  @SuppressWarnings("UnusedDeclaration")
  public static <T> void that(T value, Matcher<? super T> matcher) {
    throw new InvalidSpecException("that() can only be used where a condition is expected");
  }

  /**
   * Alias for {@link #that(Object, org.hamcrest.Matcher)} intended for use in then-blocks.
   *
   * @param value the actual value
   * @param matcher a matcher describing the expected value
   * @param <T> the actual value's type
   */
  @SuppressWarnings("UnusedDeclaration")
  public static <T> void expect(T value, Matcher<? super T> matcher) {
    that(value, matcher);
  }
}
