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

package spock.builder;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.runtime.InvokerHelper;

import org.spockframework.gentyref.GenericTypeReflector;

import groovy.lang.Closure;

public class PojoGestalt implements IGestalt {
  private final Object pojo;
  private final Type pojoType;
  private final Closure blueprint;
  private final List<ISlotFactory> slotFactories;

  public PojoGestalt(Object pojo, Type pojoType, Closure blueprint, List<ISlotFactory> slotFactories) {
    this.pojo = pojo;
    this.pojoType = pojoType;
    this.blueprint = blueprint;
    this.slotFactories = slotFactories;
  }

  public Object getSubject() {
    return pojo;
  }

  public Closure getBlueprint() {
    return blueprint;
  }

  public Object getValue(String name) {
    return InvokerHelper.getProperty(pojo, name);
  }

  public void setValue(String name, Object value) {
    InvokerHelper.setProperty(pojo, name, value);
  }

  // foo([a:1],b) and foo(a:1,b) are currently not distinguishable in Groovy
  // neither are foo([a:1],b) and foo(b,a:1)
  // so we should probably add some heuristics to tell them apart (look at subject's method signatures)
  // same for telling apart last arg (could be blueprint or last constructor arg)
  // current impl is dead stupid:
  // - named args not treated specially
  // - last arg is closure => treat as blueprint
  public IGestalt subGestalt(String name, Object[] args) {
    ISlot slot = findSlot(name, args);
    IGestalt gestalt = createGestalt(slot.getType(), args);
    slot.write(gestalt.getSubject());
    new Sculpturer().$form(gestalt);
    return gestalt;
  }

  private ISlot findSlot(String name, Object[] args) {
    for (ISlotFactory factory : slotFactories) {
      ISlot slot = factory.create(pojo, pojoType, name);
      if (slot != null) return slot;
    }
    throw new RuntimeException(String.format("Cannot find a slot named '%s'", name));
  }

  private IGestalt createGestalt(Type newType, Object[] args) {
    Closure newBlueprint = null;
    if (args != null && args[args.length - 1] instanceof Closure) {
      newBlueprint = (Closure)args[args.length - 1];
      args = Arrays.copyOfRange(args, 0, args.length - 1);
    }

    Class<?> newClazz = GenericTypeReflector.erase(newType); // TODO: check that this succeeds (Type could be a TypeVariable etc.)
    Object newPojo = BuilderHelper.createInstance(newClazz, args);
    return new PojoGestalt(newPojo, newType, newBlueprint, slotFactories);
  }
}
