package org.spockframework.runtime;

import org.spockframework.runtime.extension.ISpockExecution;
import org.spockframework.runtime.extension.IStore;

public class SpockExecution implements ISpockExecution {
  private final StoreProvider storeProvider;

  public SpockExecution(StoreProvider storeProvider) {
    this.storeProvider = storeProvider;
  }

  @Override
  public IStore getStore(IStore.Namespace namespace) {
    return storeProvider.getStore(namespace);
  }
}
