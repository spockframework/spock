package org.spockframework.runtime;

import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;
import org.spockframework.runtime.extension.IStore;

public class StoreProvider implements AutoCloseable {
  private static final NamespacedHierarchicalStore.CloseAction<IStore.Namespace> CLOSE_ACTION = (IStore.Namespace namespace, Object key, Object value) -> {
    if (value instanceof AutoCloseable) {
      ((AutoCloseable) value).close();
    }
  };
  private final NamespacedHierarchicalStore<IStore.Namespace> backend;

  private StoreProvider(NamespacedHierarchicalStore<IStore.Namespace> backend) {
    this.backend = backend;
  }

  public static StoreProvider createRootStore() {
    return new StoreProvider(newBackendStore(null));
  }

  public StoreProvider createChildStore() {
    return new StoreProvider(newBackendStore(backend));
  }

  public NamespacedExtensionStore getStore(IStore.Namespace namespace) {
    return new NamespacedExtensionStore(backend, namespace);
  }

  private static NamespacedHierarchicalStore<IStore.Namespace> newBackendStore(NamespacedHierarchicalStore<IStore.Namespace> backend) {
    return new NamespacedHierarchicalStore<>(backend, CLOSE_ACTION);
  }

  @Override
  public void close() throws Exception {
    backend.close();
  }
}
