package org.spockframework.lang;

import org.spockframework.util.Beta;

import java.lang.annotation.*;

/**
 * Indicates that the closure argument(s) of the annotated method are code blocks
 * containing conditions, allowing to leave off the assert keyword.
 * As in expect-blocks and then-blocks, variable declarations
 * and void method invocations will not be considered conditions.
 *
 * <p>This annotation only takes effect if the closures are passed as literals,
 * and the Groovy compiler can (at compilation time) determine the target
 * type of the method invocation referencing the annotated method. If the annotated
 * method is overloaded, the closure arguments of all overloads are considered code blocks.
 */
@Beta
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionBlock {}
