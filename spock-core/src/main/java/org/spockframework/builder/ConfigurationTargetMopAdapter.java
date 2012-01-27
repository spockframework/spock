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

import java.util.Arrays;

import groovy.lang.*;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.spockframework.util.CollectionUtil;
import org.spockframework.util.Nullable;

public class ConfigurationTargetMopAdapter extends GroovyObjectSupport implements GroovyInterceptable {
  private final IModelTarget $target;
  private final Object thisObject;
  
  public ConfigurationTargetMopAdapter(IModelTarget target, @Nullable Object thisObject) {
    this.$target = target;
    this.thisObject = thisObject;
  }

  // IDEA: if we want to allow foo.bar.baz = 42, we need to return a special configuration
  // source here (and potentially add a coercion from this configuration source to value)
  @Override
  public Object getProperty(String property) {
    if (thisObject != null) {
      try {
        return InvokerHelper.getProperty(thisObject, property);
      } catch (MissingPropertyException ignored) {
      } catch (InvalidConfigurationException ignored) { // TODO: need better exception, e.g. Missing(Read)SlotException w/ targeted error msg
      }
    }

    return $target.readSlot(property);
  }

  @Override
  public void setProperty(String property, Object newValue) {
    if (thisObject != null) {
      try {
        InvokerHelper.setProperty(thisObject, property, newValue);
        return;
      } catch (MissingPropertyException ignored) {
      } catch (InvalidConfigurationException ignored) {
      }
    }

    $target.writeSlot(property, newValue);
  }

  @Override
  public Object invokeMethod(String name, Object args) {
    if (thisObject != null) {
      try {
        return InvokerHelper.invokeMethod(thisObject, name, args);
      } catch (MissingMethodException ignored) {
      } catch (InvalidConfigurationException ignored) {
      }
    }

    Object[] argsArray = InvokerHelper.asArray(args);
    Closure block = null;
    if (argsArray.length > 0) {
      Object lastArg = argsArray[argsArray.length - 1];
      if (lastArg instanceof Closure) {
        block = (Closure) lastArg;
        argsArray = CollectionUtil.copyArray(argsArray, 0, argsArray.length - 1);
      }
    }

    $target.configureSlot(name, Arrays.asList(argsArray), block != null ? new ClosureModelSource(block) : null);
    return null;
  }
}
