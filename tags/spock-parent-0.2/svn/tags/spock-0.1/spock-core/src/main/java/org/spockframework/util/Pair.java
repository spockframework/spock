/*
 * Copyright 2009 the original author or authors.
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

public class Pair<E1,E2> {
  private E1 first;
  private E2 second;
  
  public Pair(E1 first, E2 second) {
    this.first = first;
    this.second = second;
  }
  
  public E1 first() { return first; }
  
  public E2 second() { return second; }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((first == null) ? 0 : first.hashCode());
    result = prime * result + ((second == null) ? 0 : second.hashCode());
    return result;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (getClass() != obj.getClass()) return false;
    Pair<?,?> p = (Pair<?,?>)obj;
    return first == null ? p.first == null : first.equals(p.first)
      && second == null ? p.second == null : second.equals(p.second);
  }
  
  public static <E1,E2> Pair<E1,E2> create(E1 first, E2 second) {
    return new Pair<E1,E2>(first, second);
  }
}
