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

import org.spockframework.runtime.GroovyRuntimeUtil;

import java.util.*;

public class DiffedMapRenderer implements IObjectRenderer<Map> {
  private final boolean sort;

  public DiffedMapRenderer(boolean sort) {
    this.sort = sort;
  }

  @Override
  @SuppressWarnings("unchecked")
  public String render(Map map) {
    LineBuilder builder = new LineBuilder();

    for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
      String line = GroovyRuntimeUtil.toString(entry.getKey())
          + ": " + GroovyRuntimeUtil.toString(entry.getValue());
      builder.addLine(line);
    }

    if (sort) builder.sort();
    return builder.toString();
  }
}
