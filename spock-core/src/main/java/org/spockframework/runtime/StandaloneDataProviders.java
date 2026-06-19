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

import groovy.lang.Closure;
import groovy.lang.Tuple;
import org.spockframework.util.Beta;
import org.spockframework.util.InternalSpockError;
import org.spockframework.util.Nullable;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.spockframework.runtime.GroovyRuntimeUtil.closeQuietly;

/**
 * Runtime helper for standalone {@code @DataProvider} methods.
 * <p>
 * Called by code the {@code @DataProvider} transform generates; this class is not
 * intended to be used directly.
 *
 * @since 2.5
 */
@Beta
public abstract class StandaloneDataProviders {
  private StandaloneDataProviders() {
  }

  /**
   * Builds the row iterator of a standalone {@code @DataProvider} method from the closures
   * the transform emitted inline into the method body.
   *
   * @param dataVariableNames the names of the data variables, i.e. the tuple columns, in row order
   * @param whereVariables computes the where-block variable values once, or {@code null} if there are none
   * @param dataProviders the data providers, in declaration order
   * @param dataProcessor turns one set of raw provider values into the data variable values
   * @param filter drops rows by throwing an {@code AssertionError}, or {@code null} if there is none
   * @param dataVariableMultiplications provides the {@code combined:} multiplications, or {@code null} if there are none
   * @return a fresh, lazy iterator over the rows
   */
  public static IStandaloneDataIterator dataIterator(String[] dataVariableNames,
                                                     @Nullable Closure<?> whereVariables,
                                                     StandaloneDataProviderDescriptor[] dataProviders,
                                                     Closure<?> dataProcessor,
                                                     @Nullable Closure<?> filter,
                                                     @Nullable Closure<?> dataVariableMultiplications) {
    return new StandaloneDataIterator(DataIteratorFactory.createDataIterator(
      new StandaloneDataIterationContext(dataVariableNames, whereVariables, dataProviders,
        dataProcessor, filter, dataVariableMultiplications)));
  }

  /**
   * Decorates the {@code Object[]} row iterator: rows are mapped to arity-specific tuples,
   * exhaustion closes the underlying iterator (and thereby any {@code AutoCloseable}
   * where-block variables), and {@code close()} is idempotent.
   */
  private static class StandaloneDataIterator implements IStandaloneDataIterator {
    private final IDataIterator delegate;
    private Object[] nextRow;
    private boolean closed = false;

    StandaloneDataIterator(IDataIterator delegate) {
      this.delegate = delegate;
    }

    @Override
    public boolean hasNext() {
      if (nextRow != null) {
        return true;
      }
      if (closed) {
        return false;
      }
      // the delegate's next() returns null instead of a row when a filter block
      // dropped all remaining rows, so look ahead until a real row appears
      while (nextRow == null && delegate.hasNext()) {
        nextRow = delegate.next();
      }
      if (nextRow == null) {
        // exhausted; close eagerly so AutoCloseable where-block variables are
        // released even if the iterator is discarded without an explicit close()
        close();
        return false;
      }
      return true;
    }

    @Override
    public Tuple next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      Object[] row = nextRow;
      nextRow = null;
      return createTuple(row);
    }

    @Override
    public void close() {
      if (closed) {
        return;
      }
      closed = true;
      closeQuietly(delegate);
    }

    @Override
    public int getEstimatedNumIterations() {
      return delegate.getEstimatedNumIterations();
    }

    @Override
    public List<String> getDataVariableNames() {
      return delegate.getDataVariableNames();
    }

    @Override
    public Object[] getWhereVariableValues() {
      return delegate.getWhereVariableValues();
    }
  }

  /**
   * The highest tuple arity any supported Groovy version ships as a dedicated class;
   * Groovy 2.5 only ships {@code Tuple1}-{@code Tuple9}, Groovy 3.0+ adds
   * {@code Tuple10}-{@code Tuple16}.
   */
  private static final int HIGHEST_FIXED_TUPLE_ARITY = 16;

  // lazily resolved (benign race); the classes must not be referenced statically
  // because not all of them exist on every supported Groovy version
  private static final Constructor<?>[] TUPLE_CONSTRUCTORS = new Constructor<?>[HIGHEST_FIXED_TUPLE_ARITY + 1];
  private static final boolean[] TUPLE_CONSTRUCTOR_MISSING = new boolean[HIGHEST_FIXED_TUPLE_ARITY + 1];

  private static Tuple createTuple(Object[] row) {
    Constructor<?> constructor = tupleConstructor(row.length);
    if (constructor == null) {
      // arity not available as a dedicated class on the current Groovy version
      return new Tuple(row.clone());
    }
    try {
      return (Tuple) constructor.newInstance(row);
    } catch (ReflectiveOperationException e) {
      throw new InternalSpockError("Failed to create tuple of arity %d", e).withArgs(row.length);
    }
  }

  @Nullable
  private static Constructor<?> tupleConstructor(int arity) {
    if (arity < 1 || arity > HIGHEST_FIXED_TUPLE_ARITY) {
      return null;
    }
    Constructor<?> constructor = TUPLE_CONSTRUCTORS[arity];
    if (constructor == null && !TUPLE_CONSTRUCTOR_MISSING[arity]) {
      constructor = resolveTupleConstructor(arity);
      if (constructor == null) {
        TUPLE_CONSTRUCTOR_MISSING[arity] = true;
      } else {
        TUPLE_CONSTRUCTORS[arity] = constructor;
      }
    }
    return constructor;
  }

  @Nullable
  private static Constructor<?> resolveTupleConstructor(int arity) {
    Class<?> tupleClass;
    try {
      tupleClass = Class.forName("groovy.lang.Tuple" + arity, false, Tuple.class.getClassLoader());
    } catch (ClassNotFoundException e) {
      return null;
    }
    for (Constructor<?> candidate : tupleClass.getConstructors()) {
      // the element constructor's erased parameter types are all Object, which also
      // rules out the copy constructor (e.g. Tuple1(Tuple1) vs. Tuple1(T1))
      if (candidate.getParameterCount() == arity
        && Arrays.stream(candidate.getParameterTypes()).allMatch(type -> type == Object.class)) {
        return candidate;
      }
    }
    return null;
  }
}
