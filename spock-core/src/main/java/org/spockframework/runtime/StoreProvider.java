package org.spockframework.runtime;

import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;
import org.spockframework.runtime.extension.IStore;
import org.spockframework.util.Nullable;

import java.util.Objects;

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

  public static StoreProvider createRootStore() {
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
