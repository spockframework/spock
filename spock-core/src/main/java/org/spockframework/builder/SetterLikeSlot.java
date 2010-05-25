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

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.codehaus.groovy.runtime.InvokerHelper;

import org.spockframework.gentyref.GenericTypeReflector;
import org.spockframework.util.MopUtil;

import groovy.lang.MetaMethod;

public class SetterLikeSlot implements ISlot {
  private final Object owner;
  private final Type ownerType;
  private final MetaMethod setterLikeMethod;

  public SetterLikeSlot(Object owner, Type ownerType, MetaMethod setterLikeMethod) {
    this.owner = owner;
    this.ownerType = ownerType;
    this.setterLikeMethod = setterLikeMethod;
  }

  public Type getType() {
    Method m = MopUtil.methodFor(setterLikeMethod);
    return m != null ? GenericTypeReflector.getExactParameterTypes(m, ownerType)[0] :
        setterLikeMethod.getNativeParameterTypes()[0];
  }

  public void write(Object value) {
    setterLikeMethod.doMethodInvoke(owner, InvokerHelper.asArray(value));
  }
}
