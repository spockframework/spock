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

package org.spockframework.builder;

import java.lang.reflect.*;
import java.util.*;

import groovy.lang.MissingPropertyException;
import org.spockframework.gentyref.GenericTypeReflector;
import org.spockframework.util.MopUtil;
import org.spockframework.util.UnreachableCodeError;

import groovy.lang.MetaProperty;

public class CollectionElementSlot implements ISlot {
  private final String name;
  private final Object owner;
  private final Type ownerType;
  private final MetaProperty property;

  CollectionElementSlot(String name, Object owner, Type ownerType, MetaProperty property) {
    this.name = name;
    this.owner = owner;
    this.ownerType = ownerType;
    this.property = property;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    return GenericTypeReflector.getTypeParameter(getCollectionType(), Collection.class.getTypeParameters()[0]);
  }

  public boolean isReadable() {
    return false;
  }

  public boolean isWriteable() {
    return false;
  }

  public Object read() {
    throw new MissingPropertyException(name, owner.getClass());
  }

  public void write(Object value) {
    throw new MissingPropertyException(name, owner.getClass());
  }

  @SuppressWarnings("unchecked")
  public void configure(Object value) {
    Collection collection = (Collection) property.getProperty(owner);
    if (collection == null) {
      if (MopUtil.isWriteable(property)) {
        collection = createCollection(property.getType());
        property.setProperty(owner, collection);
      } else {
        throw new RuntimeException(String.format(
            "Cannot add element to collection property '%s' because it is neither initialized nor does it have a setter",
            name));
      }
    }
    collection.add(value);
  }

  private Type getCollectionType() {
    Method getter = MopUtil.getterFor(property);
    if (getter != null) return GenericTypeReflector.getExactReturnType(getter, ownerType);

    Field field = MopUtil.fieldFor(property);
    if (field != null) return GenericTypeReflector.getExactFieldType(field, ownerType);

    throw new UnreachableCodeError();
  }

  // IDEA: might be able to use coercer for this (e.g. if coercion were done only here)
  // would create empty collection by coercing, say, null to collection type
  // note that "null as List == null" in Groovy (coercer)
  private Collection createCollection(Class clazz) {
    if ((clazz.getModifiers() & Modifier.ABSTRACT) == 0) {
      return (Collection)BuilderHelper.createInstance(clazz);
    }

    if (List.class.isAssignableFrom(clazz)) return new ArrayList();
    if (Set.class.isAssignableFrom(clazz)) return new HashSet();

    throw new RuntimeException(String.format("Don't know how to create a collection of type '%s'", clazz.getName()));
  }
}

