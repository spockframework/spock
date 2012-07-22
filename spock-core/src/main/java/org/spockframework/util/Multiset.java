/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.util;

import java.util.*;

/**
 * Set-like data structure where each element has a cardinality. The cardinality
 * can be queried with count(). add() and remove() operations increase and decrease
 * the cardinality, respectively. Operations try to follow the java.util.Collection
 * contract.
 *
 * Note that iterator() and toArray() return each element just once, irrespective of
 * its cardinality. Cardinality-aware iteration is possible with entrySet().
 * Iteration order is not guaranteed.
 *
 * @param <E> the element type of the multiset
 */
@NotThreadSafe
public class Multiset<E> implements Collection<E> {
  private final Map<E, Integer> elements = new HashMap<E, Integer>();

  public Multiset() {}

  public Multiset(Collection<? extends E> collection) {
    addAll(collection);
  }

  public int size() {
    return elements.size();
  }

  public boolean isEmpty() {
    return elements.isEmpty();
  }

  public boolean contains(Object element) {
    return elements.containsKey(element);
  }

  public Iterator<E> iterator() {
    return elements.keySet().iterator();
  }

  public Object[] toArray() {
    return elements.keySet().toArray();
  }

  public <T> T[] toArray(T[] array) {
    return elements.keySet().toArray(array);
  }

  public boolean add(E element) {
    Integer count = elements.get(element);
    if (count == null) {
      elements.put(element, 1);
    } else {
      elements.put(element, count + 1);
    }
    return true;
  }

  public boolean remove(Object element) {
    Integer count = elements.get(element);
    if (count == null) {
      return false;
    }
    if (count == 0) {
      throw new InternalSpockError("MultiSet element count is zero");
    }
    if (count == 1) {
      elements.remove(element);
    } else {
      elements.put((E) element, count - 1);
    }
    return true;
  }

  public boolean containsAll(Collection<?> collection) {
    for (Object element : collection) {
      if (!elements.containsKey(element)) return false;
    }
    return true;
  }

  public boolean addAll(Collection<? extends E> collection) {
    if (collection.isEmpty()) return false;

    for (E element : collection) add(element);
    return true;
  }

  public boolean retainAll(Collection<?> collection) {
    boolean changed = false;
    Iterator<E> iterator = elements.keySet().iterator();
    while (iterator.hasNext()) {
      E next = iterator.next();
      if (!collection.contains(next)) {
        iterator.remove();
        changed = true;
      }
    }
    return changed;
  }

  public boolean removeAll(Collection<?> collection) {
    boolean changed = false;
    for (Object element : collection) {
      changed |= (elements.remove(element) != null);
    }
    return changed;
  }

  public void clear() {
    elements.clear();
  }

  public int count(E element) {
    Integer count = elements.get(element);
    return count == null ? 0 : count;
  }

  public Set<Map.Entry<E, Integer>> entrySet() {
    return elements.entrySet();
  }
}
