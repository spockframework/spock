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

package org.spockframework.builder;

import org.spockframework.util.*;

import java.lang.reflect.*;

import groovy.lang.MetaProperty;

public class PropertySlot implements ISlot {
  private final Object owner;
  private final Type ownerType;
  private final MetaProperty property;

  PropertySlot(Object owner, Type ownerType, MetaProperty property) {
    this.owner = owner;
    this.ownerType = ownerType;
    this.property = property;
  }

  @Override
  public Type getType() {
    // could possibly add fast path here, but be careful (inner classes etc.)

    Method setter = MopUtil.setterFor(property);
    if (setter != null) return GenericTypeReflectorUtil.getParameterTypes(setter, ownerType)[0];

    Field field = MopUtil.fieldFor(property);
    if (field != null) return GenericTypeReflectorUtil.getExactFieldType(field, ownerType);

    throw new UnreachableCodeError();
  }

  @Override
  public void write(Object value) {
    property.setProperty(owner, value);
  }
}
