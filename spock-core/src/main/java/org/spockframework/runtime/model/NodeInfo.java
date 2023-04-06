/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.model;

import org.spockframework.util.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * Base class for runtime information about an element in a Spock specification.
 *
 * @author Peter Niederwieser
 */
public abstract class NodeInfo<P extends NodeInfo, R extends AnnotatedElement> {
  private String name;
  private int line = -1;
  private P parent;
  private R reflection;
  private Object metadata;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getLine() {
    return line;
  }

  public void setLine(int line) {
    this.line = line;
  }

  public P getParent() {
    return parent;
  }

  public void setParent(P parent) {
    this.parent = parent;
  }

  public R getReflection() {
    return reflection;
  }

  public void setReflection(R reflection) {
    this.reflection = reflection;
  }

  @Nullable
  public Object getMetadata() {
    return metadata;
  }

  public void setMetadata(Object metadata) {
    this.metadata = metadata;
  }

  public Annotation[] getAnnotations() {
    return getReflection().getAnnotations();
  }

  public <T extends Annotation> T getAnnotation(Class<T> clazz) {
    return getReflection().getAnnotation(clazz);
  }

  public <T extends Annotation> T[] getAnnotationsByType(Class<T> clazz) {
    return getReflection().getAnnotationsByType(clazz);
  }

  public boolean isAnnotationPresent(Class<? extends Annotation> clazz) {
    return getReflection().isAnnotationPresent(clazz);
  }
}
