/*
 * Copyright 2023 the original author or authors.
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
 *
 */

package org.spockframework.runtime;

import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;
import org.spockframework.runtime.extension.IStore;
import org.spockframework.util.Nullable;

import java.util.Objects;

/**
 * @author Leonard Brünings
 * @since 2.4
 */
public class StoreProvider implements AutoCloseable {
  private static final NamespacedHierarchicalStore.CloseAction<IStore.Namespace> CLOSE_ACTION = (IStore.Namespace namespace, Object key, Object value) -> {
    if (value instanceof AutoCloseable) {
      ((AutoCloseable) value).close();
    }
  };

  private final NamespacedHierarchicalStore<IStore.Namespace> backend;
  @Nullable
  private final StoreProvider parent;

  private StoreProvider(NamespacedHierarchicalStore<IStore.Namespace> backend, StoreProvider parent) {
    this.backend = Objects.requireNonNull(backend);
    this.parent = parent;
  }

  public static StoreProvider createRootStoreProvider() {
    return new StoreProvider(newBackendStore(null), null);
  }

  public StoreProvider createChildStoreProvider() {
    return new StoreProvider(newBackendStore(backend), this);
  }

  public NamespacedExtensionStore getStore(IStore.Namespace namespace) {
    return new NamespacedExtensionStore(backend,
      () -> parent == null ? null : parent.getStore(namespace),
      namespace);
  }

  private static NamespacedHierarchicalStore<IStore.Namespace> newBackendStore(NamespacedHierarchicalStore<IStore.Namespace> backend) {
    return new NamespacedHierarchicalStore<>(backend, CLOSE_ACTION);
  }

  @Override
  public void close() throws Exception {
    backend.close();
  }
}
