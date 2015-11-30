/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.util;

import java.lang.reflect.Array;
import java.util.*;

public abstract class CollectionUtil {
  public static <E, F> ArrayList<F> filterMap(Collection<E> collection, IFunction<? super E, ? extends F> function) {
    ArrayList<F> result = new ArrayList<F>(collection.size());

    for (E elem : collection) {
      F resultElem = function.apply(elem);
      if (resultElem != null)
        result.add(resultElem);
    }

    return result;
  }

  /**
   * (Partial) replacement for Arrays.copyOfRange, which is only available in JDK6.
   */
  public static Object[] copyArray(Object[] array, int from, int to) {
    Object[] result = new Object[to - from];
    System.arraycopy(array, from, result, 0, to - from);
    return result;
  }

  public static List<Object> arrayToList(Object array) {
    List<Object> result = new ArrayList<Object>();
    int length = Array.getLength(array);
    for (int i = 0; i < length; i++) {
      result.add(Array.get(array, i));
    }
    return result;
  }

  public static @Nullable <T> T getFirstElement(Iterable<T> iterable) {
    Iterator<T> iterator = iterable.iterator();
    Assert.that(iterator.hasNext());
    return iterator.next();
  }

  public static @Nullable <T> T getLastElement(List<T> list) {
    Assert.that(list.size() > 0);

    return list.get(list.size() - 1);
  }

  public static <T> void setLastElement(List<T> list, T elem) {
    Assert.that(list.size() > 0);

    list.set(list.size() - 1, elem);
  }

  public static <T> void addLastElement(List<T> list, T element) {
    list.add(list.size(), element);
  }

  public static <T> Iterable<T> reverse(final List<T> list) {
    return new Iterable<T>() {
      public Iterator<T> iterator() {
        final ListIterator<T> listIterator = list.listIterator(list.size());

        return new Iterator<T>() {
          public boolean hasNext() {
            return listIterator.hasPrevious();
          }

          public T next() {
            return listIterator.previous();
          }

          public void remove() {
            listIterator.remove();
          }
        };
      }
    };
  }

  public static <T> Set<T> asSet(T[] values) {
    return new HashSet<T>(Arrays.asList(values));
  }

  public static <E> List<E> listOf(E... elements) {
    List<E> result = new ArrayList<E>(elements.length);
    result.addAll(Arrays.asList(elements));
    return result;
  }

  public static <K, V> Map<K, V> mapOf(K key, V value) {
    Map<K, V> map = new LinkedHashMap<K, V>();
    map.put(key, value);
    return map;
  }

  public static <K, V> Map<K, V> mapOf(K key, V value, K key2, V value2) {
    Map<K, V> map = new LinkedHashMap<K, V>();
    map.put(key, value);
    map.put(key2, value2);
    return map;
  }

  public static <K, V> Map<K, V> mapOf(K key, V value, K key2, V value2, K key3, V value3) {
    Map<K, V> map = new LinkedHashMap<K, V>();
    map.put(key, value);
    map.put(key2, value2);
    map.put(key3, value3);
    return map;
  }

  public static <K, V> Map<K, V> mapOf(K key, V value, K key2, V value2, K key3, V value3, K key4, V value4) {
    Map<K, V> map = new LinkedHashMap<K, V>();
    map.put(key, value);
    map.put(key2, value2);
    map.put(key3, value3);
    map.put(key4, value4);
    return map;
  }

  public static <K, V> Map<K, V> mapOf(K key, V value, K key2, V value2, K key3, V value3, K key4, V value4, K key5, V value5) {
    Map<K, V> map = new LinkedHashMap<K, V>();
    map.put(key, value);
    map.put(key2, value2);
    map.put(key3, value3);
    map.put(key4, value4);
    map.put(key5, value5);
    return map;
  }

  public static Map filterNullValues(Map map) {
    Iterator<Map.Entry> iterator = map.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry next = iterator.next();
      if (next.getValue() == null) {
        iterator.remove();
      }
    }
    return map;
  }

  public static Map putAll(Map original, Map... others) {
    for (Map other : others) {
      original.putAll(other);
    }
    return original;
  }

  public static <T> Iterable<T> concat(Iterable<? extends T>... iterables) {
    return concat(Arrays.asList(iterables));
  }

  public static <T> Iterable<T> concat(final List<Iterable<? extends T>> iterables) {
    return new Iterable<T>() {
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          Iterator<? extends T> iter;
          int pos = 0;

          public boolean hasNext() {
            while (pos < iterables.size()) {
              if (iter == null) iter = iterables.get(pos).iterator();
              if (iter.hasNext()) return true;
              iter = null;
              pos++;
            }
            return false;
          }

          public T next() {
            while (pos < iterables.size()) {
              if (iter == null) iter = iterables.get(pos).iterator();
              if (iter.hasNext()) return iter.next();
              iter = null;
              pos++;
            }
            throw new NoSuchElementException();
          }

          public void remove() {
            throw new UnsupportedOperationException("remove");
          }
        };
      }
    };
  }

  public static boolean containsAny(Iterable<?> iterable, Object... elements) {
    for (Object curr : iterable)
      for (Object elem : elements)
        if (ObjectUtil.equals(curr, elem))
          return true;
    return false;
  }

  public static <T> int findIndexOf(Iterable<T> iterable, IFunction<? super T, Boolean> predicate) {
    int index = 0;
    for (T elem : iterable) {
      if (predicate.apply(elem))
        return index;
      index++;
    }
    return -1;
  }

  public static <T> T[] toArray(Collection<T> elems, Class<T> type) {
    T[] results = (T[]) Array.newInstance(type, elems.size());
    return elems.toArray(results);
  }
}
