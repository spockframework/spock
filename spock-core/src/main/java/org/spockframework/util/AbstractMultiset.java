package org.spockframework.util;

import java.util.*;

@SuppressWarnings("SuspiciousMethodCalls")
public abstract class AbstractMultiset<E> implements IMultiset<E> {
  private final Map<E, Integer> elements;

  public AbstractMultiset(Map<E, Integer> elements) {
    this.elements = elements;
  }

  @Override
  public int size() {
    return elements.size();
  }

  @Override
  public boolean isEmpty() {
    return elements.isEmpty();
  }

  @Override
  public boolean contains(Object element) {
    return elements.containsKey(element);
  }

  @Override
  public Iterator<E> iterator() {
    return elements.keySet().iterator();
  }

  @Override
  public Object[] toArray() {
    return elements.keySet().toArray();
  }

  @Override
  public <T> T[] toArray(T[] array) {
    return elements.keySet().toArray(array);
  }

  @Override
  public boolean add(E element) {
    elements.merge(element, 1, Integer::sum);
    return true;
  }

  @Override
  @SuppressWarnings("unchecked")
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

  @Override
  public boolean containsAll(Collection<?> collection) {
    for (Object element : collection) {
      if (!elements.containsKey(element)) return false;
    }
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends E> collection) {
    if (collection.isEmpty()) return false;

    for (E element : collection) add(element);
    return true;
  }

  @Override
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

  @Override
  public boolean removeAll(Collection<?> collection) {
    boolean changed = false;
    for (Object element : collection) {
      changed |= (elements.remove(element) != null);
    }
    return changed;
  }

  @Override
  public void clear() {
    elements.clear();
  }

  @Override
  public int count(E element) {
    Integer count = elements.get(element);
    return count == null ? 0 : count;
  }

  @Override
  public Set<Map.Entry<E, Integer>> entrySet() {
    return elements.entrySet();
  }
}
