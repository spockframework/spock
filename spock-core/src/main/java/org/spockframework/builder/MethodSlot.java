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

import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.runtime.InvokerHelper;

import org.spockframework.gentyref.GenericTypeReflector;
import org.spockframework.util.MopUtil;

import groovy.lang.MetaMethod;

public class MethodSlot implements ISlot {
  private final String name;
  private final Object owner;
  private final Type ownerType;
  private final MetaMethod method;

  public MethodSlot(String name, Object owner, Type ownerType, MetaMethod method) {
    this.name = name;
    this.owner = owner;
    this.ownerType = ownerType;
    this.method = method;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    Method m = MopUtil.methodFor(method);
    return m != null ? GenericTypeReflector.getExactParameterTypes(m, ownerType)[0] :
        method.getNativeParameterTypes()[0];
  }

  public boolean isReadable() {
    return false;
  }

  public boolean isWriteable() {
    return false;
  }

  public Object read() {
    throw new MissingPropertyException(method.getName(), owner.getClass());
  }

  public void write(Object value) {
    throw new MissingPropertyException(method.getName(), owner.getClass());
  }

  public void configure(Object value) {
    method.doMethodInvoke(owner, InvokerHelper.asArray(value));
  }
}
