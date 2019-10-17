/*
 * Copyright 2009 the original author or authors.
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
import org.spockframework.runtime.extension.builtin.IgnoreIfExtension;

/**
 * Ignores the annotated spec or feature if the given condition holds.
 * Same as {@link Requires} except that the condition is inverted.
 *
 * The configured closure is called with a delegate of type
 * {@link org.spockframework.runtime.extension.builtin.PreconditionContext}
 * which provides access to system properties, environment variables, the type
 * of operating system and JVM.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtensionAnnotation(IgnoreIfExtension.class)
public @interface IgnoreIf {
	Class<? extends Closure> value();
}
