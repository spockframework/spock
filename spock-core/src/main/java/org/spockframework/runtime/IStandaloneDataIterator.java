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

package org.spockframework.runtime;

import groovy.lang.Tuple;
import org.spockframework.util.Beta;

/**
 * The iterator returned by a standalone {@code @DataProvider} method: each row is an
 * arity-specific Groovy tuple.
 * <p>
 * Unlike the feature path, {@link #next()} never returns {@code null}; errors during
 * value calculation are thrown. The iterator closes its {@code AutoCloseable}
 * where-block variables when it is closed and when it is exhausted; {@link #close()}
 * is idempotent.
 *
 * @since 2.5
 */
@Beta
public interface IStandaloneDataIterator extends IBaseDataIterator<Tuple> {
}
