/*
 * Copyright 2010 the original author or authors.
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

import java.util.*;

public class Filter<T> {
  private final IMatcher<T> matcher;

  private Filter(IMatcher<T> matcher) {
    this.matcher = matcher;
  }

  public List<T> filter(List<? extends T> items) {
    List<T> result = new ArrayList<>();
    for (T item : items)
      if (matcher.matches(item))
        result.add(item);

    return result;
  }

  public void filterInPlace(List<? extends T> items) {
    items.removeIf(t -> !matcher.matches(t));
  }

  public static <T> Filter<T> create(IMatcher<T> matcher) {
    return new Filter<>(matcher);
  }
}
