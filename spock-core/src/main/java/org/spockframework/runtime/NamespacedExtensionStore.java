package org.spockframework.runtime;

import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStoreException;
import org.spockframework.runtime.extension.IStore;
import org.spockframework.util.ObjectUtil;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class NamespacedExtensionStore implements IStore {
  private final NamespacedHierarchicalStore<Namespace> delegate;
  private final Supplier<NamespacedExtensionStore> parentProvider;
  private final IStore.Namespace namespace;

  NamespacedExtensionStore(NamespacedHierarchicalStore<Namespace> delegate, Supplier<NamespacedExtensionStore> parentProvider, Namespace namespace) {
    this.delegate = Objects.requireNonNull(delegate);
    this.parentProvider =  Objects.requireNonNull(parentProvider);
    this.namespace =  Objects.requireNonNull(namespace);
  }

  @Override
  public <V> V get(Object key) {
    return ObjectUtil.uncheckedCast(execute(() -> delegate.get(namespace, key)));
  }

  @Override
  public <V> V get(Object key, Class<V> requiredType) {
    return execute(() -> delegate.get(namespace, key, requiredType));
  }

  @Override
  public <K, V> V getOrComputeIfAbsent(K key, Function<K, V> defaultCreator) {
    return ObjectUtil.uncheckedCast(execute(() -> delegate.getOrComputeIfAbsent(namespace, key, defaultCreator)));
  }

  @Override
  public <K, V> V getOrComputeIfAbsent(K key, Function<K, V> defaultCreator, Class<V> requiredType) {
    return execute(() -> delegate.getOrComputeIfAbsent(namespace, key, defaultCreator, requiredType));
  }

  @Override
  public <V> V put(Object key, Object value) {
    return ObjectUtil.uncheckedCast(execute(() -> delegate.put(namespace, key, value)));
  }

  @Override
  public <V> V remove(Object key) {
    return ObjectUtil.uncheckedCast(execute(() -> delegate.remove(namespace, key)));
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
