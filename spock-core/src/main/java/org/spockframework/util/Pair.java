/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.util;

import java.util.Objects;

/**
 * An immutable pair of elements.
 *
 * @author Peter Niederwieser
 */
public class Pair<E1, E2> {
  private final E1 first;
  private final E2 second;

  private Pair(E1 first, E2 second) {
    this.first = first;
    this.second = second;
  }

  public E1 first() {
    return first;
  }

  public E2 second() {
    return second;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other == null || getClass() != other.getClass()) return false;

    Pair tuple2 = (Pair) other;

    return Objects.equals(first, tuple2.first) && Objects.equals(second, tuple2.second);
  }

  @Override
  public int hashCode() {
    int result = first != null ? first.hashCode() : 0;
    result = 31 * result + (second != null ? second.hashCode() : 0);
    return result;
  }

  public static <E1, E2> Pair<E1, E2> of(E1 first, E2 second) {
    return new Pair<>(first, second);
  }
}
