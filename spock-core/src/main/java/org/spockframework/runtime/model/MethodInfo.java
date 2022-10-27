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

import org.jetbrains.annotations.NotNull;
import org.spockframework.runtime.SpockExecutionException;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.util.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;

import static org.spockframework.runtime.model.MethodKind.FEATURE;

/**
 * Runtime information about a method in a Spock specification.
 *
 * @author Peter Niederwieser
 */
public class MethodInfo extends NodeInfo<SpecInfo, Method> implements IExcludable, IInterceptable {
  public static final Object MISSING_ARGUMENT = new Object();

  private MethodKind kind;
  private FeatureInfo feature;
  private IterationInfo iteration;
  private boolean excluded = false;
  private final List<IMethodInterceptor> interceptors = new ArrayList<>();
  private Invoker invoker;
  private List<ParameterInfo> parameters;

  public MethodInfo() {
    invoker = (Object target, Object... arguments) ->
      ReflectionUtil.invokeMethod(target, getReflection(), arguments);
  }

  public MethodInfo(Invoker invoker) {
    this.invoker = invoker;
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
    this.interceptors.addAll(other.interceptors);
    this.invoker = other.invoker;
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

  public List<ParameterInfo> getParameters() {
    if (parameters == null) {
      Function<Integer, String> nameProvider = getNameProvider();
      Parameter[] params = getReflection().getParameters();
      List<ParameterInfo> result = new ArrayList<>(params.length);
      for (int i = 0; i < params.length; i++) {
        result.add(new ParameterInfo(this, nameProvider.apply(i), params[i]));
      }
      parameters = Collections.unmodifiableList(result);
    }
    return parameters;
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
    for (int i = 0, argCount = arguments.length; i < argCount; i++) {
      if (arguments[i] == MISSING_ARGUMENT) {
        Function<Integer, String> nameProvider = getNameProvider();

        StringJoiner missingArguments = new StringJoiner("', '", "No argument was provided for parameters: '", "' in method: '" + getReflection().getDeclaringClass().getName()+ "." + getName() + "'");
        missingArguments.add(nameProvider.apply(i));
        for (int j = i + 1; j < argCount; j++) {
          if (arguments[j] == MISSING_ARGUMENT) {
            missingArguments.add(nameProvider.apply(j));
          }
        }
        throw new SpockExecutionException(missingArguments.toString());
      }
    }
    return invoker.invoke(target, arguments);
  }

  @NotNull
  private Function<Integer, String> getNameProvider() {
    if (getKind() == FEATURE) {
      List<String> parameterNames = getFeature().getParameterNames();
      return parameterNames::get;
    } else {
      Parameter[] parameters = getReflection().getParameters();
      return index -> parameters[index].getName();
    }
  }
}
