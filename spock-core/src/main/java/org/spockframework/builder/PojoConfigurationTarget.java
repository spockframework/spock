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

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.spockframework.RecordingConfigurationTarget;
import org.spockframework.gentyref.GenericTypeReflector;
import org.spockframework.util.Nullable;
import org.spockframework.util.ReflectionUtil;

public class PojoConfigurationTarget implements IConfigurationTarget {
  private final Object value;
  private final Type pojoType;
  private final ITypeCoercer coercer;
  private final ISlotFinder slotFinder;
  private final RecordingConfigurationTarget recordingTarget = new RecordingConfigurationTarget();

  public PojoConfigurationTarget(@Nullable Object value, Type pojoType, ITypeCoercer coercer, ISlotFinder slotFinder) {
    this.value = value;
    this.pojoType = pojoType;
    this.coercer = coercer;
    this.slotFinder = slotFinder;
  }
  
  public Object getSubject() {
    if (value == null) {
      Constructor<?> constructor = getConstructor(pojoType, constructorArguments);
      Type[] parameterTypes = constructor.getGenericParameterTypes();
      List<Object> coercedConstructorArguments = new ArrayList<Object>();
      for (int i = 0; i < constructorArguments.size(); i++) {
        coercedConstructorArguments.add(coercer.coerce(constructorArguments.get(i), parameterTypes[i]));
      }
      ReflectionUtil.invokeConstructor(constructor, coercedConstructorArguments.toArray());
    }
    return value;
  }

  // IDEA: maybe readSlot() and writeSlot() should be just getProperty/setProperty, w/o any magic
  public IConfigurationTarget readSlot(String name) {
    ISlot slot = findSlot(name);
    return (IConfigurationTarget) coercer.coerce(
        new ConfigurationValue(slot.read(), slot.getType()), IConfigurationTarget.class);
  }

  public void writeSlot(String name, Object value) {
    if (name.startsWith("_")) {
      recordingTarget.writeSlot(name, value);
      return;
    }

    ISlot slot = findSlot(name);
    slot.write(coerceValue(value, slot));
  }

  // foo([a:1],b) and foo(a:1,b) are currently not distinguishable in Groovy
  // neither are foo([a:1],b) and foo(b,a:1)
  // so we should probably add some heuristics to tell them apart (look at subject's method signatures)
  // current impl is dead stupid:
  // - named args not treated specially
  public void configureSlot(String name, List<Object> values, IConfigurationSource source) {
    if (name.startsWith("_")) {
      recordingTarget.configureSlot(name, values, source);
      return;
    }

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
  private void configureSlot(ISlot slot, List<Object> values, IConfigurationSource source) {
    Object slotValue = null;
    if (values.isEmpty() && slot.isReadable()) {
      slotValue = slot.read();
    }
    if (slotValue == null && values.isEmpty() && source != null) {
      Class<?> clazz = GenericTypeReflector.erase(slot.getType());
      if (ReflectionUtil.getConstructorBySignature(clazz) == null)
    }
    if (slotValue == null && values.size() == 1) {
      slotValue = coerceValue(values.get(0), slot);
    }
    if (slotValue == null) {
      slotValue = coerceValue(values, slot);
    }
    if (source != null) {
      IConfigurationTarget slotTarget = (IConfigurationTarget) coercer.coerce(
          new ConfigurationValue(slotValue, slot.getType()), IConfigurationTarget.class);
      source.configure(slotTarget);
    }
    slot.configure(slotValue);
  }

  private Object coerceValue(Object value, ISlot slot) {
    Object result = coercer.coerce(value, slot.getType());
    if (result == null) {
      throw new NotCoerceableException("value %s cannot be coerced to type %s expected by slot %s")
          .withArgs(value, slot.getType(), slot.getName());
    }
    return result;
  }
}
