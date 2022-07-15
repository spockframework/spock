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

import java.lang.reflect.Field;

public class DiffedObjectAsBeanRenderer implements IObjectRenderer<Object> {
  @Override
  public String render(Object object) {
    LineBuilder builder = new LineBuilder();

    for (Class<?> clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
      for (Field field : clazz.getDeclaredFields()) {
        if (field.isSynthetic()) continue;
        boolean accessible = field.isAccessible();
        try {
          field.setAccessible(true);
          String value = GroovyRuntimeUtil.toString(field.get(object));
          builder.addLine(field.getName() + ": " + value);
        } catch (IllegalAccessException e) {
          builder.addLine(field.getName() + ": [threw an exception on access: "+e.getMessage()+"]");
        } finally {
          field.setAccessible(accessible);
        }
      }
    }

    builder.sort();
    return builder.toString();

  }
}
