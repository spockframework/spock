/*
 * Copyright 2009 the original author or authors.
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

/**
 * An immmutable 2-Tuple.
 *
 * @author Peter Niederwieser
 */
public class Tuple2<T0, T1> {
  private final T0 v0;
  private final T1 v1;

  private Tuple2(T0 v0, T1 v1) {
    this.v0 = v0;
    this.v1 = v1;
  }

  public T0 get0() {
    return v0;
  }

  public T1 get1() {
    return v1;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Tuple2 tuple2 = (Tuple2) o;

    if (v0 != null ? !v0.equals(tuple2.v0) : tuple2.v0 != null) return false;
    if (v1 != null ? !v1.equals(tuple2.v1) : tuple2.v1 != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = v0 != null ? v0.hashCode() : 0;
    result = 31 * result + (v1 != null ? v1.hashCode() : 0);
    return result;
  }

  public static <T0, T1> Tuple2<T0, T1> of(T0 v0, T1 v1) {
    return new Tuple2<T0, T1>(v0, v1);
  }
}
