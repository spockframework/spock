/*
 * Copyright 2025 the original author or authors.
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

package spock.lang;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;
import org.spockframework.compiler.condition.VerifyAllMethodTransform;
import org.spockframework.util.Beta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the code blocks of the annotated methods
 * may contain conditions, allowing to omit the {@code assert} keyword.
 * As in expect-blocks and then-blocks, variable declarations
 * and void method invocations will not be considered conditions.
 * The method can be defined inside the {@link Specification} class or in a separate class.
 * The method must have a {@code void} return type.
 * In contrast to {@link Verify}, all conditions will be evaluated before failing the test.
 *
 * @author Pavlo Shevchenko
 * @since 2.4
 */
@Beta
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
@GroovyASTTransformationClass(classes = VerifyAllMethodTransform.class)
public @interface VerifyAll {
}
