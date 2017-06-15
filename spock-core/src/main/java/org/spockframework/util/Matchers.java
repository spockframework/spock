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

public class Matchers {
  @SafeVarargs
  public static <T> IMatcher<T> and(final IMatcher<? super T>... matchers) {
    return new IMatcher<T>() {
      @Override
      public boolean matches(T item) {
        for (IMatcher<? super T> matcher : matchers)
          if (!matcher.matches(item)) return false;

        return true;
      }
    };
  }

  @SafeVarargs
  public static <T> IMatcher<T> or(final IMatcher<? super T>... matchers) {
    return new IMatcher<T>() {
      @Override
      public boolean matches(T item) {
        for (IMatcher<? super T> matcher : matchers)
          if (matcher.matches(item)) return true;

        return false;
      }
    };
  }

  public static <T> IMatcher<T> not(final IMatcher<T> matcher) {
    return new IMatcher<T>() {
      @Override
      public boolean matches(T item) {
        return !matcher.matches(item);
      }
    };
  }
}
