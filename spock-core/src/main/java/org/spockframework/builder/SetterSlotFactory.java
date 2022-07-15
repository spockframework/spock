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
import org.spockframework.util.MopUtil;

import java.lang.reflect.Type;

import groovy.lang.MetaProperty;

public class SetterSlotFactory implements ISlotFactory {
  @Override
  public ISlot create(Object owner, Type ownerType, String name) {
    MetaProperty property = GroovyRuntimeUtil.getMetaClass(owner).getMetaProperty(name);
    return property != null && MopUtil.isWriteable(property) ? new PropertySlot(owner, ownerType, property) : null;
  }
}
