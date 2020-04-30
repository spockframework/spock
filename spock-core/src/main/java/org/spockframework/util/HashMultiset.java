package org.spockframework.util;

import java.util.Collection;
import java.util.HashMap;

/**
 * HashMap based implementation of IMultiset. Iteration order is not guaranteed.
 *
 * @param <E> the element type of the multiset
 */
public class HashMultiset<E> extends AbstractMultiset<E> {
  public HashMultiset() {
    super(new HashMap<>());
  }

  public HashMultiset(Collection<? extends E> collection) {
    this();
    addAll(collection);
  }
}
