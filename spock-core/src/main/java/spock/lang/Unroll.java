package spock.lang;

import groovy.lang.Closure;

import java.lang.annotation.*;

/**
 * Indicates that iterations of a data-driven feature should be made visible
 * as separate features to the outside world (IDEs, reports, etc.). By default,
 * the name of an iteration is the feature's name followed by a consecutive number.
 * This can be changed by providing a naming pattern after @Unroll. A naming pattern
 * is a closure returning a GString. Data variables may be referenced with the
 * usual GString '$' notation. Example:
 *
 * <pre>
 * &#64;Unroll({ "$name should have length $length" })
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
 * @author Peter Niederwieser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Unroll {
  // to ensure best possible tool support, we use the same default naming
  // scheme as Junit's @Parameterized
  Class<? extends Closure> value() default Closure.class;
}
