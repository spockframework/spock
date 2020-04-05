package org.spockframework.util;

import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * LinkedHashMap based implementation of IMultiset. Iteration order follows
 * order of first insertion of an element.
 *
 * @param <E> the element type of the multiset
 */
public class LinkedHashMultiset<E> extends AbstractMultiset<E> {
  public LinkedHashMultiset() {
    super(new LinkedHashMap<>());
  }

  public LinkedHashMultiset(Collection<? extends E> collection) {
    this();
    addAll(collection);
  }
}
