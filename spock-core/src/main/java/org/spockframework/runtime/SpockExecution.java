package org.spockframework.runtime;

import org.spockframework.runtime.extension.ISpockExecution;
import org.spockframework.runtime.extension.IStore;

import java.util.Objects;

public class SpockExecution implements ISpockExecution {
  private final StoreProvider storeProvider;

  public SpockExecution(StoreProvider storeProvider) {
    this.storeProvider = Objects.requireNonNull(storeProvider);
  }

  @Override
  public IStore getStore(IStore.Namespace namespace) {
    return storeProvider.getStore(namespace);
  }
}
