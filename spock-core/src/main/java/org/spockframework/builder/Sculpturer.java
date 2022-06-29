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

import org.spockframework.runtime.GroovyRuntimeUtil;

import groovy.lang.*;

/**
 * Forms a gestalt from its blueprint.
 */
public class Sculpturer extends GroovyObjectSupport {
  private IGestalt $gestalt;

  public void $form(IGestalt gestalt) {
    IBlueprint blueprint = gestalt.getBlueprint();
    if (blueprint == null) return;

    $gestalt = gestalt;
    blueprint.setDelegate(this);
    blueprint.evaluate();
  }

  @Override
  public Object getProperty(String name) {
    Object thisObject = $gestalt.getBlueprint().getThisObject();
    if (thisObject != null) {
      try {
        return GroovyRuntimeUtil.getProperty(thisObject, name);
      } catch (MissingPropertyException ignored) {}
    }
    return $gestalt.getProperty(name);
  }

  @Override
  public void setProperty(String name, Object value) {
    Object thisObject = $gestalt.getBlueprint().getThisObject();
    if (thisObject != null) {
      try {
        GroovyRuntimeUtil.setProperty(thisObject, name, value);
        return;
      } catch (MissingPropertyException ignored) {}
    }
    $gestalt.setProperty(name, value);
  }

  @Override
  public Object invokeMethod(String name, Object args) {
    Object thisObject = $gestalt.getBlueprint().getThisObject();
    if (thisObject != null) {
      try {
        return GroovyRuntimeUtil.invokeMethod(thisObject, name, GroovyRuntimeUtil.asArgumentArray(args));
      } catch (MissingMethodException ignored) {}
    }
    return $gestalt.invokeMethod(name, GroovyRuntimeUtil.asArgumentArray(args));
  }
}
