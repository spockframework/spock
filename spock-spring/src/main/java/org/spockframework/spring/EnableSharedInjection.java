package org.spockframework.spring;

import org.spockframework.util.Beta;

import java.lang.annotation.*;

/**
 * By default attempting to inject beans into {@code @Shared} fields will lead to an exception. This is because there
 * are many scenarios where using injection together with {@code @Shared} fields can leads to unexpected behaviour.
 * <p>
 * To opt-in for injection into {@code @Shared} fields the specification needs to be annotated with
 * {@code @EnableSharedInjection}.
 * <p>
 * One particular example of where using injection together with {@code @Shared} fields would lead to unexpected
 * behaviour and which is therefore unsupported even when the spec is annotated with {@code @EnableSharedInjection} is
 * when feature methods make the application context dirty. This is signified by {@code @DirtiesContext} annotation on
 * any of the feature methods or on the specification class with either {@code BEFORE_EACH_TEST_METHOD} or
 * {code @AFTER_EACH_TEST_METHOD} class mode.
 * <p>
 * Another example where using injection together with {@code @Shared} fields could lead to unexpected behaviour is
 * when the specification is using {@code @Transactional} annotation on either class or feature method level. Please
 * note that in such case the shared initializers, {@code setupSpec()} methods and {@code where} blocks are executed
 * outside of a transaction.
 *
 * @since 2.0
 */
@Beta
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EnableSharedInjection {
}
