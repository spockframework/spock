package org.spockframework.runtime;

import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStoreException;
import org.spockframework.runtime.extension.IStore;

import java.util.function.Function;
import java.util.function.Supplier;

public class NamespacedExtensionStore implements IStore {
  private final NamespacedHierarchicalStore<Namespace> delegate;
  private final Supplier<NamespacedExtensionStore> parentProvider;
  private final IStore.Namespace namespace;

  public NamespacedExtensionStore(NamespacedHierarchicalStore<Namespace> delegate, Supplier<NamespacedExtensionStore> parentProvider, Namespace namespace) {
    this.delegate = delegate;
    this.parentProvider = parentProvider;
    this.namespace = namespace;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V get(Object key) {
    return (V) execute(() -> delegate.get(namespace, key));
  }

  @Override
  public <V> V get(Object key, Class<V> requiredType) {
    return execute(() -> delegate.get(namespace, key, requiredType));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <K, V> V getOrComputeIfAbsent(K key, Function<K, V> defaultCreator) {
    return (V) execute(() -> delegate.getOrComputeIfAbsent(namespace, key, defaultCreator));
  }

  @Override
  public <K, V> V getOrComputeIfAbsent(K key, Function<K, V> defaultCreator, Class<V> requiredType) {
    return execute(() -> delegate.getOrComputeIfAbsent(namespace, key, defaultCreator, requiredType));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V put(Object key, Object value) {
    return (V) execute(() -> delegate.put(namespace, key, value));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V remove(Object key) {
    return (V) execute(() -> delegate.remove(namespace, key));
  }

  @Override
  public <V> V remove(Object key, Class<V> requiredType) {
    return execute(() -> delegate.remove(namespace, key, requiredType));
  }

  @Override
  public IStore getParentStore() {
    return parentProvider.get();
  }

  private <V> V execute(Supplier<V> exec) {
    try {
      return exec.get();
    } catch (NamespacedHierarchicalStoreException | ClassCastException e) {
      throw new StoreException(e.getMessage(), e);
    }
  }
}
