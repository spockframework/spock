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

import org.junit.runner.notification.RunNotifier;

import org.spockframework.builder.DelegatingScript;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.*;

import spock.config.RunnerConfiguration;

public class RunContext {
  private static final ThreadLocal<LinkedList<RunContext>> contextStacks =
      new ThreadLocal<LinkedList<RunContext>>() {
        protected LinkedList<RunContext> initialValue() {
          return new LinkedList<RunContext>();
        }
      };

  private final List<Class<?>> extensionClasses;
  
  private ExtensionRegistry extensionRegistry;
  private RunnerConfiguration runnerConfiguration = new RunnerConfiguration();

  private RunContext(@Nullable DelegatingScript configurationScript, List<Class<?>> extensionClasses) {
    this.extensionClasses = extensionClasses;

    List<Object> configurations = new ArrayList<Object>();
    configurations.add(runnerConfiguration);

    extensionRegistry = new ExtensionRegistry(extensionClasses, configurations);
    extensionRegistry.loadExtensions();

    if (configurationScript != null) {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.build(configurations, configurationScript);
    }
  }

  public ExtensionRunner createExtensionRunner(SpecInfo spec) {
    return new ExtensionRunner(spec, extensionRegistry.getExtensions());
  }

  public ParameterizedSpecRunner createSpecRunner(SpecInfo spec, RunNotifier notifier) {
    return new ParameterizedSpecRunner(spec, new JUnitSupervisor(spec, notifier, createStackTraceFilter(spec)));
  }

  private IStackTraceFilter createStackTraceFilter(SpecInfo spec) {
    return runnerConfiguration.filterStackTrace ? new StackTraceFilter(spec) : new DummyStackTraceFilter();
  }

  public static <T> T withNewContext(@Nullable DelegatingScript configurationScript,
      List<Class<?>> extensionClasses, boolean inheritParentExtensions, IFunction<RunContext, T> command) {
    List<Class<?>> allExtensionClasses = new ArrayList<Class<?>>(extensionClasses);
    if (inheritParentExtensions) allExtensionClasses.addAll(getCurrentExtensions());
    
    RunContext context = new RunContext(configurationScript, allExtensionClasses);
    LinkedList<RunContext> contextStack = contextStacks.get();
    contextStack.addFirst(context);
    try {
      return command.apply(context);
    } finally {
      contextStack.removeFirst();
    }
  }

  public static RunContext get() {
    LinkedList<RunContext> contextStack = contextStacks.get();
    RunContext context = contextStack.peek();
    if (context == null) {
      context = createBottomContext();
      contextStack.addFirst(context);
    }
    return context;
  }

  private static List<Class<?>> getCurrentExtensions() {
    RunContext context = contextStacks.get().peek();
    if (context == null) return Collections.emptyList();
    return context.extensionClasses;
  }
  
  // This context will stay around until the thread dies.
  // It would be more accurate to remove the context once the test run
  // has finished, but the JUnit Runner SPI doesn't provide an adequate hook.
  private static RunContext createBottomContext() {
    DelegatingScript script = new ConfigurationScriptLoader().loadScriptFromConfiguredLocation();
    List<Class<?>> classes = new ExtensionClassesLoader().loadClassesFromDefaultLocation();
    return new RunContext(script, classes);
  }
}
