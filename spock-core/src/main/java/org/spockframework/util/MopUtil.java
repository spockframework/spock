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

import java.lang.reflect.*;

import org.codehaus.groovy.reflection.CachedField;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.runtime.metaclass.*;

import groovy.lang.*;

public abstract class MopUtil {
  private static final Field ReflectionMetaMethod_method  = getDeclaredField(ReflectionMetaMethod.class, "method");
  private static final Field CachedField_field = getDeclaredField(CachedField.class, "field");

  private static Field getDeclaredField(Class clazz, String name) {
    try {
      Field result = clazz.getDeclaredField(name);
      result.setAccessible(true);
      return result;
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(String.format(
"Found an incompatibility between the Spock and Groovy versions you are using: field '%s.%s' does not exist.",
          clazz, name));
    }
  }

  public static Method methodFor(MetaMethod method) {
    if (method instanceof CachedMethod)
      return ((CachedMethod) method).getCachedMethod();

    if (method instanceof ClosureMetaMethod)
      return ((ClosureMetaMethod) method).getDoCall().getCachedMethod();

    if (method instanceof ReflectionMetaMethod) {
      try {
        return ((CachedMethod) ReflectionMetaMethod_method.get(method)).getCachedMethod();
      } catch (IllegalAccessException e) {
        throw new UnreachableCodeError(e);
      }
    }

    // could try stunts for MixinInstanceMetaMethod and TransformMetaMethod or
    // even apply some general heuristics (e.g. look for field named "method" or
    // "metaMethod" (or with type Method/MetaMethod) and invoke methodFor()
    // recursively), but we won't do this for now
    return null;
  }

  public static Member memberFor(MetaProperty property) {
    if (property instanceof CachedField)
      try {
        return (Member)CachedField_field.get(property);
      } catch (IllegalAccessException e) {
        throw new UnreachableCodeError(e);
      }

    if (property instanceof MetaBeanProperty) {
      MetaBeanProperty mbp = (MetaBeanProperty) property;
      if (mbp.getGetter() != null) return methodFor(mbp.getGetter());
      if (mbp.getSetter() != null) return methodFor(mbp.getSetter());
      if (mbp.getField() != null) return memberFor(mbp.getField());
      return null;
    }

    return null;
  }

  public static Field fieldFor(MetaProperty property) {
    if (property instanceof CachedField)
      try {
        return (Field)CachedField_field.get(property);
      } catch (IllegalAccessException e) {
        throw new UnreachableCodeError(e);
      }

    if (property instanceof MetaBeanProperty) {
      MetaBeanProperty mbp = (MetaBeanProperty) property;
      if (mbp.getField() != null) return fieldFor(mbp.getField());
      return null;
    }

    return null;
  }

  public static Method getterFor(MetaProperty property) {
    if (property instanceof MetaBeanProperty) {
      MetaBeanProperty mbp = (MetaBeanProperty) property;
      if (mbp.getGetter() != null) return methodFor(mbp.getGetter());
      return null;
    }

    return null;
  }

  public static Method setterFor(MetaProperty property) {
    if (property instanceof MetaBeanProperty) {
      MetaBeanProperty mbp = (MetaBeanProperty) property;
      if (mbp.getSetter() != null) return methodFor(mbp.getSetter());
      return null;
    }

    return null;
  }

  public static boolean isReadable(MetaProperty property) {
    if (property instanceof CachedField) return true;

    if (property instanceof MetaBeanProperty) {
       MetaBeanProperty mbp = (MetaBeanProperty) property;
      return mbp.getGetter() != null || mbp.getField() != null;
    }

    return false;
  }

  public static boolean isWriteable(MetaProperty property) {
    if (property instanceof CachedField) return true;

    if (property instanceof MetaBeanProperty) {
       MetaBeanProperty mbp = (MetaBeanProperty) property;
      return mbp.getSetter() != null || mbp.getField() != null;
    }

    return false;
  }
}
