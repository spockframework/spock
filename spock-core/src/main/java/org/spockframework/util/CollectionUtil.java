package org.spockframework.util;

import java.util.*;

public class CollectionUtil<T, E, T, F> {
  public static <E, F> ArrayList<F> filterMap(Collection<E> collection, IFunction<E, F> function) {
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

  public static
  @Nullable
  <T> T getFirstElement(List<T> list) {
    Assert.that(list.size() > 0);
    return list.get(0);
  }

  public static
  @Nullable
  <T> T getLastElement(List<T> list) {
    Assert.that(list.size() > 0);
    return list.get(list.size() - 1);
  }
}