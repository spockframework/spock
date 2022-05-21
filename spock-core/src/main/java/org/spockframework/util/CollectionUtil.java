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

import static java.util.Arrays.asList;

public abstract class CollectionUtil {
  public static <E, F> ArrayList<F> filterMap(Collection<E> collection, IFunction<? super E, ? extends F> function) {
    ArrayList<F> result = new ArrayList<>(collection.size());

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
    List<Object> result = new ArrayList<>();
    int length = Array.getLength(array);
    for (int i = 0; i < length; i++) {
      result.add(Array.get(array, i));
    }
    return result;
  }

  @Nullable
  public static <T> T getFirstElement(Iterable<T> iterable) {
    Iterator<T> iterator = iterable.iterator();
    Assert.that(iterator.hasNext());
    return iterator.next();
  }

  @Nullable
  public static <T> T getLastElement(List<T> list) {
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
    return () -> {
      final ListIterator<T> listIterator = list.listIterator(list.size());

      return new Iterator<T>() {
        @Override
        public boolean hasNext() {
          return listIterator.hasPrevious();
        }

        @Override
        public T next() {
          return listIterator.previous();
        }

        @Override
        public void remove() {
          listIterator.remove();
        }
      };
    };
  }

  public static <T> Set<T> asSet(T[] values) {
    return new HashSet<>(asList(values));
  }

  @SafeVarargs
  public static <E> List<E> listOf(E... elements) {
    List<E> result = new ArrayList<>(elements.length);
    result.addAll(asList(elements));
    return result;
  }

  public static <K, V> Map<K, V> mapOf(K key, V value) {
    Map<K, V> map = new LinkedHashMap<>();
    map.put(key, value);
    return map;
  }

  public static <K, V> Map<K, V> mapOf(K key, V value, K key2, V value2) {
    Map<K, V> map = new LinkedHashMap<>();
    map.put(key, value);
    map.put(key2, value2);
    return map;
  }

  public static <K, V> Map<K, V> mapOf(K key, V value, K key2, V value2, K key3, V value3) {
    Map<K, V> map = new LinkedHashMap<>();
    map.put(key, value);
    map.put(key2, value2);
    map.put(key3, value3);
    return map;
  }

  public static <K, V> Map<K, V> mapOf(K key, V value, K key2, V value2, K key3, V value3, K key4, V value4) {
    Map<K, V> map = new LinkedHashMap<>();
    map.put(key, value);
    map.put(key2, value2);
    map.put(key3, value3);
    map.put(key4, value4);
    return map;
  }

  public static <K, V> Map<K, V> mapOf(K key, V value, K key2, V value2, K key3, V value3, K key4, V value4, K key5, V value5) {
    Map<K, V> map = new LinkedHashMap<>();
    map.put(key, value);
    map.put(key2, value2);
    map.put(key3, value3);
    map.put(key4, value4);
    map.put(key5, value5);
    return map;
  }

  public static <K, V> Map<K, V> filterNullValues(Map<K, V> map) {
    map.entrySet().removeIf(next -> next.getValue() == null);
    return map;
  }

  @SafeVarargs
  public static <T> Iterable<T> concat(Iterable<? extends T>... iterables) {
    return concat(asList(iterables));
  }

  public static <T> Iterable<T> concat(final List<Iterable<? extends T>> iterables) {
    return () -> new Iterator<T>() {
      Iterator<? extends T> iter;
      int pos = 0;

      @Override
      public boolean hasNext() {
        while (pos < iterables.size()) {
          if (iter == null) iter = iterables.get(pos).iterator();
          if (iter.hasNext()) return true;
          iter = null;
          pos++;
        }
        return false;
      }

      @Override
      public T next() {
        while (pos < iterables.size()) {
          if (iter == null) iter = iterables.get(pos).iterator();
          if (iter.hasNext()) return iter.next();
          iter = null;
          pos++;
        }
        throw new NoSuchElementException();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("remove");
      }
    };
  }

  public static boolean containsAny(Iterable<?> iterable, Object... elements) {
    for (Object curr: iterable) {
      for (Object elem: elements) {
        if (ObjectUtil.equals(curr, elem)) return true;
      }
    }
    return false;
  }

  public static <T> int findIndexOf(Iterable<T> iterable, IFunction<? super T, Boolean> predicate) {
    int index = 0;
    for (T elem : iterable) {
      if (predicate.apply(elem)) return index;
      index++;
    }
    return -1;
  }
}
