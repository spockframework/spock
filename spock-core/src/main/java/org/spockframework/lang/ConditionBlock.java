/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.lang;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the closure argument(s) of the annotated method are code blocks
 * containing conditions, allowing to leave off the assert keyword.
 * As in expect-blocks and then-blocks, variable declarations
 * and void method invocations will not be considered conditions.
 *
 * <p>This annotation only takes effect if:
 * - the closures are passed as literals, and the Groovy compiler can (at compilation time) determine the target
 * type of the method invocation referencing the annotated method,
 * - the annotated method is called on object of type known at compilation time (no "def").
 * If the annotated method is overloaded, the closure arguments of all overloads are considered code blocks.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionBlock {}
