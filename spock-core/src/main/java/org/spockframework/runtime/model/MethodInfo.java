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

import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.util.*;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Runtime information about a method in a Spock specification.
 *
 * @author Peter Niederwieser
 */
public class MethodInfo extends NodeInfo<SpecInfo, Method> implements IExcludable, IInterceptable {
  private MethodKind kind;
  private FeatureInfo feature;
  private IterationInfo iteration;
  private boolean excluded = false;
  private final List<IMethodInterceptor> interceptors = new ArrayList<>();

  public MethodInfo() {
  }

  public MethodInfo(MethodInfo other) {
    this.kind = other.kind;
    this.feature = other.feature;
    this.iteration = other.iteration;
    this.excluded = other.excluded;
    this.setName(other.getName());
    this.setLine(other.getLine());
    this.setParent(other.getParent());
    this.setReflection(other.getReflection());
    this.setMetadata(other.getMetadata());
    this.setDescription(other.getDescription());
    this.interceptors.addAll(other.interceptors);
  }

  public MethodKind getKind() {
    return kind;
  }

  public void setKind(MethodKind kind) {
    this.kind = kind;
  }

  @Nullable
  public FeatureInfo getFeature() {
    return feature;
  }

  public void setFeature(FeatureInfo feature) {
    this.feature = feature;
  }

  @Nullable
  public IterationInfo getIteration() {
    return iteration;
  }

  public void setIteration(IterationInfo iteration) {
    this.iteration = iteration;
  }

  @Override
  public boolean isExcluded() {
    return excluded;
  }

  @Override
  public void setExcluded(boolean excluded) {
    this.excluded = excluded;
  }

  @Override
  public List<IMethodInterceptor> getInterceptors() {
    return interceptors;
  }

  @Override
  public void addInterceptor(IMethodInterceptor interceptor) {
    interceptors.add(interceptor);
  }

  public boolean hasBytecodeName(String name) {
    return getReflection().getName().equals(name);
  }

  /**
   * Invokes this method on the specified target and with the specified arguments.
   * Does <em>not</em> handle interceptors.
   *
   * @param target the target of the method call
   * @param arguments the arguments for the method call
   * @return the return value of the method call
   */
  public Object invoke(Object target, Object... arguments) throws Throwable {
    return ReflectionUtil.invokeMethod(target, getReflection(), arguments);
  }
}
