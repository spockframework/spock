package org.spockframework.runtime.model.parallel;

import java.util.Objects;

/**
 * An exclusive resource identified by a key with a lock mode that is used to
 * synchronize access to shared resources when executing nodes in parallel.
 *
 * @since 2.0
 */
public class ExclusiveResource {

  private final String key;
  private final ResourceAccessMode mode;
  private int hash;

  public ExclusiveResource(String key, ResourceAccessMode mode) {
    this.key = Objects.requireNonNull(key, "key must not be null");
    this.mode = Objects.requireNonNull(mode, "mode must be non null");
  }

  public String getKey() {
    return key;
  }

  public ResourceAccessMode getMode() {
    return mode;
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExclusiveResource that = (ExclusiveResource) o;
    return Objects.equals(key, that.key) && mode == that.mode;
  }

  @Override
  public int hashCode() {
    int h = hash;
    if (h == 0) {
      h = hash = Objects.hash(key, mode);
    }
    return h;
  }

}
