package org.spockframework.util;

import java.util.*;

@SuppressWarnings("SuspiciousMethodCalls")
public abstract class AbstractMultiset<E> implements IMultiset<E> {
  private final Map<E, Integer> elements;
  
  public AbstractMultiset(Map<E, Integer> elements) {
    this.elements = elements;
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

  public <E> E[] toArray(E[] array) {
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
