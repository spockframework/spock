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

import org.spockframework.util.Beta;

/**
 * Provides access to the {@link IStore} for a given {@linkplain IStore.Namespace namespace}.
 * @since 2.4
 */
@Beta
public interface IStoreProvider {

  /**
   * Get the {@link IStore} for the supplied {@linkplain IStore.Namespace namespace}.
   * <p>
   * A store is bound to its context lifecycle. When a
   * context lifecycle ends it closes its associated store. All stored values
   * that are instances of {@link AutoCloseable} are
   * notified by invoking their {@code close()} methods.
   *
   * @param namespace the {@code Namespace} to get the store for; never {@code null}
   * @return the store in which to put and get objects for other invocations
   * working in the same namespace; never {@code null}
   */
  @Beta
  IStore getStore(IStore.Namespace namespace);
}
