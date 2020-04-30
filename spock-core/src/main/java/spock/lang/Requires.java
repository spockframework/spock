/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spock.lang;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import groovy.lang.Closure;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.PreconditionContext;
import org.spockframework.runtime.extension.builtin.RequiresExtension;

/**
 * Ignores the annotated spec, feature or selected iterations unless the given condition holds.
 * Same as {@link IgnoreIf} except that the condition is inverted.
 *
 * <p>The configured closure is called with a delegate of type {@link PreconditionContext}
 * which provides access to system properties, environment variables, the type
 * of operating system and JVM.
 *
 * <p>If applied to a data driven feature, the closure can also access the data variables.
 * If the closure does not reference any actual data variables or is applied to a spec,
 * the whole annotated element is skipped up-front, no fixtures, data providers or anything
 * else will be executed. But if the closure actually does reference valid data variables,
 * the whole workflow is followed up to the feature method invocation, where then the closure
 * is checked and it is decided whether to abort the specific iteration or not.
 *
 * @see IgnoreIf
 * @see PreconditionContext
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtensionAnnotation(RequiresExtension.class)
public @interface Requires {
  Class<? extends Closure> value();
}
