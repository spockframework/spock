/*
 * Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.runtime.extension;

import org.spockframework.runtime.StoreProvider;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.IterationInfo;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.Checks;
import org.spockframework.util.CollectionUtil;

import java.util.Iterator;
import java.util.List;

import static org.spockframework.runtime.model.MethodInfo.MISSING_ARGUMENT;

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

  private final StoreProvider storeProvider;

  public MethodInvocation(FeatureInfo feature, IterationInfo iteration, StoreProvider storeProvider, Object sharedInstance,
                          Object instance, Object target, MethodInfo method, List<IMethodInterceptor> scopedInterceptors, Object[] arguments) {
    this.feature = feature;
    this.iteration = iteration;
    this.storeProvider = storeProvider;
    this.sharedInstance = sharedInstance;
    this.instance = instance;
    this.target = target;
    this.method = method;
    this.arguments = arguments;
    interceptors = scopedInterceptors.isEmpty()
      ? method.getInterceptors().iterator()
      : CollectionUtil.concat(scopedInterceptors, method.getInterceptors()).iterator();
  }

  @Override
  public SpecInfo getSpec() {
    return method.getParent();
  }

  @Override
  public FeatureInfo getFeature() {
    return feature;
  }

  @Override
  public IterationInfo getIteration() {
    return iteration;
  }

  @Override
  public Object getSharedInstance() {
    return sharedInstance;
  }

  @Override
  public Object getInstance() {
    return instance;
  }

  @Override
  public Object getTarget() {
    return target;
  }

  @Override
  public MethodInfo getMethod() {
    return method;
  }

  @Override
  public Object[] getArguments() {
    return arguments;
  }

  @Override
  public IStore getStore(IStore.Namespace namespace) {
    return storeProvider.getStore(namespace);
  }

  @Override
  public void setArguments(Object[] arguments) {
    Checks.checkArgument(arguments.length != this.arguments.length, () -> "length of arguments array must not change from " + this.arguments.length + " to " + arguments.length);
    this.arguments = arguments;
  }

  @Override
  public void resolveArgument(int index, Object value) {
    Checks.checkState(arguments[index] == MISSING_ARGUMENT, () -> "Parameter " + method.getParameters().get(index).getName() + " is already set");
    arguments[index] = value;
  }

  @Override
  public void proceed() throws Throwable {
    if (interceptors.hasNext())
      interceptors.next().intercept(this);
    else method.invoke(target, arguments);
  }
}
