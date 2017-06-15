/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.mock.runtime;

import org.spockframework.gentyref.GenericTypeReflector;
import org.spockframework.mock.*;
import org.spockframework.util.*;

import java.lang.reflect.Type;
import java.util.*;

@Beta
public class MockConfiguration implements IMockConfiguration {
  private final String name;
  private final Type type;
  private final Object instance;
  private final MockNature nature;
  private final MockImplementation implementation;
  private final List<Object> constructorArgs;
  private final IDefaultResponse defaultResponse;
  private final boolean global;
  private final boolean verified;
  private final boolean useObjenesis;

  public MockConfiguration(@Nullable String name, Type type, MockNature nature,
      MockImplementation implementation, Map<String, Object> options) {
      this(name, type, null, nature, implementation, options);
  }

  @SuppressWarnings("unchecked")
  public MockConfiguration(@Nullable String name, Type type, @Nullable Object instance, MockNature nature,
      MockImplementation implementation, Map<String, Object> options) {
    this.name = getOption(options, "name", String.class, name);
    this.type = getOption(options, "type", Type.class, type);
    this.instance = getOption(options, "instance", Object.class, instance);
    this.nature = getOption(options, "nature", MockNature.class, nature);
    this.implementation = getOption(options, "implementation", MockImplementation.class, implementation);
    this.constructorArgs = getOption(options, "constructorArgs", List.class, null);
    this.defaultResponse = getOption(options, "defaultResponse", IDefaultResponse.class, this.nature.getDefaultResponse());
    this.global = getOption(options, "global", Boolean.class, false);
    this.verified = getOption(options, "verified", Boolean.class, this.nature.isVerified());
    this.useObjenesis = getOption(options, "useObjenesis", Boolean.class, this.nature.isUseObjenesis());
  }

  @Override
  @Nullable
  public String getName() {
    return name;
  }

  @Override
  public Class<?> getType() {
    return GenericTypeReflector.erase(type);
  }

  @Override
  public Object getInstance() {
    return instance;
  }

  @Override
  public Type getExactType() {
    return type;
  }

  @Override
  public MockNature getNature() {
    return nature;
  }

  @Override
  public MockImplementation getImplementation() {
    return implementation;
  }

  @Override
  @Nullable
  public List<Object> getConstructorArgs() {
    return constructorArgs;
  }

  @Override
  public IDefaultResponse getDefaultResponse() {
    return defaultResponse;
  }

  @Override
  public boolean isGlobal() {
    return global;
  }

  @Override
  public boolean isVerified() {
    return verified;
  }

  @Override
  public boolean isUseObjenesis() {
    return useObjenesis;
  }

  private <T> T getOption(Map<String, Object> options, String key, Class<T> type, T defaultValue) {
    return options.containsKey(key) ? type.cast(options.get(key)) : defaultValue;
  }
}
