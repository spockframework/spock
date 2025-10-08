/*
 * Copyright 2023 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.runtime;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;
import spock.lang.Specification;

public class SpockEngineDescriptor extends EngineDescriptor implements Node<SpockExecutionContext> {
  private final RunContext runContext;

  public SpockEngineDescriptor(UniqueId uniqueId, RunContext runContext) {
    super(uniqueId, "Spock");
    this.runContext = runContext;
  }

  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    return context.withRunContext(runContext);
  }

  @Override
  public SpockExecutionContext before(SpockExecutionContext context) throws Exception {
    DefaultGroovyMethods.mixin(Specification.class, SpecialMethodCallTarget.class);
    SpockExecution spockExecution = new SpockExecution(context.getStoreProvider());
    context.getRunContext().startExecution(spockExecution);
    return context;
  }

  @Override
  public void after(SpockExecutionContext context) throws Exception {
    ThrowableCollector collector = new ThrowableCollector(__ -> false);
    SpockExecution spockExecution = new SpockExecution(context.getStoreProvider());
    collector.execute(() -> runContext.stopExecution(spockExecution));
    collector.execute(runContext::stop);
    collector.execute(context.getStoreProvider()::close);
    collector.assertEmpty();
  }

  RunContext getRunContext() {
    return runContext;
  }
}
