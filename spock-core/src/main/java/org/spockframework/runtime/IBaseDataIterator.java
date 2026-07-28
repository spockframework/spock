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

import java.util.Iterator;
import java.util.List;

/**
 * A special iterator that gives access to the data produced by Spock's data providers.
 * <p>
 * The creator of the data iterator is responsible to close it.
 * <p>
 * On the feature path (i.e. {@link IDataIterator}), {@link #next()} will return {@code null} when an error occurs
 * during calculation of the values, because errors are reported to the run supervisor instead of being thrown.
 * Consumers of such a data iterator should check for {@code null} values, and skip the iteration if it is
 * {@code null}. Standalone implementations have no supervisor and throw instead, so their {@link #next()} never
 * returns {@code null}.
 *
 * @param <T> the row type produced by this iterator
 * @since 2.5
 */
public interface IBaseDataIterator<T> extends Iterator<T>, AutoCloseable {
  /**
   * Shared empty result for {@link #getWhereVariableValues()} when no where-block variables are declared.
   * <p>
   * This array is shared across all callers and must never be mutated. It is safe to share precisely because a
   * zero-length array has no elements to write.
   *
   * @since 2.5
   */
  Object[] NO_WHERE_VARIABLE_VALUES = new Object[0];

  /**
   * @return the number of data sets that are provided by this iterator. This will be {@code -1} if it cannot be determined.
   */
  int getEstimatedNumIterations();

  /**
   * @return the names of the data variables in the order they are present in the row
   */
  List<String> getDataVariableNames();

  /**
   * @return the values of the where-block variables, computed once, in
   * declaration order; empty if no where-block variables are declared
   * @since 2.5
   */
  default Object[] getWhereVariableValues() {
    return NO_WHERE_VARIABLE_VALUES;
  }
}
