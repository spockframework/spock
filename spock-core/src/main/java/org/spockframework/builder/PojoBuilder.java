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
import java.util.List;

import groovy.lang.Closure;

import static java.util.Arrays.asList;

// TODO: test building pojo enriched with expandometaclass/category property
// TODO: test call to void varargs method in expect: block
// (related: find all calls to MetaMethod.invoke and replace with doMethodInvoke)
// IDEA: allow syntax foo = { ... } to make it clear built object should be assigned to a property (can't handle all cases with method syntax)
// IDEA: helpful coercions (esp. for configuration): List -> Set (enables the use of list literals for sets), potentially String -> Enum
// TODO: provide a way to set visibility of methods/properties to look for
public class PojoBuilder {
  private final GestaltBuilder gestaltBuilder = new GestaltBuilder();
  private final List<ISlotFactory> slotFactories =
      asList(new SetterSlotFactory(), new AddSlotFactory(), new CollectionSlotFactory());

  public Object build(Object pojo, Closure closure) {
    return build(pojo, new ClosureBlueprint(closure, pojo));
  }

  public Object build(Object pojo, IBlueprint blueprint) {
    IGestalt gestalt = new PojoGestalt(pojo, pojo.getClass(), blueprint, slotFactories);
    gestaltBuilder.build(gestalt);
    return pojo;
  }
}
