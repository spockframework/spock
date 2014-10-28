package spock.lang;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.AssertionModeExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets {@link spock.lang.AssertionType} for the feature.
 * If assertion mode set to {@link spock.lang.AssertionType#CHECK_ALL_THEN_FAIL} all assertion in then: or expect: block
 * will be evaluated and all failures will be reported.
 * If assertion mode set to {@link spock.lang.AssertionType#FAIL_AFTER_FIRST_FAILURE} (it is enabled by default) assertion will be evaluated until first failure will occur.
 * Only this failure will be reported.
 * For example:
 * <pre>
 * {@code
 * &#064;AssertionMode(CHECK_ALL_THEN_FAIL)
 * def "test"(){
 *   when:
 *       def x = 5
 *       def y = 7
 *   then:
 *       x == 42
 *       y == 42
 * }
 * }
 * </pre>
 *
 * will produce two failures
 *
 * <pre>
 *
 Condition not satisfied:

 x == 42
 | |
 5 false
 <Click to see difference>

 at IntegrationTest.test1(Tests.groovy:12)


 Condition not satisfied:

 y == 42
 | |
 7 false
 <Click to see difference>

 at IntegrationTest.test1(Tests.groovy:13)
 * </pre>
 *
 *
 * You can separate evaluation blocks by chaining then: blocks. For example, this will produce only one failure:
 * <pre>
 * {@code
 * &#064;AssertionMode(CHECK_ALL_THEN_FAIL)
 * def "test"(){
 *   when:
 *       def x = 5
 *       def y = 7
 *   then:
 *       x == 42
 *   then:
 *       y == 42
 * }
 * }
 * </pre>
 *
 *
 * &#064;AssertionMode can be specified on method and on class level. If it is specified on class level it will affect all features unless them has own &#064;AssertionMode annotation.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(AssertionModeExtension.class)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface AssertionMode {
  AssertionType value();
}
