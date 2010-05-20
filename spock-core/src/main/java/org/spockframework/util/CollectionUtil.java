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

import java.util.*;

public abstract class CollectionUtil {
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

  public static @Nullable <T> T getLastElement(List<T> list) {
    Assert.that(list.size() > 0);
    
    return list.get(list.size() - 1);
  }

  public static <T> void addLastElement(List<T> list, T element) {
    list.add(list.size(), element);
  }
}