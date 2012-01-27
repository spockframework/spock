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

package org.spockframework.runtime;

import java.util.*;

import org.spockframework.builder.*;

public class SpockConfigurationBuilder {
  private final ISlotFinder slotFinder = createSlotFinder();
  private final ITypeCoercer coercer = createCoercer();
  private final List<IModelSource> sources = new ArrayList<IModelSource>();

  public SpockConfigurationBuilder fromSource(IModelSource source) {
    sources.add(source);
    return this;
  }

  public void build(List<Object> configurationBlocks) {
    IModelTarget target = new SpockConfigurationModelTarget(configurationBlocks, coercer);
    for (IModelSource source: sources) {
      source.configure(target);
    }
  }

  private ISlotFinder createSlotFinder() {
    return new SlotFinderChain(Arrays.asList(new PropertySlotFinder(), new AddSlotFinder(), new CollectionSlotFinder()));
  }

  private ITypeCoercer createCoercer() {
    CoercerChain chain = new CoercerChain();
    chain.add(new ServiceProvidingCoercer(slotFinder, ISlotFinder.class));
    chain.add(new StringCoercer(chain));
    chain.add(new PojoConfigurationTargetCoercer(chain));
    chain.add(new ClosureConfigurationSourceCoercer());
    chain.add(new GroovyCoercer());
    return chain;
  }
}
