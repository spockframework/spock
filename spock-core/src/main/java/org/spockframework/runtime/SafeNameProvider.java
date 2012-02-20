/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.runtime;

import org.spockframework.runtime.model.NameProvider;
import org.spockframework.util.Nullable;

public class SafeNameProvider<T> implements NameProvider<T> {
  private final NameProvider<T> delegate;
  private final String defaultName;
  
  private SafeNameProvider(@Nullable NameProvider<T> delegate, String defaultName) {
    this.delegate = delegate;
    this.defaultName = defaultName;
  }
  
  public String getName(T t) {
    if (delegate == null) return defaultName;

    try {
      String name = delegate.getName(t);
      if (name != null) return name;
      return defaultName;
    } catch (Exception e) {
      return defaultName;
    }
  }
  
  public static <T> NameProvider<T> create(NameProvider<T> delegate, String defaultName) {
    return new SafeNameProvider<T>(delegate, defaultName);
  }
}
