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

import java.lang.reflect.Type;
import java.util.List;

public class PojoModelTarget implements IModelTarget {
  private final Object value;
  private final Type pojoType;
  private final ITypeCoercer coercer;
  private final ISlotFinder slotFinder;

  public PojoModelTarget(Object value, Type pojoType, ITypeCoercer coercer, ISlotFinder slotFinder) {
    this.value = value;
    this.pojoType = pojoType;
    this.coercer = coercer;
    this.slotFinder = slotFinder;
  }
  
  public Object getSubject() {
    return value;
  }

  // IDEA: maybe readSlot() and writeSlot() should be just getProperty/setProperty, w/o any magic
  public IModelTarget readSlot(String name) {
    ISlot slot = findSlot(name);
    return (IModelTarget) coercer.coerce(
        new ConfigurationValue(slot.read(), slot.getType()), IModelTarget.class);
  }

  public void writeSlot(String name, Object value) {
    ISlot slot = findSlot(name);
    slot.write(coerceValue(value, slot));
  }

  // foo([a:1],b) and foo(a:1,b) are currently not distinguishable in Groovy
  // neither are foo([a:1],b) and foo(b,a:1)
  // so we should probably add some heuristics to tell them apart (look at subject's method signatures)
  // current impl is dead stupid:
  // - named args not treated specially
  public void configureSlot(String name, List<Object> values, IModelSource source) {
    ISlot slot = findSlot(name);
    configureSlot(slot, values, source);
  }

  private ISlot findSlot(String name) {
    ISlot slot = slotFinder.find(name, value, pojoType);
    if (slot != null) return slot;

    throw new RuntimeException(String.format("Cannot find a slot named '%s'", name));
  }

  // note: currently we allow empty list of values w/o source; should we throw exception instead?

  // IDEA: empty values, source != null, no zero-arg ctor: treat source as list target for ctor args
  // (nice way to build immutable models)
  private void configureSlot(ISlot slot, List<Object> values, IModelSource source) {
    Object slotValue = null;
    if (values.isEmpty() && slot.isReadable()) {
      slotValue = slot.read();
    }
    if (slotValue == null && values.size() == 1) {
      slotValue = coercer.coerce(values.get(0), slot.getType());
    }
    if (slotValue == null) {
      slotValue = coerceValue(values, slot);
    }
    if (source != null) {
      IModelTarget slotTarget = (IModelTarget) coercer.coerce(
          new ConfigurationValue(slotValue, slot.getType()), IModelTarget.class);
      source.configure(slotTarget);
    }
    slot.configure(slotValue);
  }

  private Object coerceValue(Object value, ISlot slot) {
    Object result = coercer.coerce(value, slot.getType());
    if (result == null) {
      throw new NotCoerceableException("value '%s' cannot be coerced to type '%s' expected by slot '%s'")
          .withArgs(value, slot.getType(), slot.getName());
    }
    return result;
  }
}
