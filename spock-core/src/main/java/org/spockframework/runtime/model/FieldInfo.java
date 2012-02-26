/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.spockframework.util.GroovyRuntimeUtil;
import spock.lang.Shared;

/**
 * Runtime information about a field in a Spock specification.
 * 
 * @author Peter Niederwieser
 */
public class FieldInfo extends NodeInfo<SpecInfo, Field> {
  private int ordinal;

  public int getOrdinal() {
    return ordinal;
  }

  public void setOrdinal(int ordinal) {
    this.ordinal = ordinal;
  }

  public Class<?> getType() {
    return getReflection().getType();
  }

  public boolean isStatic() {
    return Modifier.isStatic(getReflection().getModifiers());
  }

  public boolean isShared() {
    return getReflection().isAnnotationPresent(Shared.class);
  }

  public <T extends Annotation> T getAnnotation(Class<T> clazz) {
    return getReflection().getAnnotation(clazz);
  }

  public boolean isAnnotationPresent(Class<? extends Annotation> clazz) {
    return getReflection().isAnnotationPresent(clazz);
  }

  public Object readValue(Object target) {
    return GroovyRuntimeUtil.getProperty(target, getReflection().getName());
  }

  public void writeValue(Object target, Object value) {
    GroovyRuntimeUtil.setProperty(target, getReflection().getName(), value);
  }
}
