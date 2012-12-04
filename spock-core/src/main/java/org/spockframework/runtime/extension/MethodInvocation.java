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

package org.spockframework.runtime.extension;

import java.util.Iterator;

import org.spockframework.runtime.model.*;
import org.spockframework.util.ReflectionUtil;

/**
 *
 * @author Peter Niederwieser
 */
public class MethodInvocation implements IMethodInvocation {
  private final FeatureInfo feature;
  private final IterationInfo iteration;
  private final Object sharedInstance;
  private final Object instance;
  private final Object target;
  private final MethodInfo method;
  private Object[] arguments;
  private final Iterator<IMethodInterceptor> interceptors;

  public MethodInvocation(FeatureInfo feature, IterationInfo iteration, Object sharedInstance,
      Object instance, Object target, MethodInfo method, Object[] arguments) {
    this.feature = feature;
    this.iteration = iteration;
    this.sharedInstance = sharedInstance;
    this.instance = instance;
    this.target = target;
    this.method = method;
    this.arguments = arguments;
    interceptors = method.getInterceptors().iterator();
  }

  public SpecInfo getSpec() {
    return method.getParent();
  }

  public FeatureInfo getFeature() {
    return feature;
  }

  public IterationInfo getIteration() {
    return iteration;
  }

  public Object getSharedInstance() {
    return sharedInstance;
  }

  public Object getInstance() {
    return instance;
  }

  public Object getTarget() {
    return target;
  }

  public MethodInfo getMethod() {
    return method;
  }

  public Object[] getArguments() {
    return arguments;
  }

  public void setArguments(Object[] arguments) {
    this.arguments = arguments;
  }

  public void proceed() throws Throwable {
    if (interceptors.hasNext())
      interceptors.next().intercept(this);
    else invokeTargetMethod();
  }

  protected void invokeTargetMethod() throws Throwable {
    ReflectionUtil.invokeMethod(target, method.getReflection(), arguments);
  }
}
