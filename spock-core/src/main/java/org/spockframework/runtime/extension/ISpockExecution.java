package org.spockframework.runtime.extension;

import org.spockframework.util.Beta;

public interface ISpockExecution {
  /**
   * Get the {@link IStore} for the supplied {@linkplain IStore.Namespace namespace}.
   *
   * <p>A store is bound to its context lifecycle. When a
   * context lifecycle ends it closes its associated store. All stored values
   * that are instances of {@link AutoCloseable} are
   * notified by invoking their {@code close()} methods.
   *
   * @param namespace the {@code Namespace} to get the store for; never {@code null}
   * @return the store in which to put and get objects for other invocations
   * working in the same namespace; never {@code null}
   * @since 2.4
   */
  @Beta
  IStore getStore(IStore.Namespace namespace);
}
