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

import java.util.List;

public class SpockConfigurationModelTarget implements IModelTarget {
  private final List<Object> configurationBlocks;
  private final ITypeCoercer coercer;

  public SpockConfigurationModelTarget(List<Object> configurationBlocks, ITypeCoercer coercer) {
    this.configurationBlocks = configurationBlocks;
    this.coercer = coercer;
  }

  public Object getSubject() {
    return configurationBlocks;
  }

  public IModelTarget readSlot(String name) {
    Object config = getConfiguration(name);
    if (config == null) throw new InvalidConfigurationException("No configuration block named '%s' is registered").withArgs(name);
    return (IModelTarget) coercer.coerce(new ConfigurationValue(config), IModelTarget.class);
  }

  public void writeSlot(String name, Object value) {
    throw new InvalidConfigurationException("Configuration block '%s' cannot be set directly").withArgs(name);
  }

  public void configureSlot(String name, List<Object> args, IModelSource source) {
    Object config = getConfiguration(name);
    if (config == null) return; // handle gracefully because some configuration blocks might not be available or of interest right now

    if (!args.isEmpty()) throw new InvalidConfigurationException("Invalid syntax for configuring configuration block '%s'").withArgs(name);
    
    if (source == null) throw new InvalidConfigurationException("Invalid syntax for configuring configuration block '%s'").withArgs(name); 
    
    IModelTarget target = (IModelTarget) coercer.coerce(new ConfigurationValue(config), IModelTarget.class);
    source.configure(target);
  }

  private Object getConfiguration(String name) {
    for (Object config : configurationBlocks) {
      String configName = getConfigurationName(config);
      if (configName.equalsIgnoreCase(name)) return config;
    }

    return null;
  }

  // IDEA: declare name with annotation
  private String getConfigurationName(Object config) {
    String className = config.getClass().getSimpleName();
    if (className.endsWith("Configuration")) {
      return className.substring(0, className.length() - "Configuration".length());
    }
    return className;
  }
}
