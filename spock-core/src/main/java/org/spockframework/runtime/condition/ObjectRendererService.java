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

package org.spockframework.runtime.condition;

import org.spockframework.util.InternalSpockError;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

public class ObjectRendererService implements IObjectRendererService {
  private final HashMap<Class<?>, IObjectRenderer<?>> renderers = new HashMap<>();

  @Override
  public <T> void addRenderer(Class<T> type, IObjectRenderer<? super T> renderer) {
    renderers.put(type, renderer);
  }

  @Override
  @SuppressWarnings("unchecked")
  public String render(Object object) {
    if (object == null) return "null\n";

    // explicit parameterization required although IDEA thinks it's redundant
    Set<Class<?>> types = singleton(object.getClass());

    while (!types.isEmpty()) {
      for (Class<?> type : types) {
        IObjectRenderer renderer = renderers.get(type);
        if (renderer != null) return renderer.render(object);
      }
      types = getParents(types);
    }

    // only fall back to renderer for type Object once all other options have been exhausted
    IObjectRenderer renderer = renderers.get(Object.class);
    if (renderer != null) return renderer.render(object);

    throw new InternalSpockError("no renderer for type Object found");
  }

  private Set<Class<?>> getParents(Set<Class<?>> types) {
    Set<Class<?>> parents = new HashSet<>();

    for (Class<?> type : types) {
      Class<?> superclass = type.getSuperclass();
      if (superclass != null && superclass != Object.class) {
        parents.add(superclass);
      }
      parents.addAll(asList(type.getInterfaces()));
    }

    return parents;
  }
}
