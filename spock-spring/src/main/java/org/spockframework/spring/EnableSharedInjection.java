package org.spockframework.spring;

import org.spockframework.util.Beta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * By default attempting to inject beans into {@code @Shared} fields will lead to an exception. This is because there
 * are many scenarios where using injection together with {@code @Shared} fields can leads to unexpected behaviour.
 *
 * To opt-in for injection into {@code @Shared} fields the specification needs to be annotated with
 * {@code @EnableSharedInjection}.
 *
 * Two examples of where using injection together with {@code @Shared} fields would lead to unexpected behaviour
 * and which are therefore unsupported even when the spec is annotated with {@code @EnableSharedInjection} are:
 * <ul>
 *   <li>
 *     feature methods make the context dirty which is signified by {@code @DirtiesContext} annotation on any of the
 *     feature methods or on the specification class with either {@code BEFORE_EACH_TEST_METHOD} or
 *     {code @AFTER_EACH_TEST_METHOD} class mode
 *   </li>
 *   <li>
 *     the spec or any of its feature methods are transactional which is signified by the use of {@code @Transactional}
 *   </li>
 * </ul>
 */
@Beta
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@interface EnableSharedInjection {
}
