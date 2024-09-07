/*
 *  Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.spockframework.runtime.extension;

import java.lang.reflect.Type;

import org.spockframework.mock.IMockMethod;
import org.spockframework.util.Beta;
import org.spockframework.util.Nullable;
import org.spockframework.util.ThreadSafe;

/**
 * Allows to enhance {@link org.spockframework.mock.EmptyOrDummyResponse} with custom default values.
 * <p>
 * Will be instantiated via the {@link java.util.ServiceLoader} mechanism.
 * <p>
 * Implementations must be thread-safe and should not have any state.
 * <p>
 * This extension is intended for framework authors and users who want to provide default values for their own types.
 * The extension will only be called if no sensible default value can be provided by the default mechanism.
 * If you want to change the default behavior for all types, you should implement a custom {@link org.spockframework.mock.IDefaultResponse}.
 *
 * @since 2.4
 * @see org.spockframework.mock.EmptyOrDummyResponse
 * @see org.spockframework.mock.IDefaultResponse
 */
@Beta
@ThreadSafe
public interface IDefaultValueProviderExtension {
  /**
   * Provides a default value for the given type.
   * <p>
   * This method will be called for every `EmptyOrDummyResponse` non-default type, the returned values are not cached.
   *
   * @param type the type for which a default value should be provided, see {@link IMockMethod#getReturnType()}
   * @param exactType the exact type for which a default value should be provided, see {@link IMockMethod#getExactReturnType()}
   * @return the value or null if no default value can be provided
   */
  @Nullable
  Object provideDefaultValue(Class<?> type, Type exactType);
}
