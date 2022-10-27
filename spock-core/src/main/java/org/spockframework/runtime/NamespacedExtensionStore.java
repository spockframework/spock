package org.spockframework.runtime;

import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;
import org.spockframework.runtime.extension.IStore;

import java.util.function.Function;

public class NamespacedExtensionStore implements IStore {
  private final NamespacedHierarchicalStore<Namespace> delegate;
  private final IStore.Namespace namespace;

  public NamespacedExtensionStore(NamespacedHierarchicalStore<Namespace> delegate, Namespace namespace) {
    this.delegate = delegate;
    this.namespace = namespace;
  }

  @Override
  public Object get(Object key) {
    return delegate.get(namespace, key);
  }

  @Override
  public <V> V get(Object key, Class<V> requiredType) {
    return delegate.get(namespace, key, requiredType);
  }

  @Override
  public <K, V> Object getOrComputeIfAbsent(K key, Function<K, V> defaultCreator) {
    return delegate.getOrComputeIfAbsent(namespace, key, defaultCreator);
  }

  @Override
  public <K, V> V getOrComputeIfAbsent(K key, Function<K, V> defaultCreator, Class<V> requiredType) {
    return delegate.getOrComputeIfAbsent(namespace, key, defaultCreator, requiredType);
  }

  @Override
  public Object put(Object key, Object value) {
      return delegate.put(namespace, key, value);
  }

  @Override
  public Object remove(Object key) {
    return delegate.remove(namespace, key);
  }

  @Override
  public <V> V remove(Object key, Class<V> requiredType) {
    return delegate.remove(namespace, key, requiredType);
  }
}
