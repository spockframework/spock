package spock.lang;

import java.lang.annotation.*;

/**
 * Indicates that iterations of a data-driven feature should be made visible
 * as separate features to the outside world (IDEs, reports, etc.). By default,
 * the name of an iteration is the feature's name followed by the data variables
 * and their values and the iteration index.
 * This can be changed by providing a naming pattern after @Unroll. A naming pattern
 * may refer to data variables by prepending their names with #. Example:
 *
 * <pre>
 * &#64;Unroll("#name should have length #length")
 * def "name length"() {
 *   expect:
 *   name.size() == length
 *
 *   where:
 *   name << ["Kirk", "Spock", "Scotty"]
 *   length << [4, 5, 6]
 * }
 * </pre>
 *
 * Alternatively, the naming pattern can also be embedded in the method name:
 *
 * <pre>
 * &#64;Unroll
 * def "#name should have length #length"() {
 *   ...
 * }
 * </pre>
 *
 * The {@code Unroll} annotation can also be put on a spec class. This has the same
 * effect as putting it on every data-driven feature method that is not already
 * annotated with {@code Unroll}. By embedding the naming pattern in the method
 * names, each method can still have its own pattern.
 *
 * <p>Having {@code @Unroll} on a super spec does not influence the features of sub specs,
 * that is {@code @Unroll} is not inheritable.
 *
 * @author Peter Niederwieser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Unroll {
  String value() default "";
}
