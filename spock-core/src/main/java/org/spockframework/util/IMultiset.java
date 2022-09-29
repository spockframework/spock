/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
 * the cardinality, respectively. Tries to follow the java.util.Collection
 * contract. Implements all optional collection operations.
 *
 * Note that iterator() and toArray() return each element just once, irrespective of
 * its cardinality. Cardinality-aware iteration is possible with entrySet().
 *
 * @param <E> the element type of the multiset
 */
@NotThreadSafe
public interface IMultiset<E> extends Collection<E> {
  int count(E element);

  Set<Map.Entry<E, Integer>> entrySet();
}
