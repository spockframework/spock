package org.spockframework.runtime;

import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStoreException;
import org.spockframework.runtime.extension.IStore;

import java.util.function.Function;
import java.util.function.Supplier;

public class NamespacedExtensionStore implements IStore {
  private final NamespacedHierarchicalStore<Namespace> delegate;
  private final IStore.Namespace namespace;

  public NamespacedExtensionStore(NamespacedHierarchicalStore<Namespace> delegate, Namespace namespace) {
    this.delegate = delegate;
    this.namespace = namespace;
  }

  @Override
  public Object get(Object key) {
    return execute(() -> delegate.get(namespace, key));
  }

  @Override
  public <V> V get(Object key, Class<V> requiredType) {
    return execute(() -> delegate.get(namespace, key, requiredType));
  }

  @Override
  public <K, V> Object getOrComputeIfAbsent(K key, Function<K, V> defaultCreator) {
    return execute(() -> delegate.getOrComputeIfAbsent(namespace, key, defaultCreator));
  }

  @Override
  public <K, V> V getOrComputeIfAbsent(K key, Function<K, V> defaultCreator, Class<V> requiredType) {
    return execute(() -> delegate.getOrComputeIfAbsent(namespace, key, defaultCreator, requiredType));
  }

  @Override
  public Object put(Object key, Object value) {
    return execute(() -> delegate.put(namespace, key, value));
  }

  @Override
  public Object remove(Object key) {
    return execute(() -> delegate.remove(namespace, key));
  }

  @Override
  public <V> V remove(Object key, Class<V> requiredType) {
    return execute(() -> delegate.remove(namespace, key, requiredType));
  }

  private <V> V execute(Supplier<V> exec) {
    try {
      return exec.get();
    } catch (NamespacedHierarchicalStoreException | ClassCastException e) {
      throw new StoreException(e.getMessage(), e);
    }
  }
}
