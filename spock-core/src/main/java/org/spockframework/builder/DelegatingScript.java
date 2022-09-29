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

import groovy.lang.*;
import org.spockframework.runtime.GroovyRuntimeUtil;

public abstract class DelegatingScript extends Script {
  private volatile Object $delegate;

  public void $setDelegate(Object delegate) {
    this.$delegate = delegate;
  }

  @Override
  public Object getProperty(String property) {
    try {
      return GroovyRuntimeUtil.getProperty($delegate, property);
    } catch (MissingPropertyException e) {
      return super.getProperty(property);
    }
  }

  @Override
  public void setProperty(String property, Object newValue) {
    try {
      GroovyRuntimeUtil.setProperty($delegate, property, newValue);
    } catch (MissingPropertyException e) {
      super.setProperty(property, newValue);
    }
  }

  @Override
  public Object invokeMethod(String name, Object args) {
    try {
      return GroovyRuntimeUtil.invokeMethod($delegate, name, GroovyRuntimeUtil.asArgumentArray(args));
    } catch (MissingMethodException e) {
      return super.invokeMethod(name, args);
    }
  }
}
