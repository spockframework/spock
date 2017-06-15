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

package org.spockframework.runtime.condition;

import java.util.Collection;

public class DiffedCollectionRenderer implements IObjectRenderer<Collection> {
  @Override
  public String render(Collection collection) {
    LineBuilder builder = new LineBuilder();

    for (Object element : collection) {
      builder.addLine(element);
    }

    return builder.toString();
  }
}
