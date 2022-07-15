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

package org.spockframework.builder;

import org.spockframework.runtime.IConfigurationRegistry;

import java.util.List;

import groovy.lang.*;

public class SpockConfigurationGestalt implements IGestalt {
  private final IConfigurationRegistry configurationRegistry;
  private final IBlueprint blueprint;
  private final List<ISlotFactory> slotFactories;

  public SpockConfigurationGestalt(IConfigurationRegistry configurationRegistry, IBlueprint blueprint,
      List<ISlotFactory> slotFactories) {
    this.configurationRegistry = configurationRegistry;
    this.blueprint = blueprint;
    this.slotFactories = slotFactories;
  }

  @Override
  public IBlueprint getBlueprint() {
    return blueprint;
  }

  @Override
  public Object getProperty(String name) {
    Object config = configurationRegistry.getConfigurationByName(name);
    if (config == null) throw new MissingPropertyException("configuration not found");
    return config;
  }

  @Override
  public void setProperty(String name, Object value) {
    throw new MissingPropertyException("configurations cannot be set directly");
  }

  @Override
  public Object invokeMethod(String name, Object[] args) {
    if (args.length != 1 || !(args[0] instanceof Closure))
      throw new MissingMethodException(name, this.getClass(), args);

    Object config = configurationRegistry.getConfigurationByName(name);
    if (config == null) throw new MissingMethodException(name, this.getClass(), args);

    ClosureBlueprint blueprint = new ClosureBlueprint((Closure)args[0], config);
    IGestalt gestalt = new PojoGestalt(config, config.getClass(), blueprint, slotFactories);
    new Sculpturer().$form(gestalt);
    return null;
  }
}
