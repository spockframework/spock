/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spock.lang;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;
import org.spockframework.compiler.DataProviderMethodTransformation;
import org.spockframework.util.Beta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Turns a method into a standalone data provider whose body is written in the
 * same grammar as a feature's {@code where:} block.
 *
 * <p>The body accepts the full {@code where:}-block grammar: leading {@code final}
 * where-block local variables, data tables, {@code <<} data pipes, derived
 * {@code c = a + b} assignments, {@code combined:} multiplications, and an
 * optional {@code filter:} block whose assertions drop rows. All resulting data
 * variables become the tuple columns, in declaration order; where-block local
 * variables and method parameters do not.
 *
 * <p>When invoked, the method returns a fresh, lazy {@code Iterator<Tuple>} of
 * rows; each row is an arity-specific Groovy tuple ({@code Tuple1},
 * {@code Tuple2}, ...). The iterator can be fed into any feature via the
 * existing multi-variable data pipe:
 *
 * <pre><code>
 * &#64;DataProvider
 * def pairs() {
 *   a | b
 *   1 | 2
 *   2 | 3
 * }
 *
 * // in a feature
 * where:
 * [a, b] &lt;&lt; pairs()
 * </code></pre>
 *
 * <p>The annotated method may be static or an instance method, may declare
 * parameters (which are in scope for the whole body, like {@code final}
 * where-block variables, but are not tuple columns), and may be declared on any
 * class, not just specifications. Inside a {@code Specification} only
 * {@code @Shared} and static fields may be referenced, on any other class
 * instance fields are accessible as usual.
 *
 * <p>The returned iterator is {@link AutoCloseable}; closing it (or exhausting
 * it) closes any {@code AutoCloseable} where-block local variables in reverse
 * declaration order. A feature consuming the iterator through a data pipe
 * closes it automatically.
 *
 * <p>{@code @CompileStatic} is not supported on {@code @DataProvider} methods,
 * because the data provider grammar is inherently dynamic.
 *
 * @since 2.5
 */
@Beta
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@GroovyASTTransformationClass(classes = DataProviderMethodTransformation.class)
public @interface DataProvider {
}
