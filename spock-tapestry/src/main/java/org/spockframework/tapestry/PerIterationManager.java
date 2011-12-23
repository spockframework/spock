/*
 * Copyright 2009, 2011 the original author or authors.
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

package org.spockframework.tapestry;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages a collection of {@link PerIterationValue}s as a thread-safe set of weak references.
 */
public class PerIterationManager implements IPerIterationManager {

    private class PerIterationValueImpl<T> implements PerIterationValue<T> {

        final AtomicReference<T> ref = new AtomicReference<T>();

        @Override
        public T get() {
            return ref.get();
        }

        @Override
        public T set(T newValue) {

            ref.set(newValue);

            return newValue;
        }

        void cleanup() {
            ref.set(null);
        }
    }

    private final Set<WeakReference<PerIterationValueImpl>> values = new CopyOnWriteArraySet<WeakReference<PerIterationValueImpl>>();

    @Override
    public <T> PerIterationValue<T> createValue() {

        PerIterationValueImpl<T> result = new PerIterationValueImpl<T>();

        values.add(new WeakReference<PerIterationValueImpl>(result));

        return result;
    }

    @Override
    public void cleanup() {
        Iterator<WeakReference<PerIterationValueImpl>> iterator = values.iterator();

        while (iterator.hasNext()) {
            WeakReference<PerIterationValueImpl> ref = iterator.next();
            PerIterationValueImpl value = ref.get();

            if (value == null) {
                iterator.remove();
            } else {
                value.cleanup();
            }
        }
    }
}
